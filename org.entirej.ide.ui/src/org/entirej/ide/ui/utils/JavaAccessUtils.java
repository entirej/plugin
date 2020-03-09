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
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.actions.OpenNewClassWizardAction;
import org.eclipse.jdt.ui.wizards.NewClassWizardPage;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.cf.CFProjectHelper;
import org.entirej.ide.core.cf.lib.DevRuntimeClasspathContainer;

public class JavaAccessUtils
{

    public static boolean isJavaIdentifier(String arg)
    {
        boolean start = true;
        boolean validIdentifier = true;

        for (byte b : arg.getBytes())
        {
            if (start)
            {
                validIdentifier = validIdentifier && Character.isJavaIdentifierStart(b);
                start = false;
            }
            else
            {
                validIdentifier = validIdentifier && Character.isJavaIdentifierPart(b);
            }
        }
        return validIdentifier;
    }

    public static IType selectType(Shell shell, IResource resource, int scope)
    {
        if (resource == null)
            return null;
        IProject project = resource.getProject();
        try
        {
            SelectionDialog dialog = JavaUI.createTypeDialog(shell, PlatformUI.getWorkbench().getProgressService(), getSearchScope(project, false), scope,
                    false, ""); //$NON-NLS-1$
            dialog.setTitle("Select Type");
            if (dialog.open() == Window.OK)
            {
                IType type = (IType) dialog.getResult()[0];
                return type;
            }
        }
        catch (JavaModelException e)
        {
        }
        return null;
    }

    public static IType selectClassType(Shell shell, IResource resource, String filter, String superTypeName)
    {
        return selectType(shell, resource, IJavaElementSearchConstants.CONSIDER_CLASSES, filter, superTypeName);
    }

    public static IType selectType(Shell shell, IResource resource, int scope, String filter, String superTypeName)
    {
        if (resource == null)
            return null;
        IProject project = resource.getProject();

        IJavaProject javaProject = JavaCore.create(project);
        boolean addDevPath = false;
        try
        {
            if (addDevPath = !CFProjectHelper.hasClasspath(javaProject, DevRuntimeClasspathContainer.ID))
                CFProjectHelper.addToClasspath(javaProject, DevRuntimeClasspathContainer.ID);
            IJavaSearchScope searchScope = null;
            if (superTypeName != null && !superTypeName.equals("java.lang.Object")) { //$NON-NLS-1$

                IType superType = javaProject.findType(superTypeName);
                if (superType != null)
                    searchScope = SearchEngine.createStrictHierarchyScope(javaProject, superType, true, false, null);

            }
            if (searchScope == null)
                searchScope = getSearchScope(project, false);

            if (filter == null || filter.trim().length() == 0)
            {
                filter = "**";
            }

            SelectionDialog dialog = JavaUI.createTypeDialog(shell, PlatformUI.getWorkbench().getProgressService(), searchScope, scope, false, filter);
            dialog.setTitle("Select Type");

            if (dialog.open() == Window.OK)
            {
                IType type = (IType) dialog.getResult()[0];
                return type;
            }
        }
        catch (JavaModelException e)
        {
        }

        finally
        {
            if (addDevPath)
                try
                {
                    CFProjectHelper.removeFromClasspath(javaProject, DevRuntimeClasspathContainer.ID);
                }
                catch (JavaModelException e)
                {
                    //
                }
        }
        return null;
    }

    public static IJavaSearchScope getSearchScope(IJavaProject project, boolean ignoreJRE)
    {
        return SearchEngine.createJavaSearchScope(getRoots(project, ignoreJRE));
    }

    public static IJavaSearchScope getSearchScope(IProject project, boolean ignoreJRE)
    {
        return getSearchScope(JavaCore.create(project), ignoreJRE);
    }

    public static IPackageFragmentRoot[] getRoots(IJavaProject project, boolean ignoreJRE)
    {
        ArrayList<IPackageFragmentRoot> result = new ArrayList<IPackageFragmentRoot>();
        try
        {
            IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
            for (int i = 0; i < roots.length; i++)
            {
                if (!ignoreJRE || !isJRELibrary(roots[i]))
                {
                    result.add(roots[i]);
                }
            }
        }
        catch (JavaModelException e)
        {
        }
        return result.toArray(new IPackageFragmentRoot[result.size()]);
    }

    public static boolean isJRELibrary(IPackageFragmentRoot root)
    {
        try
        {
            IPath path = root.getRawClasspathEntry().getPath();
            if (path.equals(new Path(JavaRuntime.JRE_CONTAINER)) || path.equals(new Path(JavaRuntime.JRELIB_VARIABLE)))
            {
                return true;
            }
        }
        catch (JavaModelException e)
        {
        }
        return false;
    }

