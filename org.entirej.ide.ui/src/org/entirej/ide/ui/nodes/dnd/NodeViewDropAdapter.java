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
package org.entirej.ide.ui.nodes.dnd;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.ICompositeOperation;
import org.eclipse.core.commands.operations.TriggeredOperations;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Display;
import org.entirej.ide.ui.editors.operations.ReversibleOperation;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.INodeDeleteProvider;

public class NodeViewDropAdapter extends ViewerDropAdapter
{
    private final NodeContext context;

    public NodeViewDropAdapter(NodeContext context, Viewer viewer)
    {
        super(viewer);
        this.context = context;
        setFeedbackEnabled(true);
    }

    public void dragEnter(DropTargetEvent event)
    {
        // if
        // ((NodeTransfer.getInstance().isSupportedType(event.currentDataType))
        // && (event.detail == DND.DROP_DEFAULT || event.detail ==
        // DND.DROP_MOVE))
        // {
        // Object dndData =
        // NodeTransfer.getInstance().nativeToJava(event.currentDataType);
        //
        // }
        super.dragEnter(event);
    }

    public void dragOperationChanged(DropTargetEvent event)
    {
        // if
        // ((NodeTransfer.getInstance().isSupportedType(event.currentDataType))
        // && (event.detail == DND.DROP_DEFAULT || event.detail ==
        // DND.DROP_MOVE))
        // {
        // Object dndData =
        // NodeTransfer.getInstance().nativeToJava(event.currentDataType);
        //
        // }
        super.dragOperationChanged(event);
    }

    @Override
    public boolean performDrop(Object data)
    {
        final int location = getCurrentLocation();
        Object target = getCurrentTarget();

        if (data instanceof Object[])
        {

            Object[] sourceNodes = (Object[]) data;

            for (Object object : sourceNodes)
            {
                if (object instanceof AbstractNode<?>)
                {
                    final AbstractNode<?> sourceNode = (AbstractNode<?>) object;
                    final AbstractNode<?> node;
                    if (target instanceof AbstractNode<?>)
                        node = (AbstractNode<?>) target;
                    else
                        node = null;

                    if ((location == LOCATION_BEFORE || location == LOCATION_AFTER))
                    {
                        final NodeMoveProvider.Neighbor neighbor;
                        if (node instanceof NodeMoveProvider.Neighbor)
                        {
                            neighbor = (NodeMoveProvider.Neighbor) node;
                        }
                        else
                            return false;

                        NodeMoveProvider provider = null;
                        if (node.getParent() == null)
                        {
                            provider = context.getRootNodeMoveProvider();
                        }
                        else if (node.getParent() instanceof NodeMoveProvider)
                        {
                            provider = (NodeMoveProvider) node.getParent();
                        }

                        if (provider != null && neighbor != null)
                        {
                            final NodeMoveProvider moveProvider = provider;
                            final Object source;
                            if (sourceNode instanceof NodeMoveProvider.Neighbor)
                                source = ((NodeMoveProvider.Neighbor) sourceNode).getNeighborSource();
                            else
                                source = sourceNode.getSource();

                            INodeDeleteProvider sourceDelProvider = sourceNode.getDeleteProvider();

                            AbstractOperation deleteOperation = null;
                            if (sourceDelProvider != null)
                            {
                                deleteOperation = sourceDelProvider.deleteOperation(false);
                            }
                            AbstractOperation moveOperation = moveProvider.moveOperation(context, neighbor, source, location == LOCATION_BEFORE);

                            if (deleteOperation == null || moveOperation == null)
                            {
                                System.err.println("INodeDeleteProvider.deleteOperation or NodeMoveProvider.moveOperation not impl: " + deleteOperation + ", "
                                        + moveProvider);
                                if (sourceDelProvider != null)
                                {
                                    sourceDelProvider.delete(false);
                                }
                                moveProvider.move(context, neighbor, source, location == LOCATION_BEFORE);

                                context.nodesUpdated();

                                context.refresh(node != null ? node.getParent() : null);
                                context.selectNodes(false, sourceNode);
                            }
                            else
                            {
                                ReversibleOperation operation = new ReversibleOperation("Move")
                                {
                                    @Override
                                    protected void refresh()
                                    {
                                        Display.getCurrent().asyncExec(new Runnable()
                                        {

                                            public void run()
                                            {
                                                context.nodesUpdated();

                                                context.refresh(node != null ? node.getParent() : null);
                                                context.selectNodes(false, sourceNode);

                                            }
                                        });
                                    }
                                };
                                operation.add(deleteOperation);
                                operation.add(moveOperation);
                                context.getEditor().execute(operation);
                            }

                        }

                    }
                    else if (location == LOCATION_ON || location == LOCATION_NONE)
                    {
                        NodeMoveProvider tagetProvider = null;
                        if (node == null)
                            tagetProvider = context.getRootNodeMoveProvider();
                        else if (node instanceof NodeMoveProvider)
                        {
                            tagetProvider = (NodeMoveProvider) node;
                        }
                        final NodeMoveProvider provider = tagetProvider;

                        final Object source;
                        if (sourceNode instanceof NodeMoveProvider.Neighbor)
                            source = ((NodeMoveProvider.Neighbor) sourceNode).getNeighborSource();
                        else
                            source = sourceNode.getSource();

                        if (provider != null)
                        {

                            INodeDeleteProvider sourceDelProvider = sourceNode.getDeleteProvider();
                            
                            
                            AbstractOperation deleteOperation = null;
                            if (sourceDelProvider != null)
                            {
                                deleteOperation = sourceDelProvider.deleteOperation(false);
                            }
                            AbstractOperation moveOperation = provider.moveOperation(context, null, source, false);;

                            if (deleteOperation == null || moveOperation == null)
                            {
                                
                                System.err.println("INodeDeleteProvider.deleteOperation or NodeMoveProvider.moveOperation not impl: " + deleteOperation + ", "
                                        + provider);
                                if (sourceDelProvider != null)
                                {
                                    sourceDelProvider.delete(false);
                                }
                                provider.move(context, null, source, false);
                                context.nodesUpdated();

                                context.refresh(node);
                                sourceNode.setParent(node);
                                context.expand(node);
                                context.selectNodes(false, sourceNode);
                            }
                            else
                            {
                                ReversibleOperation operation = new ReversibleOperation("Move")
                                {
                                    @Override
                                    protected void refresh()
                                    {
                                        Display.getCurrent().asyncExec(new Runnable()
                                        {

                                            public void run()
                                            {
                                                context.nodesUpdated();

                                                context.refresh(node);
                                                sourceNode.setParent(node);
                                                context.expand(node);
                                                context.selectNodes(false, sourceNode);

                                            }
                                        });
                                    }
                                };
                                operation.add(deleteOperation);
                                operation.add(moveOperation);
                                context.getEditor().execute(operation);
                            }
                            
                            

                        }
                    }
                }
            }

        }
        return false;
    }

