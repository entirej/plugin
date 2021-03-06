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
package org.entirej.ide.ui.editors.report.wizards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;
import org.entirej.framework.plugin.reports.EJPluginReportBorderProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.AlignmentBaseItem;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.Date.DateFormats;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.Number.NumberFormats;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.RotatableItem;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.ValueBaseItem;
import org.entirej.framework.plugin.utils.EJPluginEntireJNumberVerifier;
import org.entirej.framework.report.enumerations.EJReportScreenAlignment;
import org.entirej.framework.report.enumerations.EJReportScreenItemType;
import org.entirej.framework.report.enumerations.EJReportScreenRotation;
import org.entirej.framework.report.interfaces.EJReportBorderProperties;
import org.entirej.ide.ui.editors.descriptors.AbstractBooleanDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptorPart;
import org.entirej.ide.ui.editors.descriptors.AbstractProjectSrcFileDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDropDownDescriptor;
import org.entirej.ide.ui.editors.descriptors.IJavaProjectProvider;
import org.entirej.ide.ui.editors.report.ReportBlockColumnGroupNode;
import org.entirej.ide.ui.editors.report.ReportBlockItemsGroupNode;

public class BlockColumnSelectionPage extends WizardPage
{

    private final BlockColumnWizardContext     wizardContext;
    private String                             blockColumnName;
    private String                             blockColumnLabel = "";
    private String                             blockColumnLabelVA ;
    private int                                width            = 90;
    private EJPluginReportScreenItemProperties itemProperties;
    Composite                                  body;

    protected BlockColumnSelectionPage(BlockColumnWizardContext wizardContext)
    {
        super("ej.data.column.selection");
        this.wizardContext = wizardContext;
        setTitle("New Screen Column");
        setDescription("Properties for the new screen column.");
    }

    public void createControl(Composite parent)
    {
        initializeDialogUnits(parent);
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
        Dialog.applyDialogFont(composite);
        int nColumns = 4;

        GridLayout layout = new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout(layout);
        createBlockName(composite, nColumns);
        createBlockScreenTypeControls(composite, nColumns);
        setControl(composite);

        setPageComplete(false);
    }

