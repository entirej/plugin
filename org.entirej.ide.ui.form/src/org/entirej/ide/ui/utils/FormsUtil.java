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
package org.entirej.ide.ui.utils;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.entirej.framework.dev.EJDevConstants;
import org.entirej.framework.plugin.framework.properties.reader.EntireJFormReader;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.ui.EJUIPlugin;

public class FormsUtil
{
    private FormsUtil()
    {
        throw new AssertionError();
    }

    public static boolean isFormExist(IJavaProject project, String id)
    {

        IPackageFragmentRoot[] packageFragmentRoots;
        try
        {
            packageFragmentRoots = project.getPackageFragmentRoots();

            for (IPackageFragmentRoot iPackageFragmentRoot : packageFragmentRoots)
            {
                if (iPackageFragmentRoot.getResource() instanceof IContainer)
                    if (handelIsFormIn((IContainer) iPackageFragmentRoot.getResource(), id, true))
                        return true;
            }

        }
        catch (CoreException e)
        {
            EJCoreLog.log(e);
        }
        return false;
    }
    public static boolean openForm(IJavaProject project, String id)
    {
        
        IPackageFragmentRoot[] packageFragmentRoots;
        try
        {
            packageFragmentRoots = project.getPackageFragmentRoots();
            
            for (IPackageFragmentRoot iPackageFragmentRoot : packageFragmentRoots)
            {
                if (iPackageFragmentRoot.getResource() instanceof IContainer)
                    if (handelOpenFormIn((IContainer) iPackageFragmentRoot.getResource(), id, true))
                    {
                        
                        return true;
                    }
            }
            
        }
        catch (CoreException e)
        {
            EJCoreLog.log(e);
        }
        return false;
    }
    public static boolean openObjectGroupRefrence(IJavaProject project, String id)
    {
        
        IPackageFragmentRoot[] packageFragmentRoots;
        try
        {
            packageFragmentRoots = project.getPackageFragmentRoots();
            
            for (IPackageFragmentRoot iPackageFragmentRoot : packageFragmentRoots)
            {
                if (iPackageFragmentRoot.getResource() instanceof IContainer)
                    if (handelOpenRefresourceIn((IContainer) iPackageFragmentRoot.getResource(), id, EJDevConstants.OBJECT_GROUP_PROPERTIES_FILE_SUFFIX))
                    {
                        
                        return true;
                    }
            }
            
        }
        catch (CoreException e)
        {
            EJCoreLog.log(e);
        }
        return false;
    }
    public static boolean openLovRefrence(IJavaProject project, String id)
    {
        
        IPackageFragmentRoot[] packageFragmentRoots;
        try
        {
            packageFragmentRoots = project.getPackageFragmentRoots();
            
            for (IPackageFragmentRoot iPackageFragmentRoot : packageFragmentRoots)
            {
                if (iPackageFragmentRoot.getResource() instanceof IContainer)
                    if (handelOpenRefresourceIn((IContainer) iPackageFragmentRoot.getResource(), id, EJDevConstants.REFERENCED_LOVDEF_PROPERTIES_FILE_SUFFIX))
                    {
                        
                        return true;
                    }
            }
            
        }
        catch (CoreException e)
        {
            EJCoreLog.log(e);
        }
        return false;
    }
    public static boolean openRefBlockRefrence(IJavaProject project, String id)
    {
        
        IPackageFragmentRoot[] packageFragmentRoots;
        try
        {
            packageFragmentRoots = project.getPackageFragmentRoots();
            
            for (IPackageFragmentRoot iPackageFragmentRoot : packageFragmentRoots)
            {
                if (iPackageFragmentRoot.getResource() instanceof IContainer)
                    if (handelOpenRefresourceIn((IContainer) iPackageFragmentRoot.getResource(), id, EJDevConstants.REFERENCED_BLOCK_PROPERTIES_FILE_SUFFIX))
                    {
                        
                        return true;
                    }
            }
            
        }
        catch (CoreException e)
        {
            EJCoreLog.log(e);
        }
        return false;
    }

    public static String getFormTitle(IJavaProject project, String id)
    {
        // EntireJFormReader.readFormName(file);
        IPackageFragmentRoot[] packageFragmentRoots;
        try
        {
            packageFragmentRoots = project.getPackageFragmentRoots();

            for (IPackageFragmentRoot iPackageFragmentRoot : packageFragmentRoots)
            {
                if (iPackageFragmentRoot.getResource() instanceof IContainer)
                {
                    String title = handelFormTitle((IContainer) iPackageFragmentRoot.getResource(), id, true);
                    if (title != null)
                        return title;
                }
            }

        }
        catch (CoreException e)
        {
            EJCoreLog.log(e);
        }
        return null;
    }

