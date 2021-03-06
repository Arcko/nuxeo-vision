/*
 * (C) Copyright 2015-2017 Nuxeo (http://nuxeo.com/) and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *     Michael Vachette
 */
package org.nuxeo.vision.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.nuxeo.vision.core.service.Vision.EVENT_IMAGE_HANDLED;
import static org.nuxeo.vision.core.service.Vision.EVENT_VIDEO_HANDLED;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.automation.AutomationService;
import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationContext;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.test.AutomationFeature;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventBundle;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.event.impl.EventBundleImpl;
import org.nuxeo.ecm.core.event.impl.EventContextImpl;
import org.nuxeo.ecm.core.event.test.CapturingEventListener;
import org.nuxeo.ecm.core.test.DefaultRepositoryInit;
import org.nuxeo.ecm.core.test.annotations.Granularity;
import org.nuxeo.ecm.core.test.annotations.RepositoryConfig;
import org.nuxeo.ecm.platform.tag.Tag;
import org.nuxeo.ecm.platform.tag.TagService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.Features;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.vision.core.listener.PictureConversionChangedListener;
import org.nuxeo.vision.core.service.Vision;

@RunWith(FeaturesRunner.class)
@Features(AutomationFeature.class)
@RepositoryConfig(init = DefaultRepositoryInit.class, cleanup = Granularity.METHOD)
@Deploy({ "nuxeo-vision-core", "org.nuxeo.ecm.platform.picture.core", "org.nuxeo.ecm.platform.tag",
        "org.nuxeo.ecm.automation.scripting" })
@LocalDeploy({ "nuxeo-vision-core:OSGI-INF/mock-adapter-contrib.xml",
        "nuxeo-vision-core:OSGI-INF/disabled-listener-contrib.xml",
        "nuxeo-vision-core:OSGI-INF/mock-provider-contrib.xml" })
public class TestPictureEventChain {

    @Inject
    CoreSession session;

    @Inject
    protected Vision vision;

    @Inject
    protected TagService tagService;

    @Test
    public void testPictureChain() throws IOException, OperationException {

        DocumentModel picture = session.createDocumentModel("/", "Picture", "Picture");
        File file = new File(getClass().getResource("/files/nyc.jpg").getPath());
        Blob blob = new FileBlob(file);
        picture.setPropertyValue("file:content", (Serializable) blob);
        picture = session.createDocument(picture);

        AutomationService as = Framework.getService(AutomationService.class);
        OperationContext ctx = new OperationContext();
        ctx.setInput(picture);
        ctx.setCoreSession(session);
        OperationChain chain = new OperationChain("TestChain");
        chain.add("javascript.PictureVisionDefaultMapper");
        picture = (DocumentModel) as.run(ctx, chain);

        List<Tag> tags = tagService.getDocumentTags(session, picture.getId(), session.getPrincipal().getName());

        Assert.assertTrue(tags.size() > 0);

    }

    @Test
    public void testPictureListener() throws IOException, OperationException {
        try (CapturingEventListener listener = new CapturingEventListener(EVENT_IMAGE_HANDLED, EVENT_VIDEO_HANDLED)) {
            DocumentModel picture = session.createDocumentModel("/", "Picture", "Picture");
            File file = new File(getClass().getResource("/files/plane.jpg").getPath());
            Blob blob = new FileBlob(file);
            picture.setPropertyValue("file:content", (Serializable) blob);
            picture = session.createDocument(picture);

            EventContextImpl evctx = new DocumentEventContext(session, session.getPrincipal(), picture);
            Event event = evctx.newEvent("pictureViewsGenerationDone");
            EventBundle bundle = new EventBundleImpl();
            bundle.push(event);

            PictureConversionChangedListener pictureListener = new PictureConversionChangedListener();
            pictureListener.handleEvent(bundle);

            picture = session.getDocument(picture.getRef());

            List<Tag> tags = tagService.getDocumentTags(session, picture.getId(), session.getPrincipal().getName());

            Assert.assertTrue(tags.size() > 0);

            assertEquals(1, listener.getCapturedEventCount(EVENT_IMAGE_HANDLED));
            assertFalse(listener.hasBeenFired(EVENT_VIDEO_HANDLED));
        }
    }

}
