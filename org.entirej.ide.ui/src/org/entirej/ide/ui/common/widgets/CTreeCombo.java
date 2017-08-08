/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Schindl - tom.schindl@bestsolution.at (modified CCombo-Code to use a Tree)
 ******************************************************************************/

package org.entirej.ide.ui.common.widgets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.accessibility.AccessibleTextAdapter;
import org.eclipse.swt.accessibility.AccessibleTextEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

public class CTreeCombo extends Composite {
	Text text;
	Tree tree;
	Button arrow;
	Listener listener, filter;
	Shell popup;
	boolean hasFocus;
	int visibleItemCount = 5;
	Color foreground, background;
	Font font;
	List<CTreeComboItem> items = new ArrayList<CTreeComboItem>();
	List<CTreeComboColumn> columns = new ArrayList<CTreeComboColumn>();
	List<TreeListener> treeListeners = new ArrayList<TreeListener>();

	private TreeListener hookListener = new TreeListener() {

		public void treeCollapsed(TreeEvent e) {
			e.item = (Widget) e.item.getData(CTreeComboItem.DATA_ID);
			e.widget = CTreeCombo.this;
			for( TreeListener l: treeListeners ) {
				l.treeCollapsed(e);
			}
		}

		public void treeExpanded(TreeEvent e) {
			e.item = (Widget) e.item.getData(CTreeComboItem.DATA_ID);
			e.widget = CTreeCombo.this;
			for( TreeListener l: treeListeners ) {
				l.treeExpanded(e);
			}
		}

	};

	public CTreeCombo(Composite parent, int style) {
		super (parent, style = checkStyle (style));

		GridLayout layout = new GridLayout(2,false);
		layout.horizontalSpacing=0;
		layout.marginWidth=0;
		layout.marginHeight=0;
		layout.marginTop=0;
		layout.marginBottom=0;
		layout.marginLeft=0;
		layout.marginRight=0;
		layout.verticalSpacing = 0;
		
        setLayout(layout);
		int textStyle = SWT.NONE;
		if ((style & SWT.READ_ONLY) != 0) textStyle |= SWT.READ_ONLY;
		if ((style & SWT.FLAT) != 0) textStyle |= SWT.FLAT;
		text = new Text (this, textStyle);
	
		text.setLayoutData(new GridData(GridData.FILL_BOTH|GridData.GRAB_HORIZONTAL));
		int arrowStyle = SWT.ARROW | SWT.DOWN;
		if ((style & SWT.FLAT) != 0) arrowStyle |= SWT.FLAT;
		arrow = new Button (this, arrowStyle);

		listener = new Listener () {
			public void handleEvent (Event event) {
				if (popup == event.widget) {
					popupEvent (event);
					return;
				}
				if (text == event.widget) {
					textEvent (event);
					return;
				}
				if (tree == event.widget) {
					listEvent (event);
					return;
				}
				if (arrow == event.widget) {
					arrowEvent (event);
					return;
				}
				if (CTreeCombo.this == event.widget) {
					comboEvent (event);
					return;
				}
				if (getShell () == event.widget) {
					getDisplay().asyncExec(new Runnable() {
						public void run() {
							if (isDisposed()) return;
							handleFocus (SWT.FocusOut);
						}
					});
				}
			}
		};
		filter = new Listener() {
			public void handleEvent(Event event) {
				Shell shell = ((Control)event.widget).getShell ();
				if (shell == CTreeCombo.this.getShell ()) {
					handleFocus (SWT.FocusOut);
				}
			}
		};

		int [] comboEvents = {SWT.Dispose, SWT.FocusIn, SWT.Move, SWT.Resize};
		for (int i=0; i<comboEvents.length; i++) this.addListener (comboEvents [i], listener);

		int [] textEvents = {SWT.DefaultSelection, SWT.KeyDown, SWT.KeyUp, SWT.MenuDetect, SWT.Modify, SWT.MouseDown, SWT.MouseUp, SWT.MouseDoubleClick, SWT.MouseWheel, SWT.Traverse, SWT.FocusIn, SWT.Verify};
		for (int i=0; i<textEvents.length; i++) text.addListener (textEvents [i], listener);

		int [] arrowEvents = {SWT.MouseDown, SWT.MouseUp, SWT.Selection, SWT.FocusIn};
		for (int i=0; i<arrowEvents.length; i++) arrow.addListener (arrowEvents [i], listener);

		createPopup(null, null);
		initAccessible();
	}

