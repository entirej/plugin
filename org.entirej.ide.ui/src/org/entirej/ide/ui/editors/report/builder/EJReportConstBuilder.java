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
package org.entirej.ide.ui.editors.report.builder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
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
import org.entirej.framework.plugin.EJPluginConstants;
import org.entirej.framework.plugin.framework.properties.EJPluginApplicationParameter;
import org.entirej.framework.plugin.reports.EJPluginEntireJReportProperties;
import org.entirej.framework.plugin.reports.EJPluginEntireJReportPropertiesLoader;
import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
import org.entirej.framework.plugin.reports.EJPluginReportItemProperties;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.plugin.reports.reader.EntireJReportReader;
import org.entirej.framework.plugin.reports.reader.ReportHandler;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJPluginEntireJClassLoader;
import org.entirej.ide.core.project.EJReportProject;
import org.entirej.ide.ui.utils.FormsUtil;

public class EJReportConstBuilder extends IncrementalProjectBuilder
{

    private static final String REPORT_PREFIX  = "R_";
    private static final String CONSTANTS_PATH = "/constants";

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
                if (isReportFile(candidate))
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

                genPropertiesConstantFile(EJReportProject.getPropertiesFile(p), monitor);
                IPackageFragmentRoot[] packageFragmentRoots = project.getPackageFragmentRoots();

