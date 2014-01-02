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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;

public abstract class AbstractFilteredTree extends Composite
{

    protected Text       filterText;

    protected TreeViewer treeViewer;

    protected Composite  filterComposite;

    protected Composite  parent;

    protected Composite  treeComposite;

    public AbstractFilteredTree(Composite parent, int treeStyle)
    {
        super(parent, SWT.NONE);
        this.parent = parent;
        init(treeStyle);
    }

    protected void init(int treeStyle)
    {
        createControl(parent, treeStyle);
        setInitialText(null);
        setFont(parent.getFont());

    }

    protected void createControl(Composite parent, int treeStyle)
    {
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);
        setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        filterComposite = new Composite(this, SWT.NONE);

        GridLayout filterLayout = new GridLayout(2, false);
        filterLayout.marginHeight = 0;
        filterLayout.marginWidth = 0;
        filterComposite.setLayout(filterLayout);
        filterComposite.setFont(parent.getFont());

        createFilterControls(filterComposite);
        filterComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

        treeComposite = new Composite(this, SWT.NONE);
        GridLayout treeCompositeLayout = new GridLayout();
        treeCompositeLayout.marginHeight = 0;
        treeCompositeLayout.marginWidth = 0;
        treeComposite.setLayout(treeCompositeLayout);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        treeComposite.setLayoutData(data);
        createTreeControl(treeComposite, treeStyle);
    }

    protected Composite createFilterControls(Composite parent)
    {
        createFilterText(parent);

        return parent;
    }

    protected Control createTreeControl(Composite parent, int style)
    {
        treeViewer = doCreateTreeViewer(parent, style);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        treeViewer.getControl().setLayoutData(data);

        return treeViewer.getControl();
    }

    protected TreeViewer doCreateTreeViewer(Composite parent, int style)
    {
        return new TreeViewer(parent, style);
    }

    protected void createFilterText(Composite parent)
    {
        filterText = doCreateFilterText(parent);

        filterText.addFocusListener(new FocusAdapter()
        {

            public void focusGained(FocusEvent e)
            {

                Display display = filterText.getDisplay();
                display.asyncExec(new Runnable()
                {
                    public void run()
                    {
                        if (!filterText.isDisposed())
                        {

                            filterText.selectAll();

                        }
                    }
                });

            }

            public void focusLost(FocusEvent e)
            {

            }
        });

        filterText.addKeyListener(new KeyAdapter()
        {

            public void keyPressed(KeyEvent e)
            {
                // on a CR we want to transfer focus to the list
                boolean hasItems = getViewer().getTree().getItemCount() > 0;
                if (hasItems && e.keyCode == SWT.ARROW_DOWN)
                {
                    treeViewer.getTree().setFocus();
                    return;
                }
            }
        });

        filterText.addTraverseListener(new TraverseListener()
        {
            public void keyTraversed(TraverseEvent e)
            {
                if (e.detail == SWT.TRAVERSE_RETURN)
                {
                    e.doit = false;
                    getViewer().getTree().setFocus();

                }
            }
        });

        filterText.addModifyListener(new ModifyListener()
        {

            public void modifyText(ModifyEvent e)
            {
                textChanged();
            }
        });

        if ((filterText.getStyle() & SWT.ICON_CANCEL) != 0)
        {
            filterText.addSelectionListener(new SelectionAdapter()
            {

                public void widgetDefaultSelected(SelectionEvent e)
                {
                    if (e.detail == SWT.ICON_CANCEL)
                        clearText();
                }
            });
        }

        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        // if the text widget supported cancel then it will have it's own
        // integrated button. We can take all of the space.
        if ((filterText.getStyle() & SWT.ICON_CANCEL) != 0)
            gridData.horizontalSpan = 2;
        filterText.setLayoutData(gridData);
    }

    protected Text doCreateFilterText(Composite parent)
    {

        return new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);

    }

    /**
     * Update the receiver after the text has changed.
     */
    protected void textChanged()
    {
        filter(getFilterString());
        // TODO
    }

    public abstract void filter(String filter);

    public void clearText()
    {
        if (getFilterString() != null && getFilterString().trim().length() > 0)
            setFilterText("");
    }

    protected void setFilterText(String string)
    {
        if (filterText != null && !filterText.isDisposed())
        {
            filterText.setText(string != null ? string : "");
            selectAll();
        }

    }

    public TreeViewer getViewer()
    {
        return treeViewer;
    }

    public Text getFilterControl()
    {
        return filterText;
    }

    protected String getFilterString()
    {
        return (filterText != null && !filterText.isDisposed()) ? filterText.getText() : null;
    }

    public void setInitialText(String text)
    {
        setFilterText(text);
        textChanged();

    }

    protected void selectAll()
    {
        if (filterText != null)
        {
            filterText.selectAll();
        }
    }

    public static abstract class FilteredContentProvider implements ITreeContentProvider
    {
        protected String filter;

        public void setFilter(String filter)
        {
            this.filter = filter;
        }

        public String getFilter()
        {
            return filter;
        }

    }

}
