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
package org.entirej.ide.ui.editors.form.builder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.osgi.util.NLS;
import org.eclipse.text.edits.TextEdit;
import org.entirej.framework.core.enumerations.EJScreenType;
import org.entirej.framework.core.properties.EJCoreLayoutContainer;
import org.entirej.framework.core.properties.EJCoreLayoutItem;
import org.entirej.framework.core.properties.EJCoreLayoutItem.LayoutComponent;
import org.entirej.framework.core.properties.EJCoreLayoutItem.TYPE;
import org.entirej.framework.core.properties.EJCoreLayoutItem.TabGroup;
import org.entirej.framework.core.properties.definitions.EJPropertyDefinitionType;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionPropertyList;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionPropertyListEntry;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinition;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionGroup;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionList;
import org.entirej.framework.core.properties.interfaces.EJCanvasProperties;
import org.entirej.framework.core.properties.interfaces.EJDrawerPageProperties;
import org.entirej.framework.core.properties.interfaces.EJRendererAssignment;
import org.entirej.framework.core.properties.interfaces.EJScreenItemProperties;
import org.entirej.framework.core.properties.interfaces.EJStackedPageProperties;
import org.entirej.framework.core.properties.interfaces.EJTabPageProperties;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevBlockRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevFormRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevInsertScreenRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevItemRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevQueryScreenRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevUpdateScreenRendererDefinition;
import org.entirej.framework.plugin.EJPluginConstants;
import org.entirej.framework.plugin.framework.properties.EJPluginApplicationParameter;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJPropertiesLoader;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginItemGroupProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovMappingProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMainScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuLeafActionProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuLeafBranchProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuLeafFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuLeafProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuLeafSpacerProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuProperties;
import org.entirej.framework.plugin.framework.properties.EntirejPropertiesUtils;
import org.entirej.framework.plugin.framework.properties.ExtensionsPropertiesFactory;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.reader.EntireJFormReader;
import org.entirej.framework.plugin.framework.properties.reader.FormHandler;
import org.entirej.framework.plugin.utils.EJPluginCanvasRetriever;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJPluginEntireJClassLoader;
import org.entirej.ide.core.project.EJProject;
import org.entirej.ide.ui.utils.FormsUtil;

public class EJFormConstBuilder extends IncrementalProjectBuilder
{
    private static final String OBJECTGROUP_PREFIX = "OG_";
    private static final String REFBLOCK_PREFIX    = "RB_";
    private static final String LOV_PREFIX         = "RL_";
    private static final String FORM_PREFIX        = "F_";
    private static final String CONSTANTS_PATH     = "/constants";

    class DeltaVisitor implements IResourceDeltaVisitor
    {
        private IProgressMonitor monitor;

        public DeltaVisitor(IProgressMonitor monitor)
        {
            this.monitor = monitor;
        }

        public boolean visit(IResourceDelta delta)
        {
            IResource resource = delta.getResource();

            if (resource instanceof IProject)
                return isInterestingProject((IProject) resource);

            if (resource instanceof IFolder)
                return true;

            if (resource instanceof IFile)
            {

                // see if this is it
                IFile candidate = (IFile) resource;
                if (candidate.exists() && isFormFile(candidate))
                {
                    // That's it, but only check it if it has been added or
                    // changed
                    if (delta.getKind() != IResourceDelta.REMOVED)
                    {
                        // try
                        // {
                        // candidate.deleteMarkers(EJMarkerFactory.MARKER_ID,
                        // true, IResource.DEPTH_ZERO);
                        // }
                        // catch (CoreException e)
                        // {
                        // EJCoreLog.log(e);
                        // }
                        genConstantFile(candidate, monitor);
                    }
                    else if (isRefFormFile(candidate))
                    {
//                        try
//                        {
////                            List<IFile> forms = new ArrayList<IFile>();
////                            clean(monitor);
////                            genFormConstantsIn(getProject(), monitor,forms);
//                        }
//                        catch (CoreException e)
//                        {
//                            EJCoreLog.logException(e);
//                        }
                        genConstantFile(candidate, monitor);
                    }
                }
                else if (isEJProperties(candidate))
                {
                    if (delta.getKind() != IResourceDelta.REMOVED)
                    {
                        genPropertiesConstantFile(candidate, monitor);
                    }
                }
            }
            return false;
        }
    }

    protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException
    {
        IResourceDelta delta = null;
        if (kind != FULL_BUILD)
            delta = getDelta(getProject());

        if (delta == null || kind == FULL_BUILD)
        {
            if (isInterestingProject(getProject()))
            {
                clean(monitor);
                IProject p = getProject();
                IJavaProject project = JavaCore.create(p);
                // make sure it is refresh before build again
                EJPluginEntireJClassLoader.reload(project);

                genPropertiesConstantFile(EJProject.getPropertiesFile(p), monitor);
                IPackageFragmentRoot[] packageFragmentRoots = project.getPackageFragmentRoots();

                List<IFile> forms = new ArrayList<IFile>();
                for (IPackageFragmentRoot iPackageFragmentRoot : packageFragmentRoots)
                {
                    if (iPackageFragmentRoot.getResource() instanceof IContainer)
                        genFormConstantsIn((IContainer) iPackageFragmentRoot.getResource(), monitor,forms);
                }
                p.refreshLocal(IResource.DEPTH_INFINITE, monitor);
            }

        }
        else
        {
            delta.accept(new DeltaVisitor(monitor));
        }
        return new IProject[0];
    }

    protected void clean(IProgressMonitor monitor) throws CoreException
    {
        SubMonitor localmonitor = SubMonitor.convert(monitor, NLS.bind("Cleaning EJ Form Constants in {0}", getProject().getName()), 1);
        try
        {
            List<String> formNames = FormsUtil.getFormNames(JavaCore.create(getProject()), true);

            // clean existing markers on schema files
            cleanFormsIn(getProject(), formNames, localmonitor);

            localmonitor.worked(1);
        }
        finally
        {
            localmonitor.done();
        }
    }

