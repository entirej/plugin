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
import org.entirej.ide.core.EJCoreLog;

public class ReportsUtil
{
    private ReportsUtil()
    {
        throw new AssertionError();
    }

    public static boolean isReportExist(IJavaProject project, String id)
    {

        IPackageFragmentRoot[] packageFragmentRoots;
        try
        {
            packageFragmentRoots = project.getPackageFragmentRoots();

            for (IPackageFragmentRoot iPackageFragmentRoot : packageFragmentRoots)
            {
                if (iPackageFragmentRoot.getResource() instanceof IContainer)
                    if (handelIsReportIn((IContainer) iPackageFragmentRoot.getResource(), id, true))
                        return true;
            }

        }
        catch (CoreException e)
        {
            EJCoreLog.log(e);
        }
        return false;
    }
  
   


    private static boolean handelIsReportIn(IContainer container, String name, boolean formsonly) throws CoreException
    {

        IResource[] members = container.members();
        for (int i = 0; i < members.length; i++)
        {
            final IResource member = members[i];
            if (member instanceof IContainer)
            {
                if (handelIsReportIn((IContainer) member, name, formsonly))
                    return true;
            }
                else if (member instanceof IFile && isReportFile((IFile) member))
                {

                    if (getReportName((IFile) member).equals(name))
                        return true;
                }
        }

        return false;
    }
 
    

    public static List<String> getReportNames(IJavaProject project)
    {
        List<String> result = new ArrayList<String>();
        IPackageFragmentRoot[] packageFragmentRoots;
        try
        {
            packageFragmentRoots = project.getPackageFragmentRoots();

            for (IPackageFragmentRoot iPackageFragmentRoot : packageFragmentRoots)
            {
                if (iPackageFragmentRoot.getResource() instanceof IContainer)
                    handelReportIn((IContainer) iPackageFragmentRoot.getResource(), result);
            }

        }
        catch (CoreException e)
        {
            EJCoreLog.log(e);
        }
        return result;
    }

    

    private static void handelReportIn(IContainer container, List<String> result) throws CoreException
    {

        IResource[] members = container.members();
        for (int i = 0; i < members.length; i++)
        {
            final IResource member = members[i];
            if (member instanceof IContainer)
                handelReportIn((IContainer) member, result);
            else if (member instanceof IFile && ( isReportFile((IFile) member)  ))
            {

                result.add(getReportName((IFile) member));
            }
        }
    }

    private static boolean isReportFile(IFile file)
    {
        return "ejreport".equalsIgnoreCase(file.getFileExtension());
    }


    private static String getReportName(IFile file)
    {
        String fileName = file.getName();
        int lastIndexOf = fileName.lastIndexOf(".");
        if (lastIndexOf > 0)
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
        return fileName;
    }
}
