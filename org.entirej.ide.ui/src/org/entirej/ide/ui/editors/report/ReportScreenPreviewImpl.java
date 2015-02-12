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
package org.entirej.ide.ui.editors.report;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.Line;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.Line.LineDirection;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.framework.plugin.reports.containers.EJReportScreenItemContainer;
import org.entirej.framework.report.enumerations.EJReportScreenItemType;
import org.entirej.framework.report.enumerations.EJReportScreenType;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.nodes.AbstractNode;

public class ReportScreenPreviewImpl implements IReportPreviewProvider
{
    protected final Color                          COLOR_BLOCK        = new Color(Display.getCurrent(), new RGB(255, 251, 227));
    protected final Color                          COLOR_BLOCK_ITEM   = new Color(Display.getCurrent(), new RGB(240, 240, 240));
    protected final Color                          COLOR_LIGHT_YELLOW = Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
    protected final Color                          COLOR_WHITE        = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
    protected final Color                          COLOR_LIGHT_SHADOW = Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
    protected final Cursor                         RESIZE             = new Cursor(Display.getCurrent(), SWT.CURSOR_SIZESE);
    protected final Cursor                         MOVE               = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
    protected final EJPluginReportScreenProperties properties;

    private int                                    x, y;

    public ReportScreenPreviewImpl(EJPluginReportScreenProperties properties)
    {
        this.properties = properties;
    }

    public void dispose()
    {
        COLOR_BLOCK_ITEM.dispose();
        COLOR_BLOCK.dispose();
        RESIZE.dispose();
        MOVE.dispose();
    }

    
    protected int getHeight()
    {
        
        return properties.getHeight();
    }
    
    protected EJPluginReportProperties getReportProperties(AbstractEJReportEditor editor)
    {
        return editor.getReportProperties();
    }

    void updateSize(final AbstractEJReportEditor editor, EJPluginReportScreenProperties layoutScreenProperties, Composite parent)
    {

        int width = layoutScreenProperties.getWidth();
        int height = getHeight();

        boolean updated = false;
        Control[] children = parent.getChildren();
        for (Control control : children)
        {
            if (width < (control.getBounds().x + control.getBounds().width))
            {
                width = (control.getBounds().x + control.getBounds().width);
                updated = true;
            }
            if (height < (control.getBounds().y + control.getBounds().height))
            {
                height = (control.getBounds().y + control.getBounds().height);
                updated = true;
            }
        }

        if (updated)
        {
            parent.setBounds(parent.getBounds().x, parent.getBounds().y, width, height);
        }
    }

