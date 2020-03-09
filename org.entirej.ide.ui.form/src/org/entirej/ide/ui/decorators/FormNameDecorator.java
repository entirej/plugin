/*******************************************************************************
 * Copyright 2013 CRESOFT AG
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
 *     CRESOFT AG - initial API and implementation
 ******************************************************************************/
package org.entirej.ide.ui.decorators;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.entirej.framework.dev.EJDevConstants;
import org.entirej.framework.plugin.framework.properties.reader.EntireJFormReader;

public class FormNameDecorator implements ILightweightLabelDecorator
{

    public void addListener(ILabelProviderListener listener)
    {
       //ignore

    }

    public void dispose()
    {
        //ignore

    }

    public boolean isLabelProperty(Object element, String property)
    {
       
        return false;
    }

    public void removeListener(ILabelProviderListener listener)
    {
        

    }

    public void decorate(Object element, IDecoration decoration)
    {
        if (element instanceof IFile && isFormFile((IFile) element))
        {
            IFile file = (IFile) element;
            String formName = EntireJFormReader.readFormName(file, true);
            if (formName != null && formName.length() > 0)
            {
                decoration.addSuffix(" ");
                decoration.addSuffix(formName);
            }

        }
    }

    private boolean isFormFile(IFile file)
    {
        return EJDevConstants.FORM_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(file.getFileExtension()) || isRefFormFile(file);
    }

    private boolean isRefFormFile(IFile file)
    {
        String fileExtension = file.getFileExtension();
        return EJDevConstants.REFERENCED_BLOCK_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(fileExtension)
                || EJDevConstants.REFERENCED_LOVDEF_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(fileExtension)
                || EJDevConstants.OBJECT_GROUP_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(fileExtension);
    }
}
