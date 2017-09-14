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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
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
import org.entirej.ide.ui.editors.descriptors.IJavaProjectProvider;

public final class PackageAssistProvider implements IContentProposalProvider
{
    private final IJavaProjectProvider projectProvider;
    private final Image                PKG_IMG       = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);
    private final Image                PKG_IMG_EMPTY = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_EMPTY_PACKAGE);

    public PackageAssistProvider(IJavaProjectProvider projectProvider, int typeScope)
    {
        super();
        this.projectProvider = projectProvider;
    }

    public static void createTypeAssist(Control control, final IJavaProjectProvider projectProvider, final int typeScope)
    {

        ContentAssistCommandAdapter adapter = new ContentAssistCommandAdapter(control, new TextContentAdapter(), new PackageAssistProvider(projectProvider,
                typeScope), ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS, new char[0], true);
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
            this.content = type.pkg;
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
            return type.pkg;
        }

        public String getDescription()
        {
            return discription;
        }
    }

    private boolean filterPkg(String contents, int position, String type)
    {
        if (contents.length() > position)
        {
            contents = contents.substring(0, position);
        }
        return type.contains(contents);
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
                                if (element instanceof IPackageFragment)
                                {

                                    if (filterPkg(currentContent, position, element.getElementName()))
                                    {
                                        IPackageFragment fragment = (IPackageFragment) element;
                                        proposals.add(new TypeProposal(new Type(element.getElementName(), fragment.hasChildren() ? PKG_IMG : PKG_IMG_EMPTY),
                                                null));
                                    }
                                }
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

        Collections.sort(proposals, new Comparator<TypeProposal>()
        {

            public int compare(TypeProposal o1, TypeProposal o2)
            {

                return o1.type.pkg.compareTo(o2.type.pkg);
            }
        });
        return proposals.toArray(new IContentProposal[0]);
    }

    private class Type
    {
        private final String pkg;
        private final Image  image;

        public Type(String pkg, Image image)
        {
            super();
            this.pkg = pkg;
            this.image = image;
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
                if (type.pkg == null || type.pkg.length() == 0)
                {
                    ss.append("(default package)");
                }
                else
                    ss.append(type.pkg);
            }
            return ss;
        }

    }

}