                for (IPackageFragmentRoot iPackageFragmentRoot : packageFragmentRoots)
                {
                    if (iPackageFragmentRoot.getResource() instanceof IContainer)
                        genReportConstantsIn((IContainer) iPackageFragmentRoot.getResource(), monitor);
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
            cleanReportIn(getProject(), formNames, localmonitor);

            localmonitor.worked(1);
        }
        finally
        {
            localmonitor.done();
        }
    }

    private void cleanReportIn(IContainer container, List<String> formNames, IProgressMonitor monitor) throws CoreException
    {
        if (monitor.isCanceled())
        {
            throw new OperationCanceledException();
        }

        IFolder pkgPath = container.getFolder(new Path(CONSTANTS_PATH));
        if (pkgPath.exists())
        {

            IResource[] pkgResources = pkgPath.members();
            for (IResource resource : pkgResources)
            {
                if (resource instanceof IFile)
                {
                    IFile file = (IFile) resource;
                    if (file.exists() && (file.getName().startsWith(REPORT_PREFIX)) && file.getName().endsWith(".java"))
                    {
                        String name = file.getName().substring(0, file.getName().length() - 5);
                        boolean ignore = false;

                        if (name.startsWith(REPORT_PREFIX))
                        {
                            for (String formName : formNames)
                            {

                                if (getReportId(formName).equals((name)))
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
                cleanReportIn((IContainer) member, formNames, monitor);
            }
        }
    }

    private boolean isInterestingProject(IProject project)
    {
        return EJReportProject.hasPluginNature(project);
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
        IFile propFile = EJReportProject.getPropertiesFile(file.getProject());
        if (propFile == null || !propFile.exists())
        {
            return;
        }

        String message = NLS.bind("Constants Generating {0} ...", file.getFullPath().toString());
        monitor.subTask(message);

        SubProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor, 1);

        IProject _project = file.getProject();

        IJavaProject project = JavaCore.create(_project);
        EJPluginReportProperties formProperties = getReportProperties(file, project);
        if (formProperties != null)
            buildReportConstant(project, formProperties, file, subProgressMonitor);

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
        IFile propFile = EJReportProject.getPropertiesFile(file.getProject());
        if (propFile == null || !propFile.exists())
        {
            return;
        }

        String message = NLS.bind("Constants Generating {0} ...", file.getFullPath().toString());
        monitor.subTask(message);

        SubProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor, 1);

        IProject _project = file.getProject();

        IJavaProject project = JavaCore.create(_project);
        EJPluginEntireJReportProperties entireJProperties = EJPluginEntireJReportPropertiesLoader.getEntireJProperties(project);

        if (entireJProperties != null)
            buildPropertiesConstant(project, entireJProperties, file, subProgressMonitor);

        subProgressMonitor.done();
        monitor.subTask("Constants Updating ...");
        monitor.done();
    }

    private void genReportConstantsIn(IContainer container, IProgressMonitor monitor) throws CoreException
    {

        monitor.subTask("Compiling EJ Report Constants...");
        IResource[] members = container.members();
        for (int i = 0; i < members.length; i++)
        {
            IResource member = members[i];
            if (member instanceof IContainer)
                genReportConstantsIn((IContainer) member, monitor);
            else if (member instanceof IFile && isReportFile((IFile) member))
            {
                genConstantFile((IFile) member, monitor);
            }
        }
        monitor.done();
    }

    private boolean isReportFile(IFile file)
    {
        return EJPluginConstants.REPORT_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(file.getFileExtension());
    }

    private boolean isEJProperties(IFile file)
    {
        return file.getName().equals("report") && "ejprop".equalsIgnoreCase(file.getFileExtension());
    }

    static void buildPropertiesConstant(IJavaProject project, EJPluginEntireJReportProperties entireJProperties, IFile file, IProgressMonitor monitor)
    {
        String propID = "EJ_REPORT_PROPERTIES";

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
            builder.append("* entirej plugin from the EntireJReportProperties.  It\n");
            builder.append("* should not be modified by hand.\n");
            builder.append(" */");
            builder.append("\n");
            builder.append("public class ");
            builder.append(propID);
            builder.append("\n");
            builder.append("{");
            builder.append("\n");

            Set<String> actions = new TreeSet<String>();

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

    static void buildReportConstant(IJavaProject project, EJPluginReportProperties formProperties, IFile file, IProgressMonitor monitor)
    {
        String formID = getReportId(formProperties);

        try
        {
            IFile javaFile = getReportJavaSource(file, monitor, formID);

            StringBuilder builder = new StringBuilder();

            builder.append("package ");
            builder.append(javaFile.getParent().getProjectRelativePath().toString().replaceFirst("src/", "").replaceAll("/", "."));
            builder.append(";");

            builder.append("\n");
            builder.append("\n");
            builder.append("/* AUTO-GENERATED FILE.  DO NOT MODIFY. \n");
            builder.append("*\n");
            builder.append("* This class was automatically generated by the\n");
            builder.append("* entirej plugin from the report.  It\n");
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
            builder.append(formProperties.getReportName());
            builder.append("\"");
            builder.append(";");
            Set<String> actions = new TreeSet<String>();

            // build Block
            List<EJPluginReportBlockProperties> allBlockProperties = formProperties.getBlockContainer().getAllBlockProperties();
            for (EJPluginReportBlockProperties blockProp : allBlockProperties)
            {
                if (blockProp.getName() != null && blockProp.getName().length() > 0)
                {
                    createBlockCode(blockProp, builder);

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
            Collection<EJPluginApplicationParameter> formParameters = formProperties.getAllReportParameters();
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

    public static IFile getReportJavaSource(IFile file, IProgressMonitor monitor, String formID) throws CoreException
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

    public static String getReportId(String name)
    {
        return getReportPrifix() + toVAR(name).toUpperCase().replaceAll(" ", "_");
    }

    public static String getReportId(EJPluginReportProperties formProperties)
    {
        return getReportPrifix(formProperties) + toVAR(formProperties.getReportName()).toUpperCase().replaceAll(" ", "_");
    }

    private static String getReportPrifix()
    {
        return REPORT_PREFIX;
    }

    private static String getReportPrifix(EJPluginReportProperties formProperties)
    {

        return REPORT_PREFIX;
    }

    private static void createBlockCode(EJPluginReportBlockProperties blockProperties, StringBuilder builder)
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
        Collection<EJPluginReportItemProperties> itemProperties = blockProperties.getItemContainer().getAllItemProperties();
        for (EJPluginReportItemProperties item : itemProperties)
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

        builder.append("\n");
        builder.append("}");
        builder.append("\n");
    }

    static String toVAR(String item)
    {
        
        
        StringBuilder nameBuild = new StringBuilder();
        
        for (char c : item.toCharArray())
        {
            if( !Character.isJavaIdentifierPart(c))
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

}
