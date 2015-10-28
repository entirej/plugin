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
package org.entirej.ide.ui.editors.descriptors;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Control;
import org.entirej.ide.ui.editors.descriptors.IGroupProvider.IRefreshHandler;

public abstract class AbstractDescriptor<T>
{
    public enum TYPE
    {
        TEXT, DESCRIPTION, SELECTION, BOOLEAN, REFERENCE, GROUP, CUSTOM
    }

    private final TYPE type;
    private boolean    required;
    private boolean    override ;
    private String     text;
    private String     tooltip;

    public AbstractDescriptor(TYPE type)
    {
        super();
        this.type = type;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    public String getTooltip()
    {
        return tooltip;
    }

    public void setTooltip(String tooltip)
    {
        this.tooltip = tooltip;
    }

    
    public boolean isOverride()
    {
        return override;
    }
    
    public void setOverride(boolean override)
    {
        this.override = override;
    }
    
    public boolean isRequired()
    {
        return required;
    }

    public void setRequired(boolean required)
    {
        this.required = required;
    }

    public TYPE getType()
    {
        return type;
    }

    public void addEditorAssist(Control control)
    {

    }

    public T browseType()
    {
        return getValue();
    }

    public boolean hasLableLink()
    {
        return false;
    }

    public T lableLinkActivator()
    {
        return getValue();
    }

    public abstract T getValue();

    public abstract void setValue(T value);
    
    public  void runOperation(AbstractOperation operation)
    {
        try
        {
            operation.execute(null, null);
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }
    }

    
    
    public AbstractOperation createOperation(final T newValue,final IRefreshHandler handler)
    {
        final T oldValue = getValue();
        return new AbstractOperation(String.format("Change : %s", getText()))
        {

            @Override
            public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
            {
                setValue(oldValue);
                handler.refresh();
                return Status.OK_STATUS;
            }

            @Override
            public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
            {
               
                setValue(newValue);
                handler.refresh();
                return Status.OK_STATUS;
            }

            @Override
            public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
            {
                setValue(newValue);
                return Status.OK_STATUS;
            }
        };
    }

    public String getErrors()
    {
        return null;
    }

    public String getWarnings()
    {
        return null;
    }

}
