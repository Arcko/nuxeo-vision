/*
 * (C) Copyright 2015-2016 Nuxeo SA (http://nuxeo.com/) and others.
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

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.platform.test.PlatformFeature;
import org.nuxeo.runtime.test.runner.Deploy;
import org.nuxeo.runtime.test.runner.FeaturesRunner;
import org.nuxeo.runtime.test.runner.LocalDeploy;
import org.nuxeo.vision.core.image.ColorInfo;
import org.nuxeo.vision.core.image.TextEntity;
import org.nuxeo.vision.core.service.ComputerVision;
import org.nuxeo.vision.core.service.ComputerVisionFeature;
import org.nuxeo.vision.core.service.ComputerVisionResponse;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(FeaturesRunner.class)
@org.nuxeo.runtime.test.runner.Features({ PlatformFeature.class })
@Deploy("nuxeo-vision-core")
@LocalDeploy({
        "nuxeo-vision-core:OSGI-INF/mock-adapter-contrib.xml",
        "nuxeo-vision-core:OSGI-INF/disabled-listener-contrib.xml"
})
public class TestComputerVisionService {

    @Inject
    protected ComputerVision computerVision;

    @Test
    public void testLabelFeature() throws IOException, GeneralSecurityException {
        File file = new File(getClass().getResource("/files/plane.jpg").getPath());
        Blob blob = new FileBlob(file);
        ComputerVisionResponse result =
                computerVision.execute(
                        blob, ImmutableList.of(ComputerVisionFeature.LABEL_DETECTION),5);
        List<TextEntity> labels = result.getClassificationLabels();
        assertNotNull(labels);
        assertTrue(labels.size()>0);
        System.out.print(labels);
    }

    @Test
    public void testTextFeature() throws IOException, GeneralSecurityException {
        File file = new File(getClass().getResource("/files/text.png").getPath());
        Blob blob = new FileBlob(file);
        ComputerVisionResponse result =
                computerVision.execute(
                        blob, ImmutableList.of(ComputerVisionFeature.TEXT_DETECTION),5);
        List<TextEntity> texts = result.getOcrText();
        assertNotNull(texts);
        assertTrue(texts.size()>0);
        System.out.print(texts.get(0));
    }

    @Test
    public void testColorFeature() throws IOException, GeneralSecurityException {
        File file = new File(getClass().getResource("/files/plane.jpg").getPath());
        Blob blob = new FileBlob(file);
        ComputerVisionResponse result =
                computerVision.execute(
                        blob, ImmutableList.of(ComputerVisionFeature.IMAGE_PROPERTIES),5);
        List<ColorInfo> colors = result.getImageProperties().getColors();
        assertNotNull(colors);
        assertTrue(colors.size()>0);
        System.out.print(colors.get(0));
    }


    @Test
    public void testMultipleFeatures() throws IOException, GeneralSecurityException {
        File file = new File(getClass().getResource("/files/plane.jpg").getPath());
        Blob blob = new FileBlob(file);
        ComputerVisionResponse result = computerVision.execute(
                blob, ImmutableList.of(ComputerVisionFeature.TEXT_DETECTION,
                        ComputerVisionFeature.LABEL_DETECTION),5);
        List<TextEntity> labels = result.getClassificationLabels();
        assertNotNull(labels);
        assertTrue(labels.size()>0);
        List<TextEntity> texts = result.getOcrText();
        assertNotNull(texts);
        assertTrue(texts.size()>0);
        System.out.print(texts.get(0));
    }

    @Test
    public void testAllFeatures() throws IOException, GeneralSecurityException {
        File file = new File(getClass().getResource("/files/nyc.jpg").getPath());
        Blob blob = new FileBlob(file);
        ComputerVisionResponse result = computerVision.execute(
                blob, ImmutableList.of(
                        ComputerVisionFeature.TEXT_DETECTION,
                        ComputerVisionFeature.LABEL_DETECTION,
                        ComputerVisionFeature.IMAGE_PROPERTIES,
                        ComputerVisionFeature.FACE_DETECTION,
                        ComputerVisionFeature.LOGO_DETECTION,
                        ComputerVisionFeature.LANDMARK_DETECTION,
                        ComputerVisionFeature.SAFE_SEARCH_DETECTION),5);
        List<TextEntity> labels = result.getClassificationLabels();
        assertNotNull(labels);
        assertTrue(labels.size()>0);
        System.out.print(labels);
        List<TextEntity> texts = result.getOcrText();
        assertNotNull(texts);
        assertTrue(texts.size()>0);
        System.out.print(texts.get(0));
    }

    @Test
    public void testMultipleBlobs() throws IOException, GeneralSecurityException {
        List<Blob> blobs = new ArrayList<>();
        blobs.add(new FileBlob(new File(getClass().getResource("/files/plane.jpg").getPath())));
        blobs.add(new FileBlob(new File(getClass().getResource("/files/text.png").getPath())));

        List<ComputerVisionResponse> results = computerVision.execute(
                blobs, ImmutableList.of(ComputerVisionFeature.TEXT_DETECTION,
                        ComputerVisionFeature.LABEL_DETECTION), 5);
        assertTrue(results.size() == 2);
    }

}
