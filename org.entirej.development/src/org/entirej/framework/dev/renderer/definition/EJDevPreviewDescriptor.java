/*******************************************************************************
 * Copyright 2013 CRESOFT AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Contributors: CRESOFT AG - initial API and implementation
 ******************************************************************************/
package org.entirej.framework.dev.renderer.definition;

import java.io.Serializable;

public final class EJDevPreviewDescriptor implements Serializable
{
    private static final long serialVersionUID = 3053086469222660923L;

    private final EJDevPreviewKind kind;
    private final String           name;
    private final String           label;
    private final String           rendererName;
    private final int              preferredWidth;
    private final int              preferredHeight;

    public EJDevPreviewDescriptor(EJDevPreviewKind kind, String name, String label, String rendererName, int preferredWidth, int preferredHeight)
    {
        this.kind = kind == null ? EJDevPreviewKind.UNKNOWN : kind;
        this.name = name;
        this.label = label;
        this.rendererName = rendererName;
        this.preferredWidth = Math.max(0, preferredWidth);
        this.preferredHeight = Math.max(0, preferredHeight);
    }

    public static EJDevPreviewDescriptor create(EJDevPreviewKind kind, String name, String label, String rendererName, int preferredWidth,
            int preferredHeight)
    {
        return new EJDevPreviewDescriptor(kind, name, label, rendererName, preferredWidth, preferredHeight);
    }

    public static EJDevPreviewDescriptor box(String name, String label, String rendererName)
    {
        return new EJDevPreviewDescriptor(EJDevPreviewKind.UNKNOWN, name, label, rendererName, 0, 0);
    }

    public EJDevPreviewDescriptor withKind(EJDevPreviewKind newKind)
    {
        return new EJDevPreviewDescriptor(newKind, name, label, rendererName, preferredWidth, preferredHeight);
    }

    public EJDevPreviewDescriptor withName(String newName)
    {
        return new EJDevPreviewDescriptor(kind, newName, label, rendererName, preferredWidth, preferredHeight);
    }

    public EJDevPreviewDescriptor withLabel(String newLabel)
    {
        return new EJDevPreviewDescriptor(kind, name, newLabel, rendererName, preferredWidth, preferredHeight);
    }

    public EJDevPreviewDescriptor withRendererName(String newRendererName)
    {
        return new EJDevPreviewDescriptor(kind, name, label, newRendererName, preferredWidth, preferredHeight);
    }

    public EJDevPreviewKind getKind()
    {
        return kind;
    }

    public String getName()
    {
        return name;
    }

    public String getLabel()
    {
        return label;
    }

    public String getRendererName()
    {
        return rendererName;
    }

    public int getPreferredWidth()
    {
        return preferredWidth;
    }

    public int getPreferredHeight()
    {
        return preferredHeight;
    }
}
