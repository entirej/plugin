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
package org.entirej.ide.ui.table;

import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableColumn;

public class TableViewerColumnFactory
{
    private final TableViewer viewer;

    public TableViewerColumnFactory(TableViewer viewer)
    {
        super();
        this.viewer = viewer;
    }

    public TableViewerColumn createColumn(String header, int width, ColumnLabelProvider provider)
    {
        return createColumn(header, width, provider, SWT.LEFT);

    }

    public TableViewerColumn createColumn(String header, int width, ColumnLabelProvider provider, int alignment)
    {
        final TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
        final TableColumn column = viewerColumn.getColumn();
        column.setText(header);
        column.setWidth(width);
        column.setResizable(true);
        column.setMoveable(true);
        column.setAlignment(alignment);
        viewerColumn.setLabelProvider(provider);

        return viewerColumn;

    }

}
