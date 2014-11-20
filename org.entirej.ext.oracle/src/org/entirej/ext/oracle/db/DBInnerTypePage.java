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
package org.entirej.ext.oracle.db;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.utils.JavaAccessUtils;

public abstract class DBInnerTypePage extends WizardPage
{

    private TreeViewer             dbfilteredTree;

    private ITreeContentProvider   contentProvider;
    private LabelProvider          labelProvider;

    private List<TypeMapper>       typeMappers = new ArrayList<DBInnerTypePage.TypeMapper>();
    

    public DBInnerTypePage()
    {
        super("ej.db.db.inner.types");
        setTitle("Inner Types");
        setDescription("Select/Generate Inner Types for Funtion/Procedure.");
    }

    public ITreeContentProvider getContentProvider()
    {
        return contentProvider;
    }

    public LabelProvider getLabelProvider()
    {
        return labelProvider;
    }

    
    void collectTypes(ObjectArgument objectArgument ,List<String> addedInner)
    {
        if (objectArgument.objName != null && !addedInner.contains(objectArgument.objName))
        {
            addedInner.add(objectArgument.objName);
            typeMappers.add(new TypeMapper(objectArgument));
        }
        List<Argument> arguments = objectArgument.getArguments();
        for (Argument argument : arguments)
        {
            if (argument instanceof ObjectArgument)
            {
             

                collectTypes((ObjectArgument) argument,addedInner);
            }
        }
    }
    
    protected void init(Procedure procedure)
    {
        List<String> addedInner = new ArrayList<String>();
        typeMappers.clear();
        ObjectArgument collectionType = procedure.getCollectionType();
        if (collectionType != null)
        {
           
            for (Argument argument : collectionType.getArguments())
            {
              

                if (argument instanceof ObjectArgument)
                {
                    ObjectArgument objectArgument = (ObjectArgument) argument;

                    collectTypes(objectArgument,addedInner);
                }
            }

        }
        List<Argument> arguments = procedure.getArguments();
        for (Argument argument : arguments)
        {
          

            if (argument instanceof ObjectArgument)
            {
                ObjectArgument objectArgument = (ObjectArgument) argument;

                collectTypes(objectArgument,addedInner);
            }
        }

    }

    boolean skipPage()
    {
        return typeMappers.isEmpty();
    }

    String getMappedClass(ObjectArgument argument)
    {
        for (TypeMapper mapper : typeMappers)
        {
            if (argument.objName.equals(mapper.type.objName))
                return mapper.mapedClass;
        }
        return null;
    }

    @Override
    public void setVisible(boolean visible)
    {

        super.setVisible(visible);
        if (visible)
        {

            if (dbfilteredTree != null)
                dbfilteredTree.setInput(getDBInput());
            doUpdateStatus();
        }

    }

    public void createControl(Composite parent)
    {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
        int nColumns = 2;

        GridLayout layout = new GridLayout();
        layout.numColumns = nColumns;
        layout.makeColumnsEqualWidth = false;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);
        createDBViewComponent(composite);

        setControl(composite);
        Dialog.applyDialogFont(composite);
    }

    public static Control createEmptySpace(Composite parent, int span)
    {
        Label label = new Label(parent, SWT.LEFT);
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.BEGINNING;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = span;
        gd.horizontalIndent = 0;
        gd.widthHint = 0;
        gd.heightHint = 0;
        label.setLayoutData(gd);
        return label;
    }

    private void createDBViewComponent(Composite composite)
    {
        dbfilteredTree = new TreeViewer(composite, SWT.VIRTUAL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);

        GridData treeGD = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
        treeGD.widthHint = 250;
        treeGD.heightHint = 300;
        dbfilteredTree.getControl().setLayoutData(treeGD);
        dbfilteredTree.setComparator(new ViewerComparator()
        {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2)
            {
                if (e1 instanceof TypeMapper && e2 instanceof TypeMapper)
                {
                    TypeMapper column1 = (TypeMapper) e1;
                    TypeMapper column2 = (TypeMapper) e2;
                    String name1 = column1.type.objName;
                    String name2 = column2.type.objName;
                    if (name1 == null)
                    {
                        name1 = "";//$NON-NLS-1$
                    }
                    if (name2 == null)
                    {
                        name2 = "";//$NON-NLS-1$
                    }
                    return name1.compareToIgnoreCase(name2);
                }
                return super.compare(viewer, e1, e2);
            }

        });

        final Button button = new Button(composite, SWT.PUSH);
        button.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
        button.setText("Use Existing");
        final TreeViewer viewer = dbfilteredTree;
        button.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                Object node = new Object();
                ISelection selection = viewer.getSelection();
                IStructuredSelection strutruredSelection = (IStructuredSelection) selection;
                if (strutruredSelection.size() == 1 && strutruredSelection.getFirstElement() != null)
                {
                    node = strutruredSelection.getFirstElement();
                }

                if (node instanceof TypeMapper)
                {
                    TypeMapper mapper = (TypeMapper) node;
                    IType type = JavaAccessUtils.selectClassType(getShell(), getProject().getResource(), (mapper.mapedClass != null ? mapper.mapedClass
                            : "*"), null);
                    if (type != null)
                    {
                        mapper.mapedClass = type.getFullyQualifiedName('$');
                    }
                    else
                        mapper.mapedClass = null;
                    viewer.refresh(mapper);
                }
            }
        });

        
       
        
        viewer.setContentProvider(contentProvider = new InnerTypeContentProvider());
        viewer.setLabelProvider(labelProvider = new InnerLabelProvider());
        viewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {
                Object node = new Object();
                ISelection selection = viewer.getSelection();
                IStructuredSelection strutruredSelection = (IStructuredSelection) selection;
                if (strutruredSelection.size() == 1 && strutruredSelection.getFirstElement() != null)
                {
                    node = strutruredSelection.getFirstElement();
                }

                button.setEnabled((node instanceof TypeMapper));

                doUpdateStatus();
            }
        });
    }

    public abstract IJavaProject getProject();
    
    protected void doUpdateStatus()
    {
        setPageComplete(validatePage());
    }

    protected boolean validatePage()
    {

        setErrorMessage(null);
        setMessage(null);
        return true;
    }

    private Object getDBInput()
    {
        return new Object();
    }

    private final class InnerLabelProvider extends LabelProvider
    {
        @Override
        public String getText(Object element)
        {

            if (element instanceof TypeMapper)
            {
                TypeMapper mapper = ((TypeMapper) element);
                ObjectArgument argument = ((TypeMapper) element).type;
                String name = argument.objName;

                return String.format("%s --> %s", name, (mapper.mapedClass != null ? mapper.mapedClass : "[CREATE TYPE]"));
            }

            return super.getText(element);
        }

        @Override
        public Image getImage(Object element)
        {

            return EJUIImages.SHARED_INNER_CLASS_PUBLIC;
        }
    }

    private class InnerTypeContentProvider implements ITreeContentProvider
    {

        public void dispose()
        {

        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {

        }

        public Object[] getElements(Object inputElement)
        {

            return typeMappers.toArray();
        }

        public Object[] getChildren(Object parentElement)
        {
            return new Object[0];
        }

        public Object getParent(Object element)
        {
            return null;
        }

        public boolean hasChildren(Object element)
        {
            return false;
        }

    }

    private static class TypeMapper
    {
        final ObjectArgument type;
        String               mapedClass;

        public TypeMapper(ObjectArgument type)
        {
            this.type = type;
        }
    }
}
