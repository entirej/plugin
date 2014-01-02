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
package org.entirej.ide.ui.editors.descriptors;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.swt.widgets.Control;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.utils.JavaAccessUtils;
import org.entirej.ide.ui.utils.TypeAssistProvider;

public abstract class AbstractTypeDescriptor extends AbstractDescriptor<String>
{

    private final IJavaProjectProvider projectProvider;
    private String                     baseClass;
    private String                     defaultClass;
    private boolean                    defaultClassOpen;
    private int                        scope = IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES;

    public AbstractTypeDescriptor(IJavaProjectProvider projectProvider)
    {
        super(AbstractDescriptor.TYPE.REFERENCE);
        this.projectProvider = projectProvider;
    }

    public AbstractTypeDescriptor(IJavaProjectProvider projectProvider, String lable)
    {
        this(projectProvider);
        setText(lable);

    }

    public AbstractTypeDescriptor(IJavaProjectProvider projectProvider, String lable, String tooltip)
    {
        this(projectProvider, lable);
        setTooltip(tooltip);
    }

    public int getScope()
    {
        return scope;
    }

    public void setScope(int scope)
    {
        this.scope = scope;
    }

    public void setBaseClass(String baseClass)
    {
        this.baseClass = baseClass;
        // when setting a base type by default set to class only mode
        scope = IJavaElementSearchConstants.CONSIDER_CLASSES;
    }

    public String getBaseClass()
    {
        return baseClass;
    }

    public void setDefaultClass(String defaultClass)
    {
        this.defaultClass = defaultClass;
    }

    public String getDefaultClass()
    {
        return defaultClass;
    }
    
    
    

    public boolean isDefaultClassOpen()
    {
        return defaultClassOpen;
    }

    public void setDefaultClassOpen(boolean defaultClassOpen)
    {
        this.defaultClassOpen = defaultClassOpen;
    }

    @Override
    public boolean hasLableLink()
    {
        return true;
    }

    @Override
    public String lableLinkActivator()
    {
        return JavaAccessUtils.findOrCreateClass(getValue(), projectProvider.getJavaProject().getProject(), baseClass, defaultClass,defaultClassOpen);
    }

    @Override
    public String getTooltip()
    {

        String tooltip = super.getTooltip();
        if (tooltip == null && baseClass != null)
        {
            return String.format("a fully qualified name of the class that implements/extends <b>%s</b>.", baseClass);
        }

        return tooltip;
    }

    @Override
    public void addEditorAssist(Control control)
    {
        TypeAssistProvider.createTypeAssist(control, projectProvider, scope, baseClass);
    }

    @Override
    public String browseType()
    {
        IType type;
        if (baseClass != null)
        {
            String value = getValue();
            type = JavaAccessUtils.selectType(EJUIPlugin.getActiveWorkbenchShell(), projectProvider.getJavaProject().getResource(), scope, value == null ? ""
                    : value, baseClass);
        }
        else

            type = JavaAccessUtils.selectType(EJUIPlugin.getActiveWorkbenchShell(), projectProvider.getJavaProject().getResource(), scope);
        if (type != null)
        {
            return type.getFullyQualifiedName('$');
        }

        return getValue();
    }

}
