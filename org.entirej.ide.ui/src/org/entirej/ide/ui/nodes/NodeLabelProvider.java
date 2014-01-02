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
package org.entirej.ide.ui.nodes;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.entirej.ide.ui.EJUIImages;

public class NodeLabelProvider extends LabelProvider implements DelegatingStyledCellLabelProvider.IStyledLabelProvider
{
    public String getText(Object obj)
    {
        if (obj instanceof AbstractNode)
        {
            AbstractNode<?> node = (AbstractNode<?>) obj;
            String name = node.getName();
            if (name == null)
                return "";
            return name;
        }
        return obj.toString();
    }

    public Image getImage(Object obj)
    {
        if (obj instanceof AbstractNode)
        {
            AbstractNode<?> node = (AbstractNode<?>) obj;
            Image image = node.getImage();
            if (image != null)
            {
                NodeValidateProvider validateProvider = node.getAdapter(NodeValidateProvider.class);
                if (validateProvider != null)
                {
                    validateProvider.validate();
                    if (validateProvider.hasErrors())
                    {
                        ImageDescriptor error = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_DEC_FIELD_ERROR);
                        if (error == null)
                            error = EJUIImages.DESC_ERROR_CO;
                        image = EJUIImages.getImage(EJUIImages.createOverlay(image, error, IDecoration.BOTTOM_LEFT));
                    }
                    else if (validateProvider.hasWarnings())
                    {
                        ImageDescriptor warning = PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_DEC_FIELD_WARNING);
                        if (warning == null)
                            warning = EJUIImages.DESC_WARNING_CO;
                        image = EJUIImages.getImage(EJUIImages.createOverlay(image, warning, IDecoration.BOTTOM_LEFT));
                    }
                }
            }
            return image;
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider.
     * IStyledLabelProvider#getStyledText(java.lang.Object)
     */
    public StyledString getStyledText(Object element)
    {
        StyledString ss = new StyledString();
        ss.append(getText(element));
        if (element instanceof NodeOverview)
        {
            NodeOverview overview = (NodeOverview) element;
            overview.addOverview(ss);
        }

        return ss;
    }
}
