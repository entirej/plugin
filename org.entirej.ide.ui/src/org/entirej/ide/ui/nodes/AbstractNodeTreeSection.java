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

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.ui.dialogs.PatternFilter;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.progress.WorkbenchJob;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.AbstractEditor;
import org.entirej.ide.ui.editors.EditorLayoutFactory;
import org.entirej.ide.ui.nodes.dnd.NodeContext;
import org.entirej.ide.ui.nodes.dnd.NodeDragAdapter;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider;
import org.entirej.ide.ui.nodes.dnd.NodeTransfer;
import org.entirej.ide.ui.nodes.dnd.NodeViewDropAdapter;

public abstract class AbstractNodeTreeSection extends SectionPart
{
    protected int                   expand_level = 2;
    private AbstractEditor          editor;
    private FormPage                page;
    protected FilteredTree          filteredTree;
    protected INodeDescriptorViewer descriptorViewer;
    private boolean refreshDetails;
    private Menu                    addElementMenu;

    public AbstractNodeTreeSection(AbstractEditor editor, FormPage page, Composite parent)
    {
        super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
        this.editor = editor;
        buildBody(getSection(), page.getEditor().getToolkit());
        createSectionToolbar(getSection(), page.getEditor().getToolkit());

    }

    public AbstractEditor getEditor()
    {
        return editor;
    }

    public void setNodeDescriptorViewer(INodeDescriptorViewer descriptorViewer)
    {
        this.descriptorViewer = descriptorViewer;
    }

    public INodeDescriptorViewer getDescriptorViewer()
    {
        return descriptorViewer;
    }

    public abstract Object getTreeInput();

    public abstract String getSectionTitle();

    public abstract String getSectionDescription();

    public NodeLabelProvider getLabelProvider()
    {
        return new NodeLabelProvider();
    }

    public abstract AbstractNodeContentProvider getContentProvider();

    public Action[] getBaseActions()
    {
        return new Action[0];
    }

    protected void initTree()
    {
        if (filteredTree != null)
            filteredTree.getViewer().setInput(getTreeInput());
    }

    @Override
    public void refresh()
    {
        TreeViewer treeview = filteredTree.getViewer();
        Object[] expanded = treeview.getExpandedElements();

        treeview.getControl().setRedraw(false);
        treeview.setInput(getTreeInput());
        treeview.setExpandedElements(expanded);
        treeview.getControl().setRedraw(true);
        treeview.refresh();
        getManagedForm().fireSelectionChanged(AbstractNodeTreeSection.this, treeview.getSelection());
        showNodeDetails(refreshDetails);
        super.refresh();
    }

    public void refreshNodes()
    {
        if (filteredTree != null && !filteredTree.isDisposed())
        {
            filteredTree.getViewer().refresh();
            if (descriptorViewer != null)
                descriptorViewer.showDetails(getSelectedNode());
        }
    }

    
    public void expand(Object node)
    {
        node = findNode(node,true);
        if (filteredTree != null && node != null)
        {
            TreeViewer treeview = filteredTree.getViewer();

            treeview.expandToLevel(node,1);

        }
    }
    public void expand(Object node, int level)
    {
        node = findNode(node,true);
        if (filteredTree != null && node != null)
        {
            TreeViewer treeview = filteredTree.getViewer();

            treeview.expandToLevel(node, level);

        }
    }

    

    public void refresh(Object node)
    {
        node = findNode(node,true);
        refresh(node, false);
    }

    public void refresh(Object node, boolean expand)
    {
        
        if (node == null)
        {
            refresh();
            return;
        }
       
        if (filteredTree != null)
        {
            node = findNode(node,true);
            TreeViewer treeview = filteredTree.getViewer();
            treeview.refresh(node);
            if (expand)
                expand(node);

        }
    }

