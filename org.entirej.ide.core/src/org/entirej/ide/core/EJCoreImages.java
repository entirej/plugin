/*******************************************************************************
 * Copyright 2013 Mojave Innovations GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributors:
 *     Mojave Innovations GmbH - initial API and implementation
 ******************************************************************************/
package org.entirej.ide.core;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class EJCoreImages
{
    private EJCoreImages()
    {
    }

    public final static String          ICONS_PATH      = "icons/";                         //$NON-NLS-1$
    private final static ImageRegistry  PLUGIN_REGISTRY = new ImageRegistry();
    public static final ImageDescriptor DESC_EJ_ICON    = create(ICONS_PATH, "entirej.png");

    private static ImageDescriptor create(String prefix, String name)
    {
        return ImageDescriptor.createFromURL(makeImageURL(prefix, name));
    }

    private static URL makeImageURL(String prefix, String name)
    {
        String path = "$nl$/" + prefix + name; //$NON-NLS-1$
        return FileLocator.find(EJCorePlugin.getDefault().getBundle(), new Path(path), null);
    }

    public static Image getImage(ImageDescriptor desc)
    {
        String key = String.valueOf(desc.hashCode());
        Image image = PLUGIN_REGISTRY.get(key);
        if (image == null)
        {
            image = desc.createImage();
            PLUGIN_REGISTRY.put(key, image);
        }
        return image;
    }
}
