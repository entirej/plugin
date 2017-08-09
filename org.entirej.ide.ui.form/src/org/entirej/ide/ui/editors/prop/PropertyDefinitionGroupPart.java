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
package org.entirej.ide.ui.editors.prop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.entirej.framework.core.application.definition.interfaces.EJApplicationDefinition;
import org.entirej.framework.core.extensions.properties.EJCoreFrameworkExtensionProperties;
import org.entirej.framework.core.extensions.properties.EJCoreFrameworkExtensionPropertyList;
import org.entirej.framework.core.extensions.properties.EJCoreFrameworkExtensionPropertyListEntry;
import org.entirej.framework.core.properties.definitions.EJPropertyDefinitionType;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionPropertyList;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionPropertyListEntry;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinition;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionGroup;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionList;
import org.entirej.framework.core.properties.interfaces.EJItemProperties;
import org.entirej.framework.dev.properties.EJDevPropertyDefinition;
import org.entirej.framework.plugin.framework.properties.EJPluginApplicationParameter;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuProperties;
import org.entirej.framework.plugin.framework.properties.ExtensionsPropertiesFactory;
import org.entirej.framework.plugin.utils.EJPluginEntireJFloatVerifier;
import org.entirej.framework.plugin.utils.EJPluginEntireJNumberVerifier;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.editors.AbstractEditor;
import org.entirej.ide.ui.editors.descriptors.AbstractCustomDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptorPart;
import org.entirej.ide.ui.editors.descriptors.AbstractDropDownDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractGroupDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractProjectSrcFileDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDropDownDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTypeDescriptor;
import org.entirej.ide.ui.editors.form.AbstractEJFormEditor;
import org.entirej.ide.ui.table.TableViewerColumnFactory;
import org.entirej.ide.ui.utils.FormsUtil;

public class PropertyDefinitionGroupPart extends AbstractDescriptorPart
{
    private final EJPropertiesEditor editor;

    private static interface IExtensionPropertiesAdapter
    {

        EJFrameworkExtensionPropertyList getPropertyList(String name);

        void setPropertyValue(String key, String value);

        EJFrameworkExtensionProperties getProperties();

        String getStringProperty(String groupName);

        EJFrameworkExtensionPropertyList createPropertyList(String name);

    }

    public static interface IExtensionValues
    {
        EJPluginBlockProperties getBlockProperties();

