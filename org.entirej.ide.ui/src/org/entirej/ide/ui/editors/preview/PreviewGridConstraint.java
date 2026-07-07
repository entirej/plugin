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
package org.entirej.ide.ui.editors.preview;

public class PreviewGridConstraint
{
    private int     horizontalSpan = 1;
    private int     verticalSpan   = 1;
    private int     preferredWidth;
    private int     preferredHeight;
    private int     minimumWidth;
    private int     minimumHeight;
    private boolean fillHorizontal = true;
    private boolean fillVertical   = true;
    private boolean grabHorizontal = true;
    private boolean grabVertical   = true;

    public static PreviewGridConstraint defaults()
    {
        return new PreviewGridConstraint();
    }

    public int getHorizontalSpan()
    {
        return horizontalSpan;
    }

    public PreviewGridConstraint setHorizontalSpan(int horizontalSpan)
    {
        this.horizontalSpan = Math.max(1, horizontalSpan);
        return this;
    }

    public int getVerticalSpan()
    {
        return verticalSpan;
    }

    public PreviewGridConstraint setVerticalSpan(int verticalSpan)
    {
        this.verticalSpan = Math.max(1, verticalSpan);
        return this;
    }

    public int getPreferredWidth()
    {
        return preferredWidth;
    }

    public PreviewGridConstraint setPreferredWidth(int preferredWidth)
    {
        this.preferredWidth = Math.max(0, preferredWidth);
        return this;
    }

    public int getPreferredHeight()
    {
        return preferredHeight;
    }

    public PreviewGridConstraint setPreferredHeight(int preferredHeight)
    {
        this.preferredHeight = Math.max(0, preferredHeight);
        return this;
    }

    public int getMinimumWidth()
    {
        return minimumWidth;
    }

    public PreviewGridConstraint setMinimumWidth(int minimumWidth)
    {
        this.minimumWidth = Math.max(0, minimumWidth);
        return this;
    }

    public int getMinimumHeight()
    {
        return minimumHeight;
    }

    public PreviewGridConstraint setMinimumHeight(int minimumHeight)
    {
        this.minimumHeight = Math.max(0, minimumHeight);
        return this;
    }

    public boolean isFillHorizontal()
    {
        return fillHorizontal;
    }

    public PreviewGridConstraint setFillHorizontal(boolean fillHorizontal)
    {
        this.fillHorizontal = fillHorizontal;
        return this;
    }

    public boolean isFillVertical()
    {
        return fillVertical;
    }

    public PreviewGridConstraint setFillVertical(boolean fillVertical)
    {
        this.fillVertical = fillVertical;
        return this;
    }

    public boolean isGrabHorizontal()
    {
        return grabHorizontal;
    }

    public PreviewGridConstraint setGrabHorizontal(boolean grabHorizontal)
    {
        this.grabHorizontal = grabHorizontal;
        return this;
    }

    public boolean isGrabVertical()
    {
        return grabVertical;
    }

    public PreviewGridConstraint setGrabVertical(boolean grabVertical)
    {
        this.grabVertical = grabVertical;
        return this;
    }
}
