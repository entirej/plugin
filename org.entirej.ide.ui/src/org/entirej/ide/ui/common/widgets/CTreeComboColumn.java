/*******************************************************************************
 * Copyright (c) 2008 BestSolution.at Systemhaus GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     tom.schindl@bestsolution - initial API and implementation
 ******************************************************************************/

package org.entirej.ide.ui.common.widgets;

import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeColumn;

public class CTreeComboColumn extends Item {
	private TreeColumn realTreeColumn;
	private CTreeCombo parent;
	private int width;
	
	public CTreeComboColumn(CTreeCombo parent, int style) {
		super(parent, style);
		this.parent = parent;
		this.parent.columns.add(this);
		
		if( this.parent.tree != null && ! this.parent.tree.isDisposed() ) {
			setRealTreeColumn(new TreeColumn(parent.tree,style));	
		}		
	}
	
	public CTreeComboColumn(CTreeCombo parent, int style, int index) {
		super(parent, style, index);
		this.parent = parent;
		this.parent.columns.add(this);
		
		if( this.parent.tree != null && ! this.parent.tree.isDisposed() ) {
			setRealTreeColumn(new TreeColumn(parent.tree,style,index));	
		}
	}

	void setRealTreeColumn(TreeColumn realTreeColumn) {
		this.realTreeColumn = realTreeColumn;
		this.realTreeColumn.setText(getText());
		this.realTreeColumn.setWidth(width);
	}
	
	TreeColumn getRealTreeColumn() {
		return realTreeColumn;
	}

	private boolean checkRealColumn() {
		return realTreeColumn != null && ! realTreeColumn.isDisposed();
	}
	
	public int getWidth() {
		if( checkRealColumn() ) {
			return realTreeColumn.getWidth();
		}
		return 0;
	}
	
	public void setWidth(int width) {
		this.width = width;
		realTreeColumn.setWidth(width);
	}

	@Override
	public void setText(String string) {
		super.setText(string);
		realTreeColumn.setText(string);
	}
	
	
}