    private void cleanFormsIn(IContainer container, List<String> formNames, IProgressMonitor monitor) throws CoreException
    {
        if (monitor.isCanceled())
        {
            throw new OperationCanceledException();
        }

        IFolder pkgPath = container.getFolder(new Path(CONSTANTS_PATH));
        if (pkgPath.exists())
        {
            EJPluginEntireJProperties entireJProperties = EJPluginEntireJPropertiesLoader.getEntireJProperties(JavaCore.create(getProject()));

            IResource[] pkgResources = pkgPath.members();
            for (IResource resource : pkgResources)
            {
                if (resource instanceof IFile)
                {
                    IFile file = (IFile) resource;
                    if (file.exists()
                            && (file.getName().startsWith(FORM_PREFIX) || file.getName().startsWith(OBJECTGROUP_PREFIX)
                                    || file.getName().startsWith(REFBLOCK_PREFIX) || file.getName().startsWith(LOV_PREFIX)) && file.getName().endsWith(".java"))
                    {
                        String name = file.getName().substring(0, file.getName().length() - 5);
                        boolean ignore = false;

                        if (name.startsWith(FORM_PREFIX))
                        {
                            for (String formName : formNames)
                            {

                                if (getFormId(formName).equals((name)))
                                {
                                    ignore = true;
                                    break;
                                }

                            }
                        }
                        else if (name.startsWith(OBJECTGROUP_PREFIX))
                        {
                            for (String formName : entireJProperties.getObjectGroupDefinitionNames())
                            {

                                if (getObjectGroupId(formName).equals((name)))
                                {
                                    ignore = true;
                                    break;
                                }

                            }
                        }
                        else if (name.startsWith(REFBLOCK_PREFIX))
                        {
                            for (String formName : entireJProperties.getReusableBlockNames())
                            {

                                if (getRefBlockId(formName).equals((name)))
                                {
                                    ignore = true;
                                    break;
                                }

                            }
                        }
                        else if (name.startsWith(LOV_PREFIX))
                        {
                            for (String formName : entireJProperties.getReusableLovDefinitionNames())
                            {

                                if (getLovId(formName).equals((name)))
                                {
                                    ignore = true;
                                    break;
                                }

                            }
                        }
                        if (ignore)
                            continue;
                        try
                        {
                            file.delete(true, false, monitor);
                        }
                        catch (Exception e)
                        {
                            EJCoreLog.logWarnning(e);
                        }
                    }

                }
            }
        }

        IResource[] members = container.members();

        for (int i = 0; i < members.length; i++)
        {
            IResource member = members[i];
            if (member instanceof IContainer)
            {
                cleanFormsIn((IContainer) member, formNames, monitor);
            }
        }
    }

    private boolean isInterestingProject(IProject project)
    {
        return EJProject.hasPluginNature(project);
    }

    private void genConstantFile(IFile file, IProgressMonitor monitor)
    {

        try
        {
            // try to ignore output path
            IJavaProject project = JavaCore.create(file.getProject());
            IPath outputLocation = project.getOutputLocation();
            if (outputLocation.isPrefixOf(file.getFullPath()))
                return;
        }
        catch (JavaModelException e)
        {
            // ignore
        }
        IFile propFile = EJProject.getPropertiesFile(file.getProject());
        if (propFile == null || !propFile.exists())
        {
            return;
        }

        String message = NLS.bind("Constants Generating {0} ...", file.getFullPath().toString());
        monitor.subTask(message);

        SubProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor, 1);

        IProject _project = file.getProject();

        IJavaProject project = JavaCore.create(_project);
        EJPluginFormProperties formProperties = getFormProperties(file, project);
        if (formProperties != null)
        {
            if(isRefFormFile(propFile))
            {
                
            }
            
            buildFormConstant(project, formProperties, file, subProgressMonitor);
        }

