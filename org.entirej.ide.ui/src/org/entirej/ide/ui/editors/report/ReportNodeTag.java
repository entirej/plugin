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
package org.entirej.ide.ui.editors.report;

public interface ReportNodeTag
{

    public static final String BLOCK_ID         = "b_id";
    public static final String ITEM_ID          = "i_id";

    public static final int    NONE             = 0;
    public static final int    REPORT             = 1 << 1;
    public static final int    GROUP            = 1 << 2;
    public static final int    BLOCK            = 1 << 3;
    public static final int    RENDERER         = 1 << 4;
    public static final int    TITLE            = 1 << 5;
    public static final int    ACTION_PROCESSOR = 1 << 6;
    public static final int    COL              = 1 << 7;
    public static final int    WIDTH            = 1 << 8;
    public static final int    HEIGHT           = 1 << 9;
    public static final int    REF              = 1 << 10;
    public static final int    CANVAS           = 1 << 11;
    public static final int    SERVICE          = 1 << 12;
    public static final int    MAPPING          = 1 << 13;
    public static final int    LOV              = 1 << 14;
    public static final int    REALTION         = 1 << 15;
    public static final int    ITEM             = 1 << 16;
    public static final int    TYPE             = 1 << 17;
    public static final int    MASTER           = 1 << 18;
    public static final int    DETAIL           = 1 << 19;
    public static final int    MAIN             = 1 << 20;
    public static final int    INSET            = 1 << 21;
    public static final int    UPDATE           = 1 << 22;
    public static final int    QUERY            = 1 << 23;
    public static final int    OBJGROUP         = 1 << 24;

}