    public static String findOrCreateClass(String name, IProject project, String supperType, String defaultType, boolean defaultClassOpen)
    {
        if (name != null)
            name = trimNonAlphaChars(name).replace('$', '.');
        try
        {
            if (project.hasNature(JavaCore.NATURE_ID))
            {
                IJavaProject javaProject = JavaCore.create(project);
                IJavaElement result = null;
                if (name != null && name.length() > 0 && !name.equals(defaultType))
                    result = javaProject.findType(name);
                if (result != null)
                    JavaUI.openInEditor(result);
                else
                {
                    final NewClassWizardPage classWizardPage = new NewClassWizardPage();

                    OpenNewClassWizardAction classWizardAction = new OpenNewClassWizardAction();
                    classWizardAction.setOpenEditorOnFinish(true);
                    classWizardAction.setConfiguredWizardPage(classWizardPage);
                    classWizardPage.init(new StructuredSelection(javaProject));
                    if (name != null && !name.equals(defaultType))
                    {
                        String className = name;
                        String pkgName = null;
                        if (name.lastIndexOf('.') > -1)
                        {
                            if (name.lastIndexOf('.') > 0)
                                pkgName = name.substring(0, name.lastIndexOf('.') - 1);
                            if (name.lastIndexOf('.') < name.length())
                                className = name.substring(name.lastIndexOf('.') + 1, name.length());
                        }
                        if (pkgName != null && classWizardPage.getPackageFragmentRoot() != null)
                            classWizardPage.setPackageFragment(classWizardPage.getPackageFragmentRoot().getPackageFragment(pkgName), true);
                        if (className != null)
                            classWizardPage.setTypeName(className, true);

                    }
                    if (supperType != null)
                    {

                        IType baseType = javaProject.findType(supperType);
                        if (baseType != null)
                        {
                            if (baseType.isClass())
                            {
                                classWizardPage.setSuperClass(supperType, false);
                            }
                            if (baseType.isInterface())
                            {
                                if (defaultType != null)
                                    classWizardPage.setSuperClass(defaultType, true);
                                classWizardPage.setSuperInterfaces(Arrays.asList(supperType), false);
                            }
                        }
                    }

                    classWizardAction.run();
                    IJavaElement createdElement = classWizardAction.getCreatedElement();
                    if (createdElement instanceof IType)
                    {
                        return ((IType) createdElement).getFullyQualifiedName('$');
                    }

                }
            }
        }
        catch (CoreException e)
        {
            EJCoreLog.logException(e);
        }

        return name;
    }

    public static String trimNonAlphaChars(String value)
    {
        value = value.trim();
        while (value.length() > 0 && !Character.isLetter(value.charAt(0)))
            value = value.substring(1, value.length());
        int loc = value.indexOf(":"); //$NON-NLS-1$
        if (loc != -1 && loc > 0)
            value = value.substring(0, loc);
        else if (loc == 0)
            value = ""; //$NON-NLS-1$
        return value;
    }

    public static IPackageFragment[] choosePackage(Shell shell, IJavaProject javaProject, String filter, Boolean multipleSelection)
    {
        return choosePackage(shell, javaProject, filter, multipleSelection, null);
    }

    public static IPackageFragment[] choosePackage(Shell shell, IJavaProject javaProject, String filter, Boolean multipleSelection,
            IPackageFragmentFilter packageFragmentFilter)
    {
        List<IPackageFragment> elements = new ArrayList<IPackageFragment>();
        if (javaProject.exists())
        {
            try
            {
                IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
                for (int i = 0; i < roots.length; i++)
                {
                    if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE)
                    {
                        IPackageFragmentRoot sourceRoot = roots[i];
                        if (sourceRoot != null && sourceRoot.exists())
                        {
                            IJavaElement[] children = sourceRoot.getChildren();
                            for (IJavaElement element : children)
                            {
                                if (element instanceof IPackageFragment
                                        && (packageFragmentFilter == null || packageFragmentFilter.acccept((IPackageFragment) element)))
                                    elements.add((IPackageFragment) element);
                            }
                        }
                    }
                }
            }
            catch (JavaModelException e)
            {
                EJCoreLog.logException(e);
            }
        }

        ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell, new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_DEFAULT));
        dialog.setIgnoreCase(false);
        dialog.setTitle("Package Selection");
        dialog.setMessage("Choose a folder");
        dialog.setFilter(filter);
        dialog.setMultipleSelection(multipleSelection);
        dialog.setEmptyListMessage("");
        dialog.setElements(elements.toArray(new IPackageFragment[0]));
        dialog.setHelpAvailable(false);

        if (dialog.open() == Window.OK)
        {
            Object[] result = dialog.getResult();
            IPackageFragment[] fragments = new IPackageFragment[result.length];
            for (int i = 0; i < result.length; i++)
            {
                fragments[i] = (IPackageFragment) result[i];
            }
            return fragments;

        }
        return new IPackageFragment[0];
    }

    public static interface IPackageFragmentFilter
    {
        boolean acccept(IPackageFragment fragment);
    }

    public static boolean isSubTypeOfInterface(IType findType, Class<?>... _interfaces) throws CoreException
    {
        if (findType != null)
        {
            ITypeHierarchy typeHier = findType.newSupertypeHierarchy(null);
            IType[] allInterfaces = typeHier.getAllInterfaces();
            for (Class<?> _interface : _interfaces)
            {
                for (IType iType : allInterfaces)
                {
                    //
                    if (iType.getFullyQualifiedName('$').equals(_interface.getName()))
                        return true;
                }
            }

        }

        return false;
    }

    public static boolean isSubTypeOfInterface(Class<?> findType, Class<?>... _interfaces) throws CoreException
    {
        if (findType != null)
        {
            Class<?>[] allInterfaces = findType.getInterfaces();
            for (Class<?> _interface : _interfaces)
            {
                for (Class<?> iType : allInterfaces)
                {
                    //
                    if (iType.getName().equals(_interface.getName()))
                        return true;
                }
            }

        }

        return false;
    }

    public static List<String> getStaticFieldsOfType(IType findType) throws CoreException
    {
        List<String> fields = new ArrayList<String>();
        if (findType != null)
        {
            IField[] ifields = findType.getFields();
            for (IField iField : ifields)
            {

                int flags = iField.getFlags();
                // The field needs to be static and final to have a constant
                // value.
                if (Flags.isStatic(flags) && Flags.isPublic(flags))
                {
                    fields.add(iField.getElementName());
                }
            }
        }
        return fields;

    }

}
