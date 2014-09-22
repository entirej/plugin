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
package org.entirej.ide.ui;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.swt.graphics.Image;

public class EJUIImages
{
    private EJUIImages()
    {
    }

    public final static String         ICONS_PATH      = "icons/";                //$NON-NLS-1$
    private static final String        PATH_OBJ16      = ICONS_PATH + "obj16/";   //$NON-NLS-1$
    private static final String        PATH_ETOOLS16   = ICONS_PATH + "etools16/"; //$NON-NLS-1$
    private static final String        PATH_DTOOLS16   = ICONS_PATH + "dtools16/"; //$NON-NLS-1$
    private static final String        PATH_WIZBAN     = ICONS_PATH + "wizban/";  //$NON-NLS-1$

    private final static ImageRegistry PLUGIN_REGISTRY = new ImageRegistry();

    private static ImageDescriptor create(String prefix, String name)
    {
        return ImageDescriptor.createFromURL(makeImageURL(prefix, name));
    }

    public static ImageDescriptor createOverlay(ImageDescriptor base, ImageDescriptor overlay, int quadrant)
    {
        return new DecorationOverlayIcon(getImage(base), overlay, quadrant);
    }

    public static ImageDescriptor createOverlay(Image base, ImageDescriptor overlay, int quadrant)
    {
        return new DecorationOverlayIcon((base), overlay, quadrant);
    }

    private static URL makeImageURL(String prefix, String name)
    {
        String path = "$nl$/" + prefix + name; //$NON-NLS-1$
        return FileLocator.find(EJUIPlugin.getDefault().getBundle(), new Path(path), null);
    }

    public static Image getImage(ImageDescriptor desc)
    {
        String key = String.valueOf(desc.hashCode());
        Image image = PLUGIN_REGISTRY.get(key);
        if (image == null)
        {
            image = desc.createImage();
            PLUGIN_REGISTRY.put(key, image);
        }
        return image;
    }

    public static final ImageDescriptor DESC_NEWEJPRJ_WIZ            = create(PATH_WIZBAN, "newejprj_wiz.png");                                        //$NON-NLS-1$
    public static final ImageDescriptor DESC_NEWEJFRM_WIZ            = create(PATH_WIZBAN, "newejfrm_wiz.png");                                        //$NON-NLS-1$
    public static final ImageDescriptor DESC_NEWEJPOJO_SERV_WIZ      = create(PATH_WIZBAN, "newservpojo_wiz.png");                                     //$NON-NLS-1$

    public static final ImageDescriptor DESC_HELP                    = create(PATH_ETOOLS16, "help.gif");                                              //$NON-NLS-1$

    public static final ImageDescriptor DESC_COLLAPSE_ALL            = create(PATH_ETOOLS16, "collapseall.gif");                                       //$NON-NLS-1$
    public static final ImageDescriptor DESC_EXPAND_ALL              = create(PATH_ETOOLS16, "expandall.gif");                                         //$NON-NLS-1$
    public static final ImageDescriptor DESC_UP                      = create(PATH_ETOOLS16, "up.gif");                                                //$NON-NLS-1$
    public static final ImageDescriptor DESC_DOWN                    = create(PATH_ETOOLS16, "down.gif");                                              //$NON-NLS-1$
    public static final ImageDescriptor DESC_ADD_ITEM                = create(PATH_ETOOLS16, "add_item.gif");                                          //$NON-NLS-1$
    public static final ImageDescriptor DESC_DELETE_ITEM             = create(PATH_ETOOLS16, "delete_item.gif");                                       //$NON-NLS-1$
    public static final ImageDescriptor DESC_DELETE_ITEM_DISABLED    = create(PATH_DTOOLS16, "delete_item.gif");                                       //$NON-NLS-1$
    public static final ImageDescriptor DESC_REFRESH                 = create(PATH_ETOOLS16, "refresh.gif");                                           //$NON-NLS-1$
    public static final ImageDescriptor DESC_ALPHAB_SORT_CO          = create(PATH_ETOOLS16, "alphab_sort_co.gif");                                    //$NON-NLS-1$
    public static final ImageDescriptor DESC_ERROR_CO                = create(PATH_ETOOLS16, "error_co.gif");                                          //$NON-NLS-1$
    public static final ImageDescriptor DESC_WARNING_CO              = create(PATH_ETOOLS16, "warning_co.gif");                                        //$NON-NLS-1$
    public static final ImageDescriptor DESC_DESELECT_ALL            = create(PATH_ETOOLS16, "deselect_all.gif");                                      //$NON-NLS-1$
    public static final ImageDescriptor DESC_SELECT_ALL              = create(PATH_ETOOLS16, "select_all.gif");                                        //$NON-NLS-1$
    public static final ImageDescriptor DESC_FORM_EDIT_PROP          = create(PATH_ETOOLS16, "form_edit_prop.gif");                                    //$NON-NLS-1$
    public static final ImageDescriptor DESC_FORM_EDIT_TREE          = create(PATH_ETOOLS16, "form_edit_tree.gif");                                    //$NON-NLS-1$
    public static final ImageDescriptor DESC_FORM_MOVE_OBJ           = create(PATH_ETOOLS16, "move_object.png");                                       //$NON-NLS-1$
    public static final ImageDescriptor DESC_FORM_RESIZE_OBJ         = create(PATH_ETOOLS16, "resize_object.gif");                                     //$NON-NLS-1$

