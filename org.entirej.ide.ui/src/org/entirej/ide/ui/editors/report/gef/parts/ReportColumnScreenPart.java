package org.entirej.ide.ui.editors.report.gef.parts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.CompoundSnapToHelper;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.Request;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGuides;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
import org.entirej.framework.plugin.reports.EJPluginReportColumnProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.framework.report.enumerations.EJReportScreenType;
import org.entirej.ide.ui.editors.report.gef.figures.ReportColumnScreenFigure;

public class ReportColumnScreenPart extends AbstractReportGraphicalEditPart
{
    private ReportColumnScreenFigure  base;
    
    
    
    public static class  ReportColumnScreen
    {
        final String text;
        final int height;
        final int width;
        
        final EJPluginReportScreenProperties screenProperties;

        public ReportColumnScreen(String text,int height, int width, EJPluginReportScreenProperties screenProperties)
        {
            super();
            this.text = text;
            this.height = height;
            this.width = width;
            this.screenProperties = screenProperties;
        }

        public int getWidth()
        {
            return width;
        }
        
        public int getHeight()
        {
            return height;
        }
        
        public String getText()
        {
            return text;
        }
        
        public EJPluginReportScreenProperties getScreenProperties()
        {
            return screenProperties;
        }
        
        @Override
        public int hashCode()
        {
            return screenProperties.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            
            if(obj instanceof ReportColumnScreen)
            {
                ReportColumnScreen other = (ReportColumnScreen) obj;
                if (screenProperties.equals(other.screenProperties))
                    return true;
            }
            if(obj instanceof ReportColumnScreen)
            {
                ReportColumnScreen other = (ReportColumnScreen) obj;
                if (screenProperties.equals(other.screenProperties))
                    return true;
            }
            if(obj instanceof EJPluginReportScreenProperties)
            {
               
                if (screenProperties.equals(obj))
                    return true;
            }
            
          
            return false;
        }
        
        
       
    }
    
    
    @Override
    protected IFigure createFigure()
    {
        return (base = new ReportColumnScreenFigure(getModel()));
    }

    @Override
    protected void createEditPolicies()
    {
//        installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE,
//                new ScreenResizableEditPolicy());

      
    }
    
    @Override
    public Object getAdapter(Class key)
    {
        if (key == SnapToHelper.class) {
            List<SnapToHelper> snapStrategies = new ArrayList<SnapToHelper>();
            SnapToGuides snapToGuides = new SnapToGuides(this);
            snapStrategies.add(snapToGuides);
            //snapStrategies.add(new SnapToGrid(this));
            SnapToGeometry snapToGeometry = new SnapToGeometry(this);
           
            snapStrategies.add(snapToGeometry);
            return new CompoundSnapToHelper(snapStrategies.toArray(new SnapToHelper[0]));
        }
        return super.getAdapter(key);
    }

    
    
    @Override
    public ReportColumnScreen getModel()
    {
        return (ReportColumnScreen) super.getModel();
    }

    @Override
    protected void refreshVisuals()
    {
        base.setPreferredSize(getModel().getWidth(), getModel().height);
        
    }

    @Override
    public List<?> getModelChildren()
    {
        ArrayList<Object> list= new ArrayList<Object>();
        
//        ReportColumnScreen model = getModel();
//        if(model.screenProperties.getScreenType()==EJReportScreenType.FORM_LAYOUT)
//        {
//            Collection<?> screenItems = model.screenProperties.getScreenItems();
//            list.addAll(screenItems);
//            
//            List<EJPluginReportBlockProperties> allSubBlocks = model.screenProperties.getAllSubBlocks();
//            list.addAll(allSubBlocks);
//        }
//        else if(model.screenProperties.getScreenType()==EJReportScreenType.TABLE_LAYOUT)
//        {
//            List<EJPluginReportColumnProperties> allColumnProperties = model.screenProperties.getColumnContainer().getAllColumnProperties();
//            list.addAll(allColumnProperties);
//        }
        return list;
    }
    
    
    public DragTracker getDragTracker(Request request) {
        
        return new DragEditPartsTracker(this);
    }
    
    
    

}
