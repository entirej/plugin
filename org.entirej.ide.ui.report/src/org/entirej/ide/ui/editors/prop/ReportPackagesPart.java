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
package org.entirej.ide.ui.editors.prop;

import java.util.Arrays;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.entirej.framework.plugin.reports.EJPluginEntireJReportProperties;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.EditorLayoutFactory;
import org.entirej.ide.ui.utils.JavaAccessUtils;
import org.entirej.ide.ui.utils.JavaAccessUtils.IPackageFragmentFilter;

public class ReportPackagesPart extends SectionPart
{
    private final Image              PKG_IMG = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);
    private final EJReportPropertiesEditor editor;
    private TableViewer              viewer;
    private Button                   addButton;
    private Button                   removeButton;

    public ReportPackagesPart(final EJReportPropertiesEditor editor, FormPage page, Composite parent)
    {
        super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);

        this.editor = editor;
        buildBody(getSection(), page.getEditor().getToolkit());
    }

    protected void buildBody(Section section, FormToolkit toolkit)
    {
        section.setText("Report Packages");
        section.setDescription("Choose the packages where the reports configuration files will be stored.");
        section.setLayout(EditorLayoutFactory.createClearTableWrapLayout(false, 1));
        GridData sectionData = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
        section.setLayoutData(sectionData);

        Composite body = toolkit.createComposite(section);
        section.setTabList(new Control[] { body });
        GridLayout glayout = new GridLayout();
        glayout.marginWidth = 2;
        glayout.marginHeight = 2;
        glayout.numColumns = 2;
        glayout.makeColumnsEqualWidth = false;

        GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.FILL;
        gd.grabExcessVerticalSpace = true;
        gd.grabExcessHorizontalSpace = true;

        body.setLayout(glayout);
        body.setLayoutData(gd);

        createViewer(body);
        createButtons(body, toolkit);
        viewer.setInput(new Object());
        updateButtons();
        toolkit.paintBordersFor(body);
        section.setClient(body);
        section.layout();
    }

    public void createViewer(Composite body)
    {
        GridData gd;
        viewer = new TableViewer(body, SWT.MULTI | SWT.BORDER | SWT.VIRTUAL | SWT.V_SCROLL);
        gd = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_FILL);
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.FILL;
        gd.grabExcessVerticalSpace = true;
        gd.horizontalSpan = 1;
        gd.widthHint = 30;
        viewer.getTable().setLayoutData(gd);
        viewer.setContentProvider(new IStructuredContentProvider()
        {

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {

            }

            public void dispose()
            {
                //
            }

            public Object[] getElements(Object inputElement)
            {

                return editor.getEntireJProperties().getReportPackageNames().toArray();
            }
        });
        viewer.setLabelProvider(new LabelProvider()
        {
            protected String pathTopackage(String path)
            {
                if (path != null)
                {
                    return path.replaceAll("/", ".");
                }
                return path;
            }

            @Override
            public String getText(Object element)
            {
                String pkg = pathTopackage((String) element);
                if (pkg == null || pkg.length() == 0)
                {
                    return "(default package)";
                }
                return pkg;
            }

            @Override
            public Image getImage(Object element)
            {
                return PKG_IMG;
            }

        });
        viewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {
                updateButtons();

            }
        });
    }

    private void createButtons(final Composite body, FormToolkit toolkit)
    {
        Composite buttonClient = toolkit.createComposite(body);

        GridLayout glayout = new GridLayout();
        glayout.marginWidth = glayout.marginHeight = 2;
        glayout.numColumns = 1;
        buttonClient.setLayout(glayout);

        addButton = toolkit.createButton(buttonClient, "Add", SWT.PUSH);
        addButton.addSelectionListener(new SelectionAdapter()
        {
            protected String packageToPath(String pkg)
            {
                if (pkg != null)
                {
                    return pkg.replaceAll("\\.", "/");
                }
                return pkg;
            }

            public void widgetSelected(SelectionEvent e)
            {
                final EJPluginEntireJReportProperties entireJProperties = editor.getEntireJProperties();
                IPackageFragmentFilter fragmentFilter = new IPackageFragmentFilter()
                {

                    public boolean acccept(IPackageFragment fragment)
                    {
                        return !entireJProperties.getReportPackageNames().contains(packageToPath(fragment.getElementName()));
                    }
                };
                Object[] result = JavaAccessUtils.choosePackage(EJUIPlugin.getActiveWorkbenchShell(), editor.getJavaProject(), "", true, fragmentFilter);
                if (result.length > 0)
                {

                    for (Object item : result)
                    {
                        if (item instanceof IPackageFragment)
                        {
                            entireJProperties.addReportPackageName(packageToPath(((IPackageFragment) item).getElementName()));
                        }
                    }
                    editor.setDirty(true);
                    refresh();
                }
            }
        });
        GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
        gd.horizontalSpan = 1;
        addButton.setLayoutData(gd);

        removeButton = toolkit.createButton(buttonClient, "Remove", SWT.PUSH);
        removeButton.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                Object[] items = selection.toArray();
                EJPluginEntireJReportProperties entireJProperties = editor.getEntireJProperties();
                /*
                 * List<String> pkgs = new
                 * ArrayList<String>(entireJProperties.getFormPackageNames());
                 * pkgs.removeAll(Arrays.asList(items));
                 */
                entireJProperties.getReportPackageNames().removeAll(Arrays.asList(items));
                editor.setDirty(true);
                refresh();

            }
        });
        gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
        gd.horizontalSpan = 1;
        removeButton.setLayoutData(gd);

        toolkit.paintBordersFor(body);
    }

    private void updateButtons()
    {
        IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        Object[] items = selection.toArray();
        removeButton.setEnabled(items.length > 0);

    }

    @Override
    public void refresh()
    {
        if (viewer != null)
        {
            viewer.setInput(new Object());
        }
        super.refresh();
        updateButtons();
    }
}