    protected void createSeparator(Composite composite, int nColumns)
    {
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = nColumns;
        gridData.heightHint = convertHeightInCharsToPixels(1);
        (new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL)).setLayoutData(gridData);
    }

    public static Control createEmptySpace(Composite parent, int span)
    {
        Label label = new Label(parent, SWT.LEFT);
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.BEGINNING;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = span;
        gd.horizontalIndent = 0;
        gd.widthHint = 0;
        gd.heightHint = 0;
        label.setLayoutData(gd);
        return label;
    }

    private void createBlockName(Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("Name:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        formTitleLabel.setLayoutData(gd);
        final Text blockNameText = new Text(composite, SWT.BORDER | SWT.SINGLE);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalSpan = 2;
        blockNameText.setLayoutData(gd);
        blockNameText.addModifyListener(new ModifyListener()
        {

            public void modifyText(ModifyEvent e)
            {
                blockColumnName = blockNameText.getText();
                doUpdateStatus();
            }
        });
        createEmptySpace(composite, 1);
    }

    private void createBlockScreenTypeControls(final Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("Screen Item Type:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        formTitleLabel.setLayoutData(gd);
        final ComboViewer blockRenderersViewer = new ComboViewer(composite);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 2;
        blockRenderersViewer.getCombo().setLayoutData(gd);
        blockRenderersViewer.setLabelProvider(new ColumnLabelProvider()
        {
            @Override
            public String getText(Object element)
            {
                if (element instanceof EJPluginRenderer)
                {
                    EJPluginRenderer renderer = ((EJPluginRenderer) element);
                    return String.format("%s", renderer.getAssignedName(), renderer.getRendererClassName());
                }
                return super.getText(element);
            }

        });

        blockRenderersViewer.setContentProvider(new IStructuredContentProvider()
        {

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {
            }

            public void dispose()
            {
            }

            public Object[] getElements(Object inputElement)
            {
                List<EJReportScreenItemType> renderers = wizardContext.getBlockItemTypes();
                Collections.sort(renderers,new Comparator<EJReportScreenItemType>()
                {

                    public int compare(EJReportScreenItemType o1, EJReportScreenItemType o2)
                    {
                     
                        return o1.toString().compareTo(o2.toString());
                    }
                });
                return renderers.toArray();
            }
        });

        blockRenderersViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {

                if (blockRenderersViewer.getSelection() instanceof IStructuredSelection)
                {
                    EJReportScreenItemType type = (EJReportScreenItemType) ((IStructuredSelection) blockRenderersViewer.getSelection()).getFirstElement();

                    if (body != null && !body.isDisposed())
                    {
                        body.dispose();
                    }
                    if (type != null)
                    {
                        itemProperties = wizardContext.newScreenItem(type);
                        body = new Group(composite, SWT.NONE);
                        body.setLayout(new GridLayout());

                        GridData layoutData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
                        layoutData.heightHint = 500;
                        layoutData.horizontalSpan = 2;
                        body.setLayoutData(layoutData);

                        AbstractDescriptorPart part = new AbstractDescriptorPart(wizardContext.getToolkit(), body, true)
                        {

                            @Override
                            public String getSectionTitle()
                            {
                                return "Screen Item Settings";
                            }

                            @Override
                            public String getSectionDescription()
                            {
                                return "";
                            }

                            @Override
                            public AbstractDescriptor<?>[] getDescriptors()
                            {
                                List<AbstractDescriptor<?>> descriptors = new ArrayList<AbstractDescriptor<?>>();

                                if (itemProperties != null)
                                {
                                    addTypeBaseDescriptors(descriptors, itemProperties);
                                }

                                return descriptors.toArray(new AbstractDescriptor<?>[0]);
                            }
                            
                            @Override
                            public Object getInput()
                            {
                                return new Object();
                            }
                        };
                        body.setBackground(body.getBackground());
                        part.buildUI(true);
                        composite.layout(true);
                    }
                }
                doUpdateStatus();
            }
        });
        createEmptySpace(composite, 1);
        blockRenderersViewer.setInput(new Object());
    }

    public String getBlockColumnName()
    {
        return blockColumnName;
    }

    public String getBlockColumnLabel()
    {
        return blockColumnLabel;
    }
    
    public String getBlockColumnLabelVA()
    {
        return blockColumnLabelVA;
    }

    public EJPluginReportScreenItemProperties getItemProperties()
    {
        return itemProperties;
    }

    public int getWidth()
    {
        return width;
    }

    protected void doUpdateStatus()
    {
        setPageComplete(validatePage());
    }

    private void addTypeBaseDescriptors(List<AbstractDescriptor<?>> descriptors, final EJPluginReportScreenItemProperties source)
    {

        AbstractTextDropDownDescriptor vaDescriptor = new AbstractTextDropDownDescriptor("Visual Attributes", "")
        {
            List<String> visualAttributeNames = new ArrayList<String>(source.getBlockProperties().getReportProperties().getEntireJProperties()
                                                      .getVisualAttributesContainer().getVisualAttributeNames());

            @Override
            public void setValue(String value)
            {
                source.setVisualAttributeName(value);

            }

            @Override
            public String getValue()
            {
                return source.getVisualAttributeName();
            }

            public String[] getOptions()
            {
                List<String> list = new ArrayList<String>();

                list.add("");

                list.addAll(visualAttributeNames);

                if (getValue() != null && getValue().length() > 0 && !visualAttributeNames.contains(getValue()))
                {
                    list.add(getValue());
                }
                return list.toArray(new String[0]);
            }

            public String getOptionText(String t)
            {
                if (t.length() > 0 && !visualAttributeNames.contains(t))
                {
                    return String.format("Undefined !< %s >", t);
                }

                return t;
            }
        };

        descriptors.add(vaDescriptor);

        final AbstractTextDescriptor widthDescriptor = new AbstractTextDescriptor("Width")
        {

            @Override
            public String getTooltip()
            {

                return "The width <b>(in pixels)</b> of the column.";
            }

            @Override
            public void setValue(String value)
            {
                try
                {
                    width = (Integer.parseInt(value));
                }
                catch (NumberFormatException e)
                {
                    width = 0;
                    if (text != null)
                    {
                        text.setText(getValue());
                        text.selectAll();
                    }
                }
            }

            @Override
            public String getValue()
            {
                return String.valueOf(width);
            }

            Text text;

            @Override
            public void addEditorAssist(Control control)
            {

                text = (Text) control;
                text.addVerifyListener(new EJPluginEntireJNumberVerifier());

                super.addEditorAssist(control);
            }
        };

        descriptors.add(widthDescriptor);

        final AbstractTextDescriptor headerDescriptor = new AbstractTextDescriptor("Column Header")
        {

            @Override
            public String getTooltip()
            {

                return "Column header label";
            }

            @Override
            public void setValue(String value)
            {
                blockColumnLabel = value;
            }

            @Override
            public String getValue()
            {
                return blockColumnLabel;
            }

        };
        descriptors.add(headerDescriptor);
   
        
        AbstractTextDropDownDescriptor headerVADescriptor = new AbstractTextDropDownDescriptor("Header Visual Attributes", "")
        {
            List<String> visualAttributeNames = new ArrayList<String>(source.getBlockProperties().getReportProperties().getEntireJProperties()
                                                      .getVisualAttributesContainer().getVisualAttributeNames());

            @Override
            public void setValue(String value)
            {
                blockColumnLabelVA =value;

            }

            @Override
            public String getValue()
            {
                return blockColumnLabelVA;
            }

            public String[] getOptions()
            {
                List<String> list = new ArrayList<String>();

                list.add("");

                list.addAll(visualAttributeNames);

                if (getValue() != null && getValue().length() > 0 && !visualAttributeNames.contains(getValue()))
                {
                    list.add(getValue());
                }
                return list.toArray(new String[0]);
            }

            public String getOptionText(String t)
            {
                if (t.length() > 0 && !visualAttributeNames.contains(t))
                {
                    return String.format("Undefined !< %s >", t);
                }

                return t;
            }
        };
        
        descriptors.add(headerVADescriptor);

        if (source instanceof EJPluginReportScreenItemProperties.ValueBaseItem)
        {
            final EJPluginReportScreenItemProperties.ValueBaseItem item = (ValueBaseItem) source;
            ReportBlockItemsGroupNode.ItemDefaultValue valueProvider = new ReportBlockItemsGroupNode.ItemDefaultValue(wizardContext.getEditor(), source.getBlockProperties()
                    .getReportProperties(), source.getBlockProperties(), "Value Provider")
            {
                @Override
                public String getValue()
                {
                    return item.getValue();
                }

                @Override
                public void setValue(Object value)
                {
                    item.setValue((String) value);

                }

                @Override
                public String getDefaultBlockValue()
                {
                    return wizardContext.getDefaultBlockValue();
                }

            };
            descriptors.add(valueProvider);
        }

        if (source instanceof EJPluginReportScreenItemProperties.AlignmentBaseItem)
        {
            final EJPluginReportScreenItemProperties.AlignmentBaseItem item = (AlignmentBaseItem) source;
            AbstractTextDropDownDescriptor hAlignment = new AbstractTextDropDownDescriptor("Horizontal Alignment")
            {
                @Override
                public String getValue()
                {
                    return item.getHAlignment().name();
                }

                public String[] getOptions()
                {
                    List<String> options = new ArrayList<String>();
                    options.add(EJReportScreenAlignment.NONE.name());
                    options.add(EJReportScreenAlignment.LEFT.name());
                    options.add(EJReportScreenAlignment.CENTER.name());
                    options.add(EJReportScreenAlignment.RIGHT.name());
                    options.add(EJReportScreenAlignment.JUSTIFIED.name());
                    return options.toArray(new String[0]);
                }

                public String getOptionText(String t)
                {

                    return EJReportScreenAlignment.valueOf(t).toString();
                }

                @Override
                public void setValue(String value)
                {
                    item.setHAlignment(EJReportScreenAlignment.valueOf(value));
                }

            };
            AbstractTextDropDownDescriptor vAlignment = new AbstractTextDropDownDescriptor("Vertical Alignment")
            {
                @Override
                public String getValue()
                {
                    return item.getVAlignment().name();
                }

                public String[] getOptions()
                {
                    List<String> options = new ArrayList<String>();
                    options.add(EJReportScreenAlignment.NONE.name());
                    options.add(EJReportScreenAlignment.TOP.name());
                    options.add(EJReportScreenAlignment.CENTER.name());
                    options.add(EJReportScreenAlignment.BOTTOM.name());
                    options.add(EJReportScreenAlignment.JUSTIFIED.name());
                    return options.toArray(new String[0]);
                }

                public String getOptionText(String t)
                {

                    return EJReportScreenAlignment.valueOf(t).toString();
                }

                @Override
                public void setValue(String value)
                {
                    item.setVAlignment(EJReportScreenAlignment.valueOf(value));
                }

            };

            descriptors.add(hAlignment);
            descriptors.add(vAlignment);
            
            {
               final EJPluginReportBorderProperties borderProperties = item.getBorderProperties();
                AbstractTextDropDownDescriptor lineStyle = new AbstractTextDropDownDescriptor("Border Line Style")
                {
                    @Override
                    public String getValue()
                    {
                        return borderProperties.getLineStyle().name();
                    }

                    public String[] getOptions()
                    {
                        List<String> options = new ArrayList<String>();
                        for (EJReportBorderProperties.LineStyle formats : EJReportBorderProperties.LineStyle.values())
                        {
                            options.add(formats.name());
                        }
                        return options.toArray(new String[0]);
                    }

                    public String getOptionText(String t)
                    {

                        return EJReportBorderProperties.LineStyle.valueOf(t).toString();
                    }

                    @Override
                    public void setValue(String value)
                    {
                        borderProperties.setLineStyle(EJReportBorderProperties.LineStyle.valueOf(value));

                    }

                   

                };

                final AbstractTextDescriptor LinewidthDescriptor = new AbstractTextDescriptor("Border Line Width")
                {

                    @Override
                    public String getTooltip()
                    {

                        return "The width <b>(in pixels)</b> of the Line.";
                    }

                   

                    @Override
                    public void setValue(String value)
                    {
                        try
                        {
                            borderProperties.setLineWidth(Double.parseDouble(value));
                        }
                        catch (NumberFormatException e)
                        {
                            borderProperties.setLineWidth(1);
                            if (text != null)
                            {
                                text.setText(getValue());
                                text.selectAll();
                            }
                        }
                    }

                    @Override
                    public String getValue()
                    {
                        return String.valueOf(borderProperties.getLineWidth());
                    }

                    Text text;

                    @Override
                    public void addEditorAssist(Control control)
                    {

                        text = (Text) control;
                        text.addVerifyListener(new EJPluginEntireJNumberVerifier()
                        {

                            @Override
                            protected boolean validate(String value)
                            {
                                try
                                {
                                    Double intValue = Double.parseDouble(value);

                                    if (intValue > -1)
                                    {
                                        return true;
                                    }
                                    else
                                    {
                                        return false;
                                    }
                                }
                                catch (NumberFormatException exception)
                                {
                                    // ignore
                                }

                                return false;
                            }

                        });

                        super.addEditorAssist(control);
                    }
                };

                AbstractTextDropDownDescriptor vaLineDescriptor = new AbstractTextDropDownDescriptor("Border Visual Attributes", "")
                {
                    List<String> visualAttributeNames = new ArrayList<String>(source.getBlockProperties().getReportProperties().getEntireJProperties()
                            .getVisualAttributesContainer().getVisualAttributeNames());

                    @Override
                    public void setValue(String value)
                    {
                        borderProperties.setVisualAttributeName(value);
                      
                    }

                    @Override
                    public String getValue()
                    {
                        return borderProperties.getVisualAttributeName();
                    }

                    

                    public String[] getOptions()
                    {
                        List<String> list = new ArrayList<String>();

                        list.add("");

                        list.addAll(visualAttributeNames);

                        if (getValue() != null && getValue().length() > 0 && !visualAttributeNames.contains(getValue()))
                        {
                            list.add(getValue());
                        }
                        return list.toArray(new String[0]);
                    }

                    public String getOptionText(String t)
                    {
                        if (t.length() > 0 && !visualAttributeNames.contains(t))
                        {
                            return String.format("Undefined !< %s >", t);
                        }

                        return t;
                    }
                };

                descriptors.add(lineStyle);
                descriptors.add(LinewidthDescriptor);
                descriptors.add(vaLineDescriptor);
                descriptors.add(new AbstractBooleanDescriptor("Border Top Line")
                {
                    

                    @Override
                    public void setValue(Boolean value)
                    {
                        borderProperties.setShowTopLine(value);
                      
                    }

                    @Override
                    public Boolean getValue()
                    {
                        return borderProperties.isShowTopLine();
                    }
                });
                descriptors.add(new AbstractBooleanDescriptor("Border Bottom Line")
                {

                    

                    @Override
                    public void setValue(Boolean value)
                    {
                        borderProperties.setShowBottomLine(value);
                    }

                    @Override
                    public Boolean getValue()
                    {
                        return borderProperties.isShowBottomLine();
                    }
                });
                descriptors.add(new AbstractBooleanDescriptor("Border Left Line")
                {
                    

                    @Override
                    public void setValue(Boolean value)
                    {
                        borderProperties.setShowLeftLine(value);
                    }

                    @Override
                    public Boolean getValue()
                    {
                        return borderProperties.isShowLeftLine();
                    }
                });
                descriptors.add(new AbstractBooleanDescriptor("Border Right Line")
                {

                    

                    @Override
                    public void setValue(Boolean value)
                    {
                        borderProperties.setShowRightLine(value);
                        
                    }

                    @Override
                    public Boolean getValue()
                    {
                        return borderProperties.isShowRightLine();
                    }
                });
                
                
            }
            
            
            
        }

        if (source instanceof EJPluginReportScreenItemProperties.RotatableItem)
        {
            final EJPluginReportScreenItemProperties.RotatableItem item = (RotatableItem) source;
            AbstractTextDropDownDescriptor rotation = new AbstractTextDropDownDescriptor("Rotation")
            {
                @Override
                public String getValue()
                {
                    return item.getRotation().name();
                }

                public String[] getOptions()
                {
                    List<String> options = new ArrayList<String>();
                    options.add(EJReportScreenRotation.NONE.name());
                    options.add(EJReportScreenRotation.LEFT.name());
                    options.add(EJReportScreenRotation.RIGHT.name());
                    options.add(EJReportScreenRotation.UPSIDEDOWN.name());
                    return options.toArray(new String[0]);
                }

                public String getOptionText(String t)
                {

                    return EJReportScreenRotation.valueOf(t).toString();
                }

                @Override
                public void setValue(String value)
                {
                    item.setRotation(EJReportScreenRotation.valueOf(value));
                }

            };

            descriptors.add(rotation);
        }

        switch (source.getType())
        {
            case LABEL:
            {
                final EJPluginReportScreenItemProperties.Label label = (EJPluginReportScreenItemProperties.Label) source;
                final AbstractTextDescDescriptor textDescriptor = new AbstractTextDescDescriptor("Text")
                {

                    @Override
                    public String getTooltip()
                    {

                        return "Label Text";
                    }

                    @Override
                    public void setValue(String value)
                    {
                        label.setText(value);
                    }

                    @Override
                    public String getValue()
                    {
                        return label.getText();
                    }

                };
                descriptors.add(textDescriptor);
            }
                break;
            case NUMBER:
            {
                final EJPluginReportScreenItemProperties.Number number = (EJPluginReportScreenItemProperties.Number) source;
                final AbstractTextDescriptor mformatDescriptor = new AbstractTextDescriptor("Manual Format")
                {

                    @Override
                    public void setValue(String value)
                    {
                        number.setManualFormat(value);
                    }

                    @Override
                    public String getValue()
                    {
                        return number.getManualFormat();
                    }

                };

                AbstractTextDropDownDescriptor lformat = new AbstractTextDropDownDescriptor("Locale Format")
                {
                    @Override
                    public String getValue()
                    {
                        return number.getLocaleFormat().name();
                    }

                    public String[] getOptions()
                    {
                        List<String> options = new ArrayList<String>();
                        for (NumberFormats formats : NumberFormats.values())
                        {
                            options.add(formats.name());
                        }
                        return options.toArray(new String[0]);
                    }

                    public String getOptionText(String t)
                    {

                        return NumberFormats.valueOf(t).toString();
                    }

                    @Override
                    public void setValue(String value)
                    {
                        number.setLocaleFormat(NumberFormats.valueOf(value));
                    }

                };
                descriptors.add(lformat);
                descriptors.add(mformatDescriptor);
            }
                break;
            case IMAGE:
            {

                final EJPluginReportScreenItemProperties.Image label = (EJPluginReportScreenItemProperties.Image) source;
                final AbstractProjectSrcFileDescriptor textDescriptor = new AbstractProjectSrcFileDescriptor(new IJavaProjectProvider()
                {

                    public IJavaProject getJavaProject()
                    {

                        return wizardContext.getProject();
                    }
                }, "Default Image")
                {

                    @Override
                    public void setValue(String value)
                    {
                        label.setDefaultImage(value);

                    }

                    @Override
                    public String getValue()
                    {
                        return label.getDefaultImage();
                    }
                };
                descriptors.add(textDescriptor);
            }
                break;
            case DATE:
            {
                final EJPluginReportScreenItemProperties.Date number = (EJPluginReportScreenItemProperties.Date) source;
                final AbstractTextDescriptor mformatDescriptor = new AbstractTextDescriptor("Manual Format")
                {

                    @Override
                    public void setValue(String value)
                    {
                        number.setManualFormat(value);
                    }

                    @Override
                    public String getValue()
                    {
                        return number.getManualFormat();
                    }

                };

                AbstractTextDropDownDescriptor lformat = new AbstractTextDropDownDescriptor("Locale Format")
                {
                    @Override
                    public String getValue()
                    {
                        return number.getLocaleFormat().name();
                    }

                    public String[] getOptions()
                    {
                        List<String> options = new ArrayList<String>();
                        for (DateFormats formats : DateFormats.values())
                        {
                            options.add(formats.name());
                        }
                        return options.toArray(new String[0]);
                    }

                    public String getOptionText(String t)
                    {

                        return DateFormats.valueOf(t).toString();
                    }

                    @Override
                    public void setValue(String value)
                    {
                        number.setLocaleFormat(DateFormats.valueOf(value));
                    }

                };
                descriptors.add(lformat);
                descriptors.add(mformatDescriptor);
            }
                break;

            default:
                break;
        }

    }

    protected boolean validatePage()
    {

        if (blockColumnName == null || blockColumnName.trim().length() == 0)
        {
            setErrorMessage("Block column name can't be empty.");
            return false;
        }
        else if (wizardContext.hasBlockColumn(blockColumnName))
        {
            setErrorMessage("A block column with this name already exists.");
            return false;
        }

        setErrorMessage(null);
        setMessage(null);
        return true;
    }

}
