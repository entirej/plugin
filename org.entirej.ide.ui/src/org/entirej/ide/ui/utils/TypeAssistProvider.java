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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.cf.CFProjectHelper;
import org.entirej.ide.core.cf.lib.DevRuntimeClasspathContainer;
import org.entirej.ide.ui.editors.descriptors.IJavaProjectProvider;

public final class TypeAssistProvider implements IContentProposalProvider
{
    private final IJavaProjectProvider projectProvider;
    private final int                  typeScope;
    private final String               superTypeName;
    private final SearchEngine         searchEngine;

    private final ISharedImages        javaImages = JavaUI.getSharedImages();

    public TypeAssistProvider(IJavaProjectProvider projectProvider, int typeScope, String superTypeName)
    {
        super();
        this.projectProvider = projectProvider;
        this.typeScope = typeScope;
        this.superTypeName = superTypeName;

        searchEngine = new SearchEngine();
    }

    public IJavaSearchScope getSearchScope(IJavaProject project, String superTypeName)
    {
        IJavaSearchScope searchScope = null;
        if (superTypeName != null && !superTypeName.equals("java.lang.Object")) { //$NON-NLS-1$
            try
            {
                IType superType = project.findType(superTypeName);
                if (superType != null)

                    searchScope = SearchEngine.createStrictHierarchyScope(project, superType, true, false, null);
            }
            catch (JavaModelException e)
            {
                EJCoreLog.logException(e);
            }
        }
        if (searchScope == null)
            searchScope = JavaAccessUtils.getSearchScope(project, false);

        return searchScope;
    }

    public static void createTypeAssist(Control control, final IJavaProjectProvider projectProvider, final int typeScope, String superTypeName)
    {

        ContentAssistCommandAdapter adapter = new ContentAssistCommandAdapter(control, new TextContentAdapter(), new TypeAssistProvider(projectProvider,
                typeScope, superTypeName), ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, new char[0], true);
        adapter.setLabelProvider((new TypeLabelProvider()));
        adapter.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
    }

    private static class TypeProposal implements IContentProposal
    {
        private final Type   type;
        private final String discription;
        private final String content;

        public TypeProposal(Type type, String discription)
        {
            this.type = type;
            this.discription = discription;
            this.content = type.getFullyQualifiedName();
        }

        public String getContent()
        {
            return content;
        }

        public int getCursorPosition()
        {
            return content.length();
        }

        public String getLabel()
        {
            return type.getFullyQualifiedName();
        }

        public String getDescription()
        {
            return discription;
        }
    }

    public IContentProposal[] getProposals(String currentContent, int position)
    {
        final List<TypeProposal> proposals = new ArrayList<TypeProposal>();

        if (currentContent.length() > position)
        {
            currentContent = currentContent.substring(0, position);
        }
        IJavaProject javaProject = projectProvider.getJavaProject();
        if (javaProject == null)
            return new IContentProposal[0];

        boolean addDevPath = false;
        try
        {
            if (addDevPath = !CFProjectHelper.hasClasspath(javaProject, DevRuntimeClasspathContainer.ID))
                CFProjectHelper.addToClasspath(javaProject, DevRuntimeClasspathContainer.ID);
            IJavaSearchScope scope = getSearchScope(javaProject, superTypeName);
            char[] packageName = null;
            char[] typeName = null;
            int index = currentContent.lastIndexOf('.');

            if (index == -1)
            {
                // There is no package qualification
                // Perform the search only on the type name
                typeName = currentContent.toCharArray();
            }
            else if ((index + 1) == currentContent.length())
            {
                // There is a package qualification and the last character is a
                // dot
                // Perform the search for all types under the given package
                // Pattern for all types
                typeName = "".toCharArray(); //$NON-NLS-1$
                // Package name without the trailing dot
                packageName = currentContent.substring(0, index).toCharArray();
            }
            else
            {
                // There is a package qualification, followed by a dot, and
                // a type fragment
                // Type name without the package qualification
                typeName = currentContent.substring(index + 1).toCharArray();
                // Package name without the trailing dot
                packageName = currentContent.substring(0, index).toCharArray();
            }

            try
            {
                TypeNameRequestor req = new TypeNameRequestor()
                {
                    public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path)
                    {

                        Image image = null;

                        if (Flags.isInterface(modifiers))
                            image = javaImages.getImage(ISharedImages.IMG_OBJS_INTERFACE);
                        else if (Flags.isAnnotation(modifiers))
                            image = javaImages.getImage(ISharedImages.IMG_OBJS_ANNOTATION);
                        else if (Flags.isEnum(modifiers))
                            image = javaImages.getImage(ISharedImages.IMG_OBJS_ENUM);
                        else
                            image = javaImages.getImage(ISharedImages.IMG_OBJS_CLASS);

                        Type type = new Type(String.valueOf(packageName), String.valueOf(simpleTypeName), image);
                        proposals.add(new TypeProposal(type, null));
                    }
                };
                searchEngine.searchAllTypeNames(packageName, SearchPattern.R_PREFIX_MATCH, typeName, SearchPattern.R_PREFIX_MATCH
                        | SearchPattern.R_CAMELCASE_MATCH, typeScope, scope, req, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
            }
            catch (CoreException e)
            {
                EJCoreLog.log(e);
            }

            Collections.sort(proposals, new Comparator<TypeProposal>()
            {

                public int compare(TypeProposal o1, TypeProposal o2)
                {

                    return o1.type.name.compareTo(o2.type.name);
                }
            });
        }
        catch (JavaModelException e)
        {
            // ignore
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
        return proposals.toArray(new IContentProposal[0]);
    }

    private class Type
    {
        private final String pkg;
        private final String name;
        private final Image  image;

        public Type(String pkg, String name, Image image)
        {
            super();
            this.pkg = pkg;
            this.name = name;
            this.image = image;
        }

        String getFullyQualifiedName()
        {
            StringBuilder stringBuilder = new StringBuilder();
            if (pkg != null && pkg.length() > 0)
                stringBuilder.append(pkg).append(".");
            return stringBuilder.append(name).toString();
        }

    }

    private static class TypeLabelProvider extends LabelProvider implements ILabelProvider, DelegatingStyledCellLabelProvider.IStyledLabelProvider
    {

        public Image getImage(Object element)
        {
            try
            {
                if (element instanceof TypeProposal)
                {
                    return ((TypeProposal) element).type.image;
                }
            }
            catch (Exception e)
            {
                EJCoreLog.log(e);
            }
            return null;
        }

        @Override
        public String getText(Object element)
        {
            return getStyledText(element).toString();
        }

        public StyledString getStyledText(Object element)
        {
            StyledString ss = new StyledString();
            if (element instanceof TypeProposal)
            {
                Type type = ((TypeProposal) element).type;
                ss.append(type.name);
                ss.append(" - ", StyledString.QUALIFIER_STYLER);
                ss.append(type.pkg, StyledString.QUALIFIER_STYLER);
            }
            return ss;
        }

    }

}
