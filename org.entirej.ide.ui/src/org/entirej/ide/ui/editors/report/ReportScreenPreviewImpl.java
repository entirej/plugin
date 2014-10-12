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

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
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
import org.entirej.ide.ui.EJUIPlugin;

public class ReportScreenPreviewImpl implements IReportPreviewProvider
{
    protected final Color                         COLOR_BLOCK        = new Color(Display.getCurrent(), new RGB(255, 251, 227));
    protected final Color                         COLOR_BLOCK_ITEM   = new Color(Display.getCurrent(), new RGB(240, 240, 240));
    protected final Color                         COLOR_LIGHT_YELLOW = Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
    protected final Color                         COLOR_WHITE        = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
    protected final Color                         COLOR_LIGHT_SHADOW = Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
    protected final Cursor                        RESIZE             = new Cursor(Display.getCurrent(), SWT.CURSOR_SIZESE);
    protected final Cursor                        MOVE               = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
    protected final EJPluginReportScreenProperties properties;

    private int                                   x, y;

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

    protected EJPluginReportProperties getReportProperties(AbstractEJReportEditor editor)
    {
        return editor.getReportProperties();
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

        Composite reportBody = new Composite(pContent, SWT.BORDER);
        reportBody.setLayout(null);
        setPreviewBackground(reportBody, COLOR_WHITE);

        EJPluginReportScreenProperties layoutScreenProperties = properties;

        reportBody.setBounds(10, 10, layoutScreenProperties.getWidth(), layoutScreenProperties.getHeight());

        List<EJPluginReportScreenItemProperties> allItemProperties = layoutScreenProperties.getScreenItemContainer().getAllItemProperties();
        for (EJPluginReportScreenItemProperties screenItemProperties : allItemProperties)
        {
            createItemPreview(editor, reportBody, screenItemProperties);
        }

        for (EJPluginReportBlockProperties properties : layoutScreenProperties.getAllSubBlocks())
        {
            createBlockPreview(editor, reportBody, properties);
        }

        previewComposite.setMinSize(layoutScreenProperties.getWidth() + 20, layoutScreenProperties.getHeight() + 20);// add
                                                                                                                     // offset

        final LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();

        final DropTargetAdapter dragAdapter = new DropTargetAdapter()
        {

            @Override
            public void dragOver(DropTargetEvent event)
            {

                final DragObject droppedObj = transfer.getSelection() != null ? ((DragObject) ((StructuredSelection) transfer.getSelection()).getFirstElement())
                        : null;

                if (droppedObj != null)
                {
                    droppedObj.setBond(event.x, event.y);
                }
            }

        };

        final DropTarget dropTarget = new DropTarget(reportBody, DND.DROP_MOVE | DND.DROP_COPY);
        dropTarget.setTransfer(new Transfer[] { transfer });
        dropTarget.addDropListener(dragAdapter);

        // create menu
        final Menu menu = new Menu(editor.getEditorSite().getShell(), SWT.POP_UP);
        final EJReportScreenItemContainer container = layoutScreenProperties.getScreenItemContainer();

        for (final EJReportScreenItemType type : EJReportScreenItemType.values())
        {
            MenuItem item = new MenuItem(menu, SWT.PUSH);
            item.setText(String.format("New Screen Item : [%s]", type.toString()));
            item.addSelectionListener(new SelectionAdapter()
            {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), String.format("New Screen Item : [%s]", type.toString()),
                            "Item Name", null, new IInputValidator()
                            {

                                public String isValid(String newText)
                                {
                                    if (newText == null || newText.trim().length() == 0)
                                        return "Item name can't be empty.";
                                    if (container.contains(newText.trim()))
                                        return "Item with this name already exists.";

                                    return null;
                                }
                            });
                    if (dlg.open() == Window.OK)
                    {
                        final EJPluginReportScreenItemProperties itemProperties = container.createItem(type, dlg.getValue(), -1);
                        if (itemProperties != null)
                        {
                            // set default width/height

                            itemProperties.setX(x);
                            itemProperties.setY(y);
                            itemProperties.setWidth(80);
                            itemProperties.setHeight(itemProperties.getType() == EJReportScreenItemType.LINE ? 1 : 22);
                            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                            {

                                public void run()
                                {
                                    editor.setDirty(true);
                                    editor.refresh(container);
                                    editor.select(itemProperties);

                                }
                            });
                        }
                    }
                }
            });
        }
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
    }

    protected void createItemPreview(final AbstractEJReportEditor editor, final Composite reportBody, final EJPluginReportScreenItemProperties properties)
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

        block.setBounds(properties.getX(), properties.getY(), properties.getWidth(), properties.getHeight());

        block.setLayout(null);
        block.moveAbove(null);

        final Label hint = new Label(block, SWT.NONE);
        hint.setText(String.format("%s [ %d, %d ] [ %d, %d ]", properties.getName(), properties.getX(), properties.getY(), properties.getWidth(),
                properties.getHeight()));

        hint.setToolTipText(hint.getText());
        hint.setBounds(10, 0, properties.getWidth() - 10, 25);

        final LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();

        final DragObject dragObjectMove = new DragObject()
        {

            public void setBond(int x, int y)
            {

                Point display = reportBody.toControl(x, y);
                properties.setX(display.x);
                properties.setY(display.y);
                block.setBounds(properties.getX(), properties.getY(), properties.getWidth(), properties.getHeight());

                hint.setText(String.format("%s [ %d, %d ] [ %d, %d ]", properties.getName(), properties.getX(), properties.getY(), properties.getWidth(),
                        properties.getHeight()));
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
        resize.setBounds(properties.getWidth() > 12 ? properties.getWidth() - 12 : 0, properties.getHeight() > 12 ? properties.getHeight() - 12 : 0, 10, 10);

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

                    properties.setWidth((display.x - properties.getX()));
                    properties.setHeight(display.y - properties.getY());
                    block.setBounds(properties.getX(), properties.getY(), properties.getWidth(), properties.getHeight());

                    hint.setText(String.format("%s [ %d, %d ] [ %d, %d ]", properties.getName(), properties.getX(), properties.getY(), properties.getWidth(),
                            properties.getHeight()));
                    resize.setBounds(properties.getWidth() > 12 ? properties.getWidth() - 12 : 0,
                            properties.getHeight() > 12 ? properties.getHeight() - 12 : 0, 10, 10);
                    hint.setBounds(10, 0, properties.getWidth() - 10, 25);
                    editor.setDirty(true);
                    editor.refresh(properties);
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
                    if(line.getRadius()==0)
                        e.gc.drawRectangle(x, y, block.getBounds().width - (lineWidth + x), block.getBounds().height - (lineWidth + y));
                    else
                        e.gc.drawRoundRectangle(x, y, block.getBounds().width - (lineWidth + x), block.getBounds().height - (lineWidth + y), line.getRadius(), line.getRadius());

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

                        screenProperties.setWidth((display.x - screenProperties.getX()));
                        screenProperties.setHeight(display.y - screenProperties.getY());
                        block.setBounds(screenProperties.getX(), screenProperties.getY(), screenProperties.getWidth(), screenProperties.getHeight());

                        hint.setText(String.format("%s [ %d, %d ] [ %d, %d ]", properties.getName(), screenProperties.getX(), screenProperties.getY(),
                                screenProperties.getWidth(), screenProperties.getHeight()));
                        resize.setBounds(screenProperties.getWidth() - 12, screenProperties.getHeight() - 12, 10, 10);
                        hint.setBounds(10, 0, screenProperties.getWidth() - 10, 25);
                        editor.setDirty(true);
                        editor.refresh(properties);
                    }

                }
            };
            final DragObject dragObjectMove = new DragObject()
            {

                public void setBond(int x, int y)
                {

                    Point display = reportBody.toControl(x, y);
                    screenProperties.setX(display.x);
                    screenProperties.setY(display.y);
                    block.setBounds(screenProperties.getX(), screenProperties.getY(), screenProperties.getWidth(), screenProperties.getHeight());

                    hint.setText(String.format("%s [ %d, %d ] [ %d, %d ]", properties.getName(), screenProperties.getX(), screenProperties.getY(),
                            screenProperties.getWidth(), screenProperties.getHeight()));
                    editor.setDirty(true);
                    editor.refresh(properties);
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
    }
}
