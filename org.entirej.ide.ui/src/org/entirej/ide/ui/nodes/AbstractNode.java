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
package org.entirej.ide.ui.nodes;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Image;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;

public class AbstractNode<T>
{
    protected AbstractNode<?>         parent;
    private final INodeDeleteProvider deleteProvider;
    protected T                       source;

    public AbstractNode(T source)
    {
        this(null, source);
    }

    public AbstractNode(T source, INodeDeleteProvider deleteProvider)
    {
        this(null, source, deleteProvider);
    }

    public AbstractNode(AbstractNode<?> parent, T source)
    {
        this(parent, source, null);
    }

    public AbstractNode(AbstractNode<?> parent, T source, INodeDeleteProvider deleteProvider)
    {
        this.parent = parent;
        this.source = source;
        this.deleteProvider = deleteProvider;
    }

    public String getName()
    {
        return null;
    }

    public String getNote()
    {
        return null;
    }

    public String getToolTipText()
    {
        return null;
    }

    public Image getImage()
    {
        return null;
    }

    public boolean isLeaf()
    {
        return true;
    }

    public AbstractNode<?> getParent()
    {
        return parent;
    }

    public void setParent(AbstractNode<?> parent)
    {
        this.parent = parent;
    }

    public INodeDeleteProvider getDeleteProvider()
    {
        return deleteProvider;
    }

    public INodeRenameProvider getRenameProvider()
    {
        return null;
    }

    public Action[] getActions()
    {
        return new Action[0];
    }

    public AbstractDescriptor<?>[] getNodeDescriptors()
    {
        return new AbstractDescriptor[0];
    }
    
    public String getNodeDescriptorDetails()
    {
        return null;
    }

    public T getSource()
    {
        return source;
    }

    @Override
    public int hashCode()
    {
        return getSource().hashCode();
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (this.getSource() == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass() )
            return false;
        
            AbstractNode<?> other = (AbstractNode<?>) obj;
            if (source == null)
            {
                if (other.source != null)
                    return false;
            }
            else if (!source.equals(other.source))
                return false;
        
       
       
        
        return true;
    }

    public AbstractNode<?>[] getChildren()
    {
        return new AbstractNode<?>[0];
    }

    @Override
    public String toString()
    {
        return getName();
    }

    public AbstractNode<?> toNode(Object source)
    {
        return null;
    }

    public <S> S getAdapter(Class<S> adapter)
    {

        return null;
    }
}