    public static final ImageDescriptor DESC_MENU_GROUP              = create(PATH_OBJ16, "menu_group.gif");                                           //$NON-NLS-1$
    public static final ImageDescriptor DESC_ACTION                  = create(PATH_OBJ16, "action.gif");                                               //$NON-NLS-1$
    public static final ImageDescriptor DESC_ACTION_LIB              = create(PATH_OBJ16, "action_lib.gif");                                           //$NON-NLS-1$
    public static final ImageDescriptor DESC_MENU_SEPARATOR          = create(PATH_OBJ16, "menu_separator.gif");                                       //$NON-NLS-1$

    public static final ImageDescriptor DESC_LAYOUT_MAIN             = create(PATH_OBJ16, "layout_main.gif");                                          //$NON-NLS-1$
    public static final ImageDescriptor DESC_LAYOUT_GROUP            = create(PATH_OBJ16, "layout_group.gif");                                         //$NON-NLS-1$
    public static final ImageDescriptor DESC_LAYOUT_SPLIT            = create(PATH_OBJ16, "layout_group.gif");                                         //$NON-NLS-1$
    public static final ImageDescriptor DESC_LAYOUT_COMP             = create(PATH_OBJ16, "layout_comp.gif");                                          //$NON-NLS-1$
    public static final ImageDescriptor DESC_LAYOUT_TAB              = create(PATH_OBJ16, "layout_tab.gif");                                           //$NON-NLS-1$

    public static final ImageDescriptor DESC_CANVAS_TAB_PAGE         = create(PATH_OBJ16, "canvas_tab_page.png");                                      //$NON-NLS-1$
    public static final ImageDescriptor DESC_CANVAS_TAB_PAGE_REF     = create(PATH_OBJ16, "canvas_tab_page_ref.png");                                  //$NON-NLS-1$
    public static final ImageDescriptor DESC_CANVAS_TAB              = create(PATH_OBJ16, "canvas_tab.png");                                           //$NON-NLS-1$
    public static final ImageDescriptor DESC_CANVAS_BLOCK            = create(PATH_OBJ16, "canvas_block.png");                                         //$NON-NLS-1$
    public static final ImageDescriptor DESC_CANVAS_GROUP            = create(PATH_OBJ16, "canvas_group.png");                                         //$NON-NLS-1$
    public static final ImageDescriptor DESC_CANVAS_FORM             = create(PATH_OBJ16, "form_canvas.png");                                          //$NON-NLS-1$
    public static final ImageDescriptor DESC_CANVAS_STACKED          = create(PATH_OBJ16, "canvas_stacked.png");                                       //$NON-NLS-1$
    public static final ImageDescriptor DESC_CANVAS_POPUP            = create(PATH_OBJ16, "canvas_popup.png");                                         //$NON-NLS-1$
    public static final ImageDescriptor DESC_CANVAS_TAB_REF          = create(PATH_OBJ16, "canvas_tab_ref.png");                                       //$NON-NLS-1$
    public static final ImageDescriptor DESC_CANVAS_BLOCK_REF        = create(PATH_OBJ16, "canvas_block_ref.png");                                     //$NON-NLS-1$
    public static final ImageDescriptor DESC_CANVAS_GROUP_REF        = create(PATH_OBJ16, "canvas_group_ref.png");                                     //$NON-NLS-1$
    public static final ImageDescriptor DESC_CANVAS_FORM_REF         = create(PATH_OBJ16, "form_canvas_ref.png");                                      //$NON-NLS-1$
    public static final ImageDescriptor DESC_CANVAS_STACKED_REF      = create(PATH_OBJ16, "canvas_stacked_ref.png");                                   //$NON-NLS-1$
    public static final ImageDescriptor DESC_CANVAS_POPUP_REF        = create(PATH_OBJ16, "canvas_popup_ref.png");                                     //$NON-NLS-1$