    private static String handelFormTitle(IContainer container, String name, boolean formsonly) throws CoreException
    {

        IResource[] members = container.members();
        for (int i = 0; i < members.length; i++)
        {
            final IResource member = members[i];
            if (member instanceof IContainer)
            {
                String title = handelFormTitle((IContainer) member, name, formsonly);
                if (title != null)
                    return title;
            }
            else if (member instanceof IFile && isFormFile((IFile) member))
            {

                if (getFormName((IFile) member).equals(name))
                    return EntireJFormReader.readFormName((IFile) member, false);
            }
        }

        return null;
    }

    private static boolean handelIsFormIn(IContainer container, String name, boolean formsonly) throws CoreException
    {

        IResource[] members = container.members();
        for (int i = 0; i < members.length; i++)
        {
            final IResource member = members[i];
            if (member instanceof IContainer)
            {
                if (handelIsFormIn((IContainer) member, name, formsonly))
                    return true;
            }
                else if (member instanceof IFile && isFormFile((IFile) member))
                {

                    if (getFormName((IFile) member).equals(name))
                        return true;
                }
        }

        return false;
    }
    private static boolean handelOpenFormIn(IContainer container, String name, boolean formsonly) throws CoreException
    {
        
        IResource[] members = container.members();
        for (int i = 0; i < members.length; i++)
        {
            final IResource member = members[i];
            if (member instanceof IContainer){
                if (handelOpenFormIn((IContainer) member, name, formsonly))
                   return true;
            }
                else if (member instanceof IFile && isFormFile((IFile) member))
                {
                    
                    if (getFormName((IFile) member).equals(name))
                    {
                        BasicNewResourceWizard.selectAndReveal((IFile) member, EJUIPlugin.getActiveWorkbenchWindow());
                        final IWorkbenchPage activePage = EJUIPlugin.getActiveWorkbenchWindow().getActivePage();
                        if (activePage != null)
                        {
                            IDE.openEditor(activePage, (IFile) member, true);
                        }
                        return true;
                    }
                }
        }
        
        return false;
    }
    private static boolean handelOpenRefresourceIn(IContainer container, String name, String fileExtension) throws CoreException
    {
        
        IResource[] members = container.members();
        for (int i = 0; i < members.length; i++)
        {
            final IResource member = members[i];
            if (member instanceof IContainer){
                if (handelOpenRefresourceIn((IContainer) member, name, fileExtension))
                    return true;
            }
            else if (member instanceof IFile && isRefFormFile((IFile) member))
            {
                
                if (((IFile) member).getName().equals(name+"."+fileExtension))
                {
                    BasicNewResourceWizard.selectAndReveal((IFile) member, EJUIPlugin.getActiveWorkbenchWindow());
                    final IWorkbenchPage activePage = EJUIPlugin.getActiveWorkbenchWindow().getActivePage();
                    if (activePage != null)
                    {
                        IDE.openEditor(activePage, (IFile) member, true);
                    }
                    return true;
                }
            }
        }
        
        return false;
    }

    public static List<String> getFormNames(IJavaProject project, boolean formsonly)
    {
        List<String> result = new ArrayList<String>();
        IPackageFragmentRoot[] packageFragmentRoots;
        try
        {
            packageFragmentRoots = project.getPackageFragmentRoots();

            for (IPackageFragmentRoot iPackageFragmentRoot : packageFragmentRoots)
            {
                if (iPackageFragmentRoot.getResource() instanceof IContainer)
                    handelFormsIn((IContainer) iPackageFragmentRoot.getResource(), result, formsonly);
            }

        }
        catch (CoreException e)
        {
            EJCoreLog.log(e);
        }
        return result;
    }

    public static List<String> getFormNames(IJavaProject project)
    {

        return getFormNames(project, true);
    }

    private static void handelFormsIn(IContainer container, List<String> result, boolean formsonly) throws CoreException
    {

        IResource[] members = container.members();
        for (int i = 0; i < members.length; i++)
        {
            final IResource member = members[i];
            if (member instanceof IContainer)
                handelFormsIn((IContainer) member, result, formsonly);
            else if (member instanceof IFile && (formsonly ? isFormFile((IFile) member) : (isFormFile((IFile) member) || isRefFormFile((IFile) member))))
            {

                result.add(getFormName((IFile) member));
            }
        }
    }

    private static boolean isFormFile(IFile file)
    {
        return EJDevConstants.FORM_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(file.getFileExtension());
    }

    private static boolean isRefFormFile(IFile file)
    {
        String fileExtension = file.getFileExtension();
        return EJDevConstants.REFERENCED_BLOCK_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(fileExtension)
                || EJDevConstants.REFERENCED_LOVDEF_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(fileExtension)
                || EJDevConstants.OBJECT_GROUP_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(fileExtension);
    }

    private static String getFormName(IFile file)
    {
        String fileName = file.getName();
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf > 0)
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        return fileName;
    }
}
