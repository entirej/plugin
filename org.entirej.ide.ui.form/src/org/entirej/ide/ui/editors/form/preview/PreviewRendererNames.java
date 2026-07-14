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
package org.entirej.ide.ui.editors.form.preview;

/**
 * Name matching shared by the preview fallback path.
 * <p>
 * Renderer definitions that implement the {@code EJDev*PreviewProvider} interfaces describe
 * themselves explicitly. Everything else - notably third-party custom renderers - is matched on its
 * renderer name and definition class name instead. Both the descriptor resolver and the model
 * builder do that matching, so the fragments live here and cannot drift apart.
 */
final class PreviewRendererNames
{
    /**
     * Fragments identifying a multi-record renderer, i.e. one that lays its block items out as
     * table columns rather than as a single record of controls.
     */
    private static final String[] MULTI_RECORD = { "multirecord", "multi record", "multi_record", "multitable", "multi table", "multi_table" };

    private PreviewRendererNames()
    {
    }

    /**
     * Folds a renderer name and its definition's class name into one lower-case string to match
     * against. Either may be <code>null</code>.
     */
    static String normalized(String rendererName, Object definition)
    {
        StringBuilder builder = new StringBuilder();
        if (rendererName != null)
        {
            builder.append(rendererName);
        }
        if (definition != null)
        {
            builder.append(' ');
            builder.append(definition.getClass().getSimpleName());
        }
        return builder.toString().toLowerCase();
    }

    static boolean contains(String value, String... fragments)
    {
        if (value == null)
        {
            return false;
        }
        for (String fragment : fragments)
        {
            if (value.indexOf(fragment) > -1)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether the renderer lays its items out as table columns. Only these renderers get column
     * labels in the preview; a single-record renderer draws its items as individual controls.
     */
    static boolean isMultiRecordRenderer(String rendererName, Object definition)
    {
        return contains(normalized(rendererName, definition), MULTI_RECORD);
    }
}
