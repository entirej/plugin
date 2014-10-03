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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.entirej.framework.reports.interfaces.EJReportConnectionFactory;
import org.entirej.framework.reports.interfaces.EJReportTranslator;
import org.entirej.ide.ui.editors.EditorLayoutFactory;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptorPart;
import org.entirej.ide.ui.editors.descriptors.AbstractTypeDescriptor;

public class ReportApplicationPropertiesPart extends AbstractDescriptorPart
{

    private final AbstractTypeDescriptor    connectionFactoryClass;
    private final AbstractTypeDescriptor    translatorFactoryClass;

    public ReportApplicationPropertiesPart(final EJReportPropertiesEditor editor, FormPage page, Composite parent)
    {
        super(editor, page, parent, false);
     
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
        connectionFactoryClass.setBaseClass(EJReportConnectionFactory.class.getName());
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
        translatorFactoryClass.setBaseClass(EJReportTranslator.class.getName());

        

        buildUI();
    }

    @Override
    public AbstractDescriptor<?>[] getDescriptors()
    {
        return new AbstractDescriptor<?>[] {  connectionFactoryClass, translatorFactoryClass,};
    }

    @Override
    public String getSectionTitle()
    {
        return "Report Properties";
    }

    @Override
    public String getSectionDescription()
    {
        return "This section contains properties that will be used by the entire repots.";
    }

    @Override
    public void refresh()
    {
        super.refresh();
        buildUI();
    }

    protected void buildBody(Section section, FormToolkit toolkit)
    {
        section.setLayout(EditorLayoutFactory.createClearTableWrapLayout(false, 1));
        GridData sectionData = new GridData(GridData.FILL_HORIZONTAL);
        sectionData.verticalAlignment = SWT.BEGINNING;
        section.setLayoutData(sectionData);

    }

   

}
