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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.EditorLayoutFactory;
import org.entirej.ide.ui.editors.descriptors.IGroupProvider.IRefreshHandler;

public abstract class AbstractDescriptorPart extends SectionPart
{
    protected Composite   body;

    protected FormToolkit toolkit;

    private boolean       activeForcus = false;

    private final boolean enableScroll;

    public AbstractDescriptorPart(FormToolkit toolkit, Composite parent, boolean enableScroll)
    {
        this(toolkit, parent, ExpandableComposite.TITLE_BAR, enableScroll);
    }

    public AbstractDescriptorPart(FormToolkit toolkit, Composite parent, int style, boolean enableScroll)
    {
        super(parent, toolkit, style);
        this.toolkit = toolkit;

        buildBody(getSection(), toolkit);
        createSectionToolbar(getSection(), toolkit, getToolbarActions());
        parent.setBackground(getSection().getBackground());
        this.enableScroll = enableScroll;
    }

    public boolean isActiveForcus()
    {
        return activeForcus;
    }

    protected void buildBody(Section section, FormToolkit toolkit)
    {
        section.setLayout(EditorLayoutFactory.createClearTableWrapLayout(false, 1));
        GridData sectionData = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
        section.setLayoutData(sectionData);

        final FormText decFormText = toolkit.createFormText(section, true);
        decFormText.setWhitespaceNormalized(true);

        decFormText.addHyperlinkListener(new HyperlinkAdapter()
        {

            @Override
            public void linkActivated(HyperlinkEvent e)
            {
                try
                {
                    PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL((String) e.getHref()));
                }
                catch (PartInitException e1)
                {
                    EJCoreLog.logException(e1);
                }
                catch (MalformedURLException e2)
                {
                    EJCoreLog.logException(e2);
                }
            }

        });
        section.setDescriptionControl(decFormText);

    }

    public abstract AbstractDescriptor<?>[] getDescriptors();

    public abstract String getSectionTitle();

    public abstract String getSectionDescription();

    public Action[] getToolbarActions()
    {
        return new Action[0];
    }

    public void buildUI()
    {

        FormToolkit toolkit = this.toolkit;
        final Section section = getSection();

        section.setRedraw(false);
        section.setText(getSectionTitle());
        if (body != null)
        {
            body.dispose();
        }

        AbstractDescriptor<?>[] descriptors = getDescriptors();

        String sectionDescription = getSectionDescription();

        if (sectionDescription == null || sectionDescription.trim().length() == 0)
        {
            sectionDescription = "";
        }
        if (section.getDescriptionControl() instanceof FormText)
        {
            ((FormText) section.getDescriptionControl()).setText(String.format("<form><p>%s</p></form>", sectionDescription), true, true);
        }

        body = toolkit.createComposite(section);
        section.setClient(body);
        body.setLayout(new GridLayout());
        body.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        section.setTabList(new Control[] { body });
        body.forceFocus();
        if (descriptors != null && descriptors.length > 0)
        {
            Composite pContent;

            if (enableScroll)
            {
                GridData layoutData = new GridData(GridData.FILL_BOTH);

                layoutData.widthHint = 100;
                layoutData.heightHint = 100;
                ScrolledForm previewComposite = toolkit.createScrolledForm(body);
                previewComposite.setLayoutData(layoutData);
                pContent = previewComposite.getBody();
                pContent.setBackground(body.getBackground());
                pContent.setLayout(new GridLayout());
                previewComposite.setContent(pContent);
                previewComposite.setBackground(body.getBackground());
                // previewComposite.setExpandHorizontal(true);
                // previewComposite.setExpandVertical(true);
            }
            else
            {
                GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);

                pContent = new Composite(body, SWT.NONE);
                pContent.setLayout(new GridLayout());
                pContent.setLayoutData(layoutData);
            }

            Composite descripterBody = new Composite(pContent, SWT.NONE);
            descripterBody.setBackground(body.getBackground());
            GridLayout gridLayout = EditorLayoutFactory.createSectionClientGridLayout(false, 2);
            gridLayout.marginRight = gridLayout.marginRight + 4;
            descripterBody.setLayout(gridLayout);
            descripterBody.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            for (AbstractDescriptor<?> descriptor : descriptors)
            {
                AbstractEditorSection<?> editorSection = createEditorSection(pContent, descriptor);
                if (editorSection != null)

                    editorSection.createContents(descripterBody, toolkit);
            }

            setTabComponents(descripterBody);

        }
        else
        {
            Composite descripterBody = new Composite(body, SWT.NONE);
            descripterBody.setBackground(body.getBackground());
            descripterBody.setLayout(EditorLayoutFactory.createSectionClientGridLayout(false, 2));
            descripterBody.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            toolkit.createLabel(descripterBody, "properties not available.", SWT.NULL);
        }

        toolkit.paintBordersFor(body);

        section.layout();
        section.setRedraw(true);

    }

    @SuppressWarnings("unchecked")
    public AbstractEditorSection<?> createEditorSection(Composite body, AbstractDescriptor<?> descriptor)
    {
        AbstractEditorSection<?> editorSection = null;
        switch (descriptor.getType())
        {
            case TEXT:
            case DESCRIPTION:
            case REFERENCE:
                final AbstractDescriptor<String> textDes = (AbstractDescriptor<String>) descriptor;

                switch (descriptor.getType())
                {
                    case TEXT:
                        editorSection = new TextEditorSection(textDes);
                        break;
                    case DESCRIPTION:
                        editorSection = new DescriptionEditorSection(textDes);
                        break;
                    case REFERENCE:
                        editorSection = new TypeEditorSection(textDes);
                        break;

                }

                break;

            case SELECTION:
                final AbstractDescriptor<Object> objDes = (AbstractDescriptor<Object>) descriptor;

                editorSection = new SelectionEditorSection(objDes);
                break;
            case BOOLEAN:
                final AbstractDescriptor<Boolean> booleanDes = (AbstractDescriptor<Boolean>) descriptor;

                editorSection = new BooleanEditorSection(booleanDes);
                break;
            case GROUP:
                if (descriptor instanceof IGroupProvider)
                {
                    IGroupProvider groupProvider = (IGroupProvider) descriptor;
                    createAdvancedSection(body, groupProvider, descriptor.getText(), descriptor.getTooltip(), toolkit, groupProvider.isExpand());

                }
                break;
            case CUSTOM:
                if (descriptor instanceof ICustomUIProvider)
                {
                    ICustomUIProvider customUIProvider = (ICustomUIProvider) descriptor;
                    editorSection = new CustomEditorSection((AbstractDescriptor<Object>) descriptor, customUIProvider);
                }
                break;

        }
        return editorSection;
    }

    private void setTabComponents(Composite com)
    {
        if (com == null)
            return;
        Control[] baseCtrls = com.getChildren();
        List<Control> foucsCtrls = new ArrayList<Control>();
        for (Control ctrl : baseCtrls)
        {
            if (!(ctrl instanceof Label))
            {
                foucsCtrls.add(ctrl);
            }
        }
        com.setTabList(foucsCtrls.toArray(new Control[0]));
    }

    private static void createSectionToolbar(Section section, FormToolkit toolkit, Action[] toolbarActions)
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

        for (Action action : toolbarActions)
        {
            if (action != null)
                toolBarManager.add(action);
            else
                toolBarManager.add(new Separator());
        }

        toolBarManager.update(true);

        section.setTextClient(toolbar);
    }

    protected Composite createAdvancedSection(final Composite body, final IGroupProvider groupProvider, String text, String tooltip, final FormToolkit toolkit,
            boolean expand)
    {
        final Composite advnComp;
        int style = ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE;
        // if (tooltip != null)
        // {
        // style = style | Section.DESCRIPTION;
        // }
        if (expand)
            style = style | ExpandableComposite.EXPANDED;

        final Section advnSection = toolkit.createSection(body, style);
        advnSection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        advnSection.setText(text);
        if (tooltip != null && tooltip.trim().length() > 0)
        {
            FormText decFormText = toolkit.createFormText(advnSection, true);
            decFormText.setWhitespaceNormalized(true);
            decFormText.setText(String.format("<form><p>%s</p></form>", tooltip), true, true);
            decFormText.addHyperlinkListener(new HyperlinkAdapter()
            {

                @Override
                public void linkActivated(HyperlinkEvent e)
                {
                    try
                    {
                        PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL((String) e.getHref()));
                    }
                    catch (PartInitException e1)
                    {
                        EJCoreLog.logException(e1);
                    }
                    catch (MalformedURLException e2)
                    {
                        EJCoreLog.logException(e2);
                    }
                }

            });
            advnSection.setDescriptionControl(decFormText);
        }

        advnComp = toolkit.createComposite(advnSection);
        advnComp.setLayout(new GridLayout());
        advnComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        advnSection.setClient(advnComp);
        IRefreshHandler handler = new IRefreshHandler()
        {
            Composite descripterBody;

            public void refresh()
            {

                if (descripterBody != null)
                {
                    descripterBody.dispose();

                }
                descripterBody = new Composite(advnComp, SWT.NONE);
                descripterBody.setLayout(EditorLayoutFactory.createSectionClientGridLayout(false, 2));
                descripterBody.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                for (AbstractDescriptor<?> sub : groupProvider.getDescriptors())
                {
                    AbstractEditorSection<?> editorSection = createEditorSection(advnComp, sub);
                    if (editorSection != null)

                        editorSection.createContents(descripterBody, toolkit);
                }
                if (advnSection.isExpanded())
                    EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                    {

                        public void run()
                        {
                            if (!advnComp.isDisposed())
                                advnSection.setExpanded(true);
                        }
                    });
            }
        };
        groupProvider.createHeader(handler, advnComp, new GridData(GridData.FILL_HORIZONTAL));

        handler.refresh();
        createSectionToolbar(advnSection, toolkit, groupProvider.getToolbarActions());
        return advnComp;
    }

    @Override
    public void refresh()
    {
        super.refresh();
    }

    @Override
    public void dispose()
    {
        super.dispose();
    }

    public abstract class AbstractEditorSection<T>
    {
        protected final AbstractDescriptor<T> descriptor;

        public AbstractEditorSection(AbstractDescriptor<T> descriptor)
        {
            this.descriptor = descriptor;
        }

        public void addIssueDecoration(Control control)
        {
            String dec = FieldDecorationRegistry.DEC_ERROR;
            String msg = descriptor.getErrors();
            if (msg == null || msg.trim().length() == 0)
            {
                msg = descriptor.getWarnings();
                dec = FieldDecorationRegistry.DEC_WARNING;
            }

            if (msg == null || msg.trim().length() == 0)
                return;

            ControlDecoration decoration = new ControlDecoration(control, SWT.TOP | SWT.RIGHT);
            decoration.setMarginWidth(2);
            FieldDecorationRegistry registry = FieldDecorationRegistry.getDefault();
            decoration.setImage(registry.getFieldDecoration(dec).getImage());
            decoration.setShowHover(true);
            decoration.setDescriptionText(msg);
        }

        protected Control createLabel(Composite parent, FormToolkit toolkit)
        {
            String text = descriptor.getText();
            if (descriptor.isRequired())
            {
                if (descriptor.isOverride())
                {
                    text += " [overridden] "; //$NON-NLS-1$
                }
                text += " :* "; //$NON-NLS-1$
            }
            
            else
            {
                if (descriptor.isOverride())
                {
                    text += " [overridden] "; //$NON-NLS-1$
                }
                text += " : "; //$NON-NLS-1$
            }
            if (descriptor.hasLableLink())
            {
                Hyperlink hyperlink = toolkit.createHyperlink(parent, text, SWT.NULL);
                addInfoHoverContorl(hyperlink);
                hyperlink.addHyperlinkListener(new HyperlinkAdapter()
                {

                    public void linkActivated(HyperlinkEvent e)
                    {
                        T oldVal = descriptor.getValue();
                        T newVal = descriptor.lableLinkActivator();
                        if ((oldVal != null && !oldVal.equals(newVal)) || (newVal != null && !newVal.equals(oldVal)))
                        {
                            refresh(newVal);
                        }
                    }
                });

                return hyperlink;
            }
            else
            {
                Label label = toolkit.createLabel(parent, text, SWT.NONE);
                addInfoHoverContorl(label);

                return label;
            }
        }

        protected void refresh(T newVal)
        {
            // empty

        }

        protected void addInfoHoverContorl(final Control control)
        {
            final String text = descriptor.getTooltip();
            if (text == null || text.trim().length() == 0)
                return;

            control.addMouseTrackListener(new MouseTrackListener()
            {
                AbstractInformationControl infoControl;

                public void mouseEnter(MouseEvent e)
                {
                    // ignore
                }

                void init()
                {
                    infoControl = new AbstractInformationControl(getSection().getShell(), "")
                    {
                        FormText decFormText = null;

                        public boolean hasContents()
                        {

                            return true;
                        }

                        @Override
                        public void setInformation(String information)
                        {
                            if (decFormText == null)
                            {
                                create();
                            }
                            if (decFormText != null && !decFormText.isDisposed())
                            {
                                decFormText.setText(String.format("<form><p>%s</p></form>", information), true, true);
                            }
                        }

                        /*
                         * @see IInformationControl#computeSizeHint()
                         */
                        public Point computeSizeHint()
                        {
                            // see
                            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=117602
                            int widthHint = SWT.DEFAULT;
                            Point constraints = getSizeConstraints();
                            if (constraints != null)
                                widthHint = constraints.x;

                            return getShell().computeSize(widthHint, SWT.DEFAULT, true);
                        }

                        @Override
                        protected void createContent(Composite parent)
                        {
                            decFormText = new FormText(parent, SWT.NONE);
                            decFormText.setWhitespaceNormalized(true);
                            decFormText.setForeground(parent.getForeground());
                            decFormText.setBackground(parent.getBackground());
                            decFormText.setFont(JFaceResources.getDialogFont());
                            FillLayout layout = (FillLayout) parent.getLayout();
                            layout.marginHeight = 1;
                            layout.marginWidth = 1;

                            decFormText.addHyperlinkListener(new HyperlinkAdapter()
                            {

                                @Override
                                public void linkActivated(HyperlinkEvent e)
                                {
                                    try
                                    {
                                        PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL((String) e.getHref()));
                                    }
                                    catch (PartInitException e1)
                                    {
                                        EJCoreLog.logException(e1);
                                    }
                                    catch (MalformedURLException e2)
                                    {
                                        EJCoreLog.logException(e2);
                                    }
                                }

                            });

                        }

                    };
                }

                public void mouseExit(MouseEvent e)
                {
                    if (infoControl != null && infoControl.isVisible())
                    {
                        Display.getDefault().timerExec(1500, new Runnable()
                        {

                            public void run()
                            {

                                infoControl.setVisible(false);
                            }
                        });
                    }

                }

                public void mouseHover(MouseEvent e)
                {

                    String text = descriptor.getTooltip();
                    if (text == null || text.trim().length() == 0)
                        return;
                    if (infoControl == null)
                    {
                        init();
                    }
                    infoControl.setSizeConstraints(400, 600);
                    infoControl.setInformation(text);
                    infoControl.setStatusText(descriptor.isRequired() ? "*Required" : "");
                    Point p = infoControl.computeSizeHint();
                    infoControl.setSize(p.x, p.y);
                    infoControl.setLocation(control.toDisplay(new Point(10, 25)));
                    infoControl.setVisible(true);
                }
            });
        }

        public abstract void createContents(Composite parent, FormToolkit toolkit);

    }

    class TextEditorSection extends AbstractEditorSection<String>
    {
        private Text text;

        public TextEditorSection(AbstractDescriptor<String> descriptor)
        {
            super(descriptor);
        }

        @Override
        public void createContents(Composite parent, FormToolkit toolkit)
        {
            createLabel(parent, toolkit);
            text = toolkit.createText(parent, descriptor.getValue(), SWT.SINGLE);

            text.addFocusListener(new FocusListener()
            {

                public void focusLost(FocusEvent e)
                {
                    activeForcus = false;
                    refreash();
                }

                public void focusGained(FocusEvent e)
                {
                    activeForcus = true;
                    refreash();
                }

                private void refreash()
                {
                    // todo
                }
            });
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.widthHint = 20;
            gd.horizontalSpan = 1;
            gd.horizontalIndent = 3;
            text.setLayoutData(gd);

            text.addModifyListener(new ModifyListener()
            {
                boolean enable = true;
                public void modifyText(ModifyEvent e)
                {
                    if(enable)
                    descriptor.runOperation(descriptor.createOperation(text.getText(),new IRefreshHandler()
                    {
                        
                        public void refresh()
                        {
                                try
                                {
                                    enable = false;
                                    if(!text.isDisposed())
                                        text.setText(descriptor.getValue());
                                    
                                }finally
                                {
                                    enable = true;
                                }
                            
                        }
                    }));
                    
                }
            });
            descriptor.addEditorAssist(text);
            addIssueDecoration(text);
        }
    }

    class TypeEditorSection extends AbstractEditorSection<String>
    {
        private Text               text;
        private TextContentAdapter contentAdapter = new TextContentAdapter();

        public TypeEditorSection(AbstractDescriptor<String> descriptor)
        {
            super(descriptor);
        }

        protected void refresh(String newVal)
        {
            if (text != null)
            {
                text.setText(newVal == null ? "" : newVal);
                showRightEnd();
            }
        }

        @Override
        public void createContents(Composite parent, FormToolkit toolkit)
        {
            createLabel(parent, toolkit);
            Composite composite = toolkit.createComposite(parent);
            GridLayout layout = EditorLayoutFactory.createSectionClientGridLayout(false, 2);
            layout.marginTop = 0;
            layout.marginBottom = 0;
            composite.setLayout(layout);
            composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            text = toolkit.createText(composite, descriptor.getValue(), SWT.SINGLE);

            text.addFocusListener(new FocusListener()
            {

                public void focusLost(FocusEvent e)
                {
                    activeForcus = false;
                    refreash();
                }

                public void focusGained(FocusEvent e)
                {
                    activeForcus = true;
                    refreash();
                }

                private void refreash()
                {
                    // todo
                }
            });
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.widthHint = 20;
            gd.horizontalSpan = 1;
            gd.horizontalIndent = 3;
            text.setLayoutData(gd);
            text.addModifyListener(new ModifyListener()
            {
                boolean enable = true;
                public void modifyText(ModifyEvent e)
                {
                    if(enable)
                    descriptor.runOperation(descriptor.createOperation(text.getText(),new IRefreshHandler()
                    {
                        
                        public void refresh()
                        {
                                try
                                {
                                    enable = false;
                                    if(!text.isDisposed())
                                        text.setText(descriptor.getValue());
                                    
                                }finally
                                {
                                    enable = true;
                                }
                            
                        }
                    }));
                    
                }
            });
            descriptor.addEditorAssist(text);

            final Button browse = toolkit.createButton(composite, "Browse", SWT.PUSH);

            browse.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent e)
                {
                    String oldVal = descriptor.getValue();
                    String newVal = descriptor.browseType();
                    if ((oldVal != null && !oldVal.equals(newVal)) || (newVal != null && !newVal.equals(oldVal)))
                    {
                        refresh(newVal);
                    }
                    text.setFocus();
                }

            });
            /*
             * text.addControlListener(new ControlListener() {
             * 
             * public void controlResized(ControlEvent e) { showRightEnd();
             * 
             * }
             * 
             * public void controlMoved(ControlEvent e) { showRightEnd();
             * 
             * } });
             */
            addIssueDecoration(browse);
        }

        private void showRightEnd()
        {
            contentAdapter.setCursorPosition(text, text.getText().length());
        }

    }

    class BooleanEditorSection extends AbstractEditorSection<Boolean>
    {
        private Button button;

        public BooleanEditorSection(AbstractDescriptor<Boolean> descriptor)
        {
            super(descriptor);
        }

        @Override
        public void createContents(Composite parent, FormToolkit toolkit)
        {
            createLabel(parent, toolkit);
            button = toolkit.createButton(parent, null, SWT.CHECK);
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.widthHint = 20;
            gd.horizontalSpan = 1;
            gd.horizontalIndent = 3;
            button.setLayoutData(gd);
            button.setSelection(descriptor.getValue());
            button.addSelectionListener(new SelectionListener()
            {

                public void widgetSelected(SelectionEvent e)
                {
                    descriptor.runOperation(descriptor.createOperation(button.getSelection(), new IRefreshHandler()
                    {
                        
                        public void refresh()
                        {
                            if(!button.isDisposed())
                                button.setSelection(descriptor.getValue());
                            
                        }
                    }));

                }

                public void widgetDefaultSelected(SelectionEvent e)
                {
                    descriptor.runOperation(descriptor.createOperation(button.getSelection(), new IRefreshHandler()
                    {
                        
                        public void refresh()
                        {
                            if(!button.isDisposed())
                                button.setSelection(descriptor.getValue());
                            
                        }
                    }));

                }
            });
            addIssueDecoration(button);
        }
    }

    class CustomEditorSection extends AbstractEditorSection<Object>
    {
        private final ICustomUIProvider customUIProvider;

        public CustomEditorSection(AbstractDescriptor<Object> descriptor, ICustomUIProvider customUIProvider)
        {
            super(descriptor);
            this.customUIProvider = customUIProvider;
        }

        @Override
        public void createContents(Composite parent, FormToolkit toolkit)
        {
            if (customUIProvider.isUseLabel())
                createLabel(parent, toolkit);
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.widthHint = 20;
            gd.horizontalSpan = 1;
            gd.horizontalIndent = 3;
            Control createBody = customUIProvider.createBody(parent, gd);

            createBody.setLayoutData(gd);

            addIssueDecoration(createBody);
        }
    }

    class DescriptionEditorSection extends AbstractEditorSection<String>
    {
        private Text text;

        public DescriptionEditorSection(AbstractDescriptor<String> descriptor)
        {
            super(descriptor);
        }

        @Override
        public void createContents(Composite parent, FormToolkit toolkit)
        {
            GridData sectionData = new GridData(GridData.FILL_HORIZONTAL);
            sectionData.horizontalSpan = 2;
            parent.setLayoutData(sectionData);
            Control label = createLabel(parent, toolkit);
            GridData lgd = new GridData();
            lgd.verticalAlignment = SWT.BEGINNING;
            label.setLayoutData(lgd);
            text = toolkit.createText(parent, descriptor.getValue(), SWT.MULTI);

            text.addFocusListener(new FocusListener()
            {

                public void focusLost(FocusEvent e)
                {
                    activeForcus = false;
                    refreash();
                }

                public void focusGained(FocusEvent e)
                {
                    activeForcus = true;
                    refreash();
                }

                private void refreash()
                {
                    // todo
                }
            });
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.widthHint = 20;
            gd.heightHint = 100;
            gd.horizontalSpan = 1;
            gd.horizontalIndent = 3;
            text.setLayoutData(gd);
            text.addModifyListener(new ModifyListener()
            {
                boolean enable = true;
                public void modifyText(ModifyEvent e)
                {
                    if(enable)
                    descriptor.runOperation(descriptor.createOperation(text.getText(),new IRefreshHandler()
                    {
                        
                        public void refresh()
                        {
                                try
                                {
                                    enable = false;
                                    if(!text.isDisposed())
                                        text.setText(descriptor.getValue());
                                    
                                }finally
                                {
                                    enable = true;
                                }
                            
                        }
                    }));
                    
                }
            });
            addIssueDecoration(text);
        }
    }

    class SelectionEditorSection extends AbstractEditorSection<Object>
    {
        private Combo combo;

        public SelectionEditorSection(AbstractDescriptor<Object> descriptor)
        {
            super(descriptor);
        }

        @Override
        public void createContents(Composite parent, FormToolkit toolkit)
        {
            createLabel(parent, toolkit);
            combo = new Combo(parent, SWT.READ_ONLY | SWT.BORDER);
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.widthHint = 20;
            gd.horizontalSpan = 1;
            gd.horizontalIndent = 3;
            combo.setLayoutData(gd);
            if (descriptor instanceof ISelectionValueProvider)
            {
                @SuppressWarnings("unchecked")
                final ISelectionValueProvider<Object> provider = (ISelectionValueProvider<Object>) descriptor;
                for (Object item : provider.getOptions())
                {
                    String key = provider.getOptionText(item);
                    combo.add(key);
                    combo.setData(key, item);
                }
                if (descriptor.getValue() != null)
                    combo.setText(provider.getOptionText(descriptor.getValue()));

                combo.addSelectionListener(new SelectionAdapter()
                {
                   

                    
                    
                    boolean enable = true;
                    public void widgetSelected(SelectionEvent event)
                    {
                        String key = combo.getText();
                        if(enable)
                            descriptor.runOperation(descriptor.createOperation(key != null ? combo.getData(key) : null,new IRefreshHandler()
                        {
                            
                            public void refresh()
                            {
                                    try
                                    {
                                        enable = false;
                                        if(!combo.isDisposed())
                                            combo.setText(provider.getOptionText(descriptor.getValue()));
                                        
                                    }finally
                                    {
                                        enable = true;
                                    }
                                
                            }
                        }));
                        
                    }
                });
            }
            addIssueDecoration(combo);
        }
    }
}
