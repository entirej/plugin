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

import static org.entirej.ide.ui.editors.report.ReportNodeTag.ACTION_PROCESSOR;
import static org.entirej.ide.ui.editors.report.ReportNodeTag.BLOCK;
import static org.entirej.ide.ui.editors.report.ReportNodeTag.BLOCK_ID;
import static org.entirej.ide.ui.editors.report.ReportNodeTag.GROUP;
import static org.entirej.ide.ui.editors.report.ReportNodeTag.HEIGHT;
import static org.entirej.ide.ui.editors.report.ReportNodeTag.ITEM;
import static org.entirej.ide.ui.editors.report.ReportNodeTag.ITEM_ID;
import static org.entirej.ide.ui.editors.report.ReportNodeTag.REPORT;
import static org.entirej.ide.ui.editors.report.ReportNodeTag.SERVICE;
import static org.entirej.ide.ui.editors.report.ReportNodeTag.TITLE;
import static org.entirej.ide.ui.editors.report.ReportNodeTag.TYPE;
import static org.entirej.ide.ui.editors.report.ReportNodeTag.WIDTH;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.entirej.framework.plugin.EJPluginConstants;
import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
import org.entirej.framework.plugin.reports.EJPluginReportItemProperties;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.plugin.reports.reader.EntireJReportReader;
import org.entirej.framework.plugin.reports.reader.ReportHandler;
import org.entirej.framework.report.actionprocessor.interfaces.EJReportActionProcessor;
import org.entirej.framework.report.actionprocessor.interfaces.EJReportBlockActionProcessor;
import org.entirej.framework.report.service.EJReportBlockService;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJMarkerFactory;
import org.entirej.ide.core.spi.EJReportValidateProvider;
import org.entirej.ide.ui.nodes.NodeValidateProvider;
import org.entirej.ide.ui.utils.JavaAccessUtils;

public class EJReportValidateImpl implements EJReportValidateProvider
{

    public void validate(IFile file, IProgressMonitor monitor)
    {
        IProject _project = file.getProject();

        IJavaProject project = JavaCore.create(_project);
        EJPluginReportProperties reportProperties = getReportProperties(file, project);
        if (reportProperties != null)
        {

            if (isReportFile(file))
            {

                // validate base form title
                addMarker(file, validateReportTitle(file, reportProperties, project), REPORT | TITLE);

                // validate base form Action Processor
                addMarker(file, validateReportActionProcessor(file, reportProperties, project), REPORT | ACTION_PROCESSOR);

                // validate base form Layout Settings
                validateReportLayoutSettings(file, reportProperties, project);

            }
            

            // validate blocks
            validateBlocks(file, reportProperties, project);

        }
        monitor.done();
    }


