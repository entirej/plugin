package org.entirej.framework.plugin.reports;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.entirej.framework.core.service.EJBlockService;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer.BlockContainerItem;
import org.entirej.framework.plugin.reports.containers.EJReportBlockItemContainer;
import org.entirej.framework.plugin.reports.containers.EJReportScreenItemContainer;
import org.entirej.framework.reports.interfaces.EJReportBlockProperties;
import org.entirej.framework.reports.interfaces.EJReportItemProperties;
import org.entirej.ide.core.EJCoreLog;

public class EJPluginReportBlockProperties implements EJReportBlockProperties, BlockContainerItem
{
    
    private boolean                        _isControlBlock           = false;
    
    private String                         _blockDescription         = "";
    private boolean                        _isReferenced             = false;
    private String                         _canvasName               = "";
    
    private EJPluginReportProperties       _reportProperties;
    private String                         _name                     = "";
    
    private EJPluginReportScreenProperties _layoutScreenProperties;
    
    private String                         _serviceClassName;
    private String                         _actionProcessorClassName = "";
    
    private EJReportBlockItemContainer     _itemContainer;
   
    
    public EJPluginReportBlockProperties(EJPluginReportProperties formProperties, String blockName, boolean isCcontrolBlock)
    {
        
        _reportProperties = formProperties;
        _name = blockName;
        _isControlBlock = isCcontrolBlock;
        _layoutScreenProperties = new EJPluginReportScreenProperties(this);
        _itemContainer = new EJReportBlockItemContainer(this);
    }
    
    
    
    @Override
    public EJPluginReportScreenProperties getLayoutScreenProperties()
    {
        return _layoutScreenProperties;
    }
    
   
    
    @Override
    public Collection<EJReportItemProperties> getAllItemProperties()
    {
        return new ArrayList<EJReportItemProperties>(_itemContainer.getAllItemProperties());
    }
    
    @Override
    public EJPluginReportItemProperties getItemProperties(String itemName)
    {
        return _itemContainer.getItemProperties(itemName);
    }
    
    @Override
    public String getCanvasName()
    {
        return _canvasName;
    }
    
    public void setCanvasName(String canvasName)
    {
        this._canvasName = canvasName;
    }
    
    @Override
    public boolean isControlBlock()
    {
        return _isControlBlock;
    }
    
    public void setControlBlock(boolean isControlBlock)
    {
        _isControlBlock = isControlBlock;
    }
    
    @Override
    public boolean isReferenceBlock()
    {
        return _isReferenced;
    }
    
    @Override
    public String getDescription()
    {
        return _blockDescription;
    }
    
    public void setDescription(String description)
    {
        _blockDescription = description;
    }
    
    @Override
    public EJPluginReportProperties getReportProperties()
    {
        return _reportProperties;
    }
    
    public String getName()
    {
        return _name;
    }
    
    public void internalSetName(String blockName)
    {
        if (blockName != null && blockName.trim().length() > 0)
        {
            _name = blockName;
        }
    }
 
    
    public String getActionProcessorClassName()
    {
        return _actionProcessorClassName;
    }
    
    public void setActionProcessorClassName(String processorClassName)
    {
        _actionProcessorClassName = processorClassName;
        
    }
    
    /**
     * Returns the class name of the service that is responsible for the data
     * manipulation for this block
     * 
     * @return the service class name
     */
    public String getServiceClassName()
    {
        return _serviceClassName;
    }
    
    /**
     * Sets the class name of the service that is responsible for the retrieval
     * and manipulation of this blocks data
     * 
     * @return the service class name
     */
    public void setServiceClassName(String serviceClassName, boolean addItems)
    {
        if (addItems && (_serviceClassName == null || (serviceClassName != null && !_serviceClassName.equals(serviceClassName.trim()))))
        {
            _serviceClassName = serviceClassName.trim();
            
            getItemContainer().sync(getServiceItems());
            
        }
        else
        {
            _serviceClassName = serviceClassName;
        }
        
    }
    
    public EJReportBlockItemContainer getItemContainer()
    {
        return _itemContainer;
    }
    

    
    @Override
    public EJBlockService<?> getBlockService()
    {
        return null;
    }
    
    public EJPluginEntireJProperties getEntireJProperties()
    {
        return _reportProperties.getEntireJProperties();
    }
    
    public List<EJPluginReportItemProperties> getServiceItems()
    {
        
        List<EJPluginReportItemProperties> newItems = new ArrayList<EJPluginReportItemProperties>();
        try
        {
            
            IType serviceType = getEntireJProperties().getJavaProject().findType(_serviceClassName);
            if (serviceType != null)
            {
                String[] superInterfaces = serviceType.getSuperInterfaceTypeSignatures();
                for (String superInterface : superInterfaces)
                {
                    String typeErasure = Signature.getTypeErasure(Signature.toString(superInterface));
                    
                    if (typeErasure != null && EJBlockService.class.getSimpleName().equals(typeErasure))
                    {
                        String[] typeArguments = Signature.getTypeArguments(superInterface);
                        if (typeArguments.length == 1)
                        {
                            serviceType.getTypeParameter(Signature.toString(typeArguments[0])).getPath();
                            String[][] resolveType = serviceType.resolveType(Signature.toString(typeArguments[0]));
                            if (resolveType != null && resolveType.length == 1)
                            {
                                
                                String[] typeSegments = resolveType[0];
                                String pojoName = Signature.toQualifiedName(typeSegments);
                                IType pojoType = getEntireJProperties().getJavaProject().findType(pojoName);
                                if (pojoType != null)
                                {
                                    IMethod[] methods = pojoType.getMethods();
                                    for (IMethod method : methods)
                                    {
                                        if (!method.isConstructor() && method.getNumberOfParameters() == 0
                                                && (!Signature.SIG_VOID.equals(method.getReturnType())))
                                        {
                                            String name = method.getElementName();
                                            if (name.startsWith("get"))
                                            {
                                                name = name.substring(3);
                                            }
                                            else if (name.startsWith("is"))
                                            {
                                                name = name.substring(2);
                                            }
                                            else
                                            {
                                                continue;
                                            }
                                            // check dose it Has setter name
                                            IMethod setter = pojoType.getMethod(String.format("set%s", name), new String[] { method.getReturnType() });
                                            if (setter != null && setter.exists())
                                            {
                                                String fieldName = String.format("%s%s", name.substring(0, 1).toLowerCase(), name.substring(1));
                                                EJPluginReportItemProperties item = new EJPluginReportItemProperties(this);
                                                item.setName(fieldName);
                                                String[][] resolveRetrunType = pojoType.resolveType(Signature.toString(method.getReturnType()));
                                                if (resolveRetrunType != null && resolveRetrunType.length == 1)
                                                {
                                                    item.setDataTypeClassName(Signature.toQualifiedName(resolveRetrunType[0]));
                                                }
                                                item.setBlockServiceItem(true);
                                                newItems.add(item);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                }
            }
            
        }
        catch (JavaModelException e)
        {
            EJCoreLog.logException(e);
        }
        finally
        {
            
        }
        return newItems;
    }
    
}
