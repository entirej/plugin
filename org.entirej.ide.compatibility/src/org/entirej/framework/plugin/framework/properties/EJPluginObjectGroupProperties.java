/*******************************************************************************
 * Copyright 2013 Mojave Innovations GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Contributors: Mojave Innovations GmbH - initial API and implementation
 ******************************************************************************/
/*
 * Created on Nov 5, 2005
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package org.entirej.framework.plugin.framework.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.entirej.framework.core.enumerations.EJCanvasType;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.interfaces.EJCanvasProperties;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevFormRendererDefinition;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginBlockContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginCanvasContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginLovDefinitionContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginRelationContainer;
import org.entirej.framework.plugin.utils.EJPluginCanvasRetriever;

public class EJPluginObjectGroupProperties extends EJPluginFormProperties
{

    private static final long serialVersionUID = 4791971005262621103L;

    
    private boolean  initialized ;
    
    
    
    
    
    
    public boolean isInitialized()
    {
        return initialized;
    }

    public void setInitialized(boolean initialized)
    {
        this.initialized = initialized;
    }

    public EJPluginObjectGroupProperties(String formName, IJavaProject javaProject)
    {
        super(formName, javaProject);
    }
    
    public EJPluginObjectGroupProperties getObjectgroupProperties()
    {
        return this;
    }
    
    
    public String getActionProcessorClassName()
    {
        return null;
    }
    
    
    public void setActionProcessorClassName(String processorClassName)
    {
        //ignore
    }
    
    @Override
    public String getFormRendererName()
    {
        return null;
    }
    
    @Override
    public void setFormRendererName(String formRendererName)
    {
       //ignore
    }
    
    @Override
    public void setFormRendererProperties(EJFrameworkExtensionProperties properties)
    {
       //ignore
    }
    
    @Override
    public EJDevFormRendererDefinition getFormRendererDefinition()
    {
        return null;
    }

    public void removeObjects(EJPluginFormProperties form)
    {
        EJPluginBlockContainer blockContainer = form.getBlockContainer();
        List<EJPluginBlockProperties> allBlockProperties = blockContainer.getAllBlockProperties();
        //clean all blocks to form
        for (EJPluginBlockProperties block : new ArrayList<EJPluginBlockProperties>(allBlockProperties))
        {
            if(block.isImportFromObjectGroup() && block.getReferencedObjectGroupName().equals(getName()))
            
            {
                blockContainer.removeBlockProperties(block,true);
            }
        }
        
        EJPluginRelationContainer relationContainer = form.getRelationContainer();
        List<EJPluginRelationProperties> relationProperties = relationContainer.getAllRelationProperties();
        //clean all relations
        for (EJPluginRelationProperties relation : new ArrayList<EJPluginRelationProperties>(relationProperties))
        {
            if(relation.isImportFromObjectGroup() && relation.getReferencedObjectGroupName().equals(getName()))
                
            {
                relationContainer.removeRelationProperties(relation);
            }
        }
      //clean all canvas
        EJPluginCanvasContainer canvasContainer = form.getCanvasContainer();
        Collection<EJPluginCanvasProperties> allCanvasProperties = canvasContainer.getCanvasProperties();
        for (EJPluginCanvasProperties canvas : new ArrayList<EJPluginCanvasProperties>(allCanvasProperties))
        {
            if(canvas.isImportFromObjectGroup() && canvas.getReferencedObjectGroupName().equals(getName()))
                        
            {
                canvasContainer.removeCanvasProperties(canvas);
            }
        }
        
      //clean all LOV
        EJPluginLovDefinitionContainer lovDefinitionContainer = form.getLovDefinitionContainer();
        List<EJPluginLovDefinitionProperties> allLovDefinitionProperties = lovDefinitionContainer.getAllLovDefinitionProperties();
        for (EJPluginLovDefinitionProperties lov :  new ArrayList<EJPluginLovDefinitionProperties>(allLovDefinitionProperties))
        {
            if(lov.isImportFromObjectGroup() && lov.getReferencedObjectGroupName().equals(getName()))
                
            {
                lovDefinitionContainer.removeLovDefinitionProperties(lov);
            }
        }
    }
    
    public static void renameObjectGroup(EJPluginFormProperties form, String old,String newName)
    {
        EJPluginBlockContainer blockContainer = form.getBlockContainer();
        List<EJPluginBlockProperties> allBlockProperties = blockContainer.getAllBlockProperties();
        //clean all blocks to form
        for (EJPluginBlockProperties block : new ArrayList<EJPluginBlockProperties>(allBlockProperties))
        {
            if(block.isImportFromObjectGroup() && block.getReferencedObjectGroupName().equals(old))
                
            {
                block.setReferencedObjectGroupName(newName);
            }
        }
        
        EJPluginRelationContainer relationContainer = form.getRelationContainer();
        List<EJPluginRelationProperties> relationProperties = relationContainer.getAllRelationProperties();
        //clean all relations
        for (EJPluginRelationProperties relation : new ArrayList<EJPluginRelationProperties>(relationProperties))
        {
            if(relation.isImportFromObjectGroup() && relation.getReferencedObjectGroupName().equals(old))
                
            {
               relation.setReferencedObjectGroupName(newName);
            }
        }
        //clean all canvas
        EJPluginCanvasContainer canvasContainer = form.getCanvasContainer();
        Collection<EJPluginCanvasProperties> allCanvasProperties = canvasContainer.getCanvasProperties();
        for (EJPluginCanvasProperties canvas : new ArrayList<EJPluginCanvasProperties>(allCanvasProperties))
        {
            if(canvas.isImportFromObjectGroup() && canvas.getReferencedObjectGroupName().equals(old))
                
            {
                canvas.setReferencedObjectGroupName(newName);
            }
        }
        
        //clean all LOV
        EJPluginLovDefinitionContainer lovDefinitionContainer = form.getLovDefinitionContainer();
        List<EJPluginLovDefinitionProperties> allLovDefinitionProperties = lovDefinitionContainer.getAllLovDefinitionProperties();
        for (EJPluginLovDefinitionProperties lov :  new ArrayList<EJPluginLovDefinitionProperties>(allLovDefinitionProperties))
        {
            if(lov.isImportFromObjectGroup() && lov.getReferencedObjectGroupName().equals(old))
                
            {
                lov.setReferencedObjectGroupName(newName);
            }
        }
    }
    

    public void importObjectsToForm(EJPluginFormProperties form)
    {
        
        EJPluginBlockContainer blockContainer = getBlockContainer();
        List<EJPluginBlockProperties> allBlockProperties = blockContainer.getAllBlockProperties();
        //import all blocks to form
        for (EJPluginBlockProperties block : allBlockProperties)
        {
            block.setReferencedObjectGroupName(getName());//mark as import from this
            
            EJPluginBlockProperties oldProp = form.getBlockContainer().getBlockProperties(block.getName());
            if(oldProp!=null && getName().equals(oldProp.getReferencedObjectGroupName()))
            {
                form.getBlockContainer().replaceBlockProperties(oldProp, block);
            }
            else
            {
                form.getBlockContainer().addBlockProperties(block);
            }
            
            
        }
        
        EJPluginRelationContainer relationContainer = getRelationContainer();
        List<EJPluginRelationProperties> relationProperties = relationContainer.getAllRelationProperties();
        //import all relations
        for (EJPluginRelationProperties relation : relationProperties)
        {
            relation.setReferencedObjectGroupName(getName());
            form.getRelationContainer().addRelationProperties(relation);
        }
        
        
       EJPluginCanvasContainer formContainer = form.getCanvasContainer();
        
        EJPluginCanvasContainer canvasContainer = getCanvasContainer();
        Collection<EJPluginCanvasProperties> allCanvasProperties = canvasContainer.getCanvasProperties();
        for (EJPluginCanvasProperties canvas : allCanvasProperties)
        {
            
            if(canvas.getType()==EJCanvasType.POPUP)
            {
                form.getCanvasContainer().addCanvasProperties(canvas);
                
            }
            else
            {
                EJPluginCanvasProperties canvasProperties = (EJPluginCanvasProperties) EJPluginCanvasRetriever.getCanvasProperties(form, canvas.getName());
                if(canvasProperties == null)
                {
                    formContainer.addCanvasProperties(canvas);
                }
                else
                {
                    canvasProperties.setObjectGroupRoot(true);
                    canvas.setWidth(canvasProperties.getWidth());
                    canvas.setHeight(canvasProperties.getHeight());
                    canvas.setReferredFormId(canvasProperties.getReferredFormId());
                    canvas.setExpandHorizontally(canvasProperties.canExpandHorizontally());
                    canvas.setExpandVertically(canvasProperties.canExpandVertically());
                    canvas.setVerticalSpan(canvasProperties.getVerticalSpan());
                    canvas.setHorizontalSpan(canvasProperties.getHorizontalSpan());
                    canvasProperties.getParentCanvasContainer().replaceCanvasProperties(canvasProperties, canvas);
                }
                canvas.setObjectGroupRoot(true);
            }
        }
        
        Collection<EJCanvasProperties> retriveAllCanvases = EJPluginCanvasRetriever.retriveAllCanvases(this);
        for (EJCanvasProperties canvas : retriveAllCanvases)
        {
            EJPluginCanvasProperties canvasPlug = (EJPluginCanvasProperties)canvas;
            canvasPlug.setReferencedObjectGroupName(getName());
            
           
           
        }
        
        
        EJPluginLovDefinitionContainer lovDefinitionContainer = getLovDefinitionContainer();
        List<EJPluginLovDefinitionProperties> allLovDefinitionProperties = lovDefinitionContainer.getAllLovDefinitionProperties();
        for (EJPluginLovDefinitionProperties lov : allLovDefinitionProperties)
        {
            lov.setReferencedObjectGroupName(getName());
            lov.getBlockProperties().setReferencedObjectGroupName(getName());
            form.getLovDefinitionContainer().addLovDefinitionProperties(lov);
        }
        
    }
    

    
}