        void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties, EJPropertyDefinition propertyDefinition);

    }

    public PropertyDefinitionGroupPart(final EJPropertiesEditor editor, FormPage page, Composite parent)
    {
        super(editor.getToolkit(),  parent, ExpandableComposite.TITLE_BAR, false);
        this.editor = editor;
    }

    @Override
    public Action[] getToolbarActions()
    {
        final Action refreshAction = new Action("Refresh", IAction.AS_PUSH_BUTTON)
        {

            @Override
            public void run()
            {
                buildUI(false);
            }

        };
      
        refreshAction.setImageDescriptor(EJUIImages.DESC_REFRESH);
        return new Action[] { refreshAction };
    }

    @Override
    public Object getInput()
    {
        return new Object();
    }
    @Override
    public AbstractDescriptor<?>[] getDescriptors()
    {

        EJApplicationDefinition appDef = editor.getEntireJProperties().getApplicationManager();
        if (appDef != null)
        {
            EJPropertyDefinitionGroup definitionGroup = appDef.getApplicationPropertyDefinitionGroup();
            final EJFrameworkExtensionProperties extensionProperties = editor.getEntireJProperties().getApplicationDefinedProperties();
            if (definitionGroup != null)
            {
                return createGroupDescriptors(editor, editor.getEntireJProperties(), definitionGroup, new IExtensionPropertiesAdapter()
                {

                    public void setPropertyValue(String key, String value)
                    {
                        extensionProperties.setPropertyValue(key, value);

                    }

                    public String getStringProperty(String groupName)
                    {
                        return extensionProperties.getStringProperty(groupName);
                    }

                    public EJCoreFrameworkExtensionPropertyList getPropertyList(String name)
                    {
                        return extensionProperties.getPropertyList(name);
                    }

                    public EJFrameworkExtensionProperties getProperties()
                    {
                        return extensionProperties;
                    }

                    public EJFrameworkExtensionPropertyList createPropertyList(String name)
                    {
                        EJFrameworkExtensionPropertyList list = new EJCoreFrameworkExtensionPropertyList(name);
                        if (extensionProperties instanceof EJCoreFrameworkExtensionProperties)
                            ((EJCoreFrameworkExtensionProperties) extensionProperties).addPropertyList(list);
                        return list;
                    }
                }, new IExtensionValues()
                {

                    public void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties,
                            EJPropertyDefinition propertyDefinition)
                    {
                        propertyDefinition.clearValidValues();

                        EJApplicationDefinition appDef = editor.getEntireJProperties().getApplicationManager();
                        appDef.loadValidValuesForProperty(extensionProperties, propertyDefinition);
                    }

                    public EJPluginBlockProperties getBlockProperties()
                    {
                        return null;
                    }
                });
            }
        }

        return new AbstractDescriptor<?>[0];
    }

    public static AbstractDescriptor<?>[] createGroupDescriptors(final AbstractEditor editor, final EJPluginEntireJProperties entireJProperties,
            final EJPropertyDefinitionGroup definitionGroup, final IExtensionPropertiesAdapter extensionProperties, final IExtensionValues extensionValues)
    {
        Collection<EJPropertyDefinition> definitions = definitionGroup.getPropertyDefinitions();
        List<AbstractDescriptor<?>> descriptors = new ArrayList<AbstractDescriptor<?>>();
        for (EJPropertyDefinition definition : definitions)
        {
            AbstractDescriptor<?> descriptor = null;
            descriptor = createDescriptor(editor, entireJProperties, extensionProperties, definitionGroup, definition, extensionValues);

            if (descriptor != null)
            {
                descriptors.add(descriptor);
            }
        }

        Collection<EJPropertyDefinitionList> propertyDefinitionLists = definitionGroup.getPropertyDefinitionLists();
        for (final EJPropertyDefinitionList definitionList : propertyDefinitionLists)
        {

            descriptors.add(new AbstractGroupDescriptor(definitionList.getLabel(),definitionList.getDescription())
            {
                IRefreshHandler                       handler;
                TableViewer                           tableViewer;
                EJFrameworkExtensionPropertyListEntry entry = null;
                Action                                deleteAction;
                Action                                upAction;
                Action                                downAction;

                public Action[] getToolbarActions()
                {

                    Action addAction = new Action("Add", IAction.AS_PUSH_BUTTON)
                    {

                        @Override
                        public void runWithEvent(Event event)
                        {
                            EJFrameworkExtensionPropertyListEntry newEntry = new EJCoreFrameworkExtensionPropertyListEntry();
                            EJFrameworkExtensionPropertyList propertyList = extensionProperties.getPropertyList(definitionList.getName());
                            if (propertyList == null)
                            {

                                propertyList = extensionProperties.createPropertyList(definitionList.getName());

                            }

                            // add default values
                            Collection<EJPropertyDefinition> propertyDefinitions = definitionList.getPropertyDefinitions();
                            for (EJPropertyDefinition entry : propertyDefinitions)
                            {

                                newEntry.addProperty(entry.getName(), entry.getDefaultValue());
                            }

                            propertyList.addListEntry(newEntry);
                            if (tableViewer != null)
                            {
                                tableViewer.add(newEntry);
                                tableViewer.setSelection(new StructuredSelection(newEntry), true);
                            }
                            editor.setDirty(true);
                        }

                    };
                    addAction.setImageDescriptor(EJUIImages.DESC_ADD_ITEM);

                    // create delete Action
                    deleteAction = new Action("Delete", IAction.AS_PUSH_BUTTON)
                    {

                        @Override
                        public void run()
                        {

                            if (entry == null)
                                return;

                            EJFrameworkExtensionPropertyList propertyList = extensionProperties.getPropertyList(definitionList.getName());
                            if (propertyList != null)
                            {

                                propertyList.removeListEntry(entry);
                            }

                            if (tableViewer != null)
                            {
                                tableViewer.remove(entry);
                                if (tableViewer.getTable().getItemCount() > 0)
                                    tableViewer.getTable().select(tableViewer.getTable().getItemCount() - 1);
                                if (tableViewer.getSelection() instanceof IStructuredSelection)
                                    entry = (EJFrameworkExtensionPropertyListEntry) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
                                if (handler != null)
                                    handler.refresh();
                                setEnabled(entry != null);
                            }
                            editor.setDirty(true);
                        }

                    };
                    deleteAction.setImageDescriptor(EJUIImages.DESC_DELETE_ITEM);
                    deleteAction.setDisabledImageDescriptor(EJUIImages.DESC_DELETE_ITEM_DISABLED);
                    deleteAction.setEnabled(entry != null);

                    upAction = new Action("Up", IAction.AS_PUSH_BUTTON)
                    {

                        @Override
                        public void run()
                        {

                            if (entry == null)
                                return;
                            EJFrameworkExtensionPropertyListEntry item = entry;
                            EJFrameworkExtensionPropertyList propertyList = extensionProperties.getPropertyList(definitionList.getName());
                            List<EJFrameworkExtensionPropertyListEntry> allListEntries = new ArrayList<EJFrameworkExtensionPropertyListEntry>(propertyList
                                    .getAllListEntries());

                            int indexOf = allListEntries.indexOf(item);
                            if (indexOf > 0)
                            {
                                allListEntries.remove(item);
                                allListEntries.add(--indexOf, item);

                                propertyList.removeAllEntries();
                                for (EJFrameworkExtensionPropertyListEntry i : allListEntries)
                                {
                                    propertyList.addListEntry(i);
                                }

                                tableViewer.setInput(new Object());
                                tableViewer.setSelection(new StructuredSelection(item), true);
                                editor.setDirty(true);
                            }

                        }

                    };
                    upAction.setImageDescriptor(EJUIImages.DESC_UP);
                    downAction = new Action("Down", IAction.AS_PUSH_BUTTON)
                    {

                        @Override
                        public void run()
                        {
                            if (entry == null)
                                return;
                            EJFrameworkExtensionPropertyListEntry item = entry;
                            EJFrameworkExtensionPropertyList propertyList = extensionProperties.getPropertyList(definitionList.getName());
                            List<EJFrameworkExtensionPropertyListEntry> allListEntries = new ArrayList<EJFrameworkExtensionPropertyListEntry>(propertyList
                                    .getAllListEntries());

                            int indexOf = allListEntries.indexOf(item);
                            if (indexOf < (allListEntries.size() - 1))
                            {
                                allListEntries.remove(item);
                                allListEntries.add(++indexOf, item);
                                propertyList.removeAllEntries();
                                for (EJFrameworkExtensionPropertyListEntry i : allListEntries)
                                {
                                    propertyList.addListEntry(i);
                                }
                                tableViewer.setInput(new Object());
                                tableViewer.setSelection(new StructuredSelection(item), true);
                                editor.setDirty(true);
                            }
                        }

                    };
                    downAction.setImageDescriptor(EJUIImages.DESC_DOWN);
                    updateMoveState();
                    return new Action[] { addAction, deleteAction, null, upAction, downAction };
                }

                public Control createHeader(final IRefreshHandler handler, Composite parent, GridData gd)
                {
                    this.handler = handler;
                    tableViewer = new TableViewer(parent, SWT.VIRTUAL | SWT.BORDER | SWT.FULL_SELECTION);

                    Table table = tableViewer.getTable();
                    table.setHeaderVisible(true);
                    TableViewerColumnFactory factory = new TableViewerColumnFactory(tableViewer);
                    ColumnViewerToolTipSupport.enableFor(tableViewer);
                    gd.verticalSpan = 2;
                    gd.heightHint = 150;
                    gd.widthHint = 100;
                    gd.horizontalIndent = 0;
                    table.setLayoutData(gd);

                    Collection<EJPropertyDefinition> listDefinitions = definitionList.getPropertyDefinitions();
                    for (final EJPropertyDefinition definition : listDefinitions)
                    {
                        factory.createColumn(definition.getLabel(), 120, new ColumnLabelProvider()
                        {

                            @Override
                            public String getText(Object element)
                            {

                                if (element instanceof EJFrameworkExtensionPropertyListEntry)
                                {
                                    EJFrameworkExtensionPropertyListEntry entry = (EJFrameworkExtensionPropertyListEntry) element;
                                    String property = entry.getProperty(definition.getName());
                                    if (property != null)
                                    {
                                        return property;
                                    }
                                }
                                return "";
                            }
                        });
                    }
                    tableViewer.setContentProvider(new IStructuredContentProvider()
                    {

                        public void inputChanged(Viewer arg0, Object arg1, Object arg2)
                        {
                        }

                        public void dispose()
                        {
                        }

                        public Object[] getElements(Object arg0)
                        {
                            return (Object[]) getValue();
                        }
                    });
                    tableViewer.addSelectionChangedListener(new ISelectionChangedListener()
                    {

                        public void selectionChanged(SelectionChangedEvent event)
                        {
                            EJFrameworkExtensionPropertyListEntry newEntry = null;
                            if (tableViewer.getSelection() instanceof IStructuredSelection)
                                newEntry = (EJFrameworkExtensionPropertyListEntry) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
                            if ((newEntry == null && entry != null) || (newEntry != null && !newEntry.equals(entry)))
                            {
                                entry = newEntry;
                                handler.refresh();
                            }

                            if (deleteAction != null)
                                deleteAction.setEnabled(entry != null);

                            updateMoveState();
                        }
                    });

                    tableViewer.setInput(new Object());
                    if (tableViewer.getTable().getItemCount() > 0)
                        tableViewer.getTable().select(0);
                    if (tableViewer.getSelection() instanceof IStructuredSelection)
                        entry = (EJFrameworkExtensionPropertyListEntry) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
                    return table;
                }

                private void updateMoveState()
                {
                    downAction.setEnabled(false);
                    upAction.setEnabled(false);
                    if (upAction != null && downAction != null)
                    {
                        if (entry == null)
                        {

                            return;
                        }
                        EJFrameworkExtensionPropertyList propertyList = extensionProperties.getPropertyList(definitionList.getName());
                        List<EJFrameworkExtensionPropertyListEntry> allListEntries = propertyList.getAllListEntries();

                        if (allListEntries.indexOf(entry) > 0)
                        {
                            upAction.setEnabled(true);

                        }

                        if (allListEntries.indexOf(entry) < (allListEntries.size() - 1))
                        {
                            downAction.setEnabled(true);
                        }
                    }
                }

                public Object getValue()
                {

                    EJFrameworkExtensionPropertyList propertyList = extensionProperties.getPropertyList(definitionList.getName());

                    return propertyList != null ? propertyList.getAllListEntries().toArray() : new Object[0];
                };

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    if (entry == null)
                    {
                        return new AbstractDescriptor<?>[0];
                    }
                    List<AbstractDescriptor<?>> descriptors = new ArrayList<AbstractDescriptor<?>>();
                    Collection<EJPropertyDefinition> listDefinitions = definitionList.getPropertyDefinitions();
                    for (final EJPropertyDefinition definition : listDefinitions)
                    {
                        AbstractDescriptor<?> descriptor = createDescriptor(editor, entireJProperties, new IExtensionPropertiesAdapter()
                        {

                            public void setPropertyValue(String key, String value)
                            {
                                entry.addProperty(key, value);
                                if (tableViewer != null)
                                {
                                    tableViewer.refresh(entry);
                                }

                            }

                            public String getStringProperty(String groupName)
                            {
                                return entry.getProperty(groupName);
                            }

                            public EJFrameworkExtensionPropertyList getPropertyList(String name)
                            {
                                throw new UnsupportedOperationException();
                            }

                            public EJFrameworkExtensionProperties getProperties()
                            {
                                return extensionProperties.getProperties();
                            }

                            public EJFrameworkExtensionPropertyList createPropertyList(String name)
                            {

                                throw new UnsupportedOperationException();
                            }
                        }, null, definition, extensionValues);

                        if (descriptor != null)
                        {
                            descriptors.add(descriptor);
                        }
                    }
                    return descriptors.toArray(new AbstractDescriptor<?>[0]);
                }
            });
        }

        // handle sub groups
        Collection<EJPropertyDefinitionGroup> subGroups = definitionGroup.getSubGroups();
        for (final EJPropertyDefinitionGroup subGroup : subGroups)
        {

            descriptors.add(new AbstractGroupDescriptor(subGroup.getLabel(),subGroup.getDescription())
            {

                public AbstractDescriptor<?>[] getDescriptors()
                {

                    return createGroupDescriptors(editor, entireJProperties, subGroup, new IExtensionPropertiesAdapter()
                    {

                        public void setPropertyValue(String key, String value)
                        {
                            extensionProperties.setPropertyValue(key, value);

                        }

                        public String getStringProperty(String groupName)
                        {
                            return extensionProperties.getStringProperty(groupName);
                        }

                        public EJCoreFrameworkExtensionPropertyList getPropertyList(String name)
                        {
                            EJFrameworkExtensionProperties propertyGroup = extensionProperties.getProperties().getPropertyGroup(subGroup.getName());
                            if (propertyGroup == null)
                            {
                                return null;
                            }
                            return (EJCoreFrameworkExtensionPropertyList) propertyGroup.getPropertyList(name);
                        }

                        public EJFrameworkExtensionProperties getProperties()
                        {
                            EJFrameworkExtensionProperties propertyGroup = extensionProperties.getProperties().getPropertyGroup(subGroup.getName());
                            if (propertyGroup == null)
                            {
                                propertyGroup = ExtensionsPropertiesFactory.addExtensionProperties(extensionProperties.getProperties().getFormProperties(),
                                        extensionProperties.getProperties().getBlockProperties(), subGroup, null, true);
                                ((EJCoreFrameworkExtensionProperties) extensionProperties.getProperties())
                                        .addPropertyGroup((EJCoreFrameworkExtensionProperties) propertyGroup);
                            }
                            return propertyGroup;
                        }

                        public EJFrameworkExtensionPropertyList createPropertyList(String name)
                        {

                            EJFrameworkExtensionProperties propertyGroup = extensionProperties.getProperties().getPropertyGroup(subGroup.getName());
                            if (propertyGroup == null)
                            {
                                propertyGroup = ExtensionsPropertiesFactory.addExtensionProperties(extensionProperties.getProperties().getFormProperties(),
                                        extensionProperties.getProperties().getBlockProperties(), subGroup, null, true);
                                ((EJCoreFrameworkExtensionProperties) extensionProperties.getProperties())
                                        .addPropertyGroup((EJCoreFrameworkExtensionProperties) propertyGroup);
                            }

                            EJFrameworkExtensionPropertyList list = new EJCoreFrameworkExtensionPropertyList(name);
                            if (propertyGroup instanceof EJCoreFrameworkExtensionProperties)
                                ((EJCoreFrameworkExtensionProperties) propertyGroup).addPropertyList(list);
                            return list;
                        }

                    }, extensionValues);
                }
            });
        }
        return descriptors.toArray(new AbstractDescriptor<?>[0]);
    }

    public static AbstractDescriptor<?> createDescriptor(final AbstractEditor editor, final EJPluginEntireJProperties entireJProperties,
            final IExtensionPropertiesAdapter extensionProperties, EJPropertyDefinitionGroup definitionGroup, final EJPropertyDefinition definition,
            final IExtensionValues extensionValues)
    {
        final EJPropertyDefinitionType dataType = definition.getPropertyType();
        final String label = definition.getLabel();
        String description = definition.getDescription();
        final String groupName;
        if (definitionGroup == null || definitionGroup.getFullGroupName() == null || definitionGroup.getFullGroupName().trim().length() == 0)
        {
            groupName = definition.getName();
        }
        else
        {
            groupName = String.format("%s.%s", definitionGroup.getFullGroupName(), definition.getName());
        }

        AbstractDescriptor<?> descriptor = null;
        if (dataType == EJPropertyDefinitionType.STRING || dataType == EJPropertyDefinitionType.ACTION_COMMAND || dataType == EJPropertyDefinitionType.INTEGER
                || dataType == EJPropertyDefinitionType.FLOAT)
        {

            if (definition.loadValidValuesDynamically() || definition.hasValidValues())
            {
                if (definition.loadValidValuesDynamically())
                {
                    extensionValues.loadValidValuesFromExtension(extensionProperties.getProperties(), definition);
                }

                descriptor = new AbstractTextDropDownDescriptor(label, description)
                {

                    @Override
                    public void setValue(String value)
                    {
                        extensionProperties.setPropertyValue(groupName, definition.getValidValueNameForLabel(value));
                        editor.setDirty(true);
                    }

                    @Override
                    public String getValue()
                    {
                        String property = extensionProperties.getStringProperty(groupName);
                        return definition.getLabelForValidValue(property);
                    }

                    public String[] getOptions()
                    {
                        List<String> list = new ArrayList<String>();
                        if (!definition.isMandatory())
                            list.add("");
                        list.addAll(definition.getValidValueLabels());
                        return list.toArray(new String[0]);
                    }

                    public String getOptionText(String t)
                    {
                        return t;
                    }
                };
            }
            else
                descriptor = new AbstractTextDescriptor(label, description)
                {

                    @Override
                    public void setValue(String value)
                    {
                        if(value.startsWith(".") )
                            value=  "0"+value;
                        if(value.endsWith(".") )
                            value = value.substring(0, value.length()-1);
                        extensionProperties.setPropertyValue(groupName, value);
                        editor.setDirty(true);
                    }

                    @Override
                    public String getValue()
                    {
                        return extensionProperties.getStringProperty(groupName);
                    }

                    @Override
                    public void addEditorAssist(Control control)
                    {
                        if (dataType == EJPropertyDefinitionType.INTEGER && control instanceof Text)
                        {
                            ((Text) control).addVerifyListener(new EJPluginEntireJNumberVerifier());
                        }
                        else if (dataType == EJPropertyDefinitionType.FLOAT && control instanceof Text)
                        {
                            ((Text) control).addVerifyListener(new EJPluginEntireJFloatVerifier());
                        }
                        super.addEditorAssist(control);
                    }
                };
        }
        else if (dataType == EJPropertyDefinitionType.BOOLEAN)
        {
            descriptor = new AbstractDescriptor<Boolean>(AbstractDescriptor.TYPE.BOOLEAN)
            {

                @Override
                public Boolean getValue()
                {

                    return Boolean.valueOf((extensionProperties.getStringProperty(groupName) != null) ? extensionProperties.getStringProperty(groupName)
                            : definition.getDefaultValue());
                }

                @Override
                public void setValue(Boolean value)
                {
                    extensionProperties.setPropertyValue(groupName, value.toString());
                    editor.setDirty(true);

                }

            };
            descriptor.setText(label);
            descriptor.setTooltip(description);
        }
        else if (dataType == EJPropertyDefinitionType.PROJECT_CLASS_FILE)
        {
            
            
            AbstractTypeDescriptor type  = new AbstractTypeDescriptor(editor, label, description)
            {

                @Override
                public void setValue(String value)
                {
                    extensionProperties.setPropertyValue(groupName, value);
                    editor.setDirty(true);
                }

                @Override
                public String getValue()
                {
                    return extensionProperties.getStringProperty(groupName);
                }
            };
            descriptor = type;
            if(definition instanceof EJDevPropertyDefinition)
            {
                type.setBaseClass(((EJDevPropertyDefinition)definition).getClassParent());
            }
           

        }
        else if (dataType == EJPropertyDefinitionType.PROJECT_FILE)
        {
            descriptor = new AbstractProjectSrcFileDescriptor(editor, label, description)
            {

                @Override
                public void setValue(String value)
                {
                    extensionProperties.setPropertyValue(groupName, value);
                    editor.setDirty(true);
                }

                @Override
                public String getValue()
                {
                    return extensionProperties.getStringProperty(groupName);
                }
            };

        }
        else if (dataType == EJPropertyDefinitionType.FORM_ID)
        {
            descriptor = new AbstractDropDownDescriptor<String>(label, description)
            {
                public String[] getOptions()
                {
                    IJavaProject javaProject = editor.getJavaProject();
                    if (javaProject != null)
                    {
                        return FormsUtil.getFormNames(javaProject).toArray(new String[0]);
                    }
                    return new String[0];
                }

                public String getOptionText(String t)
                {
                    return t;
                }

                @Override
                public void setValue(String value)
                {
                    extensionProperties.setPropertyValue(groupName, value);
                    editor.setDirty(true);
                }

                @Override
                public String getValue()
                {
                    return extensionProperties.getStringProperty(groupName);
                }
            };

        }
        else if (dataType == EJPropertyDefinitionType.APPLICATION_PARAMETER)
        {
            descriptor = new AbstractDropDownDescriptor<String>(label, description)
            {
                public String[] getOptions()
                {
                    Collection<EJPluginApplicationParameter> allApplicationLevelParameters = entireJProperties.getAllApplicationLevelParameters();
                    
                   
                    List<String> names = new ArrayList<String>();
                    names.add("");
                    
                    for (EJPluginApplicationParameter ejPluginApplicationParameter : allApplicationLevelParameters)
                    {
                       
                            names.add(ejPluginApplicationParameter.getName());
                        
                        
                    } 
                    
                    return names.toArray(new String[0]);
                }
                
                public String getOptionText(String t)
                {
                    return t;
                }
                
                @Override
                public void setValue(String value)
                {
                    extensionProperties.setPropertyValue(groupName, value);
                    editor.setDirty(true);
                }
                
                @Override
                public String getValue()
                {
                    return extensionProperties.getStringProperty(groupName);
                }
            };
            
        }
        else if (dataType == EJPropertyDefinitionType.VISUAL_ATTRIBUTE)
        {
            descriptor = new AbstractTextDropDownDescriptor(label, description)
            {
                List<String> visualAttributeNames = new ArrayList<String>(entireJProperties.getVisualAttributesContainer().getVisualAttributeNames());

                @Override
                public void setValue(String value)
                {
                    extensionProperties.setPropertyValue(groupName, value);
                    editor.setDirty(true);
                }

                @Override
                public String getValue()
                {
                    return extensionProperties.getStringProperty(groupName);
                }

                public String[] getOptions()
                {
                    List<String> list = new ArrayList<String>();
                    if (!definition.isMandatory())
                        list.add("");

                    list.addAll(visualAttributeNames);

                    if (getValue() != null && getValue().length() > 0 && !visualAttributeNames.contains(getValue()))
                    {
                        list.add(getValue());
                    }
                    return list.toArray(new String[0]);
                }

                public String getOptionText(String t)
                {
                    if (t.length() > 0 && !visualAttributeNames.contains(t))
                    {
                        return String.format("Undefined !< %s >", t);
                    }

                    return t;
                }
            };

        }
        else if (dataType == EJPropertyDefinitionType.BLOCK_ITEM)
        {
            descriptor = new AbstractTextDropDownDescriptor(label, description)
            {

                @Override
                public void setValue(String value)
                {
                    extensionProperties.setPropertyValue(groupName, value);
                    editor.setDirty(true);
                }

                @Override
                public String getValue()
                {
                    return extensionProperties.getStringProperty(groupName);
                }

                public String[] getOptions()
                {
                    List<String> list = new ArrayList<String>();

                    if (!definition.isMandatory())
                        list.add("");

                    EJPluginBlockProperties blockProperties = extensionValues.getBlockProperties();
                    if (blockProperties != null)
                    {
                        for (EJItemProperties itemProperties : blockProperties.getAllItemProperties())
                        {
                            list.add(itemProperties.getName());
                        }
                    }
                    return list.toArray(new String[0]);
                }

                public String getOptionText(String t)
                {
                    return t;
                }
            };

        }
        else if (dataType == EJPropertyDefinitionType.MENU_GROUP)
        {
            descriptor = new AbstractTextDropDownDescriptor(label, description)
            {

                @Override
                public void setValue(String value)
                {
                    extensionProperties.setPropertyValue(groupName, value);
                    editor.setDirty(true);
                }

                @Override
                public String getValue()
                {
                    return extensionProperties.getStringProperty(groupName);
                }

                public String[] getOptions()
                {
                    Collection<EJPluginMenuProperties> allMenuProperties = entireJProperties.getPluginMenuContainer().getAllMenuProperties();

                    List<String> list = new ArrayList<String>();
                    if (!definition.isMandatory())
                        list.add("");

                    for (EJPluginMenuProperties coreMenuProperties : allMenuProperties)
                    {
                        list.add(coreMenuProperties.getName());
                    }

                    return list.toArray(new String[0]);
                }

                public String getOptionText(String t)
                {
                    return t;
                }
            };

        }

        else if (dataType == EJPropertyDefinitionType.LOV_DEFINITION_WITH_ITEMS)
        {
            descriptor = new AbstractCustomDescriptor<String>(label, description)
            {
                ComboViewer lovDefViewer;
                ComboViewer itemViewer;

                @Override
                public void setValue(String value)
                {
                    extensionProperties.setPropertyValue(groupName, value);
                    editor.setDirty(true);
                }

                @Override
                public String getValue()
                {
                    return extensionProperties.getStringProperty(groupName);
                }

                private void updateUIState()
                {
                    if (lovDefViewer != null && itemViewer != null)
                    {
                        itemViewer.getCombo().setEnabled(lovDefViewer.getCombo().getSelectionIndex() != -1);
                    }
                }

                public boolean isUseLabel()
                {
                    return true;
                }

                public Control createBody(Composite parent, GridData gd)
                {
                    String defaultValue = getValue();
                    Composite body = new Composite(parent, SWT.NULL);
                    gd.verticalSpan = 2;
                    body.setLayoutData(gd);
                    if (isUseLabel())
                        new Label(parent, SWT.NULL);
                    else
                    {
                        gd.horizontalSpan = 2;
                    }
                    GridLayout layout = new GridLayout(1, true);
                    layout.marginWidth = 0;
                    layout.marginRight = 0;
                    layout.marginLeft = 0;
                    layout.marginHeight = 0;
                    layout.marginTop = 0;
                    layout.marginBottom = 0;
                    body.setLayout(layout);
                    GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
                    lovDefViewer = new ComboViewer(body, SWT.READ_ONLY);
                    lovDefViewer.getCombo().setLayoutData(gridData);
                    itemViewer = new ComboViewer(body, SWT.READ_ONLY);
                    itemViewer.getCombo().setLayoutData(gridData);

                    lovDefViewer.setContentProvider(new IStructuredContentProvider()
                    {

                        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
                        {
                        }

                        public void dispose()
                        {
                        }

                        public Object[] getElements(Object inputElement)
                        {
                            if (editor instanceof AbstractEJFormEditor)
                            {
                                AbstractEJFormEditor formEditor = (AbstractEJFormEditor) editor;
                                return formEditor.getFormProperties().getLovDefinitionNames().toArray();
                            }

                            return new Object[] {};
                        }
                    });
                    itemViewer.setContentProvider(new IStructuredContentProvider()
                    {

                        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
                        {
                        }

                        public void dispose()
                        {
                        }

                        public Object[] getElements(Object inputElement)
                        {
                            if (inputElement != null && editor instanceof AbstractEJFormEditor)
                            {
                                AbstractEJFormEditor formEditor = (AbstractEJFormEditor) editor;
                                List<String> lovDefinitionItemNames = formEditor.getFormProperties().getLovDefinitionItemNames((String) inputElement);
                                return lovDefinitionItemNames != null ? lovDefinitionItemNames.toArray() : new Object[0];
                            }

                            return new Object[] {};
                        }
                    });

                    lovDefViewer.addSelectionChangedListener(new ISelectionChangedListener()
                    {

                        public void selectionChanged(SelectionChangedEvent event)
                        {
                            String lov = null;
                            if (lovDefViewer.getSelection() instanceof IStructuredSelection)
                            {
                                lov = (String) ((IStructuredSelection) lovDefViewer.getSelection()).getFirstElement();

                            }
                            itemViewer.getCombo().select(-1);

                            itemViewer.setInput(lov);
                            if (itemViewer.getCombo().getItemCount() > 0)
                            {
                                itemViewer.setSelection(new StructuredSelection(itemViewer.getCombo().getItem(0)));
                            }
                            updateUIState();

                        }
                    });

                    lovDefViewer.setInput(new Object());
                    if (defaultValue != null)
                    {
                        String[] split = defaultValue.split("\\.");
                        if (split.length == 2)
                        {
                            lovDefViewer.setSelection(new StructuredSelection(split[0]));
                            itemViewer.setSelection(new StructuredSelection(split[1]));
                        }
                    }

                    itemViewer.addSelectionChangedListener(new ISelectionChangedListener()
                    {

                        public void selectionChanged(SelectionChangedEvent event)
                        {
                            if (lovDefViewer.getSelection() instanceof IStructuredSelection)
                            {
                                String lov = (String) ((IStructuredSelection) lovDefViewer.getSelection()).getFirstElement();
                                if (itemViewer.getSelection() instanceof IStructuredSelection)
                                {
                                    String item = (String) ((IStructuredSelection) itemViewer.getSelection()).getFirstElement();
                                    setValue(String.format("%s.%s", lov, item));
                                }

                            }

                        }
                    });

                    updateUIState();
                    return body;
                }
            };

        }
        if (descriptor != null)
        {
            descriptor.setRequired(definition.isMandatory());
        }
        return descriptor;
    }

    @Override
    public String getSectionTitle()
    {
        EJApplicationDefinition appDef = editor.getEntireJProperties().getApplicationManager();
        if (appDef == null)
        {
            return "Undifined Application Definition";
        }
        EJPropertyDefinitionGroup definitionGroup = appDef.getApplicationPropertyDefinitionGroup();
        return definitionGroup != null ? definitionGroup.getLabel() : "Undifined Property Definition Group";
    }

    @Override
    public void refresh()
    {
        super.refresh();
        buildUI(true);
    }

    @Override
    public String getSectionDescription()
    {
        return null;
    }

    @Override
    public boolean isStale()
    {
        // this make sure on part will refresh always if user change
        // EJApplicationDefinition.class
        return true;
    }

    public static AbstractDescriptor<?>[] createGroupDescriptors(AbstractEditor editor, EJPluginEntireJProperties entireJProperties,
            EJPropertyDefinitionGroup definitionGroup, final EJFrameworkExtensionProperties rendereProperties, IExtensionValues extensionValues)
    {

        return createGroupDescriptors(editor, entireJProperties, definitionGroup, new IExtensionPropertiesAdapter()
        {

            public void setPropertyValue(String key, String value)
            {
                rendereProperties.setPropertyValue(key, value);

            }

            public String getStringProperty(String groupName)
            {
                return rendereProperties.getStringProperty(groupName);
            }

            public EJCoreFrameworkExtensionPropertyList getPropertyList(String name)
            {
                return rendereProperties.getPropertyList(name);
            }

            public EJFrameworkExtensionProperties getProperties()
            {
                return rendereProperties;
            }

            public EJFrameworkExtensionPropertyList createPropertyList(String name)
            {
                EJFrameworkExtensionPropertyList list = new EJCoreFrameworkExtensionPropertyList(name);
                if (rendereProperties instanceof EJCoreFrameworkExtensionProperties)
                    ((EJCoreFrameworkExtensionProperties) rendereProperties).addPropertyList(list);
                return list;
            }

        }, extensionValues);
    }

    public static AbstractDescriptor<?>[] createGroupDescriptors(AbstractEditor editor, EJPluginEntireJProperties entireJProperties,
            EJPropertyDefinitionGroup definitionGroup, final EJFrameworkExtensionProperties rendereProperties)
    {

        return createGroupDescriptors(editor, entireJProperties, definitionGroup, rendereProperties, new IExtensionValues()
        {
            public EJPluginBlockProperties getBlockProperties()
            {
                return null;
            }

            public void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties, EJPropertyDefinition propertyDefinition)
            {

            }
        });
    }
}