    public void selectNodesNoRefresh(boolean focusDeatils, Object... nodes)
    {
        if (filteredTree == null)
            return;
        TreeViewer viewer = filteredTree.getViewer();
        for (int i = 0; i < nodes.length; i++)
        {
            Object object = nodes[i];
            nodes[i] = findNode(object,true);
            
        }
        IStructuredSelection selection = new StructuredSelection(nodes);

        try
        {
            refreshDetails = false;
            viewer.setSelection(selection, true);
        }
        finally
        {
            refreshDetails = true;
        }
        
        if (focusDeatils && descriptorViewer != null)
        {
            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {
                    descriptorViewer.setFocus();

                }
            });
        }

    }
    public void selectNodes(boolean focusDeatils, Object... nodes)
    {
        if (filteredTree == null)
            return;
        TreeViewer viewer = filteredTree.getViewer();
        for (int i = 0; i < nodes.length; i++)
        {
            Object object = nodes[i];
            nodes[i] = findNode(object,true);
            
        }
        IStructuredSelection selection = new StructuredSelection(nodes);
        
        
        viewer.setSelection(selection, true);
        if (focusDeatils && descriptorViewer != null)
        {
            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {
                
                public void run()
                {
                    descriptorViewer.setFocus();
                    
                }
            });
        }
        
    }

    

    public Object findNode(Object source, boolean force)
    {
        
        AbstractNodeContentProvider contentProvider = (AbstractNodeContentProvider) filteredTree.getViewer().getContentProvider();
        
        Object[] elements = contentProvider.getElements(filteredTree.getViewer().getInput());
        for (Object parent : elements)
        {
            if(parent.equals(source))
            {
                return parent;
            }
            Object node = findNode(contentProvider,parent,source);
            if (node != null )
            {
                return node;
            }
        }
        
        
        return source;
    }

    private Object findNode( AbstractNodeContentProvider contentProvider,Object parent,Object source)
    {
        Object[] children = contentProvider.getChildren(parent);
        for (Object subParent : children)
        {
            if(subParent.equals(source))
            {
                return subParent;
            }
            Object node = findNode(contentProvider,subParent,source);
            if (node != null )
            {
                return node;
            }
        }
        return null;
    }

    @Override
    public void setFocus()
    {
        if (filteredTree != null)
            filteredTree.getViewer().getControl().setFocus();
    }

    protected void buildBody(Section section, FormToolkit toolkit)
    {
        section.setLayout(EditorLayoutFactory.createClearTableWrapLayout(false, 1));
        GridData sectionData = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
        section.setLayoutData(sectionData);

        section.setText(getSectionTitle());
        section.setDescription(getSectionDescription());

        Composite body = toolkit.createComposite(section);
        body.setLayout(new GridLayout());
        body.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        filteredTree = new FilteredTree(body, SWT.V_SCROLL | SWT.BORDER | SWT.MULTI, new PatternFilter(), true)
        {

            ISelection selection;
            Object[]   expanded;

            protected WorkbenchJob doCreateRefreshJob()
            {
                WorkbenchJob refreshJob2 = super.doCreateRefreshJob();
                refreshJob2.addJobChangeListener(new JobChangeAdapter()
                {
                    public void done(IJobChangeEvent event)
                    {
                        if (!event.getResult().isOK())
                            return;

                        String text = getFilterString();
                        if (text != null && text.length() > 0)
                            return;

                        TreeViewer treeview = getViewer();
                        if (expanded != null)
                        {
                            treeview.getControl().setRedraw(false);
                            treeview.setExpandedElements(expanded);
                            treeview.getControl().setRedraw(true);

                            expanded = null;
                        }
                        if (selection != null)
                        {
                            getViewer().setSelection(selection, true);
                            getManagedForm().fireSelectionChanged(AbstractNodeTreeSection.this, selection);
                            selection = null;
                        }
                    };
                });
                return refreshJob2;

            };

            protected void textChanged()
            {
                if (selection == null)
                    selection = getViewer().getSelection();

                if (expanded == null)
                    expanded = getViewer().getExpandedElements();

                super.textChanged();

            }
        };

        GridData treeGD = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
        treeGD.widthHint = 200;
        treeGD.heightHint = 100;
        filteredTree.setLayoutData(treeGD);
        final TreeViewer viewer = filteredTree.getViewer();
        viewer.setLabelProvider(new NodeStyledCellLabelProvider(getLabelProvider()));
        viewer.setContentProvider(getContentProvider());
        new ViewerToolTipSupport(viewer);
        viewer.setAutoExpandLevel(expand_level);
        editor.getSite().setSelectionProvider(viewer);
        viewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

           

            public void selectionChanged(SelectionChangedEvent event)
            {
                showNodeDetails(refreshDetails);
                editor.getContributor().refreah();
                
            }

        });

        // add default double click support to tree
        viewer.addDoubleClickListener(new IDoubleClickListener()
        {

            public void doubleClick(DoubleClickEvent event)
            {
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                boolean expandedState = viewer.getExpandedState(selection.getFirstElement());
                viewer.setExpandedState(selection.getFirstElement(), !expandedState);

            }
        });

        viewer.getTree().addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseDown(MouseEvent e)
            {
                if (e.button == 3)
                {
                    Point point = new Point(e.x, e.y);
                    TreeItem item = viewer.getTree().getItem(point);
                    if (item == null)
                    {
                        viewer.setSelection(null);
                    }
                }
            }
        });

        connectContextMenu();
        connectAddContextMenu();
        toolkit.paintBordersFor(body);
        section.setTabList(new Control[] { body });
        section.setClient(body);

    }

    public void addDnDSupport(final NodeMoveProvider rootMoveProvider)
    {
        // node move via DND
        final TreeViewer viewer = filteredTree.getViewer();
        Transfer[] transfers = new Transfer[] { NodeTransfer.getInstance() };
        viewer.addDragSupport(DND.DROP_LINK | DND.DROP_MOVE | DND.DROP_COPY, new Transfer[] { NodeTransfer.getInstance() }, new NodeDragAdapter(viewer));

        DropTarget dropTaget = new DropTarget(viewer.getControl(), DND.DROP_LINK | DND.DROP_COPY | DND.DROP_MOVE);
        dropTaget.setTransfer(transfers);
        dropTaget.addDropListener(new NodeViewDropAdapter(new NodeContext()
        {

            public void selectNodes(boolean focusDeatils, AbstractNode<?>... nodes)
            {
                AbstractNodeTreeSection.this.selectNodes(focusDeatils, nodes);

            }
            
            public AbstractEditor getEditor()
            {
                return editor;
            }
            
            public AbstractNodeTreeSection getTreeSection()
            {
                return AbstractNodeTreeSection.this;
            }
            

            public void refresh(AbstractNode<?> node)
            {
                AbstractNodeTreeSection.this.refresh(node);

            }

            public void nodesUpdated()
            {
                AbstractNodeTreeSection.this.nodesUpdated();

            }

            public NodeMoveProvider getRootNodeMoveProvider()
            {
                return rootMoveProvider;
            }

            

            public void expand(AbstractNode<?> node)
            {
                AbstractNodeTreeSection.this.expand(node);

            }

        }, viewer));
    }

    protected void nodesUpdated()
    {
        // TODO Auto-generated method stub

    }

    public void addToolbarCustomActions(ToolBarManager toolBarManager, ToolBar toolbar)
    {

    }

    private void createSectionToolbar(Section section, FormToolkit toolkit)
    {
        ToolBarManager toolBarManager = new ToolBarManager(SWT.FLAT);
        final ToolBar toolbar = toolBarManager.createControl(section);
        final Cursor handCursor = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
        toolbar.setCursor(handCursor);
        // Cursor needs to be explicitly disposed
        toolbar.addDisposeListener(new DisposeListener()
        {
            public void widgetDisposed(DisposeEvent e)
            {
                if ((handCursor != null) && (handCursor.isDisposed() == false))
                {
                    handCursor.dispose();
                }
            }
        });
        addToolbarCustomActions(toolBarManager, toolbar);
        // create add item Action
        MenuManager popupMenuManager = new MenuManager();
        IMenuListener listener = new IMenuListener()
        {
            public void menuAboutToShow(IMenuManager mng)
            {
                fillContextMenu(mng, filteredTree.getViewer(), true);
                // if empty, add dummy action item show no actions available
                if (mng.getItems().length == 0)
                {
                    mng.add(new Action("no actions available")
                    {
                    });
                }
            }
        };
        popupMenuManager.addMenuListener(listener);
        popupMenuManager.setRemoveAllWhenShown(true);
        final Menu menu = popupMenuManager.createContextMenu(toolbar);

        Action addAction = new Action("Add...", IAction.AS_DROP_DOWN_MENU)
        {

            @Override
            public void runWithEvent(Event event)
            {

                Rectangle rect = event.getBounds();
                Point pt;
                if (event.detail != SWT.ARROW)
                    pt = new Point(rect.x, rect.y + toolbar.getBounds().height);
                else
                    pt = new Point(rect.x, rect.y + rect.height);
                pt = toolbar.toDisplay(pt);
                menu.setLocation(pt.x, pt.y);
                menu.setVisible(true);
            }

        };
        addAction.setImageDescriptor(EJUIImages.DESC_ADD_ITEM);
        toolBarManager.add(addAction);

        // create delete Action
        final Action deleteAction = new Action("Delete", IAction.AS_PUSH_BUTTON)
        {

            @Override
            public void run()
            {

                Object[] elements = ((IStructuredSelection) filteredTree.getViewer().getSelection()).toArray();

                for (int i = 0; i < elements.length; i++)
                {
                    Object element = elements[i];
                    if (element instanceof AbstractNode<?>)
                    {
                        AbstractNode<?> node = (AbstractNode<?>) element;
                        if (node != null && node.getDeleteProvider() != null)
                        {

                            INodeDeleteProvider deleteProvider = node.getDeleteProvider();
                            AbstractOperation deleteOperation = deleteProvider.deleteOperation(true);
                            if (deleteOperation == null)
                            {
                                System.err.println("INodeDeleteProvider.deleteOperation : not implements" + node.getClass());
                                node.getDeleteProvider().delete(true);
                            }
                            else
                            {
                                editor.execute(deleteOperation);
                            }

                        }
                    }
                }

            }

        };
        deleteAction.setImageDescriptor(EJUIImages.DESC_DELETE_ITEM);
        deleteAction.setDisabledImageDescriptor(EJUIImages.DESC_DELETE_ITEM_DISABLED);
        deleteAction.setEnabled(false);
        toolBarManager.add(deleteAction);

        toolBarManager.add(new Separator());
        // create expand Action
        final Action expandAllAction = new Action("Expnad  All", IAction.AS_PUSH_BUTTON)
        {

            @Override
            public void run()
            {
                expandNodes();
            }

        };
        expandAllAction.setImageDescriptor(EJUIImages.DESC_EXPAND_ALL);
        toolBarManager.add(expandAllAction);
        // create collapse Action
        final Action collapseAllAction = new Action("Collapse All", IAction.AS_PUSH_BUTTON)
        {

            @Override
            public void run()
            {
                collapseNodes();
            }
        };
        collapseAllAction.setImageDescriptor(EJUIImages.DESC_COLLAPSE_ALL);
        toolBarManager.add(collapseAllAction);

        final Action refreshAction = new Action("Refresh", IAction.AS_PUSH_BUTTON)
        {

            @Override
            public void run()
            {
                refresh();
            }

        };
        refreshAction.setImageDescriptor(EJUIImages.DESC_REFRESH);
        toolBarManager.add(refreshAction);
        toolBarManager.update(true);

        // update toolbar depend on context
        assert filteredTree != null;
        filteredTree.getViewer().addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {
                AbstractNode<?> node = getSelectedNode(filteredTree.getViewer());

                Object[] elements = ((IStructuredSelection) filteredTree.getViewer().getSelection()).toArray();
                boolean canDelete = elements.length > 0;
                for (int i = 0; i < elements.length; i++)
                {
                    Object element = elements[i];
                    if (element instanceof AbstractNode<?>)
                    {
                        AbstractNode<?> snode = (AbstractNode<?>) element;
                        canDelete = snode.getDeleteProvider() != null;
                        if (!canDelete)
                        {
                            break;
                        }
                    }
                }

                // validate can delete
                deleteAction.setEnabled(canDelete);

                if (node != null)
                {

                    collapseAllAction.setText("Collapse");
                    expandAllAction.setText("Expand");
                }
                else
                {
                    collapseAllAction.setText("Collapse All");
                    expandAllAction.setText("Expnad  All");
                }

            }
        });

        section.setTextClient(toolbar);
    }

    private void showNodeDetails(boolean  preview)
    {

        showNodeDetails(getSelectedNode(),preview);

    }

    protected void showNodeDetails(AbstractNode<?> node,boolean preview)
    {
        if (descriptorViewer != null)
            descriptorViewer.showDetails(node);
    }

    private void connectAddContextMenu()
    {
        MenuManager popupMenuManager = new MenuManager();
        IMenuListener listener = new IMenuListener()
        {
            public void menuAboutToShow(IMenuManager mng)
            {
                fillContextMenu(mng, filteredTree.getViewer(), true);
                // if empty, add dummy action item show no actions available
                if (mng.getItems().length == 0)
                {
                    mng.add(new Action("no actions available")
                    {
                    });
                }
            }
        };
        popupMenuManager.addMenuListener(listener);
        popupMenuManager.setRemoveAllWhenShown(true);
        addElementMenu = popupMenuManager.createContextMenu(filteredTree);
    }

    private void connectContextMenu()
    {
        MenuManager popupMenuManager = new MenuManager();
        IMenuListener listener = new IMenuListener()
        {
            public void menuAboutToShow(IMenuManager mng)
            {
                fillContextMenu(mng, filteredTree.getViewer(), false);
            }
        };
        popupMenuManager.addMenuListener(listener);
        popupMenuManager.setRemoveAllWhenShown(true);
        Control control = filteredTree.getViewer().getControl();
        Menu menu = popupMenuManager.createContextMenu(control);
        control.setMenu(menu);

    }

    public void showContextAddMenu()
    {
        if (addElementMenu != null && !addElementMenu.isDisposed())
        {

            TreeItem[] selection = filteredTree.getViewer().getTree().getSelection();
            Point pt;
            if (selection.length > 0)
            {
                Rectangle rect = selection[0].getBounds();
                pt = new Point((rect.x + 5), (rect.y + rect.height));
            }
            else
            {
                Rectangle rect = filteredTree.getViewer().getTree().getBounds();
                pt = new Point((rect.x + rect.width) / 2, (rect.y + rect.height) / 2);
            }

            pt = filteredTree.getViewer().getTree().toDisplay(pt);
            addElementMenu.setLocation(pt);

            addElementMenu.setVisible(true);

        }
    }

    protected void fillContextMenu(IMenuManager manager, Viewer viewer, boolean addMode)
    {

        final AbstractNode<?> node = getSelectedNode(viewer);
        Action[] actions;

        final INodeRenameProvider renameProvider;
        if (node != null)
        {

            actions = node.getActions();
            renameProvider = node.getRenameProvider();
        }
        else
        {
            actions = getBaseActions();
            renameProvider = null;
        }
        addMenuActions(manager, actions);

        if (!addMode)
        {
            if (actions.length > 0)
            {
                manager.add(new Separator());
            }
            if (renameProvider != null)
                manager.add(new Action("Rename")
                {
                    @Override
                    public void run()
                    {
                        renameProvider.rename();
                    }
                });
            Object[] elements = ((IStructuredSelection) filteredTree.getViewer().getSelection()).toArray();
            boolean canDelete = elements.length > 0;
            for (int i = 0; i < elements.length; i++)
            {
                Object element = elements[i];
                if (element instanceof AbstractNode<?>)
                {
                    AbstractNode<?> snode = (AbstractNode<?>) element;
                    canDelete = snode.getDeleteProvider() != null;
                    if (!canDelete)
                    {
                        break;
                    }
                }
            }
            if (canDelete)
                editor.getContributor().addDeleteAction(manager);

        }

    }

    protected void addMenuActions(IMenuManager manager, Action[] actions)
    {
        for (Action action : actions)
        {

            if (action == null)
            {
                manager.add(new Separator());
            }
            else
            {
                if (action instanceof AbstractSubActions)
                {
                    AbstractSubActions subActions = (AbstractSubActions) action;
                    MenuManager menuManager = new MenuManager(subActions.getText());
                    manager.add(menuManager);
                    addMenuActions(menuManager, subActions.getActions());
                }
                else
                    manager.add(action);
            }
        }
    }

    public void expandNodes()
    {
        if (filteredTree != null)
        {
            AbstractNode<?> node = getSelectedNode(filteredTree.getViewer());
            if (node == null)
                filteredTree.getViewer().expandAll();
            else
                filteredTree.getViewer().expandToLevel(node, TreeViewer.ALL_LEVELS);
        }
    }

    public void collapseNodes()
    {
        if (filteredTree != null)
        {
            AbstractNode<?> node = getSelectedNode(filteredTree.getViewer());
            if (node == null)
                filteredTree.getViewer().collapseAll();
            else
                filteredTree.getViewer().collapseToLevel(node, TreeViewer.ALL_LEVELS);
        }
    }

    public AbstractNode<?> getSelectedNode()
    {
        return filteredTree != null ? getSelectedNode(filteredTree.getViewer()) : null;
    }

    private static AbstractNode<?> getSelectedNode(Viewer viewer)
    {
        ISelection selection = viewer.getSelection();
        IStructuredSelection structuredSelection = (IStructuredSelection) selection;

        // support one selection only
        if (structuredSelection.size() > 1)
            return null;

        if (!structuredSelection.isEmpty())
        {
            Object element = structuredSelection.getFirstElement();
            if (element instanceof AbstractNode)
            {
                return (AbstractNode<?>) element;
            }
        }
        return null;
    }

    public ISelectionProvider getISelectionProvider()
    {
        return filteredTree != null ? filteredTree.getViewer() : null;
    }

    class ViewerToolTipSupport extends ColumnViewerToolTipSupport
    {

        protected ViewerToolTipSupport(ColumnViewer viewer)
        {
            super(viewer, ToolTip.NO_RECREATE, false);
        }

        protected Composite createToolTipContentArea(Event event, final Composite parent)
        {
            Composite comp = new Composite(parent, SWT.NONE);
            GridLayout gridLayout = new GridLayout();
            gridLayout.numColumns = 2;
            gridLayout.marginWidth = 5;
            gridLayout.marginHeight = 2;
            comp.setLayout(gridLayout);
            Image image = getImage(event);
            Image bgImage = getBackgroundImage(event);
            String text = getText(event);
            Color fgColor = getForegroundColor(event);
            Color bgColor = getBackgroundColor(event);
            Font font = getFont(event);

            Label label = new Label(comp, getStyle(event));
            if (text != null)
                label.setText(text);

            if (image != null)
                label.setImage(image);

            if (fgColor != null)
                label.setForeground(fgColor);

            if (bgColor != null)
                label.setBackground(bgColor);

            if (bgImage != null)
                label.setBackgroundImage(image);

            if (font != null)
                label.setFont(font);

            comp.setBackground(label.getBackground());

            return comp;
        }
    }

   
}
