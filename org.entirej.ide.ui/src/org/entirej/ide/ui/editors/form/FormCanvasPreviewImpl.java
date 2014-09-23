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
package org.entirej.ide.ui.editors.form;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.entirej.framework.core.enumerations.EJCanvasSplitOrientation;
import org.entirej.framework.core.enumerations.EJCanvasTabPosition;
import org.entirej.framework.core.enumerations.EJCanvasType;
import org.entirej.framework.core.properties.interfaces.EJBlockProperties;
import org.entirej.framework.core.properties.interfaces.EJMainScreenProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginStackedPageProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginTabPageProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginCanvasContainer;

public class FormCanvasPreviewImpl implements IFormPreviewProvider
{
    protected final Color COLOR_LIGHT_RED    = new Color(Display.getCurrent(), new RGB(255, 170, 170));
    protected final Color COLOR_LIGHT_YELLOW = Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND);

    public void dispose()
    {
        COLOR_LIGHT_RED.dispose();
    }

    protected EJPluginFormProperties getFormProperties(AbstractEJFormEditor editor)
    {
        return  editor.getFormProperties();
    }
    
    public void buildPreview(AbstractEJFormEditor editor, ScrolledComposite previewComposite)
    {
        // layout canvas preview
        Composite pContent = new Composite(previewComposite, SWT.NONE);

        EJPluginFormProperties formProperties = getFormProperties(editor);
        EJPluginCanvasContainer container = formProperties.getCanvasContainer();
        int width = formProperties.getFormWidth();
        int height = formProperties.getFormHeight();
        previewComposite.setContent(pContent);
        setPreviewBackground(previewComposite, COLOR_LIGHT_YELLOW);
        previewComposite.setExpandHorizontal(true);
        previewComposite.setExpandVertical(true);

        pContent.setLayout(new GridLayout());
        setPreviewBackground(pContent, COLOR_LIGHT_YELLOW);

        Composite layoutBody = new Composite(pContent, SWT.NONE);
        layoutBody.setLayout(new GridLayout(formProperties.getNumCols(), false));

        GridData sectionData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);

        sectionData.widthHint = width;
        sectionData.heightHint = height;
        layoutBody.setLayoutData(sectionData);
        setPreviewBackground(layoutBody, COLOR_LIGHT_YELLOW);

        List<EJPluginCanvasProperties> items = container.getCanvasProperties();
        for (EJPluginCanvasProperties canvas : items)
        {
            switch (canvas.getType())
            {
                case GROUP:
                    createGroupLayout(layoutBody, canvas);
                    break;
                case SPLIT:
                    createSplitLayout(layoutBody, canvas);
                    break;
                case POPUP:
                    // ignore
                    break;
                case TAB:
                    createTabLayout(layoutBody, canvas);
                    break;
                case STACKED:
                    createStackLayout(layoutBody, canvas);
                    break;
                default:
                    createComponent(layoutBody, canvas);
                    break;
            }
        }
        if (width > 0 && height > 0)
            previewComposite.setMinSize(width, height);
        else
            previewComposite.setMinSize(pContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));

    }

    protected GridData createGridData(EJPluginCanvasProperties layoutItem)
    {
        GridData gd = new GridData();

        if (layoutItem.getType() == EJCanvasType.BLOCK)
        {
            EJBlockProperties blockProperties = layoutItem.getPluginBlockProperties();
            if (blockProperties != null && blockProperties.getMainScreenProperties() != null)
            {
                EJMainScreenProperties screenProperties = blockProperties.getMainScreenProperties();
                gd.heightHint = screenProperties.getHeight();
                gd.widthHint = screenProperties.getWidth();
                gd.verticalSpan = screenProperties.getVerticalSpan();
                gd.horizontalSpan = screenProperties.getHorizontalSpan();
                gd.verticalAlignment = SWT.FILL;
                gd.horizontalAlignment = SWT.FILL;
                gd.grabExcessHorizontalSpace = screenProperties.canExpandHorizontally();
                gd.grabExcessVerticalSpace = screenProperties.canExpandVertically();
                if (gd.grabExcessVerticalSpace)
                    gd.minimumHeight = gd.heightHint;
                if (gd.grabExcessHorizontalSpace)
                    gd.minimumWidth = gd.widthHint;
                return gd;
            }
        }

        gd.heightHint = layoutItem.getHeight();
        gd.widthHint = layoutItem.getWidth();
        gd.verticalSpan = layoutItem.getVerticalSpan();
        gd.horizontalSpan = layoutItem.getHorizontalSpan();
        gd.verticalAlignment = SWT.FILL;
        gd.horizontalAlignment = SWT.FILL;
        gd.grabExcessHorizontalSpace = layoutItem.canExpandHorizontally();
        gd.grabExcessVerticalSpace = layoutItem.canExpandVertically();
        if (gd.grabExcessVerticalSpace)
            gd.minimumHeight = gd.heightHint;
        if (gd.grabExcessHorizontalSpace)
            gd.minimumWidth = gd.widthHint;

        return gd;
    }

    protected void createComponent(Composite parent, EJPluginCanvasProperties component)
    {

        Composite layoutBody = new Composite(parent, SWT.BORDER);
        layoutBody.setLayoutData(createGridData(component));
        layoutBody.setLayout(new GridLayout());
        Label spaceLabel = new Label(layoutBody, SWT.NONE);
        spaceLabel.setText(String.format("<%s>",
                (component.getPluginBlockProperties() == null || component.getPluginBlockProperties().getName().length() == 0) ? "<block>" : component
                        .getPluginBlockProperties().getName()));
        spaceLabel.setLayoutData(createGridData(component));
        setPreviewBackground(spaceLabel, COLOR_LIGHT_RED);
        spaceLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
        setPreviewBackground(layoutBody, COLOR_LIGHT_RED);

    }

    protected void createSplitLayout(Composite parent, EJPluginCanvasProperties group)
    {
        SashForm layoutBody = new SashForm(parent, group.getSplitOrientation() == EJCanvasSplitOrientation.HORIZONTAL ? SWT.HORIZONTAL : SWT.VERTICAL);
        layoutBody.setLayoutData(createGridData(group));

        setPreviewBackground(layoutBody, COLOR_LIGHT_YELLOW);
        List<EJPluginCanvasProperties> items = group.getSplitCanvasContainer().getCanvasProperties();
        if (items.size() > 0)
        {
            int[] weights = new int[items.size()];
            layoutBody.setLayout(new GridLayout(group.getNumCols(), false));

            for (EJPluginCanvasProperties canvas : items)
            {
                if (canvas.getType() == EJCanvasType.BLOCK && canvas.getBlockProperties() != null
                        && canvas.getBlockProperties().getMainScreenProperties() != null)
                {
                    int width = canvas.getSplitOrientation() ==EJCanvasSplitOrientation.HORIZONTAL ?canvas.getBlockProperties().getMainScreenProperties().getWidth():canvas.getBlockProperties().getMainScreenProperties().getHeight();
                    weights[items.indexOf(canvas)] = width + 1;
                }
                else
                    weights[items.indexOf(canvas)] = (canvas.getSplitOrientation() ==EJCanvasSplitOrientation.HORIZONTAL ? canvas.getWidth():canvas.getHeight()) + 1;
                switch (canvas.getType())
                {
                    case GROUP:
                        createGroupLayout(layoutBody, canvas);
                        break;
                    case SPLIT:
                        createSplitLayout(layoutBody, canvas);
                        break;
                    case POPUP:
                        // ignore
                        break;
                    case TAB:
                        createTabLayout(layoutBody, canvas);
                        break;
                    case STACKED:
                        createStackLayout(layoutBody, canvas);
                        break;
                    default:
                        createComponent(layoutBody, canvas);
                        break;
                }
            }
            layoutBody.setWeights(weights);
        }
        else
        {
            layoutBody.setLayout(new GridLayout());
            Label compLabel = new Label(layoutBody, SWT.NONE);
            compLabel.setText(String.format("<%s>", (group.getName() == null || group.getName().length() == 0) ? "<group>" : group.getName()));
            compLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
            setPreviewBackground(compLabel, COLOR_LIGHT_YELLOW);

        }
    }

    protected void createGroupLayout(Composite parent, EJPluginCanvasProperties group)
    {
        Composite layoutBody;
        if (group.getDisplayGroupFrame() && group.getGroupFrameTitle() != null && group.getGroupFrameTitle().length() > 0)
        {
            Group grp = new Group(parent, SWT.NONE);
            grp.setLayout(new FillLayout());
            grp.setLayoutData(createGridData(group));
            grp.setText(group.getGroupFrameTitle());
            setPreviewBackground(grp, COLOR_LIGHT_YELLOW);
            parent = grp;
            layoutBody = new Composite(parent, SWT.NONE);
        }
        else
        {
            layoutBody = new Composite(parent, SWT.BORDER);
            layoutBody.setLayoutData(createGridData(group));
        }
        setPreviewBackground(layoutBody, COLOR_LIGHT_YELLOW);
        List<EJPluginCanvasProperties> items = group.getGroupCanvasContainer().getCanvasProperties();
        if (items.size() > 0)
        {

            layoutBody.setLayout(new GridLayout(group.getNumCols(), false));

            for (EJPluginCanvasProperties canvas : items)
            {
                switch (canvas.getType())
                {
                    case GROUP:
                        createGroupLayout(layoutBody, canvas);
                        break;
                    case SPLIT:
                        createSplitLayout(layoutBody, canvas);
                        break;
                    case POPUP:
                        // ignore
                        break;
                    case TAB:
                        createTabLayout(layoutBody, canvas);
                        break;
                    case STACKED:
                        createStackLayout(layoutBody, canvas);
                        break;
                    default:
                        createComponent(layoutBody, canvas);
                        break;
                }
            }

        }
        else
        {
            layoutBody.setLayout(new GridLayout());
            Label compLabel = new Label(layoutBody, SWT.NONE);
            compLabel.setText(String.format("<%s>", (group.getName() == null || group.getName().length() == 0) ? "<group>" : group.getName()));
            compLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
            setPreviewBackground(compLabel, COLOR_LIGHT_YELLOW);

        }
    }

    protected void createTabLayout(Composite parent, EJPluginCanvasProperties group)
    {
        CTabFolder layoutBody = new CTabFolder(parent, SWT.BORDER | (group.getTabPosition() == EJCanvasTabPosition.TOP ? SWT.TOP : SWT.BOTTOM));

        layoutBody.setLayoutData(createGridData(group));
        setPreviewBackground(layoutBody, COLOR_LIGHT_YELLOW);
        List<EJPluginTabPageProperties> items = group.getTabPageContainer().getTabPageProperties();

        for (EJPluginTabPageProperties item : items)
        {
            CTabItem tabItem = new CTabItem(layoutBody, SWT.NONE);
            Composite composite = new Composite(layoutBody, SWT.NONE);
            composite.setLayout(new GridLayout(item.getNumCols(), false));
            setPreviewBackground(composite, COLOR_LIGHT_YELLOW);
            tabItem.setControl(composite);
            tabItem.setText((item.getPageTitle() != null && item.getPageTitle().length() > 0) ? item.getPageTitle() : "<title>: " + item.getName());

            List<EJPluginCanvasProperties> subitems = item.getContainedCanvases().getCanvasProperties();
            for (EJPluginCanvasProperties canvas : subitems)
            {
                switch (canvas.getType())
                {
                    case GROUP:
                        createGroupLayout(composite, canvas);
                        break;
                    case SPLIT:
                        createSplitLayout(composite, canvas);
                        break;
                    case POPUP:
                        // ignore
                        break;
                    case TAB:
                        createTabLayout(composite, canvas);
                        break;
                    case STACKED:
                        createStackLayout(composite, canvas);
                        break;
                    default:
                        createComponent(composite, canvas);
                        break;
                }
            }
        }
        if (items.size() > 0)
            layoutBody.setSelection(0);

    }

    protected void createStackLayout(Composite parent, EJPluginCanvasProperties group)
    {
        Group grp = new Group(parent, SWT.NONE);
        grp.setLayout(new FillLayout());
        grp.setLayoutData(createGridData(group));
        grp.setText("<STACKED_COMPONENT>");
        setPreviewBackground(grp, COLOR_LIGHT_YELLOW);
        parent = grp;
        CTabFolder layoutBody = new CTabFolder(parent, (SWT.BOTTOM));

        setPreviewBackground(layoutBody, COLOR_LIGHT_YELLOW);
        List<EJPluginStackedPageProperties> items = group.getStackedPageContainer().getStackedPageProperties();

        for (EJPluginStackedPageProperties item : items)
        {
            CTabItem tabItem = new CTabItem(layoutBody, SWT.NONE);
            Composite composite = new Composite(layoutBody, SWT.NONE);
            composite.setLayout(new GridLayout(item.getNumCols(), false));
            setPreviewBackground(composite, COLOR_LIGHT_YELLOW);
            tabItem.setControl(composite);
            tabItem.setText("<page>: " + item.getName());

            List<EJPluginCanvasProperties> subitems = item.getContainedCanvases().getCanvasProperties();
            for (EJPluginCanvasProperties canvas : subitems)
            {
                switch (canvas.getType())
                {
                    case GROUP:
                        createGroupLayout(composite, canvas);
                        break;
                    case SPLIT:
                        createSplitLayout(composite, canvas);
                        break;
                    case POPUP:
                        // ignore
                        break;
                    case TAB:
                        createTabLayout(composite, canvas);
                        break;
                    case STACKED:
                        createStackLayout(composite, canvas);
                        break;
                    default:
                        createComponent(composite, canvas);
                        break;
                }
            }
        }
        if (items.size() > 0)
            layoutBody.setSelection(0);

    }

    protected void setPreviewBackground(Control control, Color color)
    {
        control.setBackground(color);
    }

    public String getDescription()
    {
        return "preview the defined canvas layout in form.";
    }

}