    @Override
    public boolean validateDrop(Object target, int operation, TransferData transferType)
    {
        if (NodeTransfer.getInstance().isSupportedType(transferType))
        {
            Object dndData = NodeTransfer.getInstance().nativeToJava(transferType);
            final int location = getCurrentLocation();

            if (dndData instanceof Object[])
            {
                Object[] sourceNodes = (Object[]) dndData;
                AbstractNode<?> node = (AbstractNode<?>) target;
                boolean valid = false;
                for (Object element : sourceNodes)
                {
                    if (!(element instanceof AbstractNode<?>))
                    {
                        valid = false;
                        break;
                    }

                    AbstractNode<?> sourceNode = (AbstractNode<?>) element;

                    if ((location == LOCATION_BEFORE || location == LOCATION_AFTER))
                    {
                        NodeMoveProvider.Neighbor relation = null;
                        if (node instanceof NodeMoveProvider.Neighbor)
                        {
                            relation = (NodeMoveProvider.Neighbor) node;
                        }
                        else
                            return false;

                        AbstractNode<?> parent = node.getParent();

                        NodeMoveProvider provider = null;
                        if (parent == null)
                        {
                            provider = context.getRootNodeMoveProvider();
                        }
                        else if (parent instanceof NodeMoveProvider)
                        {
                            provider = (NodeMoveProvider) parent;
                        }
                        Object source;
                        if (sourceNode instanceof NodeMoveProvider.Neighbor)
                            source = ((NodeMoveProvider.Neighbor) sourceNode).getNeighborSource();
                        else
                            source = sourceNode.getSource();

                        if (parent != null && parent.getSource().equals(source))
                        {
                            return false;
                        }

                        valid = provider != null && !relation.getNeighborSource().equals(source) && provider.canMove(relation, source);

                    }
                    else if (location == LOCATION_ON || location == LOCATION_NONE)
                    {
                        NodeMoveProvider provider = null;
                        if (node == null)
                            provider = context.getRootNodeMoveProvider();
                        else if (node instanceof NodeMoveProvider)
                        {
                            provider = (NodeMoveProvider) node;
                        }
                        if (provider == null)
                        {
                            return false;
                        }
                        Object source;
                        if (sourceNode instanceof NodeMoveProvider.Neighbor)
                            source = ((NodeMoveProvider.Neighbor) sourceNode).getNeighborSource();
                        else
                            source = sourceNode.getSource();
                        if (node != null && node.getSource().equals(source))
                        {
                            return false;
                        }

                        valid = provider != null && provider.canMove(null, source);
                    }
                    if (!valid)
                        break;
                }
                return valid;

            }

        }
        return false;
    }

}
