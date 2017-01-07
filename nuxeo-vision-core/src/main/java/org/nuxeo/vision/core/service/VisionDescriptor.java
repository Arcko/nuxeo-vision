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
package org.nuxeo.vision.core.service;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.runtime.api.Framework;

@XObject("configuration")
public class VisionDescriptor {

    @XNode("pictureMapperChainName")
    protected String pictureMapperChainName = "javascript.PictureVisionDefaultMapper";

    @XNode("videoMapperChainName")
    protected String videoMapperChainName = "javascript.VideoVisionDefaultMapper";

    @XNode("provider")
    protected String provider = "google";

    public String getPictureMapperChainName() {
        return pictureMapperChainName;
    }

    public String getVideoMapperChainName() {
        return videoMapperChainName;
    }

    public String getProvider() {
        if (Framework.isTestModeSet()) {
            return System.getProperty("org.nuxeo.vision.test.provider");
        }
        return provider;
    }
}