    public static final ImageDescriptor DESC_SCHEMA                  = create(PATH_OBJ16, "schema.gif");                                               //$NON-NLS-1$
    public static final ImageDescriptor DESC_BLOCK_ITEM_ND           = create(PATH_OBJ16, "block_item_nd.png");                                        //$NON-NLS-1$
    public static final ImageDescriptor DESC_BLOCK_ITEM              = create(PATH_OBJ16, "block_item.png");                                           //$NON-NLS-1$
    public static final ImageDescriptor DESC_ITEMS_SCREEN            = create(PATH_OBJ16, "items_screen.png");                                         //$NON-NLS-1$
    public static final ImageDescriptor DESC_ITEMS_GROUP             = create(PATH_OBJ16, "item_group.png");                                           //$NON-NLS-1$
    public static final ImageDescriptor DESC_ITEMS_SPACE             = create(PATH_OBJ16, "item_space.png");                                           //$NON-NLS-1$

    public static final ImageDescriptor DESC_TABLE                   = create(PATH_OBJ16, "table.gif");                                                //$NON-NLS-1$

    public static final ImageDescriptor DESC_FORM                    = create(PATH_OBJ16, "form.png");                                                 //$NON-NLS-1$
    public static final ImageDescriptor DESC_OBJGROUP                = create(PATH_OBJ16, "object_group.png");                                         //$NON-NLS-1$

    public static final ImageDescriptor DESC_LOV_DEF                 = create(PATH_OBJ16, "lov_def.png");                                              //$NON-NLS-1$
    public static final ImageDescriptor DESC_LOV_REF                 = create(PATH_OBJ16, "lov_ref.png");                                              //$NON-NLS-1$
    public static final ImageDescriptor DESC_LOV_MAPPING             = create(PATH_OBJ16, "lov_mapping.png");                                          //$NON-NLS-1$

    public static final ImageDescriptor DESC_BLOCK                   = create(PATH_OBJ16, "block.png");                                                //$NON-NLS-1$
    public static final ImageDescriptor DESC_BLOCK_REF               = create(PATH_OBJ16, "block_ref.png");                                            //$NON-NLS-1$
    public static final ImageDescriptor DESC_BLOCK_MIRROR            = create(PATH_OBJ16, "block_mirror.png");                                         //$NON-NLS-1$
    public static final ImageDescriptor DESC_BLOCK_MIRROR_REF        = create(PATH_OBJ16, "block_mirror_ref.png");                                     //$NON-NLS-1$
    public static final ImageDescriptor DESC_BLOCK_NTB               = create(PATH_OBJ16, "block_ntb.png");
    public static final ImageDescriptor DESC_BLOCK_NTB_REF           = create(PATH_OBJ16, "block_ntb_ref.png");

    public static final ImageDescriptor DESC_BLOCK_RELATION          = create(PATH_OBJ16, "block_relation.png");                                       //$NON-NLS-1$
    public static final ImageDescriptor DESC_BLOCK_RELATION_REF      = create(PATH_OBJ16, "block_relation_ref.png");                                   //$NON-NLS-1$
    public static final ImageDescriptor DESC_BLOCK_RELATION_LINK     = create(PATH_OBJ16, "block_relation_link.gif");                                  //$NON-NLS-1$

    public static final Image           SHARED_FIELD_PROTECTED       = JavaUI.getSharedImages().getImage(ISharedImages.IMG_FIELD_PROTECTED);
    public static final Image           SHARED_FIELD_PUBLIC          = JavaUI.getSharedImages().getImage(ISharedImages.IMG_FIELD_PUBLIC);
    public static final Image           SHARED_FIELD_DEFAULT         = JavaUI.getSharedImages().getImage(ISharedImages.IMG_FIELD_DEFAULT);
    public static final Image           SHARED_CLASS                 = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_CLASS);
    public static final Image           SHARED_INNER_CLASS_PUBLIC    = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_INNER_CLASS_PUBLIC);
    public static final Image           SHARED_INNER_CLASS_PROTECTED = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_INNER_CLASS_PROTECTED);
    public static final Image           SHARED_ARG_IMG               = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_JAVADOCTAG);
    public static final Image           SHARED_PACKFRAG_ROOT_IMG     = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKFRAG_ROOT);
    public static final Image           SHARED_PACKAGE_IMG           = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PACKAGE);

}
