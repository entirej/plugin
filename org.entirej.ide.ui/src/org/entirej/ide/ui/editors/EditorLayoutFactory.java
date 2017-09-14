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
package org.entirej.ide.ui.editors;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * Refers impl from PDE FormLayoutFactory
 */
public class EditorLayoutFactory
{
    private EditorLayoutFactory()
    {
        // ignore
    }

    public static GridLayout createFormGridLayout(boolean makeColumnsEqualWidth, int numColumns)
    {
        GridLayout layout = new GridLayout();
        layout.marginHeight = FORM_BODY_MARGIN_HEIGHT;
        layout.marginWidth = FORM_BODY_MARGIN_WIDTH;
        layout.marginTop = FORM_BODY_MARGIN_TOP;
        layout.marginBottom = FORM_BODY_MARGIN_BOTTOM;
        layout.marginLeft = FORM_BODY_MARGIN_LEFT;
        layout.marginRight = FORM_BODY_MARGIN_RIGHT;
        layout.horizontalSpacing = FORM_BODY_HORIZONTAL_SPACING;
        layout.verticalSpacing = FORM_BODY_VERTICAL_SPACING;
        layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
        layout.numColumns = numColumns;

        return layout;
    }

    public static GridLayout createClearGridLayout(boolean makeColumnsEqualWidth, int numColumns)
    {
        GridLayout layout = new GridLayout();
        layout.marginHeight = CLEAR_MARGIN_HEIGHT;
        layout.marginWidth = CLEAR_MARGIN_WIDTH;
        layout.marginTop = CLEAR_MARGIN_TOP;
        layout.marginBottom = CLEAR_MARGIN_BOTTOM;
        layout.marginLeft = CLEAR_MARGIN_LEFT;
        layout.marginRight = CLEAR_MARGIN_RIGHT;
        layout.horizontalSpacing = CLEAR_HORIZONTAL_SPACING;
        layout.verticalSpacing = CLEAR_VERTICAL_SPACING;
        layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
        layout.numColumns = numColumns;
        return layout;
    }

    public static GridLayout createClearGridLayout()
    {
        GridLayout layout = new GridLayout();
        layout.marginHeight = CLEAR_MARGIN_HEIGHT;
        layout.marginWidth = CLEAR_MARGIN_WIDTH;
        layout.horizontalSpacing = CLEAR_HORIZONTAL_SPACING;
        layout.verticalSpacing = CLEAR_VERTICAL_SPACING;
        return layout;
    }

    public static GridLayout createClearFillLayout()
    {
        GridLayout layout = new GridLayout();
        layout.marginHeight = SECTION_CLIENT_MARGIN_TOP;
        layout.marginWidth = SECTION_CLIENT_MARGIN_BOTTOM;
        return layout;
    }

    public static GridLayout createSectionClientGridLayout(boolean makeColumnsEqualWidth, int numColumns)
    {
        GridLayout layout = new GridLayout();
        layout.marginHeight = SECTION_CLIENT_MARGIN_HEIGHT;
        layout.marginWidth = SECTION_CLIENT_MARGIN_WIDTH;
        layout.marginTop = SECTION_CLIENT_MARGIN_TOP;
        layout.marginBottom = SECTION_CLIENT_MARGIN_BOTTOM;
        layout.marginLeft = SECTION_CLIENT_MARGIN_LEFT;
        layout.marginRight = SECTION_CLIENT_MARGIN_RIGHT;
        layout.horizontalSpacing = SECTION_CLIENT_HORIZONTAL_SPACING;
        layout.verticalSpacing = SECTION_CLIENT_VERTICAL_SPACING;
        layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
        layout.numColumns = numColumns;

        return layout;
    }

    public static TableWrapLayout createClearTableWrapLayout(boolean makeColumnsEqualWidth, int numColumns)
    {
        TableWrapLayout layout = new TableWrapLayout();
        layout.topMargin = CLEAR_MARGIN_TOP;
        layout.bottomMargin = CLEAR_MARGIN_BOTTOM;
        layout.leftMargin = CLEAR_MARGIN_LEFT;
        layout.rightMargin = CLEAR_MARGIN_RIGHT;
        layout.horizontalSpacing = CLEAR_HORIZONTAL_SPACING;
        layout.verticalSpacing = CLEAR_VERTICAL_SPACING;
        layout.makeColumnsEqualWidth = makeColumnsEqualWidth;
        layout.numColumns = numColumns;
        return layout;
    }

    private static final int FORM_BODY_MARGIN_TOP              = 12;
    private static final int FORM_BODY_MARGIN_BOTTOM           = 12;
    private static final int FORM_BODY_MARGIN_LEFT             = 6;
    private static final int FORM_BODY_MARGIN_RIGHT            = 6;
    private static final int FORM_BODY_HORIZONTAL_SPACING      = 20;
    private static final int FORM_BODY_VERTICAL_SPACING        = 17;
    private static final int FORM_BODY_MARGIN_HEIGHT           = 0;
    private static final int FORM_BODY_MARGIN_WIDTH            = 0;

    private static final int CLEAR_MARGIN_TOP                  = 2;
    private static final int CLEAR_MARGIN_BOTTOM               = 2;
    private static final int CLEAR_MARGIN_LEFT                 = 2;
    private static final int CLEAR_MARGIN_RIGHT                = 2;
    private static final int CLEAR_HORIZONTAL_SPACING          = 0;
    private static final int CLEAR_VERTICAL_SPACING            = 0;
    private static final int CLEAR_MARGIN_HEIGHT               = 0;
    private static final int CLEAR_MARGIN_WIDTH                = 0;

    private static final int SECTION_CLIENT_MARGIN_TOP         = 5;
    private static final int SECTION_CLIENT_MARGIN_BOTTOM      = 5;
    private static final int SECTION_CLIENT_MARGIN_LEFT        = 2;
    private static final int SECTION_CLIENT_MARGIN_RIGHT       = 2;
    private static final int SECTION_CLIENT_HORIZONTAL_SPACING = 5;
    private static final int SECTION_CLIENT_VERTICAL_SPACING   = 5;
    private static final int SECTION_CLIENT_MARGIN_HEIGHT      = 0;
    private static final int SECTION_CLIENT_MARGIN_WIDTH       = 0;

}
