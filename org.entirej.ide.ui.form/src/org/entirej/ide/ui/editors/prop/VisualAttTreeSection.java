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
package org.entirej.ide.ui.editors.prop;

import java.awt.GraphicsEnvironment;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormPage;
import org.entirej.framework.core.enumerations.EJFontStyle;
import org.entirej.framework.core.enumerations.EJFontWeight;
import org.entirej.framework.core.properties.EJCoreVisualAttributeContainer;
import org.entirej.framework.core.properties.EJCoreVisualAttributeProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.plugin.utils.EJPluginEntireJNumberVerifier;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.descriptors.AbstractBooleanDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDropDownDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractGroupDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDropDownDescriptor;
import org.entirej.ide.ui.editors.descriptors.IGroupProvider.IRefreshHandler;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeContentProvider;
import org.entirej.ide.ui.nodes.AbstractNodeTreeSection;
import org.entirej.ide.ui.nodes.INodeDeleteProvider;
import org.entirej.ide.ui.nodes.INodeRenameProvider;

public class VisualAttTreeSection extends AbstractNodeTreeSection
{
    private final EJPropertiesEditor editor;

    private boolean                  sorted;
    private static final String      SORTED_STORE = "VisualAttTreeSection_SortAction.isChecked"; //$NON-NLS-1$;
    private ViewerComparator         comparator;

    public VisualAttTreeSection(EJPropertiesEditor editor, FormPage page, Composite parent)
    {
        super(editor, page, parent);
        this.editor = editor;
        initTree();
        setSorted(sorted);
    }

    @Override
    public Object getTreeInput()
    {
        return new Object();
    }

    @Override
    public String getSectionTitle()
    {
        return "Defined Visual Attributes";
    }

    @Override
    public String getSectionDescription()
    {

        return "Define visual attributes for application in the following section.";
    }

    public boolean isSorted()
    {
        return sorted;
    }

    public void setSorted(boolean sorted)
    {
        this.sorted = sorted;
        if (sorted && comparator == null)
        {
            comparator = new ViewerComparator();
        }
        else
        {
            comparator = null;
        }
        if (filteredTree != null)
            filteredTree.getViewer().setComparator(comparator);
        EJUIPlugin.getDefault().getPreferenceStore().setValue(SORTED_STORE, sorted);
    }

    @Override
    public void addToolbarCustomActions(ToolBarManager toolBarManager, ToolBar toolbar)
    {
        toolBarManager.add(new SortAction());
    }

    @Override
    public Action[] getBaseActions()
    {

        return new Action[] { createNewVisualAttributeAction() };
    }

    @Override
    public AbstractNodeContentProvider getContentProvider()
    {
        return new AbstractNodeContentProvider()
        {

            public Object[] getElements(Object inputElement)
            {
                EJPluginEntireJProperties props = editor.getEntireJProperties();
                if (props != null)
                {
                    List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();

                    EJCoreVisualAttributeContainer attributesContainer = props.getVisualAttributesContainer();
                    Collection<EJCoreVisualAttributeProperties> visualAttributes = attributesContainer.getVisualAttributes();
                    for (EJCoreVisualAttributeProperties attributeProperties : visualAttributes)
                    {
                        nodes.add(new VisualAttributeNode(attributeProperties));
                    }

                    return nodes.toArray(new AbstractNode<?>[0]);
                }

                return new Object[0];
            }
        };
    }

