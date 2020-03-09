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
package org.entirej.ide.ui.editors.descriptors;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public abstract class AbstractGroupDescriptor extends AbstractDescriptor<Object> implements IGroupProvider
{

    public AbstractGroupDescriptor(String lable)
    {
        super(AbstractDescriptor.TYPE.GROUP);
        setText(lable);
    }

    public AbstractGroupDescriptor(String lable, String tooltip)
    {
        this(lable);
        setTooltip(tooltip);
    }

    public boolean isExpand()
    {

        return true;
    }

    @Override
    public Object getValue()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setValue(Object value)
    {
        // TODO Auto-generated method stub

    }

    public Control createHeader(IRefreshHandler handler, Composite parent, GridData gd)
    {
        return null;
    }

    public Action[] getToolbarActions()
    {
        return new Action[0];
    }

}
