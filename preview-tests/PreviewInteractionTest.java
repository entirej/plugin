import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.*;
import org.entirej.framework.dev.renderer.definition.*;
import org.entirej.ide.ui.editors.preview.*;

/** Actual GEF viewer/selection and mouse-event checks, without a live workbench. */
public class PreviewInteractionTest {
    static String scenario;
    static void check(boolean value,String message){PreviewRegressionTest.check(value,scenario+": "+message);}
    static PreviewNode node(Object source,EJDevPreviewKind kind,String name,int width,int height) {
        PreviewNode node=new PreviewNode(source,EJDevPreviewDescriptor.create(kind,name,name,null,width,height));
        node.setPaintContainerTitle(false);node.setLayoutMargin(0);return node;
    }
    static void run() {
        for(EJDevPreviewKind kind:new EJDevPreviewKind[]{EJDevPreviewKind.TAB_FOLDER,EJDevPreviewKind.STACKED})
            for(boolean bottom:new boolean[]{false,true})for(boolean design:new boolean[]{false,true})runCase(kind,bottom,design);
    }
    static void runCase(EJDevPreviewKind kind,boolean bottom,boolean design) {
        scenario=kind+" "+(bottom?"bottom":"top")+" "+(design?"design":"form");
        Shell shell=new Shell(PreviewRegressionTest.display);shell.setLayout(new FillLayout());shell.setSize(640,420);
        List<Object> selected=new ArrayList<>();
        try {
            PreviewEditControl control=new PreviewEditControl(shell,selected::add);
            control.getViewer().setRootEditPart(new org.eclipse.gef.editparts.ScalableRootEditPart() {
                @Override public <T>T getAdapter(Class<T> type) {
                    if(type==org.eclipse.gef.ExposeHelper.class || type==org.eclipse.gef.AccessibleEditPart.class)return null;
                    return super.getAdapter(type);
                }
            });
            control.getViewer().setEditPartFactory((context,model)-> {
                PreviewNodePart part=new PreviewNodePart() {
                    @Override public <T>T getAdapter(Class<T> type) {
                        // These optional workbench adapters are not available in this standalone runner.
                        if(type==org.eclipse.gef.ExposeHelper.class || type==org.eclipse.gef.AccessibleEditPart.class)return null;
                        return super.getAdapter(type);
                    }
                };
                part.setModel(model);return part;
            });
            // Use the standard Draw2D tooltip helper without an OSGi extension registry.
            org.eclipse.draw2d.FigureCanvas figureCanvas=(org.eclipse.draw2d.FigureCanvas)control.getViewer().getControl();
            figureCanvas.getLightweightSystem().setEventDispatcher(new org.eclipse.gef.ui.parts.DomainEventDispatcher(
                    control.getViewer().getEditDomain(),control.getViewer()) {
                @Override protected org.eclipse.draw2d.ToolTipHelper createToolTipHelper() {
                    return new org.eclipse.draw2d.ToolTipHelper(figureCanvas);
                }
            });
            Object formSource=new Object(),tabsSource=new Object(),firstSource=new Object(),secondSource=new Object(),blockSource=new Object();
            PreviewNode form=node(formSource,EJDevPreviewKind.FORM_CONTAINER,"form",600,360);form.setBounds(0,0,600,360);
            form.setDesignHintsVisible(design);
            PreviewNode tabs=node(tabsSource,kind,"tabs",560,300);tabs.setTabsAtBottom(bottom);tabs.setTabLabels(Arrays.asList("First","Second"));
            PreviewNode first=node(firstSource,EJDevPreviewKind.TAB_PAGE,"first",0,0),second=node(secondSource,EJDevPreviewKind.TAB_PAGE,"second",0,0);
            second.addChild(node(blockSource,EJDevPreviewKind.BLOCK,"block",300,180));tabs.addChild(first);tabs.addChild(second);form.addChild(tabs);
            new PreviewGridLayoutSolver().layout(form);control.setModel(form);shell.layout(true,true);
            check(selected.isEmpty(),"setting the GEF model does not echo a selection to the outline");
            GraphicalEditPart tabsPart=(GraphicalEditPart)control.getViewer().getEditPartRegistry().get(tabs);
            PreviewNodeFigure figure=(PreviewNodeFigure)tabsPart.getFigure();figure.validate();
            Point click=null;
            for(int y=0;y<figure.getBounds().height && click==null;y++)for(int x=0;x<300;x++) {
                Point point=new Point(x,y);figure.translateToAbsolute(point);
                Point relative=point.getCopy();figure.translateToRelative(relative);
                if(figure.getTabIndexAt(relative.x,relative.y)==1){click=point;break;}
            }
            check(click!=null,"actual GEF tab figure exposes the second tab hit area");
            if(click==null)return;
            Control canvas=control.getViewer().getControl();
            Event event=new Event();event.widget=canvas;event.button=1;event.x=click.x;event.y=click.y;event.count=1;
            canvas.notifyListeners(SWT.MouseDown,event);canvas.notifyListeners(SWT.MouseUp,event);
            check(tabs.getSelectedTabIndex()==1 && tabs.getVisibleChildren().get(0)==second,"SWT mouse click switches the actual GEF tab page");
            check(control.getViewer().getContents().getModel()==form && form.isDesignHintsVisible()==design,"GEF tab navigation keeps the form root and presentation");
            check(selected.contains(secondSource),"tab navigation selects the page source for outline synchronization");
            check(selected.get(selected.size()-1)==secondSource,"tab click is not replaced by a block or folder selection on mouse release");
            GraphicalEditPart blockPart=(GraphicalEditPart)control.getViewer().getEditPartRegistry().get(second.getChildren().get(0));
            control.getViewer().setSelection(new StructuredSelection(blockPart));
            check(selected.get(selected.size()-1)==blockSource,"preview block clicks still provide their source to the outline");
            int before=selected.size();control.selectSource(blockSource);
            while(PreviewRegressionTest.display.readAndDispatch()) { }
            check(selected.size()==before && tabs.getSelectedTabIndex()==1,"programmatic selection highlighting does not echo or reset the page");
        } finally {shell.dispose();}
    }
}
