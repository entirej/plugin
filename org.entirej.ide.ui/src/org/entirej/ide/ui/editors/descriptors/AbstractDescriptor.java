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

import org.eclipse.swt.widgets.Control;

public abstract class AbstractDescriptor<T>
{
    public enum TYPE
    {
        TEXT, DESCRIPTION, SELECTION, BOOLEAN, REFERENCE, GROUP, CUSTOM
    }

    private final TYPE type;
    private boolean    required;
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

    public String getErrors()
    {
        return null;
    }

    public String getWarnings()
    {
        return null;
    }

}
