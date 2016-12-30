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
package org.entirej.ide.ui.editors.prop;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.entirej.framework.core.application.definition.interfaces.EJApplicationDefinition;
import org.entirej.framework.core.interfaces.EJConnectionFactory;
import org.entirej.framework.core.interfaces.EJTranslator;
import org.entirej.ide.ui.editors.EditorLayoutFactory;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptorPart;
import org.entirej.ide.ui.editors.descriptors.AbstractPackageDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTypeDescriptor;

public class ApplicationPropertiesPart extends AbstractDescriptorPart
{

    private final AbstractTypeDescriptor    applicationManagerClass;
    private final AbstractTypeDescriptor    connectionFactoryClass;
    private final AbstractTypeDescriptor    translatorFactoryClass;
    private final AbstractPackageDescriptor reusableBlocksPkg;
    private final AbstractPackageDescriptor reusableLovDefinitionsPkg;
    private final AbstractPackageDescriptor objectGroupDefinitionsPkg;
    private AbstractDescriptorPart          descriptorPart;

    public ApplicationPropertiesPart(final EJPropertiesEditor editor, FormPage page, Composite parent)
    {
        super(editor.getToolkit(), parent, false);
        applicationManagerClass = new AbstractTypeDescriptor(editor, "Application Definition")
        {
            boolean changed = false;

            @Override
            public void setValue(String value)
            {

                editor.getEntireJProperties().setApplicationManagerDefinitionClassName(value, true);
                editor.setDirty(true);
                changed = true;
            }

            @Override
            public void addEditorAssist(Control control)
            {
                if (descriptorPart != null)
                    control.addFocusListener(new FocusListener()
                    {

                        public void focusLost(FocusEvent e)
                        {
                            if (changed)
                            {
                                if (descriptorPart != null)
                                    descriptorPart.buildUI(true);
                                changed = false;
                            }

                        }

                        public void focusGained(FocusEvent e)
                        {
                        }
                    });
                super.addEditorAssist(control);
            }

            @Override
            public String getValue()
            {
                return editor.getEntireJProperties().getApplicationManagerDefinitionClassName();
            }
        };
        applicationManagerClass.setBaseClass(EJApplicationDefinition.class.getName());
        connectionFactoryClass = new AbstractTypeDescriptor(editor, "Connection Factory")
        {

            @Override
            public void setValue(String value)
            {
                editor.getEntireJProperties().setConnectionFactoryClassName(value);
                editor.setDirty(true);
            }

            @Override
            public String getValue()
            {

                return editor.getEntireJProperties().getConnectionFactoryClassName();
            }
        };
        connectionFactoryClass.setBaseClass(EJConnectionFactory.class.getName());
        translatorFactoryClass = new AbstractTypeDescriptor(editor, "Translator")
        {

            @Override
            public void setValue(String value)
            {
                editor.getEntireJProperties().setTranslatorClassName(value);
                editor.setDirty(true);
            }

            @Override
            public String getValue()
            {

                return editor.getEntireJProperties().getTranslatorClassName();
            }
        };
        translatorFactoryClass.setBaseClass(EJTranslator.class.getName());

        reusableBlocksPkg = new AbstractPackageDescriptor(editor, "Referenced Block Location")
        {

            @Override
            public void setValue(String value)
            {
                editor.getEntireJProperties().setReusableBlocksLocation(packageToPath(value));
                editor.setDirty(true);

            }

            @Override
            public String getValue()
            {
                return pathTopackage(editor.getEntireJProperties().getReusableBlocksLocation());
            }
        };
        reusableLovDefinitionsPkg = new AbstractPackageDescriptor(editor, "Referenced LOV Definition Location")
        {

            @Override
            public void setValue(String value)
            {
                editor.getEntireJProperties().setReusableLovDefinitionLocation(packageToPath(value));
                editor.setDirty(true);

            }

            @Override
            public String getValue()
            {
                return pathTopackage(editor.getEntireJProperties().getReusableLovDefinitionLocation());
            }
        };
        objectGroupDefinitionsPkg = new AbstractPackageDescriptor(editor, "ObjectGroup Location")
        {
            
            @Override
            public void setValue(String value)
            {
                editor.getEntireJProperties().setObjectGroupDefinitionLocation(packageToPath(value));
                editor.setDirty(true);
                
            }
            
            @Override
            public String getValue()
            {
                return pathTopackage(editor.getEntireJProperties().getObjectGroupDefinitionLocation());
            }
        };

        buildUI(true);
    }

    @Override
    public AbstractDescriptor<?>[] getDescriptors()
    {
        return new AbstractDescriptor<?>[] { applicationManagerClass, connectionFactoryClass, translatorFactoryClass, reusableBlocksPkg,
                reusableLovDefinitionsPkg ,objectGroupDefinitionsPkg};
    }
    
    @Override
    public Object getInput()
    {
        return new Object();
    }

    @Override
    public String getSectionTitle()
    {
        return "Application Properties";
    }

    @Override
    public String getSectionDescription()
    {
        return "This section contains properties that will be used by the entire application.";
    }

    @Override
    public void refresh()
    {
        super.refresh();
        buildUI(true);
    }

    protected void buildBody(Section section, FormToolkit toolkit)
    {
        section.setLayout(EditorLayoutFactory.createClearTableWrapLayout(false, 1));
        GridData sectionData = new GridData(GridData.FILL_HORIZONTAL);
        sectionData.verticalAlignment = SWT.BEGINNING;
        section.setLayoutData(sectionData);

    }

    public void setDescriptorPart(AbstractDescriptorPart descriptorPart)
    {
        this.descriptorPart = descriptorPart;
    }

}
