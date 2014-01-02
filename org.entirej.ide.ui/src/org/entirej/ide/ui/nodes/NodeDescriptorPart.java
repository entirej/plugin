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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.editors.AbstractEditor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptorPart;

public class NodeDescriptorPart extends AbstractDescriptorPart implements INodeDescriptorViewer
{

    private AbstractNode<?> selectedNode;

    public NodeDescriptorPart(AbstractEditor editor, FormPage page, Composite parent)
    {
        super(editor, page, parent, true);
    }

    @Override
    protected void buildBody(Section section, FormToolkit toolkit)
    {
        super.buildBody(section, toolkit);
    }

    @Override
    public Action[] getToolbarActions()
    {
        final Action refreshAction = new Action("Refresh", IAction.AS_PUSH_BUTTON)
        {

            @Override
            public void run()
            {
                buildUI();
            }

        };
        refreshAction.setImageDescriptor(EJUIImages.DESC_REFRESH);
        return new Action[] { refreshAction };
    }

    @Override
    public AbstractDescriptor<?>[] getDescriptors()
    {
        if (selectedNode != null)
        {
            return selectedNode.getNodeDescriptors();
        }
        return new AbstractDescriptor<?>[0];
    }

    @Override
    public String getSectionTitle()
    {
        return "Element Details";
    }

    @Override
    public String getSectionDescription()
    {
        if (selectedNode == null)
            return "Select element to edit properties of the selected element.";
        String toolTipText = selectedNode.getNodeDescriptorDetails();
        if(toolTipText!=null)
        {
            return toolTipText;
        }
        
        return "Set the properties of the selected element. Required fields are denoted by \"*\".";
    }

    public void showDetails(AbstractNode<?> node)
    {
        selectedNode = node;
        buildUI();

    }

}