    private Action createNewVisualAttributeAction()
    {

        return new Action("New Visual Attribute")
        {

            @Override
            public void runWithEvent(Event event)
            {
                final EJCoreVisualAttributeContainer attributesContainer = editor.getEntireJProperties().getVisualAttributesContainer();
                InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), "New Visual Attribute ", "Name", null, new IInputValidator()
                {

                    public String isValid(String newText)
                    {
                        if (newText == null || newText.trim().length() == 0)
                            return "Name can't be empty.";
                        if (attributesContainer.contains(newText.trim()))
                            return "Visual Attribute with this name already exists.";

                        return null;
                    }
                });
                if (dlg.open() == Window.OK)
                {
                    EJCoreVisualAttributeContainer attributeContainer = editor.getEntireJProperties().getVisualAttributesContainer();
                    EJCoreVisualAttributeProperties attributeProperties = new EJCoreVisualAttributeProperties(dlg.getValue().trim());
                    attributeContainer.addVisualAttribute(attributeProperties);
                    editor.setDirty(true);
                    refresh();
                    selectNodes(true, (attributeProperties));
                }
            }

        };
    }

    private class VisualAttributeNode extends AbstractNode<EJCoreVisualAttributeProperties>
    {
        private final Image ELEMENT = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);

        public VisualAttributeNode(EJCoreVisualAttributeProperties source)
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
            return getName();
        }

        @Override
        public Image getImage()
        {
            return ELEMENT;
        }

        @Override
        public Action[] getActions()
        {
            return new Action[] { createNewVisualAttributeAction() };
        }

        @Override
        public INodeDeleteProvider getDeleteProvider()
        {

            return new INodeDeleteProvider()
            {

                public void delete(boolean cleanup)
                {
                    EJCoreVisualAttributeContainer attributeContainer = editor.getEntireJProperties().getVisualAttributesContainer();

                    attributeContainer.removeVisualAttribute(source);
                    editor.setDirty(true);
                    refresh();

                }
                
                public AbstractOperation deleteOperation(boolean cleanup)
                {
                    // TODO Auto-generated method stub
                    return null;
                }
            };
        }

        @Override
        public INodeRenameProvider getRenameProvider()
        {
            return new INodeRenameProvider()
            {

                public void rename()
                {
                    final EJCoreVisualAttributeContainer attributesContainer = editor.getEntireJProperties().getVisualAttributesContainer();
                    InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), "Rename Visual Attribute ", "Name", source.getName(),
                            new IInputValidator()
                            {

                                public String isValid(String newText)
                                {
                                    if (newText == null || newText.trim().length() == 0)
                                        return "Name can't be empty.";
                                    if (source.getName().equals(newText.trim()))
                                        return "";
                                    if (source.getName().equalsIgnoreCase(newText.trim()))
                                        return null;
                                    if (attributesContainer.contains(newText.trim()))
                                        return "Visual Attribute with this name already exists.";

                                    return null;
                                }
                            });
                    if (dlg.open() == Window.OK)
                    {
                        source.setName(dlg.getValue().trim());
                        EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                        {

                            public void run()
                            {
                                editor.setDirty(true);
                                refresh(VisualAttributeNode.this);

                            }
                        });
                    }

                }
            };
        }

        @Override
        public AbstractDescriptor<?>[] getNodeDescriptors()
        {

            AbstractGroupDescriptor previewDG = new AbstractGroupDescriptor("Visual Attributs")
            {

                private Text previewLabel;

                Color        bg;
                Color        fg;
                Font         font;
                // default
                Color        dbg;
                Color        dfg;
                Font         dfont;

                void disposeResources()
                {
                    if (bg != null && !bg.isDisposed())
                        bg.dispose();
                    if (fg != null && !fg.isDisposed())
                        fg.dispose();
                    if (font != null && !font.isDisposed())
                        font.dispose();
                }

                void refreshVAPreview()
                {
                    disposeResources();
                    if (previewLabel != null && !previewLabel.isDisposed())
                    {
                        bg = getBackground(source);
                        if (bg != null)
                            previewLabel.setBackground(bg);
                        else
                            previewLabel.setBackground(dbg);
                        fg = getForeground(source);
                        if (fg != null)
                            previewLabel.setForeground(fg);
                        else
                            previewLabel.setForeground(dfg);

                        font = getFont(source, dfont);
                        if (font != null)
                            previewLabel.setFont(font);
                        else
                            previewLabel.setFont(dfont);
                    }
                }

                @Override
                public Control createHeader(IRefreshHandler handler, Composite parent, GridData gd)
                {

                    Group preview = new Group(parent, SWT.NONE);
                    preview.addDisposeListener(new DisposeListener()
                    {

                        public void widgetDisposed(DisposeEvent e)
                        {
                            disposeResources();

                        }
                    });
                    preview.setText("  Preview  ");

                    gd.verticalSpan = 2;
                    gd.heightHint = 60;
                    gd.widthHint = 60;
                    gd.horizontalIndent = 0;
                    preview.setLayoutData(gd);

                    preview.setLayout(new FillLayout());
                    previewLabel = new Text(preview, SWT.WRAP | SWT.READ_ONLY | SWT.CENTER);
                    previewLabel.setText("Visual attribute preview text");

                    dbg = previewLabel.getBackground();
                    dfg = previewLabel.getForeground();
                    dfont = previewLabel.getFont();
                    refreshVAPreview();
                    return preview;
                }

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    IRefreshHandler handler = new IRefreshHandler()
                    {

                        public void refresh()
                        {
                            refreshVAPreview();

                        }
                    };
                    AbstractGroupDescriptor fontGroupDescriptor = createFontDescriptorGroup(handler);

                    AbstractGroupDescriptor colorGroupDescriptor = createColorDescriptorGroup(handler);
                    return new AbstractDescriptor<?>[] { fontGroupDescriptor, colorGroupDescriptor };

                }

            };
            return new AbstractDescriptor<?>[] { previewDG };

        }

        private AbstractGroupDescriptor createFontDescriptorGroup(final IRefreshHandler handler)
        {
            final AbstractTextDropDownDescriptor fontNameDescriptor = new AbstractTextDropDownDescriptor("Name")
            {

                public String[] getOptions()
                {
                    List<String> options = new ArrayList<String>();
                    String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
                    options.add(EJCoreVisualAttributeProperties.UNSPECIFIED);// add
                                                                             // empty
                                                                             // option
                    for (String fname : fontNames)
                    {
                        options.add(fname);
                    }

                    return options.toArray(new String[0]);
                }

                public String getOptionText(String t)
                {

                    return t;
                }

                @Override
                public void setValue(String value)
                {
                    source.setFontName(value);
                    editor.setDirty(true);
                    refresh(VisualAttributeNode.this);
                    handler.refresh();
                }

                @Override
                public String getValue()
                {
                    return source.getFontName();
                }
            };

            final AbstractTextDescriptor sizeDescriptor = new AbstractTextDescriptor("Size")
            {

                @Override
                public void setValue(String value)
                {
                    try
                    {
                        source.setFontSize(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setFontSize(-1);
                    }
                    editor.setDirty(true);
                    refresh(VisualAttributeNode.this);
                    handler.refresh();
                }

                @Override
                public String getValue()
                {
                    if (source.getFontSize() == -1)
                        return EJCoreVisualAttributeProperties.UNSPECIFIED;
                    return String.valueOf(source.getFontSize());
                }

                @Override
                public void addEditorAssist(Control control)
                {
                    final Text text = (Text) control;
                    ((Text) control).addVerifyListener(new EJPluginEntireJNumberVerifier()
                    {
                        @Override
                        public void verifyText(VerifyEvent e)
                        {
                            if (e.keyCode == SWT.DEL || e.keyCode == SWT.BS && text.getText().equals(EJCoreVisualAttributeProperties.UNSPECIFIED))
                            {
                                e.doit = false;
                                text.setText("");
                            }
                            super.verifyText(e);
                        }

                    });

                    super.addEditorAssist(control);
                }
            };
            
            final AbstractBooleanDescriptor fontSizeAsPercentage = new AbstractBooleanDescriptor("Size As Percentage","")
            {
                
                @Override
                public void setValue(Boolean value)
                {
                   source.setFontSizeAsPercentage(value);
                   if(value)
                   {
                       source.setFontSize(100);  
                   }
                   else
                   {
                       source.setFontSize(-1);
                   }
                   handler.refresh();
                }
                
                @Override
                public Boolean getValue()
                {
                   
                    return source.isFontSizeAsPercentage();
                }
            };

            final AbstractDropDownDescriptor<EJFontStyle> fontStyle = new AbstractDropDownDescriptor<EJFontStyle>("Style")
            {

                public EJFontStyle[] getOptions()
                {
                    return EJFontStyle.values();
                }

                public String getOptionText(EJFontStyle t)
                {
                    return t.name();
                }

                @Override
                public void setValue(EJFontStyle value)
                {
                    source.setFontStyle(value);
                    editor.setDirty(true);
                    refresh(VisualAttributeNode.this);
                    handler.refresh();
                }

                @Override
                public EJFontStyle getValue()
                {
                    return source.getFontStyle();
                }
            };
            final AbstractDropDownDescriptor<EJFontWeight> fontWeight = new AbstractDropDownDescriptor<EJFontWeight>("Weight")
            {

                public EJFontWeight[] getOptions()
                {
                    return EJFontWeight.values();
                }

                public String getOptionText(EJFontWeight t)
                {
                    return t.name();
                }

                @Override
                public void setValue(EJFontWeight value)
                {
                    source.setFontWeight(value);
                    editor.setDirty(true);
                    refresh(VisualAttributeNode.this);
                    handler.refresh();
                }

                @Override
                public EJFontWeight getValue()
                {
                    return source.getFontWeight();
                }
            };

            AbstractGroupDescriptor fontGroupDescriptor = new AbstractGroupDescriptor("Font")
            {

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    return new AbstractDescriptor<?>[] { fontNameDescriptor, sizeDescriptor,fontSizeAsPercentage, fontStyle, fontWeight };
                }

            };
            return fontGroupDescriptor;
        }

        private AbstractGroupDescriptor createColorDescriptorGroup(final IRefreshHandler handler)
        {

            // todo add preview
            /*
             * final AbstractTextDescriptor preview = new
             * AbstractTextDescriptor("Preview") { private Text previewText =
             * null; private Color currentBG; private Color currentFG;
             *
             * @Override public void setValue(String value) { }
             *
             * @Override public String getValue() { return "Preview text"; }
             *
             * @Override public void addEditorAssist(Control control) {
             * previewText = (Text) control; previewText.setEditable(false); }
             * };
             */
            final AbstractDescriptor<String> fgDescriptor = new AbstractDescriptor<String>(AbstractDescriptor.TYPE.REFERENCE)
            {
                boolean forceSet = false;

                @Override
                public String getValue()
                {
                    if (source.getForegroundRGB() == null)
                        return EJCoreVisualAttributeProperties.UNSPECIFIED;
                    return source.getForegroundRGB();
                }

                @Override
                public void setValue(String value)
                {

                    source.setForegroundRGB(EJCoreVisualAttributeProperties.UNSPECIFIED.equals(value) ? null : value);
                    editor.setDirty(true);
                    refresh(VisualAttributeNode.this);
                    handler.refresh();
                }

                @Override
                public void addEditorAssist(Control control)
                {
                    final Text text = (Text) control;
                    ((Text) control).addVerifyListener(new EJPluginEntireJNumberVerifier()
                    {
                        @Override
                        public void verifyText(VerifyEvent e)
                        {

                            e.doit = EJCoreVisualAttributeProperties.UNSPECIFIED.equals(e.text);
                            if (e.keyCode == SWT.DEL || e.keyCode == SWT.BS)
                            {
                                e.doit = false;
                                text.setText(EJCoreVisualAttributeProperties.UNSPECIFIED);
                                return;
                            }
                            if (!forceSet && e.keyCode == 0 && !text.getText().equals(e.text)
                                    && (e.text.startsWith("r") && e.text.contains("g") && e.text.contains("b")))
                            {
                                forceSet = true;
                                text.setText(e.text);
                                return;
                            }
                            if (forceSet)
                            {
                                e.doit = true;
                                forceSet = false;
                            }
                        }

                    });

                    super.addEditorAssist(control);
                }

                @Override
                public String browseType()
                {

                    CenteringColorDialog colorDialog = new CenteringColorDialog(EJUIPlugin.getActiveWorkbenchShell());
                    RGB selectedColor = colorDialog.open("Foreground Color", new RGB(0, 0, 0));
                    if (selectedColor != null)
                    {
                        forceSet = true;
                        return ("r" + selectedColor.red + "g" + selectedColor.green + "b" + selectedColor.blue);
                    }
                    return getValue();
                }

            };
            fgDescriptor.setText("Foreground");

            final AbstractDescriptor<String> bgDescriptor = new AbstractDescriptor<String>(AbstractDescriptor.TYPE.REFERENCE)
            {
                boolean forceSet = false;

                @Override
                public String getValue()
                {
                    if (source.getBackgroundRGB() == null)
                        return EJCoreVisualAttributeProperties.UNSPECIFIED;
                    return source.getBackgroundRGB();
                }

                @Override
                public void setValue(String value)
                {

                    source.setBackgroundRGB(EJCoreVisualAttributeProperties.UNSPECIFIED.equals(value) ? null : value);
                    editor.setDirty(true);
                    refresh(VisualAttributeNode.this);
                    handler.refresh();
                }

                @Override
                public void addEditorAssist(Control control)
                {
                    final Text text = (Text) control;
                    ((Text) control).addVerifyListener(new EJPluginEntireJNumberVerifier()
                    {
                        @Override
                        public void verifyText(VerifyEvent e)
                        {

                            e.doit = EJCoreVisualAttributeProperties.UNSPECIFIED.equals(e.text);
                            if (e.keyCode == SWT.DEL || e.keyCode == SWT.BS)
                            {
                                e.doit = false;
                                text.setText(EJCoreVisualAttributeProperties.UNSPECIFIED);
                                return;
                            }
                            if (!forceSet && e.keyCode == 0 && !text.getText().equals(e.text)
                                    && (e.text.startsWith("r") && e.text.contains("g") && e.text.contains("b")))
                            {
                                forceSet = true;
                                text.setText(e.text);
                                return;
                            }
                            if (forceSet)
                            {
                                e.doit = true;
                                forceSet = false;
                            }
                        }

                    });

                    super.addEditorAssist(control);
                }

                @Override
                public String browseType()
                {
                    CenteringColorDialog colorDialog = new CenteringColorDialog(EJUIPlugin.getActiveWorkbenchShell());
                    RGB selectedColor = colorDialog.open("Background Color", new RGB(255, 255, 255));
                    if (selectedColor != null)
                    {
                        forceSet = true;
                        return ("r" + selectedColor.red + "g" + selectedColor.green + "b" + selectedColor.blue);
                    }
                    return getValue();
                }

            };
            bgDescriptor.setText("Background");

            AbstractGroupDescriptor colorGroupDescriptor = new AbstractGroupDescriptor("Colors")
            {

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    return new AbstractDescriptor<?>[] { fgDescriptor, bgDescriptor };
                }

            };
            return colorGroupDescriptor;
        }
    }

    private static class CenteringColorDialog
    {

        static private final int COLORDIALOG_WIDTH  = 222;
        static private final int COLORDIALOG_HEIGHT = 306;

        private final Shell      parentShell;

        public CenteringColorDialog(Shell parentShell)
        {
            this.parentShell = parentShell;
        }

        public RGB open(String title, RGB rgb)
        {
            final Shell centerShell = new Shell(parentShell, SWT.NO_TRIM);
            centerShell.setLocation((parentShell.getSize().x - COLORDIALOG_WIDTH) / 2, (parentShell.getSize().y - COLORDIALOG_HEIGHT) / 2);
            ColorDialog colorDg = new ColorDialog(centerShell, SWT.APPLICATION_MODAL);
            colorDg.setText(title);
            colorDg.setRGB(rgb);
            final RGB colorChose = colorDg.open();
            centerShell.dispose();
            return colorChose;
        }
    }

    public class ColorEditor
    {

        private Point  fExtent;
        private Image  fImage;
        private RGB    fColorValue;
        private Color  fColor;
        private Button fButton;

        public ColorEditor(Composite parent)
        {

            fButton = new Button(parent, SWT.PUSH);
            fExtent = computeImageSize(parent);
            fImage = new Image(parent.getDisplay(), fExtent.x, fExtent.y);

            GC gc = new GC(fImage);
            gc.setBackground(fButton.getBackground());
            gc.fillRectangle(0, 0, fExtent.x, fExtent.y);
            gc.dispose();

            fButton.setImage(fImage);
            fButton.addSelectionListener(new SelectionAdapter()
            {
                public void widgetSelected(SelectionEvent event)
                {
                    ColorDialog colorDialog = new ColorDialog(fButton.getShell());
                    colorDialog.setRGB(fColorValue);
                    RGB newColor = colorDialog.open();
                    if (newColor != null)
                    {
                        fColorValue = newColor;
                        updateColorImage();
                    }
                }
            });

            fButton.addDisposeListener(new DisposeListener()
            {
                public void widgetDisposed(DisposeEvent event)
                {
                    if (fImage != null)
                    {
                        fImage.dispose();
                        fImage = null;
                    }
                    if (fColor != null)
                    {
                        fColor.dispose();
                        fColor = null;
                    }
                }
            });
        }

        public RGB getColorValue()
        {
            return fColorValue;
        }

        public void setColorValue(RGB rgb)
        {
            fColorValue = rgb;
            updateColorImage();
        }

        public Button getButton()
        {
            return fButton;
        }

        protected void updateColorImage()
        {

            Display display = fButton.getDisplay();

            GC gc = new GC(fImage);
            gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
            gc.drawRectangle(0, 2, fExtent.x - 1, fExtent.y - 4);

            if (fColor != null)
                fColor.dispose();

            fColor = new Color(display, fColorValue);
            gc.setBackground(fColor);
            gc.fillRectangle(1, 3, fExtent.x - 2, fExtent.y - 5);
            gc.dispose();

            fButton.setImage(fImage);
        }

        protected Point computeImageSize(Control window)
        {
            GC gc = new GC(window);
            Font f = JFaceResources.getFontRegistry().get(JFaceResources.DEFAULT_FONT);
            gc.setFont(f);
            int height = gc.getFontMetrics().getHeight();
            gc.dispose();
            Point p = new Point(height * 3 - 6, height);
            return p;
        }
    }

    class SortAction extends Action
    {
        SortAction()
        {
            super("Sort");
            setChecked(sorted = EJUIPlugin.getDefault().getPreferenceStore().getBoolean(SORTED_STORE));
            setToolTipText("Sort");
            setDescription("Sorts elements in the outline.");
            setImageDescriptor(EJUIImages.DESC_ALPHAB_SORT_CO);

        }

        @Override
        public void run()
        {
            setSorted(isChecked());
        }
    }

    // USED RWT EJRwtVisualAttributeUtils code;
    public static Color getBackground(EJCoreVisualAttributeProperties visualAttributeProperties)
    {
        if (visualAttributeProperties != null)
        {
            Color background = null;

            if (visualAttributeProperties.getBackgroundRGB() == null
                    || visualAttributeProperties.getBackgroundRGB().equals(EJCoreVisualAttributeProperties.UNSPECIFIED))
                return null;
            java.awt.Color backgroundColor = visualAttributeProperties.getBackgroundColor();
            background = new Color(Display.getDefault(), backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue());

            return background;
        }

        return null;

    }

    public static Color getForeground(EJCoreVisualAttributeProperties visualAttributeProperties)
    {
        if (visualAttributeProperties != null)
        {
            Color foreground = null;
            if (visualAttributeProperties.getForegroundRGB() == null
                    || visualAttributeProperties.getForegroundRGB().equals(EJCoreVisualAttributeProperties.UNSPECIFIED))
                return null;
            java.awt.Color foregroundColor = visualAttributeProperties.getForegroundColor();
            foreground = new Color(Display.getDefault(), foregroundColor.getRed(), foregroundColor.getGreen(), foregroundColor.getBlue());

            return foreground;
        }

        return null;

    }

    public static Font getFont(EJCoreVisualAttributeProperties visualAttributeProperties, Font defaultFont)
    {
        if (visualAttributeProperties != null)
        {
            Font font = null;

            if (visualAttributeProperties.getFontName().equals(EJCoreVisualAttributeProperties.UNSPECIFIED)
                    && visualAttributeProperties.getFontStyle() == EJFontStyle.Unspecified
                    && visualAttributeProperties.getFontWeight() == EJFontWeight.Unspecified && !visualAttributeProperties.isFontSizeSet())
            {
                return null;
            }
            String name = null;
            int style = SWT.NORMAL;
            int size = 11;
            if (defaultFont != null)
            {
                name = defaultFont.getFontData()[0].getName();
                style = defaultFont.getFontData()[0].getStyle();

                size = defaultFont.getFontData()[0].getHeight();
            }
            if (visualAttributeProperties.getFontName() != null && !visualAttributeProperties.getFontName().equals(EJCoreVisualAttributeProperties.UNSPECIFIED))
                name = visualAttributeProperties.getFontName();

            if (name == null)
                return null;

            EJFontStyle fontStyle = visualAttributeProperties.getFontStyle();
            switch (fontStyle)
            {
                case Italic:
                    style = style | SWT.ITALIC;
                    break;

                case Underline:
                    // todo: how to do it in font level ???
                    break;
            }

            EJFontWeight fontWeight = visualAttributeProperties.getFontWeight();
            switch (fontWeight)
            {
                case Bold:
                    style = style | SWT.BOLD;
                    break;
            }
            if (visualAttributeProperties.getFontSize() > 0)
            {
                if(visualAttributeProperties.isFontSizeAsPercentage())
                {
                    if(visualAttributeProperties.getFontSize()!=100)
                    {
                        double fontSizeP = visualAttributeProperties.getFontSize();
                        size = (int)(size* (fontSizeP/100)); 
                    }
                }
                else
                {

                    size = visualAttributeProperties.getFontSize();
                }
            }
            font = new Font(Display.getDefault(), name, size, style);
            return font;
        }
        return null;
    }
}
