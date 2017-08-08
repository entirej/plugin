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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeItem;
import org.entirej.ide.ui.EJUIPlugin;

public class CTreeComboItem extends Item {
	static final String DATA_ID = EJUIPlugin.getID()+".ctreeitem";
	private CTreeCombo parent;
	private CTreeComboItem parentItem;
	private List<CTreeComboItem> childItems = new ArrayList<CTreeComboItem>();
	private TreeItem realTreeItem;
	
	public CTreeComboItem(CTreeComboItem parentItem, int style, int index) {
		super(parentItem.parent,style);
		this.parent = parentItem.parent;
		this.parentItem = parentItem;
		this.parentItem.childItems.add(index, this);
		
		if( parentItem.realTreeItem != null && ! parentItem.realTreeItem.isDisposed() ) {
			setRealTreeItem(new TreeItem(parentItem.realTreeItem,style,index));
		}
	}

	public CTreeComboItem(CTreeComboItem parentItem, int style) {
		super(parentItem.parent,style);
		this.parent = parentItem.parent;
		this.parentItem = parentItem;
		this.parentItem.childItems.add(this);
		
		if( parentItem.realTreeItem != null && ! parentItem.realTreeItem.isDisposed() ) {
			setRealTreeItem(new TreeItem(parentItem.realTreeItem,style));
		}
	}
	
	public CTreeComboItem(CTreeCombo parent, int style, int index) {
		super(parent, style);
		this.parent = parent;
		this.parent.items.add(index, this);
		
		if( this.parent.tree != null && ! this.parent.tree.isDisposed() ) {
			setRealTreeItem(new TreeItem(this.parent.tree,style,index));
		}
	}

	public CTreeComboItem(CTreeCombo parent, int style) {
		super(parent, style);
		this.parent = parent;
		this.parent.items.add(this);
		
		if( this.parent.tree != null && ! this.parent.tree.isDisposed() ) {
			setRealTreeItem(new TreeItem(this.parent.tree,style));
		}
	}
	
	public void dispose() {
		super.dispose();
		if( realTreeItem != null && !realTreeItem.isDisposed() ) {
			realTreeItem.dispose();
		}
		
		if( this.parentItem != null && ! this.parentItem.isDisposed() ) {
			this.parentItem.childItems.remove(this);
		}
		
		for( CTreeComboItem i: childItems ) {
			i.dispose();
		}
	}
	
	void setRealTreeItem(TreeItem realTreeItem) {
		this.realTreeItem = realTreeItem;
		this.realTreeItem.setData(DATA_ID, this);
	}
	
	TreeItem getRealTreeItem() {
		return this.realTreeItem;
	}
	
	public CTreeComboItem[] getItems() {
		return childItems.toArray(new CTreeComboItem[0]);
	}

	private boolean checkRealItem() {
		return realTreeItem != null && ! realTreeItem.isDisposed();
	}
	
	@Override
	public void setImage(Image image) {
		super.setImage(image);
		if( checkRealItem() ) {
			realTreeItem.setImage(image);
		}
	}

	@Override
	public void setText(String string) {
		super.setText(string);
		if( checkRealItem() ) {
			realTreeItem.setText(string);
		}
	}

	public Rectangle getBounds(int i) { 
		if( checkRealItem() ) {
			return realTreeItem.getBounds(i);
		}
		return null;
	}

	public Rectangle getBounds() {
		if( checkRealItem() ) {
			return realTreeItem.getBounds();
		}
		return null;
	}

	public CTreeCombo getParent() {
		return parent;
	}

	public Color getBackground(int columnIndex) {
		if( checkRealItem() ) {
			return realTreeItem.getBackground(columnIndex);
		}
		return null;
	}

	public Font getFont(int columnIndex) {
		if( checkRealItem() ) {
			return realTreeItem.getFont(columnIndex);
		}
		return null;
	}

	public Color getForeground(int columnIndex) {
		if( checkRealItem() ) {
			return realTreeItem.getForeground(columnIndex);
		}
		return null;
	}

	public Image getImage(int columnIndex) {
		if( checkRealItem() ) {
			return realTreeItem.getImage(columnIndex);
		}
		return null;
	}

	public String getText(int columnIndex) {
		if( checkRealItem() ) {
			return realTreeItem.getText(columnIndex); 
		}
		return null;
	}

	public void setBackground(int columnIndex, Color color) {
		if( checkRealItem() ) {
			realTreeItem.setBackground(columnIndex, color);
		}
	}

	public void setFont(int columnIndex, Font font) {
		if( checkRealItem() ) {
			realTreeItem.setFont(columnIndex, font);
		}
	}

	public void setForeground(int columnIndex, Color color) {
		if( checkRealItem() ) {
			realTreeItem.setForeground(columnIndex, color);
		}
	}

	public void setImage(int columnIndex, Image image) {
		if( checkRealItem() ) {
			realTreeItem.setImage(columnIndex, image);
		}
	}

	public void setText(int columnIndex, String string) {
		if( checkRealItem() ) {
			realTreeItem.setText(columnIndex, string);
		}
	}

	public CTreeComboItem getParentItem() {
		return parentItem;
	}

	public boolean getExpanded() {
		if( checkRealItem() ) {
			return realTreeItem.getExpanded();
		}
		return false;
	}

	public int getItemCount() {
		return childItems.size();
	}

	public CTreeComboItem getItem(int i) {
		return childItems.get(i);
	}

	public int indexOf(CTreeComboItem item) {
		return childItems.indexOf(item); 
	}

	public Rectangle getTextBounds(int index) {
		if( checkRealItem() ) {
			return realTreeItem.getBounds(index);
		}
		
		return null;
	}

	public Rectangle getImageBounds(int index) {
		if( checkRealItem() ) {
			return realTreeItem.getImageBounds(index);
		}
		return null;
	}

	public void setExpanded(boolean expand) {
		if( checkRealItem() ) {
			realTreeItem.setExpanded(expand);
		}
	}

	public void setItemCount(int count) {
		if( checkRealItem() ) {
			realTreeItem.setItemCount(count);
		}
	}

	public void clear(int indexToDisaccociate, boolean b) {
		realTreeItem.clear(indexToDisaccociate, b);
	}

	public void clearAll(boolean b) {
		realTreeItem.clearAll(b);
	}
}