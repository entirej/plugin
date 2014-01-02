package org.entirej.ide.cf.swing.lib;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;

public class SwingRuntimeContainerPage extends WizardPage implements IClasspathContainerPage, IClasspathContainerPageExtension
{

    IJavaProject javaProject;

    public SwingRuntimeContainerPage()
    {
        super("EntireJ Swing Runtime");
    }

    public void initialize(IJavaProject javaProject, IClasspathEntry[] currentEntries)
    {
        this.javaProject = javaProject;
    }

    public IClasspathEntry getSelection()
    {
        return JavaCore.newContainerEntry(SwingRuntimeClasspathContainer.ID);
    }

    public void setSelection(IClasspathEntry containerEntry)
    {
    }

    public void createControl(Composite parent)
    {
        setTitle("EntireJ Swing Runtime");
        setDescription("Add entirej swing libraries to project path.");

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        setControl(composite);

        Link link = new Link(composite, SWT.NONE);
        link.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        link.setText("EntireJ Swing runtime classpath container.");
        // TODO add link ???
    }

    public boolean finish()
    {
        return true;
    }
}