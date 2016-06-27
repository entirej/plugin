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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer.BlockGroup;
import org.entirej.framework.report.enumerations.EJReportScreenType;
import org.entirej.ide.ui.EJUIImages;

public class ReportPreviewImpl implements IReportPreviewProvider
{
    protected final Color  COLOR_BLOCK        = new Color(Display.getCurrent(), new RGB(255, 251, 227));
    protected final Color  COLOR_LIGHT_YELLOW = Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
    protected final Color  COLOR_WHITE        = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
    protected final Color  COLOR_LIGHT_SHADOW = Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);

    protected final Cursor RESIZE             = new Cursor(Display.getCurrent(), SWT.CURSOR_SIZESE);
    protected final Cursor MOVE               = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);

    protected final Color  COLOR_HEADER       = new Color(Display.getCurrent(), new RGB(180, 180, 180));
    protected final Color  COLOR_FOOTER       = new Color(Display.getCurrent(), new RGB(218, 218, 218));

    protected BlockGroup   page;

    public void dispose()
    {
        COLOR_BLOCK.dispose();
        RESIZE.dispose();
        COLOR_HEADER.dispose();
        COLOR_FOOTER.dispose();
        MOVE.dispose();
    }

    public ReportPreviewImpl()
    {
        this(null);
    }

    public ReportPreviewImpl(BlockGroup page)
    {
        this.page = page;
    }

    protected EJPluginReportProperties getReportProperties(AbstractEJReportEditor editor)
    {
        return editor.getReportProperties();
    }

    public void buildPreview(final AbstractEJReportEditor editor, ScrolledComposite previewComposite)
    {
        // layout canvas preview
        final Composite pContent = new Composite(previewComposite, SWT.NONE);

        final EJPluginReportProperties formProperties = getReportProperties(editor);
        int width = formProperties.getReportWidth();
        int height = formProperties.getReportHeight();
        previewComposite.setContent(pContent);
        setPreviewBackground(previewComposite, COLOR_LIGHT_YELLOW);
        previewComposite.setExpandHorizontal(true);
        previewComposite.setExpandVertical(true);

        pContent.setLayout(null);
        setPreviewBackground(pContent, COLOR_LIGHT_YELLOW);

        pContent.addPaintListener(new PaintListener()
        {

            public void paintControl(PaintEvent e)
            {
                if (formProperties.getHeaderSectionHeight() > 0)
                {
                    int y1 = formProperties.getHeaderSectionHeight() + 10 + formProperties.getMarginTop() + 1;
                    e.gc.drawLine(0, y1, pContent.getBounds().width, y1);
                    e.gc.drawString("H", 5, y1 > 20 ? y1 - 20 : 2, true);
                }
                if (formProperties.getFooterSectionHeight() > 0)
                {

                    int y1 = pContent.getBounds().height - (formProperties.getFooterSectionHeight() + 10 + formProperties.getMarginBottom());
                    e.gc.drawLine(0, y1, pContent.getBounds().width, y1);

                    e.gc.drawString("F", 5, y1 > 20 ? y1 - 20 : y1, true);

                }

            }
        });
        Composite report = new Composite(pContent, SWT.BORDER);
        setPreviewBackground(report, COLOR_LIGHT_SHADOW);

        report.setBounds(25, 10, width, height);
        report.setLayout(null);

        EJReportBlockContainer blockContainer = formProperties.getBlockContainer();

        if (page == null)
        {
            headerSection(editor, previewComposite, formProperties, width, height, report, blockContainer);
            detailSection(editor, previewComposite, formProperties, width, height, report, blockContainer);
            footerSection(editor, previewComposite, formProperties, width, height, report, blockContainer);
        }
        else
        {
            final Composite reportBody = new Composite(report, SWT.NONE);
            reportBody.setLayout(null);
            setPreviewBackground(reportBody, COLOR_WHITE);

            reportBody.setBounds(formProperties.getMarginLeft(), formProperties.getMarginTop() + formProperties.getHeaderSectionHeight(),
                    (width - (formProperties.getMarginRight() + formProperties.getMarginLeft())), (height - (formProperties.getMarginBottom()
                            + formProperties.getMarginTop() + formProperties.getHeaderSectionHeight() + formProperties.getFooterSectionHeight())));

            buildPage(page, editor, previewComposite, width, height, blockContainer, reportBody);
        }

    }

    private void headerSection(final AbstractEJReportEditor editor, ScrolledComposite previewComposite, final EJPluginReportProperties formProperties,
            int width, int height, Composite report, EJReportBlockContainer blockContainer)
    {
        final Composite reportBody = new Composite(report, SWT.NONE);
        reportBody.setLayout(null);
        setPreviewBackground(reportBody, COLOR_HEADER);

        reportBody.setBounds(formProperties.getMarginLeft(), formProperties.getMarginTop(),
                (width - (formProperties.getMarginRight() + formProperties.getMarginLeft())), formProperties.getHeaderSectionHeight());

        for (EJPluginReportBlockProperties properties : blockContainer.getHeaderSection().getAllBlockProperties())
        {
            createBlockPreview(editor, reportBody, properties);
        }

        final LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();

        final DropTargetAdapter dragAdapter = new DropTargetAdapter()
        {
            int x, y;

            @Override
            public void dragOver(DropTargetEvent event)
            {

                final DragObject droppedObj = transfer.getSelection() != null ? ((DragObject) ((StructuredSelection) transfer.getSelection()).getFirstElement())
                        : null;

                if (droppedObj != null)
                {
                    droppedObj.indicate(x = event.x, y = event.y);
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
    }

    private void footerSection(final AbstractEJReportEditor editor, ScrolledComposite previewComposite, final EJPluginReportProperties formProperties,
            int width, int height, Composite report, EJReportBlockContainer blockContainer)
    {
        final Composite reportBody = new Composite(report, SWT.NONE);
        reportBody.setLayout(null);
        setPreviewBackground(reportBody, COLOR_FOOTER);

        reportBody.setBounds(formProperties.getMarginLeft(), height - (formProperties.getMarginBottom() + formProperties.getFooterSectionHeight()),
                (width - (formProperties.getMarginRight() + formProperties.getMarginLeft())), formProperties.getFooterSectionHeight());

        for (EJPluginReportBlockProperties properties : blockContainer.getFooterSection().getAllBlockProperties())
        {
            createBlockPreview(editor, reportBody, properties);
        }

        final LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();

        final DropTargetAdapter dragAdapter = new DropTargetAdapter()
        {

            int x, y;

            @Override
            public void dragOver(DropTargetEvent event)
            {

                final DragObject droppedObj = transfer.getSelection() != null ? ((DragObject) ((StructuredSelection) transfer.getSelection()).getFirstElement())
                        : null;

                if (droppedObj != null)
                {
                    droppedObj.indicate(x = event.x, y = event.y);
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
    }

    private void detailSection(final AbstractEJReportEditor editor, ScrolledComposite previewComposite, final EJPluginReportProperties formProperties,
            int width, int height, Composite report, EJReportBlockContainer blockContainer)
    {
        final Composite reportBody = new Composite(report, SWT.NONE);
        reportBody.setLayout(null);
        setPreviewBackground(reportBody, COLOR_WHITE);

        reportBody.setBounds(formProperties.getMarginLeft(), formProperties.getMarginTop() + formProperties.getHeaderSectionHeight(),
                (width - (formProperties.getMarginRight() + formProperties.getMarginLeft())), (height - (formProperties.getMarginBottom()
                        + formProperties.getMarginTop() + formProperties.getHeaderSectionHeight() + formProperties.getFooterSectionHeight())));

        BlockGroup firstPage = blockContainer.getFirstPage();
        buildPage(firstPage, editor, previewComposite, width, height, blockContainer, reportBody);
    }

    private void buildPage(BlockGroup page, final AbstractEJReportEditor editor, ScrolledComposite previewComposite, int width, int height,
            EJReportBlockContainer blockContainer, final Composite reportBody)
    {

        for (EJPluginReportBlockProperties properties : page.getAllBlockProperties())
        {
            createBlockPreview(editor, reportBody, properties);
        }

        previewComposite.setMinSize(width + 20, height + 20);// add offset

        final LocalSelectionTransfer transfer = LocalSelectionTransfer.getTransfer();

        final DropTargetAdapter dragAdapter = new DropTargetAdapter()
        {

            int x, y;

            @Override
            public void dragOver(DropTargetEvent event)
            {

                final DragObject droppedObj = transfer.getSelection() != null ? ((DragObject) ((StructuredSelection) transfer.getSelection()).getFirstElement())
                        : null;

                if (droppedObj != null)
                {
                    droppedObj.indicate(x = event.x, y = event.y);
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
                                editor.refresh(screenProperties);
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
                                editor.refresh(screenProperties);
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
                                editor.refresh(screenProperties);
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

                public void indicate(int x, int y)
                {
                    Point display = reportBody.toControl(x, y);

                    block.setBounds(display.x, display.y, screenProperties.getWidth(), screenProperties.getHeight());

                    hint.setText(String.format("%s [ %d, %d ] [ %d, %d ]", properties.getName(), display.x, display.y, screenProperties.getWidth(),
                            screenProperties.getHeight()));

                }

                public void setBond(final int x, final int y)
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
                            editor.refresh(screenProperties);
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
                            editor.refresh(screenProperties);
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
                            editor.refresh(screenProperties);
                            return Status.OK_STATUS;
                        }
                    };

                    editor.execute(operation);

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