    private void validateBlocks(IFile file, EJPluginReportProperties reportProperties, IJavaProject project)
    {
        List<EJPluginReportBlockProperties> allBlockProperties = reportProperties.getBlockContainer().getAllBlockProperties();
        for (EJPluginReportBlockProperties blockProp : allBlockProperties)
        {

            if (blockProp.isReferenceBlock())
            {
//                if (!reportProperties.getEntireJProperties().containsReusableBlockProperties(blockProp.getReferencedBlockName()))
//                {
//                    IMarker marker = addMarker(
//                            file,
//                            new Problem(Problem.TYPE.ERROR, String.format("'%s' referenced block definition is misssing.", blockProp.getReferencedBlockName())),
//                            GROUP | BLOCK | REF);
//                    if (marker != null)
//                    {
//                        addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());
//                    }
//                }
//                continue;
            }

            // validate base block service
            IMarker marker = addMarker(file, validateBlockService(file, reportProperties, blockProp, project), GROUP | BLOCK | SERVICE);
            if (marker != null)
            {
                addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());
            }

            // validate base block items
            validateBlockItems(file, reportProperties, blockProp, project);

            // validate base block Action Processor
            marker = addMarker(file, validateBlockActionProcessor(file, reportProperties, blockProp, project), GROUP | BLOCK | ACTION_PROCESSOR);
            if (marker != null)
            {
                addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());
            }

        }

    }

    private void validateBlockItems(IFile file, EJPluginReportProperties formProperties, EJPluginReportBlockProperties blockProp, IJavaProject project)
    {
        List<EJPluginReportItemProperties> itemProperties = blockProp.getItemContainer().getAllItemProperties();

        if (!blockProp.isControlBlock()  && !blockProp.isReferenceBlock())
        {
            List<EJPluginReportItemProperties> serviceItems = blockProp.getServiceItems();

            for (EJPluginReportItemProperties itemProp : itemProperties)
            {
                if (itemProp.isBlockServiceItem())
                {
                    boolean found = false;
                    for (EJPluginReportItemProperties serviceItem : serviceItems)
                    {
                        if (serviceItem.getName().equals(itemProp.getName()))
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                    {
                        IMarker marker = addMarker(
                                file,
                                new Problem(Problem.TYPE.ERROR, String.format("['%s' block] item: '%s' is not specified in Service '%s'.", blockProp.getName(),
                                        itemProp.getName(), blockProp.getServiceClassName())), GROUP | BLOCK | ITEM | TYPE);
                        if (marker != null)
                        {
                            addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());
                            addMarkerAttribute(marker, ITEM_ID, itemProp.getName());
                        }
                    }
                }
            }

        }

        for (EJPluginReportItemProperties itemProp : itemProperties)
        {
            IMarker marker = addMarker(file, validateBlockItemDataType(file, formProperties, blockProp, itemProp, project), GROUP | BLOCK | ITEM | TYPE);
            if (marker != null)
            {
                addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());
                addMarkerAttribute(marker, ITEM_ID, itemProp.getName());
            }

        }

    }

    private Problem validateBlockItemDataType(IFile file, EJPluginReportProperties formProperties, EJPluginReportBlockProperties blockProp,
            EJPluginReportItemProperties itemProp, IJavaProject project)
    {

        String defClassName = itemProp.getDataTypeClassName();
        if (defClassName == null || defClassName.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.ERROR,
                    String.format("['%s' block] item: '%s' data type is not specified.", blockProp.getName(), itemProp.getName()));
        }

        try
        {
            IType findType = project.findType(defClassName);
            if (findType == null)
            {
                return new Problem(Problem.TYPE.ERROR, String.format("['%s' block] item: '%s' data type '%s' can't find in project build path.",
                        blockProp.getName(), itemProp.getName(), defClassName));
            }

        }
        catch (CoreException e)
        {
            return new Problem(Problem.TYPE.ERROR, e.getMessage());
        }
        return null;
    }

    private Problem validateBlockService(IFile file, EJPluginReportProperties formProperties, EJPluginReportBlockProperties blockProp, IJavaProject project)
    {

        if (blockProp.isControlBlock() || blockProp.isReferenceBlock() )
            return null;
        String defClassName = blockProp.getServiceClassName();
        if (defClassName == null || defClassName.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.WARNING, String.format("'%s' block service is not specified.", blockProp.getName()));
        }

        try
        {
            IType findType = project.findType(defClassName);
            if (findType == null)
            {
                return new Problem(Problem.TYPE.ERROR, String.format("'%s' block service: '%s' can't find in project build path.", blockProp.getName(),
                        defClassName));
            }

            if (!JavaAccessUtils.isSubTypeOfInterface(findType, EJReportBlockService.class))
            {
                return new Problem(Problem.TYPE.ERROR, String.format("'%s' block service: '%s' is not a sub type of '%s'.", blockProp.getName(), defClassName,
                        EJReportBlockService.class.getName()));
            }
        }
        catch (CoreException e)
        {
            return new Problem(Problem.TYPE.ERROR, e.getMessage());
        }
        return null;
    }

    private Problem validateBlockActionProcessor(IFile file, EJPluginReportProperties formProperties, EJPluginReportBlockProperties blockProp, IJavaProject project)
    {

        String defClassName = blockProp.getActionProcessorClassName();
        if (defClassName == null || defClassName.trim().length() == 0)
        {
            return null;
        }

        try
        {
            IType findType = project.findType(defClassName);
            if (findType == null)
            {
                return new Problem(Problem.TYPE.ERROR, String.format("'%s' block action processor: '%s' can't find in project build path.",
                        blockProp.getName(), defClassName));
            }

            if (!JavaAccessUtils.isSubTypeOfInterface(findType, EJReportBlockActionProcessor.class))
            {
                return new Problem(Problem.TYPE.ERROR, String.format("'%s' block action processor: '%s' is not a sub type of '%s'.", blockProp.getName(),
                        defClassName, EJReportBlockActionProcessor.class.getName()));
            }
        }
        catch (CoreException e)
        {
            return new Problem(Problem.TYPE.ERROR, e.getMessage());
        }
        return null;
    }

    public Problem validateReportTitle(IFile file, EJPluginReportProperties reportProperties, IJavaProject project)
    {
        String title = reportProperties.getTitle();
        if (title == null || title.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.ERROR, "Report Title is not specified.");
        }
        return null;
    }

    public Problem validateReportActionProcessor(IFile file, EJPluginReportProperties reportProperties, IJavaProject project)
    {

        String defClassName = reportProperties.getActionProcessorClassName();
        if (defClassName == null || defClassName.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.ERROR, "Action Processor class must be specified.");
        }

        try
        {
            IType findType = project.findType(defClassName);
            if (findType == null)
            {
                return new Problem(Problem.TYPE.ERROR, String.format("%s can't find in project build path.", defClassName));
            }

            if (!JavaAccessUtils.isSubTypeOfInterface(findType, EJReportActionProcessor.class))
            {
                return new Problem(Problem.TYPE.ERROR, String.format("%s is not a sub type of %s.", defClassName, EJReportActionProcessor.class.getName()));
            }
        }
        catch (CoreException e)
        {
            return new Problem(Problem.TYPE.ERROR, e.getMessage());
        }
        return null;
    }

    private void validateReportLayoutSettings(IFile file, EJPluginReportProperties reportProperties, IJavaProject project)
    {
       

        boolean hasWidth = reportProperties.getReportWidth() > 0;
        boolean hasHeight = reportProperties.getReportHeight() > 0;
        if (!hasWidth && !hasHeight)
        {
            addMarker(file, new Problem(Problem.TYPE.WARNING, "Report height and width is not specified."), REPORT | HEIGHT | WIDTH);
        }
        else
        {
            if (!hasWidth)
            {
                addMarker(file, new Problem(Problem.TYPE.WARNING, "Report width is not specified."), REPORT | WIDTH);
            }
            if (!hasHeight)
            {
                addMarker(file, new Problem(Problem.TYPE.WARNING, "Report height is not specified."), REPORT | HEIGHT);
            }
        }

    }

    private boolean isReportFile(IFile file)
    {
        return EJPluginConstants.REPORT_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(file.getFileExtension());
    }

    EJPluginReportProperties getReportProperties(IFile file, IJavaProject project)
    {

        EJPluginReportProperties reportProperties = null;

        // read from file
        InputStream inStream = null;
        try
        {

            inStream = file.getContents();

            EntireJReportReader reader = new EntireJReportReader();
            String fileName = file.getName();
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
            reportProperties = reader.readReport(new ReportHandler(project, fileName), project, inStream);
            reportProperties.initialisationCompleted();

        }
        catch (Exception exception)
        {

            EJCoreLog.logWarnningMessage(exception.getMessage());
        }
        finally
        {

            try
            {
                if (inStream != null)
                    inStream.close();
            }
            catch (IOException e)
            {
                EJCoreLog.logException(e);
            }
        }

        return reportProperties;
    }

    public IMarker addMarker(IFile file, Problem p, int tag)
    {
        return tagMarker(addMarker(file, p), tag);
    }

    public IMarker addMarker(IFile file, Problem p)
    {
        if (p != null)
        {
            try
            {
                IMarker marker = EJMarkerFactory.createMarker(file);
                switch (p.type)
                {
                    case ERROR:
                        EJMarkerFactory.addErrorMessage(marker, p.message);
                        break;
                    case INFO:
                        EJMarkerFactory.addInfoMessage(marker, p.message);
                        break;
                    case WARNING:
                        EJMarkerFactory.addWarningMessage(marker, p.message);
                        break;
                }
                return marker;
            }
            catch (CoreException e)
            {
                EJCoreLog.logException(e);
            }
        }
        return null;
    }

    public IMarker addMarkerAttribute(IMarker marker, String key, Object val)
    {
        if (marker != null)
        {
            try
            {
                marker.setAttribute(key, val);
            }
            catch (CoreException e)
            {
                EJCoreLog.log(e);
            }
        }

        return marker;
    }

    public IMarker tagMarker(IMarker marker, int tag)
    {
        if (marker != null)
        {
            try
            {
                marker.setAttribute(NodeValidateProvider.NODE_TAG, tag);
            }
            catch (CoreException e)
            {
                EJCoreLog.log(e);
            }
        }

        return marker;
    }

    public static class Problem
    {
        enum TYPE
        {
            INFO, ERROR, WARNING
        }

        final TYPE   type;
        final String message;

        Problem(TYPE type, String message)
        {
            super();
            this.type = type;
            this.message = message;
        }

    }

    protected static interface RendererPropMask
    {
        void mask(IMarker marker);
    }

}
