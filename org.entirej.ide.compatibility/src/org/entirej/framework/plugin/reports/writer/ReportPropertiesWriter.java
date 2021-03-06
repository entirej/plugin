/*******************************************************************************
 * Copyright 2013 CRESOFT AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Contributors: CRESOFT AG - initial API and implementation
 ******************************************************************************/
package org.entirej.framework.plugin.reports.writer;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.entirej.framework.plugin.framework.properties.EJPluginApplicationParameter;
import org.entirej.framework.plugin.framework.properties.writer.AbstractXmlWriter;
import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
import org.entirej.framework.plugin.reports.EJPluginReportBorderProperties;
import org.entirej.framework.plugin.reports.EJPluginReportChartProperties;
import org.entirej.framework.plugin.reports.EJPluginReportColumnProperties;
import org.entirej.framework.plugin.reports.EJPluginReportItemProperties;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.AlignmentBaseItem;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.RotatableItem;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.ValueBaseItem;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer.BlockContainerItem;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer.BlockGroup;
import org.entirej.framework.plugin.reports.containers.EJReportBlockItemContainer;
import org.entirej.framework.plugin.reports.containers.EJReportColumnContainer;
import org.entirej.framework.plugin.reports.containers.EJReportScreenItemContainer;
import org.entirej.framework.report.enumerations.EJReportScreenType;

public class ReportPropertiesWriter extends AbstractXmlWriter
{
    public void saveReport(EJPluginReportProperties form, IFile file, IProgressMonitor monitor)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        
        startTAG(buffer, "entirejFramework");
        {
            startTAG(buffer, "report");
            {
                writeStringTAG(buffer, "reportTitle", form.getTitle());
                writeStringTAG(buffer, "reportDisplayName", form.getReportDisplayName());
                writeIntTAG(buffer, "width", form.getReportWidth());
                writeBooleanTAG(buffer, "ignorePagination", form.isIgnorePagination());
                writeIntTAG(buffer, "height", form.getReportHeight());
                writeIntTAG(buffer, "marginTop", form.getMarginTop());
                writeIntTAG(buffer, "marginBottom", form.getMarginBottom());
                writeIntTAG(buffer, "marginLeft", form.getMarginLeft());
                writeIntTAG(buffer, "marginRight", form.getMarginRight());
                writeIntTAG(buffer, "headerHeight", form.getHeaderSectionHeight());
                writeIntTAG(buffer, "footerHeight", form.getFooterSectionHeight());
                writeStringTAG(buffer, "orientation", form.getOrientation().name());
                writeStringTAG(buffer, "actionProcessorClassName", form.getActionProcessorClassName());
                writeStringTAG(buffer, "va", form.getVisualAttributeName());
                writeStringTAG(buffer, "exportType", form.getExportType().name());
                // Now add the forms parameters
                Iterator<EJPluginApplicationParameter> paramNamesIti = form.getAllReportParameters().iterator();
                startTAG(buffer, "reportParameterList");
                {
                    EJPluginApplicationParameter parameter;
                    while (paramNamesIti.hasNext())
                    {
                        parameter = paramNamesIti.next();
                        
                        startOpenTAG(buffer, "reportParameter");
                        {
                            writePROPERTY(buffer, "name", parameter.getName());
                            writePROPERTY(buffer, "dataType", parameter.getDataTypeName());
                            writePROPERTY(buffer, "defaultValue", parameter.getDefaultValue());
                            closeOpenTAG(buffer);
                        }
                        closeTAG(buffer, "reportParameter");
                    }
                }
                endTAG(buffer, "reportParameterList");
                
                // Now add the forms application properties
                Iterator<String> applNamesIti = form.getAllApplicationPropertyNames().iterator();
                startTAG(buffer, "applicationProperties");
                {
                    String name = "";
                    String value = "";
                    while (applNamesIti.hasNext())
                    {
                        name = applNamesIti.next();
                        value = form.getApplicationProperty(name);
                        
                        startOpenTAG(buffer, "property");
                        {
                            writePROPERTY(buffer, "name", name);
                            closeOpenTAG(buffer);
                            
                            writeTagValue(buffer, value);
                        }
                        closeTAG(buffer, "property");
                    }
                }
                endTAG(buffer, "applicationProperties");
                
                // The block items will be added during the addBlockList method
                startTAG(buffer, "blockList");
                {
                    addBlockList(form.getBlockContainer(), buffer);
                }
                endTAG(buffer, "blockList");
                
            }
            endTAG(buffer, "report");
        }
        endTAG(buffer, "entirejFramework");
        