        subProgressMonitor.done();
        monitor.subTask("Constants Updating ...");
        monitor.done();
    }

    private void genPropertiesConstantFile(IFile file, IProgressMonitor monitor)
    {

        try
        {
            // try to ignore output path
            IJavaProject project = JavaCore.create(file.getProject());
            IPath outputLocation = project.getOutputLocation();
            if (outputLocation.isPrefixOf(file.getFullPath()))
                return;
        }
        catch (JavaModelException e)
        {
            // ignore
        }
        IFile propFile = EJProject.getPropertiesFile(file.getProject());
        if (propFile == null || !propFile.exists())
        {
            return;
        }

        String message = NLS.bind("Constants Generating {0} ...", file.getFullPath().toString());
        monitor.subTask(message);

        SubProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor, 1);

        IProject _project = file.getProject();

        IJavaProject project = JavaCore.create(_project);
        EJPluginEntireJProperties entireJProperties = EJPluginEntireJPropertiesLoader.getEntireJProperties(project);

        if (entireJProperties != null)
            buildPropertiesConstant(project, entireJProperties, file, subProgressMonitor);

        subProgressMonitor.done();
        monitor.subTask("Constants Updating ...");
        monitor.done();
    }

    private void genFormConstantsIn(IContainer container, IProgressMonitor monitor,List<IFile> forms) throws CoreException
    {

        monitor.subTask("Compiling EJ Form Constants...");
        IResource[] members = container.members();
        for (int i = 0; i < members.length; i++)
        {
            IResource member = members[i];
            if (member instanceof IContainer)
                genFormConstantsIn((IContainer) member, monitor,forms);
            else if (member instanceof IFile && isFormFile((IFile) member))
            {
                if(!forms.contains(member))
                {
                    genConstantFile((IFile) member, monitor);
                    forms.add((IFile) member);
                }
               
            }
        }
        monitor.done();
    }

    private boolean isFormFile(IFile file)
    {
        return EJPluginConstants.FORM_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(file.getFileExtension()) || isRefFormFile(file);
    }
   

    private boolean isEJProperties(IFile file)
    {
        return file.getName().startsWith("application") && "ejprop".equalsIgnoreCase(file.getFileExtension());
    }

    private boolean isRefFormFile(IFile file)
    {
        String fileExtension = file.getFileExtension();
        return EJPluginConstants.REFERENCED_BLOCK_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(fileExtension)
                || EJPluginConstants.REFERENCED_LOVDEF_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(fileExtension)
                || EJPluginConstants.OBJECT_GROUP_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(fileExtension);
    }

    static void buildPropertiesConstant(IJavaProject project, EJPluginEntireJProperties entireJProperties, IFile file, IProgressMonitor monitor)
    {
        String propID = "EJ_PROPERTIES";

        try
        {
            IFile javaFile = getPropertiesJavaSource(file, monitor, propID);

            StringBuilder builder = new StringBuilder();

            builder.append("package ");
            builder.append(javaFile.getParent().getProjectRelativePath().toString().replaceFirst("src/", "").replaceAll("/", "."));
            builder.append(";");

            builder.append("\n");
            builder.append("\n");
            builder.append("/* AUTO-GENERATED FILE.  DO NOT MODIFY. \n");
            builder.append("*\n");
            builder.append("* This class was automatically generated by the\n");
            builder.append("* entirej plugin from the EntireJProperties.  It\n");
            builder.append("* should not be modified by hand.\n");
            builder.append(" */");
            builder.append("\n");
            builder.append("public class ");
            builder.append(propID);
            builder.append("\n");
            builder.append("{");
            builder.append("\n");

            EJCoreLayoutContainer layoutContainer = entireJProperties.getLayoutContainer();
            
            List<EJCoreLayoutItem> layoutItems = EntirejPropertiesUtils.findAll(layoutContainer);
           
            
            Set<String> actions = new TreeSet<String>();
            // adding menu id's parameters
            for (EJPluginMenuProperties menuProperties : entireJProperties.getPluginMenuContainer().getAllMenuProperties())
            {
                if (menuProperties.getName() != null && menuProperties.getName().trim().length() > 0)
                {
                    builder.append("    public static final String M_");
                    builder.append(toVAR(menuProperties.getName()).toUpperCase().replaceAll(" ", "_"));
                    builder.append(" = ");
                    builder.append("\"");
                    builder.append(menuProperties.getName());
                    builder.append("\"");
                    builder.append(";");
                    builder.append("\n");
                }
                addActionsFromMenuProperties(menuProperties, actions);
            }
            
            for (EJCoreLayoutItem item : layoutItems)
            {
                if(item.getType()==TYPE.COMPONENT)
                {
                    EJCoreLayoutItem.LayoutComponent component = (LayoutComponent) item;
                    EJFrameworkExtensionProperties rendereProperties = component.getRendereProperties();
                    if(rendereProperties!=null)
                    {
                        //TODO:
                    }
                }
            }
            
            
            
            

            builder.append("\n");
            // adding form parameters
            Collection<EJPluginApplicationParameter> formParameters = entireJProperties.getAllApplicationLevelParameters();
            for (EJPluginApplicationParameter parameter : formParameters)
            {
                if (parameter.getName() != null && parameter.getName().trim().length() > 0)
                {
                    builder.append("    public static final String P_");
                    builder.append(toVAR(parameter.getName()).toUpperCase().replaceAll(" ", "_"));
                    builder.append(" = ");
                    builder.append("\"");
                    builder.append(parameter.getName());
                    builder.append("\"");
                    builder.append(";");
                    builder.append("\n");
                }
            }
            builder.append("\n");

            // adding VA
            Collection<String> visualAttributeNames = entireJProperties.getVisualAttributesContainer().getVisualAttributeNames();
            for (String va : visualAttributeNames)
            {
                if (va != null && va.trim().length() > 0)
                {
                    builder.append("    public static final String VA_");
                    builder.append(toVAR(va).toUpperCase().replaceAll(" ", "_"));
                    builder.append(" = ");
                    builder.append("\"");
                    builder.append(va);
                    builder.append("\"");
                    builder.append(";");
                    builder.append("\n");
                }
            }

            builder.append("\n");
            // adding Actions
            for (String action : actions)
            {
                builder.append("    public static final String AC_");
                builder.append(toVAR(action).toUpperCase().replaceAll(" ", "_"));
                builder.append(" = ");
                builder.append("\"");
                builder.append(action);
                builder.append("\"");
                builder.append(";");
                builder.append("\n");
            }
            
            
            
            {
                
                
                for (EJCoreLayoutItem item : layoutItems)
                {
                    if(item.getType()==TYPE.TAB)
                    {
                        builder.append("    public static final String TAB_");
                        builder.append(toVAR(item.getName()).toUpperCase().replaceAll(" ", "_"));
                        builder.append(" = ");
                        builder.append("\"");
                        builder.append(item.getName());
                        builder.append("\"");
                        builder.append(";");
                        builder.append("\n");
                    }
                }
                
                
                
                for (EJCoreLayoutItem item : layoutItems)
                {
                    if(item.getType()==TYPE.TAB)
                    {
                        builder.append("\n");
                        builder.append("public static class ");
                        builder.append("TAB_");
                        builder.append(toVAR(item.getName()).toUpperCase().replaceAll(" ", "_"));
                        builder.append("_PAGES");
                        builder.append("\n");
                        builder.append("{");
                        builder.append("\n");
                        EJCoreLayoutItem.TabGroup tabGroup = (TabGroup) item; 
                        for (EJCoreLayoutItem page : tabGroup.getItems())
                        {
                            if (page.getName() != null && page.getName().length() > 0)
                            {
                                builder.append("    public static final String ");
                                builder.append(toVAR(page.getName()).toUpperCase().replaceAll(" ", "_"));
                                builder.append(" = ");
                                builder.append("\"");
                                builder.append(page.getName());
                                builder.append("\"");
                                builder.append(";");
                                builder.append("\n");
                            }
                        }
                        builder.append("\n");
                        builder.append("}");
                        builder.append("\n");
                    }
                }
                
                
                
               
                
            }
            

            builder.append("\n");
            builder.append("}");
            String classContent = builder.toString();
            CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(project.getOptions(true));
            IDocument doc = new Document(classContent);
            TextEdit edit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT, doc.get(), 0, doc.get().length(), 0, null);
            if (edit != null)
            {
                edit.apply(doc);
                classContent = doc.get();
            }
            if (javaFile.exists())
            {
                try
                {
                    if (classContent.equals(getStringFromInputStream(javaFile.getContents(true))))
                        return;
                }
                catch (Exception e)
                {
                    // ignore
                }

                javaFile.setContents(new ByteArrayInputStream(classContent.toString().getBytes("UTF-8")), IResource.FORCE, monitor);
            }
            else
            {
                javaFile.create(new ByteArrayInputStream(classContent.toString().getBytes("UTF-8")), IResource.FORCE, monitor);
            }

        }
        catch (Exception e)
        {
            EJCoreLog.logException(e);
        }
    }

    static void buildFormConstant(IJavaProject project, EJPluginFormProperties formProperties, IFile file, IProgressMonitor monitor)
    {
        String formID = getFormId(formProperties);

        try
        {
            IFile javaFile = getFormJavaSource(file, monitor, formID);

            StringBuilder builder = new StringBuilder();

            builder.append("package ");
            builder.append(javaFile.getParent().getProjectRelativePath().toString().replaceFirst("src/", "").replaceAll("/", "."));
            builder.append(";");

            builder.append("\n");
            builder.append("\n");
            builder.append("/* AUTO-GENERATED FILE.  DO NOT MODIFY. \n");
            builder.append("*\n");
            builder.append("* This class was automatically generated by the\n");
            builder.append("* entirej plugin from the form.  It\n");
            builder.append("* should not be modified by hand.\n");
            builder.append(" */");
            builder.append("\n");
            builder.append("public class ");
            builder.append(formID);
            builder.append("\n");
            builder.append("{");
            builder.append("\n");

            // add Form ID
            builder.append("    public static final String ID = ");
            builder.append("\"");
            builder.append(formProperties.getFormName());
            builder.append("\"");
            builder.append(";");
            Set<String> actions = new TreeSet<String>();

            // process Form renderer Properties
            String renderer = formProperties.getFormRendererName();
            if (renderer != null && renderer.trim().length() > 0)
            {
                EJRendererAssignment assignment = formProperties.getEntireJProperties().getApplicationAssignedFormRenderer(renderer);
                if (assignment != null)
                {
                    EJDevFormRendererDefinition rendererDefinition = ExtensionsPropertiesFactory.loadFormRendererDefinition(
                            formProperties.getEntireJProperties(), formProperties.getFormRendererName());
                    EJFrameworkExtensionProperties rendererProperties = formProperties.getFormRendererProperties();
                    if (rendererDefinition != null && rendererProperties != null)
                    {

                        addActionsFromRendererProperties(formProperties, null, rendererDefinition.getFormPropertyDefinitionGroup(), rendererProperties, actions);

                    }
                }
            }
            // /

            // build Block
            List<EJPluginBlockProperties> allBlockProperties = formProperties.getBlockContainer().getAllBlockProperties();
            for (EJPluginBlockProperties blockProp : allBlockProperties)
            {
                if (blockProp.getName() != null && blockProp.getName().length() > 0)
                {
                    createBlockCode(blockProp, builder);

                    EJDevBlockRendererDefinition rendererDefinition = blockProp.getBlockRendererDefinition();
                    EJFrameworkExtensionProperties rendererProperties = blockProp.getBlockRendererProperties();
                    if (rendererDefinition != null && rendererProperties != null)
                    {

                        addActionsFromRendererProperties(formProperties, blockProp, rendererDefinition.getBlockPropertyDefinitionGroup(), rendererProperties,
                                actions);
                        List<EJPluginItemGroupProperties> itemGroups = blockProp.getMainScreenItemGroupDisplayContainer().getItemGroups();

                        EJPropertyDefinitionGroup propertyDefinitionGroup = rendererDefinition.getItemPropertiesDefinitionGroup();

                        for (EJPluginItemGroupProperties groupProperties : itemGroups)
                        {

                            addActionsFromItemGroupProperties(formProperties, rendererDefinition.getItemGroupPropertiesDefinitionGroup(),
                                    propertyDefinitionGroup, groupProperties, actions);
                        }

                    }
                    rendererDefinition = null;

                    if (blockProp.isInsertAllowed())
                    {
                        EJDevInsertScreenRendererDefinition insertRendererDefinition = blockProp.getInsertScreenRendererDefinition();
                        rendererProperties = blockProp.getInsertScreenRendererProperties();
                        if (insertRendererDefinition != null && rendererProperties != null)
                        {

                            addActionsFromRendererProperties(formProperties, blockProp, insertRendererDefinition.getInsertScreenPropertyDefinitionGroup(),
                                    rendererProperties, actions);
                            List<EJPluginItemGroupProperties> itemGroups = blockProp.getInsertScreenItemGroupDisplayContainer().getItemGroups();

                            EJPropertyDefinitionGroup propertyDefinitionGroup = insertRendererDefinition.getItemPropertyDefinitionGroup();

                            for (EJPluginItemGroupProperties groupProperties : itemGroups)
                            {
                                addActionsFromItemGroupProperties(formProperties, insertRendererDefinition.getItemGroupPropertiesDefinitionGroup(),
                                        propertyDefinitionGroup, groupProperties, actions);
                            }

                        }
                        insertRendererDefinition = null;

                    }

                    if (blockProp.isUpdateAllowed())
                    {
                        EJDevUpdateScreenRendererDefinition updateRendererDefinition = blockProp.getUpdateScreenRendererDefinition();
                        rendererProperties = blockProp.getUpdateScreenRendererProperties();
                        if (updateRendererDefinition != null && rendererProperties != null)
                        {

                            addActionsFromRendererProperties(formProperties, blockProp, updateRendererDefinition.getUpdateScreenPropertyDefinitionGroup(),
                                    rendererProperties, actions);
                            List<EJPluginItemGroupProperties> itemGroups = blockProp.getUpdateScreenItemGroupDisplayContainer().getItemGroups();

                            EJPropertyDefinitionGroup propertyDefinitionGroup = updateRendererDefinition.getItemPropertyDefinitionGroup();

                            for (EJPluginItemGroupProperties groupProperties : itemGroups)
                            {
                                addActionsFromItemGroupProperties(formProperties, updateRendererDefinition.getItemGroupPropertiesDefinitionGroup(),
                                        propertyDefinitionGroup, groupProperties, actions);
                            }
                        }
                        updateRendererDefinition = null;
                    }

                    if (blockProp.isQueryAllowed())
                    {
                        EJDevQueryScreenRendererDefinition queryRendererDefinition = blockProp.getQueryScreenRendererDefinition();
                        rendererProperties = blockProp.getQueryScreenRendererProperties();
                        if (queryRendererDefinition != null && rendererProperties != null)
                        {

                            addActionsFromRendererProperties(formProperties, blockProp, queryRendererDefinition.getQueryScreenPropertyDefinitionGroup(),
                                    rendererProperties, actions);
                            List<EJPluginItemGroupProperties> itemGroups = blockProp.getQueryScreenItemGroupDisplayContainer().getItemGroups();

                            EJPropertyDefinitionGroup propertyDefinitionGroup = queryRendererDefinition.getItemPropertyDefinitionGroup();

                            for (EJPluginItemGroupProperties groupProperties : itemGroups)
                            {
                                addActionsFromItemGroupProperties(formProperties, queryRendererDefinition.getItemGroupPropertiesDefinitionGroup(),
                                        propertyDefinitionGroup, groupProperties, actions);
                            }
                        }
                        queryRendererDefinition = null;
                    }

                }
                List<EJPluginBlockItemProperties> allItemProperties = blockProp.getItemContainer().getAllItemProperties();
                for (EJPluginBlockItemProperties itemProp : allItemProperties)
                {
                    String itemRenderer = itemProp.getItemRendererName();
                    if (itemRenderer == null || itemRenderer.trim().length() == 0)
                    {
                        continue;
                    }

                    EJRendererAssignment assignment = formProperties.getEntireJProperties().getApplicationAssignedItemRenderer(itemRenderer);
                    if (assignment == null)
                    {
                        continue;
                    }

                    EJDevItemRendererDefinition rendererDefinition = itemProp.getItemRendererDefinition();
                    EJFrameworkExtensionProperties rendererProperties = itemProp.getItemRendererProperties();
                    if (rendererDefinition != null && rendererProperties != null)
                    {

                        addActionsFromRendererProperties(formProperties, blockProp, rendererDefinition.getItemPropertyDefinitionGroup(), rendererProperties,
                                actions);
                    }
                }
                for (EJScreenType screenType : EJScreenType.values())
                {
                    for (String itemName : blockProp.getScreenItemNames(screenType))
                    {
                        EJScreenItemProperties itemProperties = blockProp.getScreenItemProperties(screenType, itemName);
                        if (itemProperties != null && itemProperties.getActionCommand() != null && itemProperties.getActionCommand().trim().length() > 0)
                        {
                            actions.add(itemProperties.getActionCommand());
                        }
                    }
                }

            }

            // build LOV
            // build Block
            List<EJPluginLovDefinitionProperties> lovDefinitionProperties = formProperties.getLovDefinitionContainer().getAllLovDefinitionProperties();
            for (EJPluginLovDefinitionProperties definitionProperties : lovDefinitionProperties)
            {
                if (definitionProperties.getName() != null && definitionProperties.getName().length() > 0)
                {
                    createLovCode(definitionProperties, builder);
                }
            }

            // read canvas
            Collection<EJCanvasProperties> allCanvasProperties = EJPluginCanvasRetriever.retriveAllCanvases(formProperties);
            for (EJCanvasProperties canvasProperties : allCanvasProperties)
            {
                if (canvasProperties.getName() != null && canvasProperties.getName().length() > 0)
                {
                    builder.append("    public static final String C_");
                    builder.append(toVAR(canvasProperties.getName()).toUpperCase().replaceAll(" ", "_"));
                    builder.append(" = ");
                    builder.append("\"");
                    builder.append(canvasProperties.getName());
                    builder.append("\"");
                    builder.append(";");
                    builder.append("\n");

                    switch (canvasProperties.getType())
                    {
                        case TAB:
                            Collection<EJTabPageProperties> allTabPageProperties = canvasProperties.getTabPageContainer().getAllTabPageProperties();

                            builder.append("\n");
                            builder.append("public static class ");
                            builder.append("C_");
                            builder.append(toVAR(canvasProperties.getName()).toUpperCase().replaceAll(" ", "_"));
                            builder.append("_PAGES");
                            builder.append("\n");
                            builder.append("{");
                            builder.append("\n");

                            for (EJTabPageProperties page : allTabPageProperties)
                            {
                                if (page.getName() != null && page.getName().length() > 0)
                                {
                                    builder.append("    public static final String ");
                                    builder.append(toVAR(page.getName()).toUpperCase().replaceAll(" ", "_"));
                                    builder.append(" = ");
                                    builder.append("\"");
                                    builder.append(page.getName());
                                    builder.append("\"");
                                    builder.append(";");
                                    builder.append("\n");
                                }
                            }
                            builder.append("\n");
                            builder.append("}");
                            builder.append("\n");
                            break;
                        case DRAWER:
                            Collection<EJDrawerPageProperties> allDrawerPageProperties = canvasProperties.getDrawerPageContainer().getAllDrawerPageProperties();
                            
                            builder.append("\n");
                            builder.append("public static class ");
                            builder.append("C_");
                            builder.append(toVAR(canvasProperties.getName()).toUpperCase().replaceAll(" ", "_"));
                            builder.append("_PAGES");
                            builder.append("\n");
                            builder.append("{");
                            builder.append("\n");
                            
                            for (EJDrawerPageProperties page : allDrawerPageProperties)
                            {
                                if (page.getName() != null && page.getName().length() > 0)
                                {
                                    builder.append("    public static final String ");
                                    builder.append(toVAR(page.getName()).toUpperCase().replaceAll(" ", "_"));
                                    builder.append(" = ");
                                    builder.append("\"");
                                    builder.append(page.getName());
                                    builder.append("\"");
                                    builder.append(";");
                                    builder.append("\n");
                                }
                            }
                            builder.append("\n");
                            builder.append("}");
                            builder.append("\n");
                            break;
                        case STACKED:
                            Collection<EJStackedPageProperties> allStackedPageProperties = canvasProperties.getStackedPageContainer()
                                    .getAllStackedPageProperties();
                            builder.append("\n");
                            builder.append("public static class ");
                            builder.append("C_");
                            builder.append(toVAR(canvasProperties.getName()).toUpperCase().replaceAll(" ", "_"));
                            builder.append("_PAGES");
                            builder.append("\n");
                            builder.append("{");
                            builder.append("\n");
                            for (EJStackedPageProperties page : allStackedPageProperties)
                            {
                                if (page.getName() != null && page.getName().length() > 0)
                                {
                                    builder.append("    public static final String ");
                                    builder.append(toVAR(page.getName()).toUpperCase().replaceAll(" ", "_"));
                                    builder.append(" = ");
                                    builder.append("\"");
                                    builder.append(page.getName());
                                    builder.append("\"");
                                    builder.append(";");
                                    builder.append("\n");
                                }
                            }
                            builder.append("\n");
                            builder.append("}");
                            builder.append("\n");
                            break;
                        default:
                            break;
                    }
                }
            }
            builder.append("\n");
            // adding Actions
            for (String action : actions)
            {
                builder.append("    public static final String AC_");
                builder.append(toVAR(action).toUpperCase().replaceAll(" ", "_"));
                builder.append(" = ");
                builder.append("\"");
                builder.append(action);
                builder.append("\"");
                builder.append(";");
                builder.append("\n");
            }

            builder.append("\n");
            // adding form parameters
            Collection<EJPluginApplicationParameter> formParameters = formProperties.getAllFormParameters();
            for (EJPluginApplicationParameter parameter : formParameters)
            {
                if (parameter.getName() != null && parameter.getName().trim().length() > 0)
                {
                    builder.append("    public static final String P_");
                    builder.append(toVAR(parameter.getName()).toUpperCase().replaceAll(" ", "_"));
                    builder.append(" = ");
                    builder.append("\"");
                    builder.append(parameter.getName());
                    builder.append("\"");
                    builder.append(";");
                    builder.append("\n");
                }
            }
            builder.append("\n");
            builder.append("}");
            String classContent = builder.toString();
            CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(project.getOptions(true));
            IDocument doc = new Document(classContent);
            TextEdit edit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT, doc.get(), 0, doc.get().length(), 0, null);
            if (edit != null)
            {
                edit.apply(doc);
                classContent = doc.get();
            }
            if (javaFile.exists())
            {
                try
                {
                    if (classContent.equals(getStringFromInputStream(javaFile.getContents(true))))
                        return;
                }
                catch (Exception e)
                {
                    // ignore
                }

                javaFile.setContents(new ByteArrayInputStream(classContent.toString().getBytes("UTF-8")), IResource.FORCE, monitor);
            }
            else
            {
                javaFile.create(new ByteArrayInputStream(classContent.toString().getBytes("UTF-8")), IResource.FORCE, monitor);
            }

        }
        catch (Exception e)
        {
            EJCoreLog.logException(e);
        }
    }

    private static String getStringFromInputStream(InputStream is)
    {

        final char[] buffer = new char[1024];
        final StringBuilder out = new StringBuilder();
        try
        {
            final Reader in = new InputStreamReader(is, "UTF-8");
            try
            {
                for (;;)
                {
                    int rsz = in.read(buffer, 0, buffer.length);
                    if (rsz < 0)
                        break;
                    out.append(buffer, 0, rsz);
                }
            }
            finally
            {
                in.close();
            }
        }
        catch (UnsupportedEncodingException ex)
        {
            /* ... */
        }
        catch (IOException ex)
        {
            /* ... */
        }
        return out.toString();

    }

    public static IFile getFormJavaSource(IFile file, IProgressMonitor monitor, String formID) throws CoreException
    {
        IFolder pkgPath = file.getParent().getFolder(new Path(CONSTANTS_PATH));
        if (!pkgPath.exists())
        {
            pkgPath.create(true, true, monitor);
        }
        IFile javaFile = pkgPath.getFile(formID + ".java");
        return javaFile;
    }

    public static IFile getPropertiesJavaSource(IFile file, IProgressMonitor monitor, String fileID) throws CoreException
    {
        IFolder pkgPath = file.getParent().getFolder(new Path("org/entirej/constants"));
        if (!pkgPath.exists())
        {
            pkgPath.create(true, true, monitor);
        }
        IFile javaFile = pkgPath.getFile(fileID + ".java");
        return javaFile;
    }

    public static String getFormId(String name)
    {
        return getFormPrifix() + toVAR(name).toUpperCase().replaceAll(" ", "_");
    }

    public static String getObjectGroupId(String name)
    {
        return OBJECTGROUP_PREFIX + toVAR(name).toUpperCase().replaceAll(" ", "_");
    }

    public static String getRefBlockId(String name)
    {
        return REFBLOCK_PREFIX + toVAR(name).toUpperCase().replaceAll(" ", "_");
    }

    public static String getLovId(String name)
    {
        return LOV_PREFIX + toVAR(name).toUpperCase().replaceAll(" ", "_");
    }

    public static String getFormId(EJPluginFormProperties formProperties)
    {
        return getFormPrifix(formProperties) + toVAR(formProperties.getFormName()).toUpperCase().replaceAll(" ", "_");
    }

    private static String getFormPrifix()
    {
        return FORM_PREFIX;
    }

    private static String getFormPrifix(EJPluginFormProperties formProperties)
    {
        if (formProperties.isObjectGroupForm())
        {
            return OBJECTGROUP_PREFIX;
        }
        else if (formProperties.isReusableBlockForm())
        {
            return REFBLOCK_PREFIX;
        }
        else if (formProperties.isReusableLovForm())
        {
            return LOV_PREFIX;
        }

        return FORM_PREFIX;
    }

    private static void createBlockCode(EJPluginBlockProperties blockProperties, StringBuilder builder)
    {
        builder.append("\n");
        builder.append("public static class ");
        builder.append("B_");
        builder.append(toVAR(blockProperties.getName()).toUpperCase().replaceAll(" ", "_"));
        builder.append("\n");
        builder.append("{");
        builder.append("\n");

        // add block ID
        builder.append("    public static final String ID = ");
        builder.append("\"");
        builder.append(blockProperties.getName());
        builder.append("\"");
        builder.append(";");
        builder.append("\n");

        // read item names
        Collection<EJPluginBlockItemProperties> itemProperties = blockProperties.getItemContainer().getAllItemProperties();
        for (EJPluginBlockItemProperties item : itemProperties)
        {
            if (item.getName() != null && item.getName().length() > 0)
            {
                // add block ID
                builder.append("    public static final String I_");
                builder.append(toVAR(item.getName()).toUpperCase().replaceAll(" ", "_"));
                builder.append(" = ");
                builder.append("\"");
                builder.append(item.getName());
                builder.append("\"");
                builder.append(";");
                builder.append("\n");
            }
        }
        
        List<EJPluginLovMappingProperties> allLovMappingProperties = blockProperties.getLovMappingContainer().getAllLovMappingProperties();
        for (EJPluginLovMappingProperties ejPluginLovMappingProperties : allLovMappingProperties)
        {
         // add mapping ID
            builder.append("    public static final String LM_");
            builder.append(toVAR(ejPluginLovMappingProperties.getName()).toUpperCase().replaceAll(" ", "_"));
            builder.append(" = ");
            builder.append("\"");
            builder.append(ejPluginLovMappingProperties.getName());
            builder.append("\"");
            builder.append(";");
            builder.append("\n");
        }

        builder.append("\n");
        builder.append("}");
        builder.append("\n");
    }

    private static void createLovCode(EJPluginLovDefinitionProperties blockProperties, StringBuilder builder)
    {
        builder.append("\n");
        builder.append("public static class ");
        builder.append("L_");
        builder.append(toVAR(blockProperties.getName()).toUpperCase().replaceAll(" ", "_"));
        builder.append("\n");
        builder.append("{");
        builder.append("\n");

        // add block ID
        builder.append("    public static final String ID = ");
        builder.append("\"");
        builder.append(blockProperties.getName());
        builder.append("\"");
        builder.append(";");
        builder.append("\n");

        if (blockProperties.getBlockProperties() != null)
        {
            // read item names
            Collection<EJPluginBlockItemProperties> itemProperties = blockProperties.getBlockProperties().getItemContainer().getAllItemProperties();
            for (EJPluginBlockItemProperties item : itemProperties)
            {
                if (item.getName() != null && item.getName().length() > 0)
                {
                    // add block ID
                    builder.append("    public static final String I_");
                    builder.append(toVAR(item.getName()).toUpperCase().replaceAll(" ", "_"));
                    builder.append(" = ");
                    builder.append("\"");
                    builder.append(item.getName());
                    builder.append("\"");
                    builder.append(";");
                    builder.append("\n");
                }
            }
        }

        builder.append("\n");
        builder.append("}");
        builder.append("\n");
    }

    static String toVAR(String item)
    {
        StringBuilder nameBuild = new StringBuilder();

        for (char c : item.toCharArray())
        {
            if (!Character.isJavaIdentifierPart(c))
            {
                nameBuild.append("_");
            }
            else
            {
                nameBuild.append(c);
            }
        }
        item = nameBuild.toString();

        StringBuilder builder = new StringBuilder();
        char[] charArray = item.toCharArray();
        boolean onStart = true;
        boolean ignoreUN = false;
        for (char c : charArray)
        {

            if (onStart)
            {
                onStart = false;
                builder.append(c);
            }
            else
            {

                if (!ignoreUN && (Character.isUpperCase(c) || Character.isDigit(c)))
                {
                    builder.append("_");

                }
                builder.append(c);
            }

            ignoreUN = '_' == c || Character.isUpperCase(c) || Character.isDigit(c);

        }
        return builder.toString().trim();
    }

    EJPluginFormProperties getFormProperties(IFile file, IJavaProject project)
    {

        EJPluginFormProperties formProperties = null;

        // read from file
        InputStream inStream = null;
        try
        {

            inStream = file.getContents();

            EntireJFormReader reader = new EntireJFormReader();
            String fileName = file.getName();
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
            formProperties = reader.readForm(new FormHandler(project, fileName), project,file, inStream);
            formProperties.initialisationCompleted();

            String fileExtension = file.getFileExtension();
            if (fileExtension.equalsIgnoreCase(EJPluginConstants.OBJECT_GROUP_PROPERTIES_FILE_SUFFIX))
            {
                formProperties.setIsObjectGroupForm(true);
            }
            else if (fileExtension.equalsIgnoreCase(EJPluginConstants.REFERENCED_LOVDEF_PROPERTIES_FILE_SUFFIX))
            {
                formProperties.setIsReusableLovForm(true);
            }
            else if (fileExtension.equalsIgnoreCase(EJPluginConstants.REFERENCED_BLOCK_PROPERTIES_FILE_SUFFIX))
            {
                formProperties.setIsReusableBlockForm(true);
            }

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

        return formProperties;
    }

    // renderer action process
    static void addActionsFromRendererProperties(EJPluginFormProperties formProperties, EJPluginBlockProperties blockProperties,
            EJPropertyDefinitionGroup definitionGroup, EJFrameworkExtensionProperties rendererProperties, Set<String> actions)
    {
        if (definitionGroup != null && rendererProperties != null)
        {
            addActionsFromPropertyDefinitionGroup(formProperties, blockProperties, rendererProperties, definitionGroup, actions);
        }
    }

    // menu action process
    static void addActionsFromMenuProperties(EJPluginMenuProperties menuProperties, Set<String> actions)
    {
        List<EJPluginMenuLeafProperties> leaves = new ArrayList<EJPluginMenuLeafProperties>(menuProperties.getLeaves());
        for (EJPluginMenuLeafProperties leafProperties : leaves)
        {
            if (leafProperties instanceof EJPluginMenuLeafBranchProperties)
            {
                addActionsFromMenuProperties((EJPluginMenuLeafBranchProperties) leafProperties, actions);
            }
            else if (leafProperties instanceof EJPluginMenuLeafSpacerProperties)
            {
                continue;
            }
            else if (leafProperties instanceof EJPluginMenuLeafActionProperties)
            {
                actions.add(((EJPluginMenuLeafActionProperties) leafProperties).getMenuAction());
            }
            else if (leafProperties instanceof EJPluginMenuLeafFormProperties)
            {
                continue;
            }
        }
    }

    static void addActionsFromMenuProperties(EJPluginMenuLeafBranchProperties menuProperties, Set<String> actions)
    {
        List<EJPluginMenuLeafProperties> leaves = new ArrayList<EJPluginMenuLeafProperties>(menuProperties.getLeaves());
        for (EJPluginMenuLeafProperties leafProperties : leaves)
        {
            if (leafProperties instanceof EJPluginMenuLeafBranchProperties)
            {
                addActionsFromMenuProperties((EJPluginMenuLeafBranchProperties) leafProperties, actions);
            }
            else if (leafProperties instanceof EJPluginMenuLeafSpacerProperties)
            {
                continue;
            }
            else if (leafProperties instanceof EJPluginMenuLeafActionProperties)
            {
                actions.add(((EJPluginMenuLeafActionProperties) leafProperties).getMenuAction());
            }
            else if (leafProperties instanceof EJPluginMenuLeafFormProperties)
            {
                continue;
            }
        }
    }

    static void addActionsFromPropertyDefinitionGroup(EJPluginFormProperties formProperties, EJPluginBlockProperties blockProperties,
            EJFrameworkExtensionProperties rendererProperties, EJPropertyDefinitionGroup definitionGroup, Set<String> actions)
    {
        if (definitionGroup == null || rendererProperties == null)
            return;

        Collection<EJPropertyDefinition> propertyDefinitions = definitionGroup.getPropertyDefinitions();
        for (EJPropertyDefinition definition : propertyDefinitions)
        {
            addActionsFromPropertyDefinition(formProperties, blockProperties, rendererProperties, definitionGroup, definition, actions);
        }
        Collection<EJPropertyDefinitionList> propertyDefinitionLists = definitionGroup.getPropertyDefinitionLists();
        for (final EJPropertyDefinitionList definitionList : propertyDefinitionLists)
        {
            if (definitionList == null || definitionList.getName() == null)
                continue;
            EJFrameworkExtensionPropertyList propertyList = rendererProperties.getPropertyList(definitionList.getName());

            if (propertyList == null)
                continue;
            List<EJFrameworkExtensionPropertyListEntry> allListEntries = new ArrayList<EJFrameworkExtensionPropertyListEntry>(propertyList.getAllListEntries());

            Collection<EJPropertyDefinition> listDefinitions = definitionList.getPropertyDefinitions();
            for (EJPropertyDefinition definition : listDefinitions)
            {
                final EJPropertyDefinitionType dataType = definition.getPropertyType();
                if (dataType == EJPropertyDefinitionType.ACTION_COMMAND)
                {
                    for (EJFrameworkExtensionPropertyListEntry entry : allListEntries)
                    {
                        String strValue = entry.getProperty(definition.getName());
                        boolean vlaueNull = (strValue == null || strValue.trim().length() == 0);

                        if (vlaueNull)
                            continue;
                        actions.add(strValue);
                    }

                }
            }
        }
        // handle sub groups
        Collection<EJPropertyDefinitionGroup> subGroups = definitionGroup.getSubGroups();
        for (final EJPropertyDefinitionGroup subGroup : subGroups)
        {
            addActionsFromPropertyDefinitionGroup(formProperties, blockProperties, rendererProperties.getPropertyGroup(subGroup.getName()), subGroup, actions);
        }

    }

    static void addActionsFromPropertyDefinition(EJPluginFormProperties formProperties, EJPluginBlockProperties blockProperties,
            EJFrameworkExtensionProperties rendererProperties, EJPropertyDefinitionGroup definitionGroup, EJPropertyDefinition definition, Set<String> actions)
    {

        final EJPropertyDefinitionType dataType = definition.getPropertyType();
        if (dataType == EJPropertyDefinitionType.ACTION_COMMAND)
        {

            String strValue = rendererProperties.getStringProperty(definition.getName());
            boolean vlaueNull = (strValue == null || strValue.trim().length() == 0);

            if (vlaueNull)
                return;
            actions.add(strValue);
        }

    }

    static void addActionsFromItemGroupProperties(EJPluginFormProperties formProperties, EJPropertyDefinitionGroup definitionGroup,
            EJPropertyDefinitionGroup itemDefinitionGroup, EJPluginItemGroupProperties groupProperties, Set<String> actions)
    {
        if (itemDefinitionGroup != null && groupProperties != null)
        {
            if (definitionGroup != null)
            {
                addActionsFromRendererProperties(formProperties, groupProperties.getBlockProperties(), definitionGroup,
                        groupProperties.getRendererProperties(), actions);
            }
            Collection<EJPluginScreenItemProperties> allItemDisplayProperties = groupProperties.getAllResequencableItemProperties();
            for (EJPluginScreenItemProperties itemDisplayProperties : allItemDisplayProperties)
            {
                if (itemDisplayProperties.isSpacerItem())
                    continue;

                EJFrameworkExtensionProperties rendererRequiredProperties = null;
                if ((!(itemDisplayProperties instanceof EJPluginMainScreenItemProperties))
                        || !itemDisplayProperties.getBlockProperties().isUsedInLovDefinition())
                {
                    rendererRequiredProperties = itemDisplayProperties.getBlockRendererRequiredProperties();
                }
                else
                {
                    rendererRequiredProperties = ((EJPluginMainScreenItemProperties) itemDisplayProperties).getLovRendererRequiredProperties();
                }

                addActionsFromRendererProperties(formProperties, groupProperties.getBlockProperties(), itemDefinitionGroup, rendererRequiredProperties, actions);
            }

            Collection<EJPluginItemGroupProperties> allItemGroupDisplayProperties = groupProperties.getChildItemGroupContainer().getItemGroups();
            for (EJPluginItemGroupProperties subGroupProperties : allItemGroupDisplayProperties)
            {
                addActionsFromItemGroupProperties(formProperties, definitionGroup, itemDefinitionGroup, subGroupProperties, actions);
            }
        }

    }

}