    public void buildPreview(final AbstractEJReportEditor editor, ScrolledComposite previewComposite)
    {
        // layout canvas preview
        Composite pContent = new Composite(previewComposite, SWT.NONE);

        EJPluginReportProperties formProperties = getReportProperties(editor);

        previewComposite.setContent(pContent);
        setPreviewBackground(previewComposite, COLOR_LIGHT_YELLOW);
        previewComposite.setExpandHorizontal(true);
        previewComposite.setExpandVertical(true);

        pContent.setLayout(null);
        setPreviewBackground(pContent, COLOR_LIGHT_YELLOW);

        final Composite reportBody = new Composite(pContent, SWT.BORDER);
        reportBody.setLayout(null);
        setPreviewBackground(reportBody, COLOR_WHITE);

        final EJPluginReportScreenProperties layoutScreenProperties = properties;

        reportBody.setBounds(10, 10, layoutScreenProperties.getWidth(), getHeight());

        List<EJPluginReportScreenItemProperties> allItemProperties = layoutScreenProperties.getScreenItemContainer().getAllItemProperties();
        for (EJPluginReportScreenItemProperties screenItemProperties : allItemProperties)
        {
            createItemPreview(editor, layoutScreenProperties, reportBody, screenItemProperties);
        }

        for (EJPluginReportBlockProperties properties : layoutScreenProperties.getAllSubBlocks())
        {
            createBlockPreview(editor, reportBody, properties);
        }

        previewComposite.setMinSize(layoutScreenProperties.getWidth() + 20, getHeight() + 20);// add
                                                                                                                     // offset

        final LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();

        final DropTargetAdapter dragAdapter = new DropTargetAdapter()
        {
            int x,y;
            @Override
            public void dragOver(DropTargetEvent event)
            {

                final DragObject droppedObj = transfer.getSelection() != null ? ((DragObject) ((StructuredSelection) transfer.getSelection()).getFirstElement())
                        : null;

                if (droppedObj != null)
                {
                    droppedObj.indicate(x=event.x, y=event.y);
                }
            }

            @Override
            public void drop(DropTargetEvent event)
            {
                final DragObject droppedObj = transfer.getSelection() != null ? ((DragObject) ((StructuredSelection) transfer.getSelection()).getFirstElement())
                        : null;

                if (droppedObj != null)
                {
                    droppedObj.setBond(event.x, event.y);
                }
            }
            
            @Override
            public void dragLeave(DropTargetEvent event)
            {
                final DragObject droppedObj = transfer.getSelection() != null ? ((DragObject) ((StructuredSelection) transfer.getSelection()).getFirstElement())
                        : null;

                if (droppedObj != null)
                {
                    droppedObj.setBond(x, y);
                }
            }
            
           
            
            

        };

        final DropTarget dropTarget = new DropTarget(reportBody, DND.DROP_MOVE | DND.DROP_COPY);
        dropTarget.setTransfer(new Transfer[] { transfer });
        dropTarget.addDropListener(dragAdapter);
       
        // create menu
        final Menu menu = new Menu(editor.getEditorSite().getShell(), SWT.POP_UP);
        final EJReportScreenItemContainer container = layoutScreenProperties.getScreenItemContainer();

        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText(String.format("New Screen Item "));
        item.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                AbstractNode<?> findNode = editor.findNode(container);
                if (findNode instanceof ReportBlockScreenItemsGroupNode)
                {
                    ReportBlockScreenItemsGroupNode node = (ReportBlockScreenItemsGroupNode) findNode;

                    node.newScreenItem(x, y, container, -1);
                }
            }
        });

        reportBody.setMenu(menu);
        reportBody.addMouseListener(new MouseAdapter()
        {

            @Override
            public void mouseDown(MouseEvent e)
            {
                x = e.x;
                y = e.y;
            }
        });
        updateSize(editor, layoutScreenProperties, reportBody);
    }

    protected void createItemPreview(final AbstractEJReportEditor editor, final EJPluginReportScreenProperties layoutScreenProperties,
            final Composite reportBody, final EJPluginReportScreenItemProperties properties)
    {

        int style = SWT.NONE;

        if (properties.getType() == EJReportScreenItemType.LINE || properties.getType() == EJReportScreenItemType.RECTANGLE)
        {
            style = SWT.NONE;
        }
        else
        {
            style = SWT.BORDER;
        }
        final Composite block = new Composite(reportBody, style);
        setPreviewBackground(block, COLOR_BLOCK_ITEM);

        int width = properties.getWidth();
        if(properties.isWidthAsPercentage())
        {
                width = (int) (((double)layoutScreenProperties.getWidth()/100)*properties.getWidth());
        }
        
        int height = properties.getHeight();
        if(properties.isHeightAsPercentage())
        {
            height = (int) (((double)getHeight()/100)*properties.getHeight());
        }
        block.setBounds(properties.getX(), properties.getY(), width, height);

        block.setLayout(null);
        block.moveAbove(null);

        final Label hint = new Label(block, SWT.NONE);
        hint.setText(String.format("%s [ %d, %d ] [ %d, %d ]", properties.getName(), properties.getX(), properties.getY(), width,
                height));

        hint.setToolTipText(hint.getText());
        hint.setBounds(10, 0, width - 10, 25);

        final LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();

        final DragObject dragObjectMove = new DragObject()
        {

            public void setBond(int x, int y)
            {

             
                
                
                
                
                
                
                final int oldX = properties.getX();
                final int oldY = properties.getY();

                Point display = reportBody.toControl(x, y);
                final int newX = display.x;
                final int newY = display.y;

                AbstractOperation operation = new AbstractOperation("Move")
                {

                    @Override
                    public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                    {
                        properties.setX(oldX);
                        properties.setY(oldY);
                        editor.setDirty(true);
                        editor.refresh(properties);
                        editor.refreshPreview();
                        return Status.OK_STATUS;
                    }

                    @Override
                    public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                    {
                        properties.setX(newX);
                        properties.setY(newY);
                        editor.setDirty(true);
                        editor.refresh(properties);
                        editor.refreshPreview();
                        return Status.OK_STATUS;
                    }

                    @Override
                    public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                    {
                        properties.setX(newX);
                        properties.setY(newY);
                        int width = properties.getWidth();
                        if(properties.isWidthAsPercentage())
                        {
                                width = (int) (((double)layoutScreenProperties.getWidth()/100)*properties.getWidth());
                        }
                        
                        int height = properties.getHeight();
                        if(properties.isHeightAsPercentage())
                        {
                            height = (int) (((double)getHeight()/100)* properties.getHeight());
                        }
                        block.setBounds(properties.getX(), properties.getY(), width, height);
                        block.setBounds(properties.getX(), properties.getY(), width, height);

                        hint.setText(String.format("%s [ %d, %d ] [ %d, %d ]", properties.getName(), properties.getX(), properties.getY(),
                                width, height));
                        editor.setDirty(true);
                        editor.refresh(properties);
                        return Status.OK_STATUS;
                    }
                };

                editor.execute(operation);
            }

            public void indicate(int x, int y)
            {
                Point display = reportBody.toControl(x, y);
                int width = properties.getWidth();
                if(properties.isWidthAsPercentage())
                {
                        width = (int) (((double)layoutScreenProperties.getWidth()/100)*width);
                }
                
                int height = properties.getHeight();
                if(properties.isHeightAsPercentage())
                {
                    height = (int) (((double)getHeight()/100)* properties.getHeight());
                }
                block.setBounds(display.x, display.y, width, height);

                hint.setText(String.format("%s [ %d, %d ] [ %d, %d ]", properties.getName(), display.x, display.y, width,
                        height));
                editor.setDirty(true);
                editor.refresh(properties);

            }

        };

        hint.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseDoubleClick(MouseEvent e)
            {

                editor.select(properties);
                editor.expand(properties);
            }

            @Override
            public void mouseDown(MouseEvent e)
            {

                transfer.setSelection(new StructuredSelection(dragObjectMove));
            }

            @Override
            public void mouseUp(MouseEvent e)
            {

                block.moveAbove(null);
            }

        });

        block.addMouseListener(new MouseAdapter()
        {

            @Override
            public void mouseDoubleClick(MouseEvent e)
            {

                editor.select(properties);
                editor.expand(properties);
            }

            @Override
            public void mouseDown(MouseEvent e)
            {

                transfer.setSelection(new StructuredSelection(dragObjectMove));
            }

            @Override
            public void mouseUp(MouseEvent e)
            {

                block.moveAbove(null);
            }
        });

        final Label move = new Label(block, SWT.NONE);
        move.setImage(EJUIImages.getImage(EJUIImages.DESC_FORM_MOVE_OBJ));
        move.setBounds(0, 0, 10, 10);
        final Label resize = new Label(block, SWT.NONE);
        resize.setImage(EJUIImages.getImage(EJUIImages.DESC_FORM_RESIZE_OBJ));
        resize.setBounds(width > 12 ? width - 12 : 0, height > 12 ? height - 12 : 0, 10, 10);

        move.setToolTipText("Move");
        resize.setToolTipText("Resize");
        move.setCursor(MOVE);
        resize.setCursor(RESIZE);
        resize.moveAbove(null);

        final DragObject dragObjectResize = new DragObject()
        {

            public void setBond(int x, int y)
            {

                Point display = reportBody.toControl(x, y);

                if (display.x >= properties.getX() && display.y >= properties.getY())
                {

                    final int width = display.x - properties.getX();

                    final int height = display.y - properties.getY();
                    final int oldWidth = properties.getWidth();

                    final int oldHeight = properties.getHeight();

                    
                    
                    AbstractOperation operation = new AbstractOperation("Move")
                    {

                        @Override
                        public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                        {
                            properties.setWidth(oldWidth);
                            properties.setHeight(oldHeight);
                            editor.setDirty(true);
                            editor.refresh(properties);
                            editor.refreshPreview();
                            return Status.OK_STATUS;
                        }

                        @Override
                        public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                        {
                            if(properties.isWidthAsPercentage())
                            {
                                properties.setWidth((int) (((double)(width)/layoutScreenProperties.getWidth())*100));
                            }
                            else
                            {
                                properties.setWidth(width);
                            }
                            if(properties.isHeightAsPercentage())
                            {
                                properties.setHeight((int) (((double)(height)/getHeight())*100));
                            }
                            else
                            {
                                properties.setHeight(height);
                            }
                            editor.setDirty(true);
                            editor.refresh(properties);
                            editor.refreshPreview();
                            return Status.OK_STATUS;
                        }

                        @Override
                        public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                        {
                            if(properties.isWidthAsPercentage())
                            {
                                properties.setWidth((int) (((double)(width)/layoutScreenProperties.getWidth())*100));
                            }
                            else
                            {
                                properties.setWidth(width);
                            }
                            if(properties.isHeightAsPercentage())
                            {
                                properties.setHeight((int) (((double)(height)/getHeight())*100));
                            }
                            else
                            {
                                properties.setHeight(height);
                            }
                            
                           
                            block.setBounds(properties.getX(), properties.getY(), width, height);

                            hint.setText(String.format("%s [ %d, %d ] [ %d, %d ]", properties.getName(), properties.getX(), properties.getY(), width,
                                    height));
                            resize.setBounds(width > 12 ? width - 12 : 0,
                                    height > 12 ? height - 12 : 0, 10, 10);
                            hint.setBounds(10, 0, width - 10, 25);
                            editor.setDirty(true);
                            editor.refresh(properties);
                            updateSize(editor, layoutScreenProperties, reportBody);
                            return Status.OK_STATUS;
                        }
                    };
                    editor.execute(operation);
                    
                }

            }

            public void indicate(int x, int y)
            {
                Point display = reportBody.toControl(x, y);

                if (display.x >= properties.getX() && display.y >= properties.getY())
                {

                    int width = display.x - properties.getX();

                    int height = display.y - properties.getY();

                    block.setBounds(properties.getX(), properties.getY(), width, height);

                    hint.setText(String.format("%s [ %d, %d ] [ %d, %d ]", properties.getName(), properties.getX(), properties.getY(), width, height));
                    resize.setBounds(width > 12 ? width - 12 : 0, height > 12 ? height - 12 : 0, 10, 10);
                    hint.setBounds(10, 0, width - 10, 25);
                    editor.setDirty(true);
                    editor.refresh(properties);
                    updateSize(editor, layoutScreenProperties, reportBody);
                }

            }
        };

        move.addMouseListener(new MouseAdapter()
        {

            @Override
            public void mouseDown(MouseEvent e)
            {
                block.moveAbove(null);
                transfer.setSelection(new StructuredSelection(dragObjectMove));
            }
        });
        resize.addMouseListener(new MouseAdapter()
        {

            @Override
            public void mouseDown(MouseEvent e)
            {
                block.moveAbove(null);
                transfer.setSelection(new StructuredSelection(dragObjectResize));
            }
        });

        final DragSourceAdapter resizeMoveAdapter = new DragSourceAdapter()
        {
            @Override
            public void dragSetData(final DragSourceEvent event)
            {

                transfer.setSelection(new StructuredSelection(dragObjectResize));
            }

            @Override
            public void dragFinished(DragSourceEvent event)
            {
                editor.refreshProperties();
            }
        };

        final DragSource dragSourceResize = new DragSource(resize, DND.DROP_MOVE | DND.DROP_COPY);
        dragSourceResize.setTransfer(new Transfer[] { transfer });
        dragSourceResize.addDragListener(resizeMoveAdapter);

        final DragSourceAdapter dragMoveAdapter = new DragSourceAdapter()
        {
            @Override
            public void dragSetData(final DragSourceEvent event)
            {

                transfer.setSelection(new StructuredSelection(dragObjectMove));
            }

            @Override
            public void dragFinished(DragSourceEvent event)
            {
                editor.refreshProperties();
            }
        };

        final DragSource dragSourceMove = new DragSource(move, DND.DROP_MOVE | DND.DROP_COPY);
        dragSourceMove.setTransfer(new Transfer[] { transfer });
        dragSourceMove.addDragListener(dragMoveAdapter);

        if (properties.getType() == EJReportScreenItemType.LINE)
        {
            final EJPluginReportScreenItemProperties.Line line = (Line) properties;
            final DragSource dragSourceMoveLine = new DragSource(block, DND.DROP_MOVE | DND.DROP_COPY);
            block.setCursor(MOVE);
            resize.setImage(null);
            move.setImage(null);

            block.setToolTipText(line.getName());
            dragSourceMoveLine.setTransfer(new Transfer[] { transfer });
            dragSourceMoveLine.addDragListener(dragMoveAdapter);
            hint.setVisible(false);
            block.addPaintListener(new PaintListener()
            {
                public void paintControl(PaintEvent e)
                {
                    int lineWidth = (int) Math.ceil(line.getLineWidth());
                    e.gc.setLineWidth(lineWidth);

                    switch (line.getLineStyle())
                    {
                        case DOTTED:
                            e.gc.setLineStyle(SWT.LINE_DOT);
                            break;
                        case DASHED:
                            e.gc.setLineStyle(SWT.LINE_DASH);
                            break;

                        default:
                            e.gc.setLineStyle(SWT.LINE_SOLID);
                            break;
                    }

                    if (line.getLineDirection() == LineDirection.TO_DOWN)
                        e.gc.drawLine(0, 0, block.getBounds().width <= lineWidth ? 0 : block.getBounds().width, block.getBounds().height <= lineWidth ? 0
                                : block.getBounds().height);
                    else
                        e.gc.drawLine(0, block.getBounds().height <= lineWidth ? 0 : block.getBounds().height,
                                block.getBounds().width <= lineWidth ? 0 : block.getBounds().width, 0);

                }
            });
        }
        if (properties.getType() == EJReportScreenItemType.RECTANGLE)
        {
            final EJPluginReportScreenItemProperties.Rectangle line = (EJPluginReportScreenItemProperties.Rectangle) properties;
            final DragSource dragSourceMoveLine = new DragSource(block, DND.DROP_MOVE | DND.DROP_COPY);
            block.setCursor(MOVE);
            resize.setImage(null);
            move.setImage(null);

            block.setToolTipText(line.getName());
            dragSourceMoveLine.setTransfer(new Transfer[] { transfer });
            dragSourceMoveLine.addDragListener(dragMoveAdapter);
            hint.setVisible(false);
            block.addPaintListener(new PaintListener()
            {
                public void paintControl(PaintEvent e)
                {
                    int lineWidth = (int) Math.ceil(line.getLineWidth());
                    e.gc.setLineWidth(lineWidth);

                    switch (line.getLineStyle())
                    {
                        case DOTTED:
                            e.gc.setLineStyle(SWT.LINE_DOT);
                            break;
                        case DASHED:
                            e.gc.setLineStyle(SWT.LINE_DASH);
                            break;

                        default:
                            e.gc.setLineStyle(SWT.LINE_SOLID);
                            break;
                    }

                    int x = lineWidth > 1 ? lineWidth : 0;
                    int y = lineWidth > 1 ? lineWidth : 0;
                    if (line.getRadius() == 0)
                        e.gc.drawRectangle(x, y, block.getBounds().width - (lineWidth + x), block.getBounds().height - (lineWidth + y));
                    else
                        e.gc.drawRoundRectangle(x, y, block.getBounds().width - (lineWidth + x), block.getBounds().height - (lineWidth + y), line.getRadius(),
                                line.getRadius());

                }
            });
        }

    }

    protected void createBlockPreview(final AbstractEJReportEditor editor, final Composite reportBody, final EJPluginReportBlockProperties properties)
    {

        final EJPluginReportScreenProperties screenProperties = properties.getLayoutScreenProperties();
        if (screenProperties.getScreenType() != EJReportScreenType.NONE)
        {

            final Composite block = new Composite(reportBody, SWT.BORDER);
            setPreviewBackground(block, COLOR_BLOCK);

            block.setBounds(screenProperties.getX(), screenProperties.getY(), screenProperties.getWidth(), screenProperties.getHeight());

            block.setLayout(null);
            block.moveAbove(null);
            block.addMouseListener(new MouseAdapter()
            {

                @Override
                public void mouseDown(MouseEvent e)
                {
                    block.moveAbove(null);
                }
            });

            final Label hint = new Label(block, SWT.NONE);
            hint.setText(String.format("%s [ %d, %d ] [ %d, %d ]", properties.getName(), screenProperties.getX(), screenProperties.getY(),
                    screenProperties.getWidth(), screenProperties.getHeight()));

            hint.setToolTipText(hint.getText());
            hint.setBounds(10, 0, screenProperties.getWidth() - 10, 25);
            hint.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseDoubleClick(MouseEvent e)
                {
                    editor.select(screenProperties);
                    editor.expand(screenProperties);
                }

                @Override
                public void mouseUp(MouseEvent e)
                {
                    block.moveAbove(null);
                }

            });

            final Label move = new Label(block, SWT.NONE);
            move.setImage(EJUIImages.getImage(EJUIImages.DESC_FORM_MOVE_OBJ));
            move.setBounds(0, 0, 10, 10);
            final Label resize = new Label(block, SWT.NONE);
            resize.setImage(EJUIImages.getImage(EJUIImages.DESC_FORM_RESIZE_OBJ));
            resize.setBounds(screenProperties.getWidth() - 12, screenProperties.getHeight() - 12, 10, 10);

            move.setToolTipText("Move");
            resize.setToolTipText("Resize");
            move.setCursor(MOVE);
            resize.setCursor(RESIZE);
            resize.moveAbove(null);

            final LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();

            final DragObject dragObjectResize = new DragObject()
            {

                public void setBond(int x, int y)
                {

                    Point display = reportBody.toControl(x, y);

                    if (display.x >= screenProperties.getX() && display.y >= screenProperties.getY())
                    {

                        final int width = display.x - screenProperties.getX();
                        final int height = display.y - screenProperties.getY();
                        final int oldWidth = screenProperties.getWidth();
                        final int oldHeight = screenProperties.getHeight();

                        AbstractOperation operation = new AbstractOperation("Move")
                        {

                            @Override
                            public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                            {
                                screenProperties.setWidth(oldWidth);

                                screenProperties.setHeight(oldHeight);
                                editor.setDirty(true);
                                editor.refresh(properties);
                                editor.refreshPreview();
                                return Status.OK_STATUS;
                            }

                            @Override
                            public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                            {
                                screenProperties.setWidth(width);

                                screenProperties.setHeight(height);
                                editor.setDirty(true);
                                editor.refresh(properties);
                                editor.refreshPreview();
                                return Status.OK_STATUS;
                            }

                            @Override
                            public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                            {
                                screenProperties.setWidth(width);

                                screenProperties.setHeight(height);
                                block.setBounds(screenProperties.getX(), screenProperties.getY(), screenProperties.getWidth(), screenProperties.getHeight());

                                hint.setText(String.format("%s [ %d, %d ] [ %d, %d ]", properties.getName(), screenProperties.getX(), screenProperties.getY(),
                                        screenProperties.getWidth(), screenProperties.getHeight()));
                                resize.setBounds(screenProperties.getWidth() - 12, screenProperties.getHeight() - 12, 10, 10);
                                hint.setBounds(10, 0, screenProperties.getWidth() - 10, 25);
                                editor.setDirty(true);
                                editor.refresh(properties);
                                return Status.OK_STATUS;
                            }
                        };

                        editor.execute(operation);
                    }

                }

                public void indicate(int x, int y)
                {
                    Point display = reportBody.toControl(x, y);

                    if (display.x >= screenProperties.getX() && display.y >= screenProperties.getY())
                    {

                        int width = display.x - screenProperties.getX();

                        int height = display.y - screenProperties.getY();

                        block.setBounds(screenProperties.getX(), screenProperties.getY(), width, height);

                        hint.setText(String.format("%s [ %d, %d ] [ %d, %d ]", properties.getName(), screenProperties.getX(), screenProperties.getY(), width,
                                height));
                        resize.setBounds(width - 12, height - 12, 10, 10);
                        hint.setBounds(10, 0, width - 10, 25);

                    }

                }
            };
            final DragObject dragObjectMove = new DragObject()
            {

                public void setBond(int x, int y)
                {

                    final int oldX = screenProperties.getX();
                    final int oldY = screenProperties.getY();

                    Point display = reportBody.toControl(x, y);
                    final int newX = display.x;
                    final int newY = display.y;

                    AbstractOperation operation = new AbstractOperation("Move")
                    {

                        @Override
                        public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                        {
                            screenProperties.setX(oldX);
                            screenProperties.setY(oldY);
                            editor.setDirty(true);
                            editor.refresh(properties);
                            editor.refreshPreview();
                            return Status.OK_STATUS;
                        }

                        @Override
                        public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                        {
                            screenProperties.setX(newX);
                            screenProperties.setY(newY);
                            editor.setDirty(true);
                            editor.refresh(properties);
                            editor.refreshPreview();
                            return Status.OK_STATUS;
                        }

                        @Override
                        public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                        {
                            screenProperties.setX(newX);
                            screenProperties.setY(newY);

                            block.setBounds(screenProperties.getX(), screenProperties.getY(), screenProperties.getWidth(), screenProperties.getHeight());

                            hint.setText(String.format("%s [ %d, %d ] [ %d, %d ]", properties.getName(), screenProperties.getX(), screenProperties.getY(),
                                    screenProperties.getWidth(), screenProperties.getHeight()));
                            editor.setDirty(true);
                            editor.refresh(properties);
                            return Status.OK_STATUS;
                        }
                    };

                    editor.execute(operation);

                }

                public void indicate(int x, int y)
                {
                    Point display = reportBody.toControl(x, y);

                    block.setBounds(display.x, display.y, screenProperties.getWidth(), screenProperties.getHeight());

                    hint.setText(String.format("%s [ %d, %d ] [ %d, %d ]", properties.getName(), display.x, display.y, screenProperties.getWidth(),
                            screenProperties.getHeight()));

                }
            };

            move.addMouseListener(new MouseAdapter()
            {

                @Override
                public void mouseDown(MouseEvent e)
                {
                    block.moveAbove(null);
                    transfer.setSelection(new StructuredSelection(dragObjectMove));
                }
            });
            resize.addMouseListener(new MouseAdapter()
            {

                @Override
                public void mouseDown(MouseEvent e)
                {
                    block.moveAbove(null);
                    transfer.setSelection(new StructuredSelection(dragObjectResize));
                }
            });

            final DragSourceAdapter resizeMoveAdapter = new DragSourceAdapter()
            {
                @Override
                public void dragSetData(final DragSourceEvent event)
                {

                    transfer.setSelection(new StructuredSelection(dragObjectResize));
                }

                @Override
                public void dragFinished(DragSourceEvent event)
                {
                    editor.refreshProperties();
                }
            };

            final DragSource dragSourceResize = new DragSource(resize, DND.DROP_MOVE | DND.DROP_COPY);
            dragSourceResize.setTransfer(new Transfer[] { transfer });
            dragSourceResize.addDragListener(resizeMoveAdapter);

            final DragSourceAdapter dragMoveAdapter = new DragSourceAdapter()
            {
                @Override
                public void dragSetData(final DragSourceEvent event)
                {

                    transfer.setSelection(new StructuredSelection(dragObjectMove));
                }

                @Override
                public void dragFinished(DragSourceEvent event)
                {
                    editor.refreshProperties();
                }
            };

            final DragSource dragSourceMove = new DragSource(move, DND.DROP_MOVE | DND.DROP_COPY);
            dragSourceMove.setTransfer(new Transfer[] { transfer });
            dragSourceMove.addDragListener(dragMoveAdapter);
        }
    }

    protected void setPreviewBackground(Control control, Color color)
    {
        control.setBackground(color);
    }

    public String getDescription()
    {
        return "preview the defined canvas layout in form.";
    }

    private static interface DragObject
    {
        void setBond(int x, int y);

        void indicate(int x, int y);
    }
}