	static int checkStyle (int style) {
		int mask = SWT.BORDER | SWT.READ_ONLY | SWT.FLAT | SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;
		return SWT.NO_FOCUS | (style & mask);
	}

	void initAccessible() {
		AccessibleAdapter accessibleAdapter = new AccessibleAdapter () {
			public void getName (AccessibleEvent e) {
				String name = null;
				Label label = getAssociatedLabel ();
				if (label != null) {
					name = stripMnemonic (label.getText());
				}
				e.result = name;
			}
			public void getKeyboardShortcut(AccessibleEvent e) {
				String shortcut = null;
				Label label = getAssociatedLabel ();
				if (label != null) {
					String text = label.getText ();
					if (text != null) {
						char mnemonic = _findMnemonic (text);
						if (mnemonic != '\0') {
							shortcut = "Alt+"+mnemonic; //$NON-NLS-1$
						}
					}
				}
				e.result = shortcut;
			}
			public void getHelp (AccessibleEvent e) {
				e.result = getToolTipText ();
			}
		};
		getAccessible ().addAccessibleListener (accessibleAdapter);
		text.getAccessible ().addAccessibleListener (accessibleAdapter);
		tree.getAccessible ().addAccessibleListener (accessibleAdapter);

		arrow.getAccessible ().addAccessibleListener (new AccessibleAdapter() {
			public void getName (AccessibleEvent e) {
				e.result = isDropped () ? SWT.getMessage ("SWT_Close") : SWT.getMessage ("SWT_Open"); //$NON-NLS-1$ //$NON-NLS-2$
			}
			public void getKeyboardShortcut (AccessibleEvent e) {
				e.result = "Alt+Down Arrow"; //$NON-NLS-1$
			}
			public void getHelp (AccessibleEvent e) {
				e.result = getToolTipText ();
			}
		});

		getAccessible().addAccessibleTextListener (new AccessibleTextAdapter() {
			public void getCaretOffset (AccessibleTextEvent e) {
				e.offset = text.getCaretPosition ();
			}
			public void getSelectionRange(AccessibleTextEvent e) {
				Point sel = text.getSelection();
				e.offset = sel.x;
				e.length = sel.y - sel.x;
			}
		});

		getAccessible().addAccessibleControlListener (new AccessibleControlAdapter() {
			public void getChildAtPoint (AccessibleControlEvent e) {
				Point testPoint = toControl (e.x, e.y);
				if (getBounds ().contains (testPoint)) {
					e.childID = ACC.CHILDID_SELF;
				}
			}

			public void getLocation (AccessibleControlEvent e) {
				Rectangle location = getBounds ();
				Point pt = getParent().toDisplay (location.x, location.y);
				e.x = pt.x;
				e.y = pt.y;
				e.width = location.width;
				e.height = location.height;
			}

			public void getChildCount (AccessibleControlEvent e) {
				e.detail = 0;
			}

			public void getRole (AccessibleControlEvent e) {
				e.detail = ACC.ROLE_COMBOBOX;
			}

			public void getState (AccessibleControlEvent e) {
				e.detail = ACC.STATE_NORMAL;
			}

			public void getValue (AccessibleControlEvent e) {
				e.result = getText ();
			}
		});

		text.getAccessible ().addAccessibleControlListener (new AccessibleControlAdapter () {
			public void getRole (AccessibleControlEvent e) {
				e.detail = text.getEditable () ? ACC.ROLE_TEXT : ACC.ROLE_LABEL;
			}
		});

		arrow.getAccessible ().addAccessibleControlListener (new AccessibleControlAdapter() {
			public void getDefaultAction (AccessibleControlEvent e) {
				e.result = isDropped () ? SWT.getMessage ("SWT_Close") : SWT.getMessage ("SWT_Open"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
	}

	boolean isDropped () {
		return popup.getVisible ();
	}

	public String getText () {
		checkWidget ();
		return text.getText ();
	}

	void arrowEvent (Event event) {
		switch (event.type) {
			case SWT.FocusIn: {
				handleFocus (SWT.FocusIn);
				break;
			}
			case SWT.MouseDown: {
				Event mouseEvent = new Event ();
				mouseEvent.button = event.button;
				mouseEvent.count = event.count;
				mouseEvent.stateMask = event.stateMask;
				mouseEvent.time = event.time;
				mouseEvent.x = event.x; mouseEvent.y = event.y;
				notifyListeners (SWT.MouseDown, mouseEvent);
				event.doit = mouseEvent.doit;
				break;
			}
			case SWT.MouseUp: {
				Event mouseEvent = new Event ();
				mouseEvent.button = event.button;
				mouseEvent.count = event.count;
				mouseEvent.stateMask = event.stateMask;
				mouseEvent.time = event.time;
				mouseEvent.x = event.x; mouseEvent.y = event.y;
				notifyListeners (SWT.MouseUp, mouseEvent);
				event.doit = mouseEvent.doit;
				break;
			}
			case SWT.Selection: {
				text.setFocus();
				dropDown (!isDropped ());
				break;
			}
		}
	}

	void handleFocus (int type) {
		if (isDisposed ()) return;
		switch (type) {
			case SWT.FocusIn: {
				if (hasFocus) return;
				if (getEditable ()) text.selectAll ();
				hasFocus = true;
				Shell shell = getShell ();
				shell.removeListener (SWT.Deactivate, listener);
				shell.addListener (SWT.Deactivate, listener);
				Display display = getDisplay ();
				display.removeFilter (SWT.FocusIn, filter);
				display.addFilter (SWT.FocusIn, filter);
				Event e = new Event ();
				notifyListeners (SWT.FocusIn, e);
				break;
			}
			case SWT.FocusOut: {
				if (!hasFocus) return;
				Control focusControl = getDisplay ().getFocusControl ();
				if (focusControl == arrow || focusControl == tree || focusControl == text) return;
				hasFocus = false;
				Shell shell = getShell ();
				shell.removeListener(SWT.Deactivate, listener);
				Display display = getDisplay ();
				display.removeFilter (SWT.FocusIn, filter);
				Event e = new Event ();
				notifyListeners (SWT.FocusOut, e);
				break;
			}
		}
	}

	public boolean getEditable () {
		checkWidget ();
		return text.getEditable();
	}

	void dropDown (boolean drop) {
		if (drop == isDropped () || !isVisible()) return;
		if (!drop) {
			popup.setVisible (false);
			if (!isDisposed () && isFocusControl()) {
				text.setFocus();
			}
			return;
		}

		if (getShell() != popup.getParent ()) {
			TreeItem[] s = tree.getSelection();
			CTreeComboItem selectionIndex = null;

			if( s.length > 0 ) {
				selectionIndex = (CTreeComboItem) s[0].getData(CTreeComboItem.DATA_ID);
			}

			tree.removeListener (SWT.Dispose, listener);
			popup.dispose();
			popup = null;
			tree = null;
			createPopup (items, selectionIndex);
		}

		TreeItem[] items = tree.getSelection();
		if (items.length != 0) tree.showItem(items[0]);

		adjustShellSize();
//		Point size = getSize ();
//		int itemCount = visibleItemCount;
////		int itemCount = tree.getItemCount ();
////		itemCount = (itemCount == 0) ? visibleItemCount : Math.min(visibleItemCount, itemCount);
//		int itemHeight = tree.getItemHeight () * itemCount;
//		Point listSize = tree.computeSize (SWT.DEFAULT, itemHeight, false);
//		tree.setBounds (1, 1, Math.max (size.x - 2, listSize.x), listSize.y);
//
//		TreeItem[] items = tree.getSelection();
//		if (items.length != 0) tree.setTopItem(items[0]);
//		Display display = getDisplay ();
//		Rectangle listRect = tree.getBounds ();
//		Rectangle parentRect = display.map (getParent (), null, getBounds ());
//		Point comboSize = getSize ();
//		Rectangle displayRect = getMonitor ().getClientArea ();
//		int width = Math.max (comboSize.x, listRect.width + 2);
//		int height = listRect.height + 2;
//		int x = parentRect.x;
//		int y = parentRect.y + comboSize.y;
//		if (y + height > displayRect.y + displayRect.height) y = parentRect.y - height;
//		if (x + width > displayRect.x + displayRect.width) x = displayRect.x + displayRect.width - listRect.width;
//		popup.setBounds (x, y, width, height);
//		popup.setVisible (true);
//		if (isFocusControl()) tree.setFocus ();

		popup.setVisible (true);
		popup.setActive();
		if (isFocusControl()) tree.setFocus ();
	}

	private void adjustShellSize() {
		Point size = getSize ();
		int itemCount = visibleItemCount;
//		int itemCount = tree.getItemCount ();
//		itemCount = (itemCount == 0) ? visibleItemCount : Math.min(visibleItemCount, itemCount);
		int itemHeight = tree.getItemHeight () * itemCount;
		if(itemHeight<250)
		    itemHeight = 250;
		Point listSize = tree.computeSize (SWT.DEFAULT, itemHeight, false);
		tree.setBounds (1, 1, Math.max (size.x - 2, listSize.x), listSize.y);

		Display display = getDisplay ();
		Rectangle listRect = tree.getBounds ();
		Rectangle parentRect = display.map (getParent (), null, getBounds ());
		Point comboSize = getSize ();
		Rectangle displayRect = getMonitor ().getClientArea ();
		int width = Math.max (comboSize.x, listRect.width + 2);
		int height = listRect.height + 2;
		int x = parentRect.x;
		int y = parentRect.y + comboSize.y;
		if (y + height > displayRect.y + displayRect.height) y = parentRect.y - height;
		if (x + width > displayRect.x + displayRect.width) x = displayRect.x + displayRect.width - listRect.width;
		popup.setBounds (x, y, width, height);

	}

	void popupEvent(Event event) {
		switch (event.type) {
			case SWT.Paint:
				// draw black rectangle around list
				Rectangle listRect = tree.getBounds();
				Color black = getDisplay().getSystemColor(SWT.COLOR_BLACK);
				event.gc.setForeground(black);
				event.gc.drawRectangle(0, 0, listRect.width + 1, listRect.height + 1);
				break;
			case SWT.Close:
				event.doit = false;
				dropDown (false);
				break;
			case SWT.Deactivate:
				/*
				 * Bug in GTK. When the arrow button is pressed the popup control receives a
				 * deactivate event and then the arrow button receives a selection event. If
				 * we hide the popup in the deactivate event, the selection event will show
				 * it again. To prevent the popup from showing again, we will let the selection
				 * event of the arrow button hide the popup.
				 * In Windows, hiding the popup during the deactivate causes the deactivate
				 * to be called twice and the selection event to be disappear.
				 */
				if (!"carbon".equals(SWT.getPlatform())) {
					Point point = arrow.toControl(getDisplay().getCursorLocation());
					Point size = arrow.getSize();
					Rectangle rect = new Rectangle(0, 0, size.x, size.y);
					if (!rect.contains(point)) dropDown (false);
				} else {
					dropDown(false);
				}
				break;
		}
	}

	void textEvent (Event event) {
		switch (event.type) {
			case SWT.FocusIn: {
				handleFocus (SWT.FocusIn);
				break;
			}
			case SWT.DefaultSelection: {
				dropDown (false);
				Event e = new Event ();
				e.time = event.time;
				e.stateMask = event.stateMask;
				notifyListeners (SWT.DefaultSelection, e);
				break;
			}
//			case SWT.KeyDown: {
//				Event keyEvent = new Event ();
//				keyEvent.time = event.time;
//				keyEvent.character = event.character;
//				keyEvent.keyCode = event.keyCode;
//				keyEvent.stateMask = event.stateMask;
//				notifyListeners (SWT.KeyDown, keyEvent);
//				if (isDisposed ()) break;
//				event.doit = keyEvent.doit;
//				if (!event.doit) break;
//				if (event.keyCode == SWT.ARROW_UP || event.keyCode == SWT.ARROW_DOWN) {
//					event.doit = false;
//					if ((event.stateMask & SWT.ALT) != 0) {
//						boolean dropped = isDropped ();
//						text.selectAll ();
//						if (!dropped) setFocus ();
//						dropDown (!dropped);
//						break;
//					}
//
//					//FIXME We need to make this better by checking collapse/expand
//					CTreeComboItem oldItem = internalGetSelection();
//					int oldIndex = items.indexOf(oldItem);
//
//					if (event.keyCode == SWT.ARROW_UP) {
//						select (items.get(Math.max (oldIndex - 1, 0)));
//					} else {
//						select (items.get(Math.min (oldIndex + 1, items.size() - 1)));
//					}
//
//					if (oldItem != internalGetSelection()) {
//						Event e = new Event();
//						e.time = event.time;
//						e.stateMask = event.stateMask;
//						notifyListeners (SWT.Selection, e);
//					}
//					if (isDisposed ()) break;
//				}
//
//				// Further work : Need to add support for incremental search in
//				// pop up list as characters typed in text widget
//				break;
//			}
			case SWT.KeyUp: {
				Event e = new Event ();
				e.time = event.time;
				e.character = event.character;
				e.keyCode = event.keyCode;
				e.stateMask = event.stateMask;
				notifyListeners (SWT.KeyUp, e);
				event.doit = e.doit;
				break;
			}
			case SWT.MenuDetect: {
				Event e = new Event ();
				e.time = event.time;
				notifyListeners (SWT.MenuDetect, e);
				break;
			}
			case SWT.Modify: {
				tree.deselectAll ();
				Event e = new Event ();
				e.time = event.time;
				notifyListeners (SWT.Modify, e);
				break;
			}
			case SWT.MouseDown: {
				Event mouseEvent = new Event ();
				mouseEvent.button = event.button;
				mouseEvent.count = event.count;
				mouseEvent.stateMask = event.stateMask;
				mouseEvent.time = event.time;
				mouseEvent.x = event.x; mouseEvent.y = event.y;
				notifyListeners (SWT.MouseDown, mouseEvent);
				if (isDisposed ()) break;
				event.doit = mouseEvent.doit;
				if (!event.doit) break;
				if (event.button != 1) return;
				if (text.getEditable ()) return;
				boolean dropped = isDropped ();
				text.selectAll ();
				if (!dropped) setFocus ();
				dropDown (!dropped);
				break;
			}
			case SWT.MouseUp: {
				Event mouseEvent = new Event ();
				mouseEvent.button = event.button;
				mouseEvent.count = event.count;
				mouseEvent.stateMask = event.stateMask;
				mouseEvent.time = event.time;
				mouseEvent.x = event.x; mouseEvent.y = event.y;
				notifyListeners (SWT.MouseUp, mouseEvent);
				if (isDisposed ()) break;
				event.doit = mouseEvent.doit;
				if (!event.doit) break;
				if (event.button != 1) return;
				if (text.getEditable ()) return;
				text.selectAll ();
				break;
			}
			case SWT.MouseDoubleClick: {
				Event mouseEvent = new Event ();
				mouseEvent.button = event.button;
				mouseEvent.count = event.count;
				mouseEvent.stateMask = event.stateMask;
				mouseEvent.time = event.time;
				mouseEvent.x = event.x; mouseEvent.y = event.y;
				notifyListeners (SWT.MouseDoubleClick, mouseEvent);
				break;
			}
//			case SWT.MouseWheel: {
//				Event keyEvent = new Event ();
//				keyEvent.time = event.time;
//				keyEvent.keyCode = event.count > 0 ? SWT.ARROW_UP : SWT.ARROW_DOWN;
//				keyEvent.stateMask = event.stateMask;
//				notifyListeners (SWT.KeyDown, keyEvent);
//				if (isDisposed ()) break;
//				event.doit = keyEvent.doit;
//				if (!event.doit) break;
//				if (event.count != 0) {
//					event.doit = false;
//
//					CTreeComboItem oldItem = internalGetSelection();
//					int oldIndex = items.indexOf(oldItem);
//					if (event.count > 0) {
//						select (items.get(Math.max (oldIndex - 1, 0)));
//					} else {
//						select (items.get(Math.min (oldIndex + 1, items.size() - 1)));
//					}
//					if (oldItem != internalGetSelection()) {
//						Event e = new Event();
//						e.time = event.time;
//						e.stateMask = event.stateMask;
//						notifyListeners (SWT.Selection, e);
//					}
//					if (isDisposed ()) break;
//				}
//				break;
//			}
			case SWT.Traverse: {
				switch (event.detail) {
					case SWT.TRAVERSE_ARROW_PREVIOUS:
					case SWT.TRAVERSE_ARROW_NEXT:
						// The enter causes default selection and
						// the arrow keys are used to manipulate the list contents so
						// do not use them for traversal.
						event.doit = false;
						break;
					case SWT.TRAVERSE_TAB_PREVIOUS:
						event.doit = traverse(SWT.TRAVERSE_TAB_PREVIOUS);
						event.detail = SWT.TRAVERSE_NONE;
						return;
				}
				Event e = new Event ();
				e.time = event.time;
				e.detail = event.detail;
				e.doit = event.doit;
				e.character = event.character;
				e.keyCode = event.keyCode;
				notifyListeners (SWT.Traverse, e);
				event.doit = e.doit;
				event.detail = e.detail;
				break;
			}
			case SWT.Verify: {
				Event e = new Event ();
				e.text = event.text;
				e.start = event.start;
				e.end = event.end;
				e.character = event.character;
				e.keyCode = event.keyCode;
				e.stateMask = event.stateMask;
				notifyListeners (SWT.Verify, e);
				event.doit = e.doit;
				break;
			}
		}
	}

	void listEvent (Event event) {
		switch (event.type) {
			case SWT.Dispose:
				if (getShell () != popup.getParent ()) {
					CTreeComboItem selectionIndex = internalGetSelection();
					popup = null;
					tree = null;
					createPopup (items, selectionIndex);
				}
				break;
			case SWT.FocusIn: {
				handleFocus (SWT.FocusIn);
				break;
			}
			case SWT.MouseUp: {
//				if (event.button != 1) return;
//				Point pt = new Point(event.x,event.y);
//				TreeItem item = tree.getItem(pt);
//
//				if( item != null && ! item.getTextBounds(0).contains(pt) ) {
//					dropDown (false);
//				}

				break;
			}
			case SWT.Selection: {
				TreeItem[] items = tree.getSelection();
				if (items.length == 0) return;
				text.setFont(text.getFont());
				text.setText (items[0].getText());
				text.selectAll ();
				tree.setSelection (items);
				Event e = new Event ();
				e.time = event.time;
				e.stateMask = event.stateMask;
				e.doit = event.doit;
				notifyListeners (SWT.Selection, e);
				event.doit = e.doit;
//				dropDown(false);
				break;
			}
			case SWT.Traverse: {
				switch (event.detail) {
					case SWT.TRAVERSE_RETURN:
					case SWT.TRAVERSE_ESCAPE:
					case SWT.TRAVERSE_ARROW_PREVIOUS:
					case SWT.TRAVERSE_ARROW_NEXT:
						event.doit = false;
						break;
					case SWT.TRAVERSE_TAB_NEXT:
					case SWT.TRAVERSE_TAB_PREVIOUS:
						event.doit = text.traverse(event.detail);
						event.detail = SWT.TRAVERSE_NONE;
						if (event.doit) dropDown(false);
						return;
				}
				Event e = new Event ();
				e.time = event.time;
				e.detail = event.detail;
				e.doit = event.doit;
				e.character = event.character;
				e.keyCode = event.keyCode;
				notifyListeners (SWT.Traverse, e);
				event.doit = e.doit;
				event.detail = e.detail;
				break;
			}
			case SWT.KeyUp: {
				Event e = new Event ();
				e.time = event.time;
				e.character = event.character;
				e.keyCode = event.keyCode;
				e.stateMask = event.stateMask;
				notifyListeners (SWT.KeyUp, e);
				break;
			}
			case SWT.KeyDown: {
				if (event.character == SWT.ESC) {
					// Escape key cancels popup list
					dropDown (false);
				}
				if ((event.stateMask & SWT.ALT) != 0 && (event.keyCode == SWT.ARROW_UP || event.keyCode == SWT.ARROW_DOWN)) {
					dropDown (false);
				}
				if (event.character == SWT.CR) {
					// Enter causes default selection
					dropDown (false);
					Event e = new Event ();
					e.time = event.time;
					e.stateMask = event.stateMask;
					notifyListeners (SWT.DefaultSelection, e);
				}
				// At this point the widget may have been disposed.
				// If so, do not continue.
				if (isDisposed ()) break;
				Event e = new Event();
				e.time = event.time;
				e.character = event.character;
				e.keyCode = event.keyCode;
				e.stateMask = event.stateMask;
				notifyListeners(SWT.KeyDown, e);
				break;

			}
			case SWT.Collapse: {
				adjustShellSize();
				break;
			}
			case SWT.Expand: {
				adjustShellSize();
				break;
			}
		}
	}

	void comboEvent (Event event) {
		switch (event.type) {
			case SWT.Dispose:
				if (popup != null && !popup.isDisposed ()) {
					tree.removeListener (SWT.Dispose, listener);
					popup.dispose ();
				}
				Shell shell = getShell ();
				shell.removeListener (SWT.Deactivate, listener);
				Display display = getDisplay ();
				display.removeFilter (SWT.FocusIn, filter);
				popup = null;
				text = null;
				tree = null;
				arrow = null;
				break;
			case SWT.FocusIn:
				Control focusControl = getDisplay ().getFocusControl ();
				if (focusControl == arrow || focusControl == tree) return;
				if (isDropped()) {
					tree.setFocus();
				} else {
					text.setFocus();
				}
				break;
			case SWT.Move:
				dropDown (false);
				break;
			case SWT.Resize:
				internalLayout (false);
				break;
		}
	}

	void internalLayout (boolean changed) {
		if (isDropped ()) dropDown (false);
		Rectangle rect = getClientArea ();
		int width = rect.width;
		int height = rect.height;
		Point arrowSize = arrow.computeSize (SWT.DEFAULT, height, changed);
		text.setBounds (0, 0, width - arrowSize.x, height);
		arrow.setBounds (width - arrowSize.x, 0, arrowSize.x, arrowSize.y);
	}

	Label getAssociatedLabel () {
		Control[] siblings = getParent ().getChildren ();
		for (int i = 0; i < siblings.length; i++) {
			if (siblings [i] == this) {
				if (i > 0 && siblings [i-1] instanceof Label) {
					return (Label) siblings [i-1];
				}
			}
		}
		return null;
	}

	String stripMnemonic (String string) {
		int index = 0;
		int length = string.length ();
		do {
			while ((index < length) && (string.charAt (index) != '&')) index++;
			if (++index >= length) return string;
			if (string.charAt (index) != '&') {
				return string.substring(0, index-1) + string.substring(index, length);
			}
			index++;
		} while (index < length);
	 	return string;
	}

	char _findMnemonic (String string) {
		if (string == null) return '\0';
		int index = 0;
		int length = string.length ();
		do {
			while (index < length && string.charAt (index) != '&') index++;
			if (++index >= length) return '\0';
			if (string.charAt (index) != '&') return Character.toLowerCase (string.charAt (index));
			index++;
		} while (index < length);
	 	return '\0';
	}

	void createPopup(Collection<CTreeComboItem> items, CTreeComboItem selectedItem) {
		// create shell and list
		popup = new Shell (getShell (), SWT.NO_TRIM | SWT.ON_TOP);
		int style = getStyle ();
		int listStyle = SWT.H_SCROLL | SWT.V_SCROLL | SWT.SINGLE;
		if ((style & SWT.FLAT) != 0) listStyle |= SWT.FLAT;
		if ((style & SWT.RIGHT_TO_LEFT) != 0) listStyle |= SWT.RIGHT_TO_LEFT;
		if ((style & SWT.LEFT_TO_RIGHT) != 0) listStyle |= SWT.LEFT_TO_RIGHT;
		tree = new Tree (popup, listStyle);
		tree.addTreeListener(hookListener);
		if (font != null) tree.setFont (font);
		if (foreground != null) tree.setForeground (foreground);
		if (background != null) tree.setBackground (background);

		int [] popupEvents = {SWT.Close, SWT.Paint, SWT.Deactivate};
		for (int i=0; i<popupEvents.length; i++) popup.addListener (popupEvents [i], listener);
		int [] listEvents = {SWT.MouseUp, SWT.Selection, SWT.Traverse, SWT.KeyDown, SWT.KeyUp, SWT.FocusIn, SWT.Dispose, SWT.Collapse, SWT.Expand};
		for (int i=0; i<listEvents.length; i++) tree.addListener (listEvents [i], listener);

		for( CTreeComboColumn c: columns ) {
			TreeColumn col = new TreeColumn(tree,SWT.NONE);
			c.setRealTreeColumn(col);
		}

		if (items != null) {
			createTreeItems(items.toArray(new CTreeComboItem[0]));
		}

		if (selectedItem != null) {
			tree.setSelection(selectedItem.getRealTreeItem());
		}
	}

	private void createTreeItems(CTreeComboItem[] items) {
		for( CTreeComboItem item : items ) {
			TreeItem ti = new TreeItem(tree,item.getStyle());
			item.setRealTreeItem(ti);
			createTreeItems(item.getItems());
		}
	}

	public CTreeComboItem[] getSelection() {
		checkWidget ();

		TreeItem[] items = tree.getSelection();

		if( items.length > 0 ) {
			return new CTreeComboItem[] { (CTreeComboItem) items[0].getData(CTreeComboItem.DATA_ID) };
		}

		return new CTreeComboItem[0];
	}

	private CTreeComboItem internalGetSelection() {
		CTreeComboItem[] is = getSelection();
		if( is.length != 0 ) {
			return is[0];
		}

		return null;
	}

	public void select (CTreeComboItem item) {
		checkWidget();
		if (item == null) {
			tree.deselectAll ();
			text.setFont(text.getFont());
			text.setText (""); //$NON-NLS-1$
			return;
		}

		if (item != null) {
			if (item != internalGetSelection()) {
				text.setFont(text.getFont());
				text.setText (item.getText());
				text.selectAll ();
				tree.select (item.getRealTreeItem());
				tree.showSelection ();
			}
		}
	}

	public Point computeSize (int wHint, int hHint, boolean changed) {
		checkWidget ();
		int width = 0, height = 0;
		TreeItem[] items = tree.getItems ();
		GC gc = new GC (text);
		int spacer = gc.stringExtent (" ").x; //$NON-NLS-1$
		int textWidth = gc.stringExtent (text.getText ()).x;
		for (int i = 0; i < items.length; i++) {
			textWidth = Math.max (gc.stringExtent (items[i].getText()).x, textWidth);
		}
		gc.dispose ();
		Point textSize = text.computeSize (SWT.DEFAULT, SWT.DEFAULT, changed);
		Point arrowSize = arrow.computeSize (SWT.DEFAULT, SWT.DEFAULT, changed);
		Point listSize = tree.computeSize (SWT.DEFAULT, SWT.DEFAULT, changed);
		int borderWidth = getBorderWidth ();

		height = Math.max (textSize.y, arrowSize.y+5);
		width = Math.max (textWidth + 2*spacer + arrowSize.x + 2*borderWidth, listSize.x);
		if (wHint != SWT.DEFAULT) width = wHint;
		if (hHint != SWT.DEFAULT) height = hHint;
		return new Point (width + 2*borderWidth, height + 2*borderWidth);
	}

	public void addTreeListener(TreeListener listener) {
		treeListeners.add(listener);
	}

	public int getColumnCount() {
		return tree.getColumnCount();
	}

	public int indexOf(CTreeComboItem item) {
		return tree.indexOf(item.getRealTreeItem());
	}

	public int getItemCount() {
		return tree.getItemCount();
	}

	public CTreeComboItem getItem(int index) {
		return items.get(index);
	}

	public int[] getColumnOrder() {
		return tree.getColumnOrder();
	}

	public CTreeComboColumn getColumn(int columnIndex) {
		return columns.get(columnIndex);
	}

	public void setSelection(CTreeComboItem[] newItems) {
		TreeItem[] items = new TreeItem[newItems.length];
		for( int i = 0; i < items.length; i++ ) {
			items[i] = newItems[i].getRealTreeItem();
		}
		if( items.length == 0 ) {
			text.setFont(text.getFont());
			text.setText (""); //$NON-NLS-1$
		} else {
			text.setFont(text.getFont());
			text.setText(items[0].getText());
		}
		tree.setSelection(items);

	}

	public void setItemCount(int count) {
		tree.setItemCount(count);
	}

	public CTreeComboItem[] getItems() {
		return items.toArray(new CTreeComboItem[0]);
	}

	public CTreeComboItem getItem(Point p) {
		TreeItem item = tree.getItem(p);

		if( item != null ) {
			item.getData(CTreeComboItem.DATA_ID);
		}

		return null;
	}

	public void removeAll() {
		tree.removeAll();
		items.clear();
	}

	public void showItem(CTreeComboItem item) {
		tree.showItem(item.getRealTreeItem());
	}

	public void clear(int indexToDisassociate, boolean b) {
		tree.clear(indexToDisassociate, b);
	}

	public void clearAll(boolean b) {
		tree.clearAll(b);
	}
	
	public void hideDropDown()
	{
	    if(popup!=null && popup.isVisible())
	        popup.setVisible(false);
	}
}