        // Now set the contents of the file
        try
        {
            if (file.exists())
            {
                // byte[] encryptedData =
                // Encrypter.encrypt(buffer.toString().getBytes("UTF-8"));
                // file.setContents(new ByteArrayInputStream(encryptedData),
                // IResource.KEEP_HISTORY, monitor);
                file.setContents(new ByteArrayInputStream(buffer.toString().getBytes("UTF-8")), IResource.KEEP_HISTORY, monitor);
            }
            else
            {
                file.create(new ByteArrayInputStream(buffer.toString().getBytes("UTF-8")), IResource.KEEP_HISTORY, monitor);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    protected void addBlockList(EJReportBlockContainer blockContainer, StringBuffer buffer)
    {
        
        BlockGroup headerSection = blockContainer.getHeaderSection();
        BlockGroup footerSection = blockContainer.getFooterSection();
        
        if (headerSection.getAllBlockProperties().size() > 0)
        {
            startOpenTAG(buffer, "ej.header.blocks");
            {
                closeOpenTAG(buffer);
                List<EJPluginReportBlockProperties> blockProperties = headerSection.getAllBlockProperties();
                
                for (EJPluginReportBlockProperties blockProps : blockProperties)
                {
                    addBlockProperties(blockProps, buffer);
                }
            }
            endTAG(buffer, "ej.header.blocks");
            
        }
        
        List<BlockGroup> blockContainerItems = blockContainer.getPages();
        for (BlockContainerItem item : blockContainerItems)
        {
            
            // write Block groups
            if (item instanceof BlockGroup)
            {
                BlockGroup group = (BlockGroup) item;
                startOpenTAG(buffer, "blockGroup");
                {
                    writePROPERTY(buffer, "name", group.getName());
                    closeOpenTAG(buffer);
                    List<EJPluginReportBlockProperties> allBlockProperties = group.getAllBlockProperties();
                    for (EJPluginReportBlockProperties blockProps : allBlockProperties)
                    {
                        
                        if (blockProps.isReferenceBlock())
                        {
                            // FIXME
                            // addReferencedBlockProperties(blockProps, buffer);
                        }
                        else
                        {
                            addBlockProperties(blockProps, buffer);
                        }
                        continue;
                    }
                }
                endTAG(buffer, "blockGroup");
            }
        }
        
        if (footerSection.getAllBlockProperties().size() > 0)
        {
            startOpenTAG(buffer, "ej.footer.blocks");
            {
                closeOpenTAG(buffer);
                List<EJPluginReportBlockProperties> blockProperties = footerSection.getAllBlockProperties();
                
                for (EJPluginReportBlockProperties blockProps : blockProperties)
                {
                    addBlockProperties(blockProps, buffer);
                }
            }
            endTAG(buffer, "ej.footer.blocks");
            
        }
        
    }
    
    protected void addBlockProperties(EJPluginReportBlockProperties blockProperties, StringBuffer buffer)
    {
        startOpenTAG(buffer, "block");
        {
            writePROPERTY(buffer, "name", blockProperties.getName());
            writePROPERTY(buffer, "referenced", "false");
            writePROPERTY(buffer, "controlBlock", "" + blockProperties.isControlBlock());
            closeOpenTAG(buffer);
            
            writeStringTAG(buffer, "description", blockProperties.getDescription());
            
            if (!blockProperties.isControlBlock()) writeStringTAG(buffer, "serviceClassName", blockProperties.getServiceClassName());
            
            writeStringTAG(buffer, "actionProcessorClassName", blockProperties.getActionProcessorClassName());
            EJPluginReportScreenProperties layoutScreenProperties = blockProperties.getLayoutScreenProperties();
            writeStringTAG(buffer, "screenType", layoutScreenProperties.getScreenType().name());
            if (layoutScreenProperties.getOddRowVAName() != null) writeStringTAG(buffer, "oddVA", layoutScreenProperties.getOddRowVAName());
            if (layoutScreenProperties.getEvenRowVAName() != null) writeStringTAG(buffer, "evenVA", layoutScreenProperties.getEvenRowVAName());
            writeStringTAG(buffer, "x", layoutScreenProperties.getX() + "");
            writeStringTAG(buffer, "y", layoutScreenProperties.getY() + "");
            writeStringTAG(buffer, "width", layoutScreenProperties.getWidth() + "");
            writeStringTAG(buffer, "height", layoutScreenProperties.getHeight() + "");
            writeStringTAG(buffer, "headerColHeight", layoutScreenProperties.getHeaderColumnHeight() + "");
            writeStringTAG(buffer, "detailColHeight", layoutScreenProperties.getDetailColumnHeight() + "");
            writeStringTAG(buffer, "footerColHeight", layoutScreenProperties.getFooterColumnHeight() + "");
            writeStringTAG(buffer, "newPage", layoutScreenProperties.isNewPage() + "");
            writeStringTAG(buffer, "fitToPage", layoutScreenProperties.isFitToPage() + "");
            // Now add the block items
            startTAG(buffer, "itemList");
            {
                addBlockItemProperties(blockProperties.getItemContainer(), buffer);
            }
            endTAG(buffer, "itemList");
            
            // Now add the block items
            if (layoutScreenProperties.getScreenType() == EJReportScreenType.FORM_LAYOUT)
            {
                startTAG(buffer, "screenItemList");
                {
                    addBlockScreenItemProperties(blockProperties.getLayoutScreenProperties().getScreenItemContainer(), buffer);
                }
                endTAG(buffer, "screenItemList");
                
                BlockGroup group = layoutScreenProperties.getSubBlocks();
                startOpenTAG(buffer, "blockGroup");
                {
                    
                    closeOpenTAG(buffer);
                    List<EJPluginReportBlockProperties> allBlockProperties = group.getAllBlockProperties();
                    for (EJPluginReportBlockProperties blockProps : allBlockProperties)
                    {
                        
                        if (blockProps.isReferenceBlock())
                        {
                            // FIXME
                            // addReferencedBlockProperties(blockProps, buffer);
                        }
                        else
                        {
                            addBlockProperties(blockProps, buffer);
                        }
                        continue;
                    }
                }
                endTAG(buffer, "blockGroup");
            }
            else if (layoutScreenProperties.getScreenType() == EJReportScreenType.TABLE_LAYOUT)
            {
                
                startTAG(buffer, "screenColumnList");
                
                EJReportColumnContainer columnContainer = layoutScreenProperties.getColumnContainer();
                List<EJPluginReportColumnProperties> allColumnProperties = columnContainer.getAllColumnProperties();
                for (EJPluginReportColumnProperties column : allColumnProperties)
                {
                    startOpenTAG(buffer, "columnitem");
                    {
                        writePROPERTY(buffer, "name", column.getName());
                        writePROPERTY(buffer, "showHeader", column.isShowHeader() + "");
                        writePROPERTY(buffer, "showFooter", column.isShowFooter() + "");
                        closeOpenTAG(buffer);
                        
                        {
                            startTAG(buffer, "headerScreen");
                            writeStringTAG(buffer, "width", column.getHeaderScreen().getWidth() + "");
                            writeStringTAG(buffer, "height", column.getHeaderScreen().getHeight() + "");
                            
                            EJPluginReportBorderProperties borderProperties = column.getHeaderBorderProperties();
                            writeStringTAG(buffer, "showTopLine", borderProperties.isShowTopLine() + "");
                            writeStringTAG(buffer, "showBottomLine", borderProperties.isShowBottomLine() + "");
                            writeStringTAG(buffer, "showLeftLine", borderProperties.isShowLeftLine() + "");
                            writeStringTAG(buffer, "showRightLine", borderProperties.isShowRightLine() + "");
                            writeStringTAG(buffer, "lineWidth", borderProperties.getLineWidth() + "");
                            writeStringTAG(buffer, "lineStyle", borderProperties.getLineStyle().name());
                            writeStringTAG(buffer, "lineVA", borderProperties.getVisualAttributeName());
                            addBlockScreenItemProperties(column.getHeaderScreen().getScreenItemContainer(), buffer);
                            endTAG(buffer, "headerScreen");
                        }
                        
                        {
                            startTAG(buffer, "detailScreen");
                            writeStringTAG(buffer, "width", column.getDetailScreen().getWidth() + "");
                            writeStringTAG(buffer, "height", column.getDetailScreen().getHeight() + "");
                            EJPluginReportBorderProperties borderProperties = column.getDetailBorderProperties();
                            writeStringTAG(buffer, "showTopLine", borderProperties.isShowTopLine() + "");
                            writeStringTAG(buffer, "showBottomLine", borderProperties.isShowBottomLine() + "");
                            writeStringTAG(buffer, "showLeftLine", borderProperties.isShowLeftLine() + "");
                            writeStringTAG(buffer, "showRightLine", borderProperties.isShowRightLine() + "");
                            writeStringTAG(buffer, "lineWidth", borderProperties.getLineWidth() + "");
                            writeStringTAG(buffer, "lineStyle", borderProperties.getLineStyle().name());
                            writeStringTAG(buffer, "lineVA", borderProperties.getVisualAttributeName());
                            addBlockScreenItemProperties(column.getDetailScreen().getScreenItemContainer(), buffer);
                            endTAG(buffer, "detailScreen");
                        }
                        
                        {
                            startTAG(buffer, "footerScreen");
                            writeStringTAG(buffer, "width", column.getFooterScreen().getWidth() + "");
                            writeStringTAG(buffer, "height", column.getFooterScreen().getHeight() + "");
                            EJPluginReportBorderProperties borderProperties = column.getFooterBorderProperties();
                            writeStringTAG(buffer, "showTopLine", borderProperties.isShowTopLine() + "");
                            writeStringTAG(buffer, "showBottomLine", borderProperties.isShowBottomLine() + "");
                            writeStringTAG(buffer, "showLeftLine", borderProperties.isShowLeftLine() + "");
                            writeStringTAG(buffer, "showRightLine", borderProperties.isShowRightLine() + "");
                            writeStringTAG(buffer, "lineWidth", borderProperties.getLineWidth() + "");
                            writeStringTAG(buffer, "lineStyle", borderProperties.getLineStyle().name());
                            writeStringTAG(buffer, "lineVA", borderProperties.getVisualAttributeName());
                            addBlockScreenItemProperties(column.getFooterScreen().getScreenItemContainer(), buffer);
                            endTAG(buffer, "footerScreen");
                        }
                    }
                    endTAG(buffer, "columnitem");
                }
                endTAG(buffer, "screenColumnList");
                
                BlockGroup group = layoutScreenProperties.getSubBlocks();
                startOpenTAG(buffer, "blockGroup");
                {
                    
                    closeOpenTAG(buffer);
                    List<EJPluginReportBlockProperties> allBlockProperties = group.getAllBlockProperties();
                    for (EJPluginReportBlockProperties blockProps : allBlockProperties)
                    {
                        
                        if (blockProps.isReferenceBlock())
                        {
                            // FIXME
                            // addReferencedBlockProperties(blockProps, buffer);
                        }
                        else
                        {
                            addBlockProperties(blockProps, buffer);
                        }
                        continue;
                    }
                }
                endTAG(buffer, "blockGroup");
            }
            else if (layoutScreenProperties.getScreenType() == EJReportScreenType.CHART_LAYOUT)
            {
                EJPluginReportChartProperties properties = layoutScreenProperties.getChartProperties();
                
                startTAG(buffer, "chartLayout");
                startOpenTAG(buffer, "config");
                {
                    writePROPERTY(buffer, "type", properties.getChartType().name());
                    writePROPERTY(buffer, "use3dView", properties.isUse3dView() + "");
                    writePROPERTY(buffer, "categoryItem", properties.getCategoryItem());
                    writePROPERTY(buffer, "labelItem", properties.getLabelItem());
                    writePROPERTY(buffer, "seriesItem", properties.getSeriesItem());
                    writePROPERTY(buffer, "value1Item", properties.getValue1Item());
                    writePROPERTY(buffer, "value2Item", properties.getValue2Item());
                    writePROPERTY(buffer, "value3Item", properties.getValue3Item());
                    writePROPERTY(buffer, "title", properties.getTitle());
                    writePROPERTY(buffer, "subtitle", properties.getSeriesItem());
                    writePROPERTY(buffer, "titleVA", properties.getTitleVA());
                    writePROPERTY(buffer, "subtitleVA", properties.getSubtitleVA());
                    closeOpenTAG(buffer);
                    
                }
                endTAG(buffer, "config");
                endTAG(buffer, "chartLayout");
            }
        }
        endTAG(buffer, "block");
    }
    
    protected void addBlockScreenItemProperties(EJReportScreenItemContainer screenItemContainer, StringBuffer buffer)
    {
        List<EJPluginReportScreenItemProperties> itemProperties = screenItemContainer.getAllItemProperties();
        
        for (EJPluginReportScreenItemProperties itemProps : itemProperties)
        {
            startOpenTAG(buffer, "screenitem");
            {
                writePROPERTY(buffer, "name", itemProps.getName());
                writePROPERTY(buffer, "type", itemProps.getType().name());
                closeOpenTAG(buffer);
                writeStringTAG(buffer, "x", itemProps.getX() + "");
                writeStringTAG(buffer, "y", itemProps.getY() + "");
                if (itemProps.getLeftPadding() > -1) writeStringTAG(buffer, "leftPadding", itemProps.getLeftPadding() + "");
                if (itemProps.getRightPadding() > -1) writeStringTAG(buffer, "rightPadding", itemProps.getRightPadding() + "");
                writeStringTAG(buffer, "width", itemProps.getWidth() + "");
                writeStringTAG(buffer, "height", itemProps.getHeight() + "");
                writeStringTAG(buffer, "widthAsPercentage", String.valueOf(itemProps.isWidthAsPercentage()));
                writeStringTAG(buffer, "heightAsPercentage", String.valueOf(itemProps.isHeightAsPercentage()));
                writeStringTAG(buffer, "visible", String.valueOf(itemProps.isVisible()));
                
                if (itemProps.getVisualAttributeName() != null) writeStringTAG(buffer, "va", itemProps.getVisualAttributeName());
                
                // add type base properties
                
                if (itemProps instanceof EJPluginReportScreenItemProperties.ValueBaseItem)
                {
                    final EJPluginReportScreenItemProperties.ValueBaseItem item = (ValueBaseItem) itemProps;
                    if (item.getValue() != null)
                    {
                        writeStringTAG(buffer, "valueProvider", item.getValue());
                    }
                    
                    writeBooleanTAG(buffer, "expandToFit", item.isExpandToFit());
                    writeStringTAG(buffer, "markup", item.getMarkup().name());
                }
                if (itemProps instanceof EJPluginReportScreenItemProperties.AlignmentBaseItem)
                {
                    final EJPluginReportScreenItemProperties.AlignmentBaseItem item = (AlignmentBaseItem) itemProps;
                    
                    writeStringTAG(buffer, "hAlignment", item.getHAlignment().name());
                    writeStringTAG(buffer, "vAlignment", item.getVAlignment().name());
                    
                    EJPluginReportBorderProperties borderProperties = item.getBorderProperties();
                    writeStringTAG(buffer, "showTopLine", borderProperties.isShowTopLine() + "");
                    writeStringTAG(buffer, "showBottomLine", borderProperties.isShowBottomLine() + "");
                    writeStringTAG(buffer, "showLeftLine", borderProperties.isShowLeftLine() + "");
                    writeStringTAG(buffer, "showRightLine", borderProperties.isShowRightLine() + "");
                    writeStringTAG(buffer, "lineWidth", borderProperties.getLineWidth() + "");
                    writeStringTAG(buffer, "lineStyle", borderProperties.getLineStyle().name());
                    writeStringTAG(buffer, "lineVA", borderProperties.getVisualAttributeName());
                    
                    
                }
                if (itemProps instanceof EJPluginReportScreenItemProperties.RotatableItem)
                {
                    final EJPluginReportScreenItemProperties.RotatableItem item = (RotatableItem) itemProps;
                    
                    writeStringTAG(buffer, "rotation", item.getRotation().name());
                }
                
                switch (itemProps.getType())
                {
                    case LABEL:
                    {
                        final EJPluginReportScreenItemProperties.Label label = (EJPluginReportScreenItemProperties.Label) itemProps;
                        writeStringTAG(buffer, "text", label.getText());
                    }
                        break;
                    case LINE:
                    {
                        final EJPluginReportScreenItemProperties.Line line = (EJPluginReportScreenItemProperties.Line) itemProps;
                        writeStringTAG(buffer, "lineWidth", line.getLineWidth() + "");
                        writeStringTAG(buffer, "lineStyle", line.getLineStyle().name());
                        writeStringTAG(buffer, "lineDirection", line.getLineDirection().name());
                    }
                        break;
                    case RECTANGLE:
                    {
                        final EJPluginReportScreenItemProperties.Rectangle line = (EJPluginReportScreenItemProperties.Rectangle) itemProps;
                        writeStringTAG(buffer, "lineWidth", line.getLineWidth() + "");
                        writeStringTAG(buffer, "lineStyle", line.getLineStyle().name());
                        writeStringTAG(buffer, "rectRadius", line.getRadius() + "");
                    }
                        break;
                    case NUMBER:
                    {
                        final EJPluginReportScreenItemProperties.Number number = (EJPluginReportScreenItemProperties.Number) itemProps;
                        writeStringTAG(buffer, "manualFormat", number.getManualFormat());
                        writeStringTAG(buffer, "localeFormat", number.getLocaleFormat().name());
                    }
                        break;
                        
                    case DATE:
                    {
                        final EJPluginReportScreenItemProperties.Date number = (EJPluginReportScreenItemProperties.Date) itemProps;
                        writeStringTAG(buffer, "manualFormat", number.getManualFormat());
                        writeStringTAG(buffer, "localeFormat", number.getLocaleFormat().name());
                    }
                        break;
                    case IMAGE:
                    {
                        final EJPluginReportScreenItemProperties.Image number = (EJPluginReportScreenItemProperties.Image) itemProps;
                        writeStringTAG(buffer, "defaultImage", number.getDefaultImage());
                    }
                        break;
                        
                    default:
                        break;
                }
                
            }
            endTAG(buffer, "screenitem");
        }
        
    }
    
    protected void addBlockItemProperties(EJReportBlockItemContainer itemContainer, StringBuffer buffer)
    {
        Iterator<EJPluginReportItemProperties> items = itemContainer.getAllItemProperties().iterator();
        while (items.hasNext())
        {
            EJPluginReportItemProperties itemProps = items.next();
            
            startOpenTAG(buffer, "item");
            {
                writePROPERTY(buffer, "name", itemProps.getName());
                closeOpenTAG(buffer);
                
                writeBooleanTAG(buffer, "blockServiceItem", itemProps.isBlockServiceItem());
                writeStringTAG(buffer, "dataTypeClassName", itemProps.getDataTypeClassName());
                writeStringTAG(buffer, "defaultQueryValue", itemProps.getDefaultQueryValue());
                
            }
            endTAG(buffer, "item");
        }
    }
    
}
