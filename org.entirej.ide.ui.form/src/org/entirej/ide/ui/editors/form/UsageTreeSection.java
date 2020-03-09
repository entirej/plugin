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
package org.entirej.ide.ui.editors.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormPage;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeContentProvider;
import org.entirej.ide.ui.nodes.AbstractNodeTreeSection;
import org.entirej.ide.ui.nodes.NodeOverview;

public abstract class UsageTreeSection extends AbstractNodeTreeSection
{
    
    public static final Styler UNSUED_STYLER = StyledString.createColorRegistryStyler(
            JFacePreferences.ERROR_COLOR, null);
    private final AbstractEJFormEditor editor;

    public UsageTreeSection(AbstractEJFormEditor editor, FormPage page, Composite parent)
    {
        super(editor, page, parent);
        this.editor = editor;
        filteredTree.getViewer().addDoubleClickListener(new IDoubleClickListener()
        {

            public void doubleClick(DoubleClickEvent event)
            {
                AbstractNode<?> selectedNode = getSelectedNode();
                if (selectedNode instanceof UsageNode)
                {
                    ((UsageNode) selectedNode).getSource().open();
                }

            }
        });
    }

    @Override
    public Object getTreeInput()
    {
        return new Object();
    }

    @Override
    public abstract String getSectionTitle();

    @Override
    public abstract String getSectionDescription();

    @Override
    public Action[] getBaseActions()
    {

        return new Action[] {};
    }

    protected abstract UsageGroup[] getUsageGroups();

    @Override
    public AbstractNodeContentProvider getContentProvider()
    {
        return new AbstractNodeContentProvider()
        {

            public Object[] getElements(Object inputElement)
            {
                List<GroupNode> nodes = new ArrayList<UsageTreeSection.GroupNode>();

                for (UsageGroup group : getUsageGroups())
                {
                    nodes.add(new GroupNode(group));
                }
                return nodes.toArray();
            }
        };
    }

    private class GroupNode extends AbstractNode<UsageGroup>
    {
        private final Image GROUP = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);

        public GroupNode(UsageGroup source)
        {
            super(null, source);
        }

        public String getName()
        {
            return source.getName();
        }

        @Override
        public String getToolTipText()
        {
            return source.getTooltip();
        }

        @Override
        public Image getImage()
        {
            return GROUP;
        }

        @Override
        public boolean isLeaf()
        {
            return source.getUsages().size() == 0;
        }

        @Override
        public AbstractNode<?>[] getChildren()
        {
            List<UsageNode> nodes = new ArrayList<UsageNode>();

            for (Usage usage : source.getUsages())
            {
                nodes.add(new UsageNode(this, usage));
            }

            return nodes.toArray(new AbstractNode<?>[0]);
        }

        @Override
        public Action[] getActions()
        {
            return new Action[] {};
        }

    }

    private class UsageNode extends AbstractNode<Usage> implements NodeOverview
    {

        public UsageNode(final AbstractNode<?> parent, Usage source)
        {
            super(parent, source);
        }

        public String getName()
        {
            return source.getName();
        }

        @Override
        public boolean isLeaf()
        {
            return source.getUsages().size() == 0;
        }

        @Override
        public AbstractNode<?>[] getChildren()
        {
            List<UsageNode> nodes = new ArrayList<UsageNode>();

            for (Usage usage : source.getUsages())
            {
                nodes.add(new UsageNode(this, usage));
            }

            return nodes.toArray(new AbstractNode<?>[0]);
        }

        @Override
        public Image getImage()
        {
            return source.getImage();
        }

        @Override
        public Action[] getActions()
        {
            return new Action[] {};
        }

        public void addOverview(StyledString styledString)
        {
            
            if(source.isUnused())
            {
                styledString.append(" : ", StyledString.QUALIFIER_STYLER);
                styledString.append("Unused", UNSUED_STYLER);
            }
            styledString.append(" : ", StyledString.QUALIFIER_STYLER);
            styledString.append(source.getUsageInfo(), StyledString.COUNTER_STYLER);

        }

    }

    public static class UsageGroup
    {
        private final String      name;
        private final String      tooltip;
        private final List<Usage> usages;

        public UsageGroup(String name, String tooltip, List<Usage> usages)
        {
            this.name = name;
            this.tooltip = tooltip;
            this.usages = usages;
        }

        public String getName()
        {
            return name;
        }

        public List<Usage> getUsages()
        {
            return usages;
        }

        public String getTooltip()
        {
            return tooltip;
        }

        @Override
        public String toString()
        {
            return name;
        }

    }

    public static abstract class Usage
    {
        private final String      name;

        private final List<Usage> usages;
        
        private boolean unused;
        
        

        public Usage(String name, List<Usage> usages)
        {
            this.name = name;
            this.usages = usages;
        }
        
        
        public boolean isUnused()
        {
            return unused;
        }
        
        public void setUnused(boolean unused)
        {
            this.unused = unused;
        }

        public Usage(String name)
        {
            this.name = name;
            usages = Collections.emptyList();
        }

        public String getName()
        {
            return name;
        }

        public List<Usage> getUsages()
        {
            return usages;
        }

        public abstract void open();

        public abstract String getUsageInfo();

        public abstract Image getImage();

        @Override
        public String toString()
        {
            return name;
        }

    }

}
