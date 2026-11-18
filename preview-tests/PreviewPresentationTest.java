import java.io.StringReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;
import org.entirej.framework.core.enumerations.EJCanvasType;
import org.entirej.framework.plugin.framework.properties.*;
import org.entirej.ide.ui.editors.preview.*;

/** Canvas design aids must not leak into the form/block renderer preview. */
public class PreviewPresentationTest {
    static void check(boolean value,String name){PreviewRegressionTest.check(value,name);}
    static EJPluginCanvasProperties canvas(EJPluginFormProperties form,String name,EJCanvasType type,int width,int height) {
        EJPluginCanvasProperties canvas=new EJPluginCanvasProperties(form,name);canvas.setType(type);canvas.setNumCols(1);
        canvas.setWidth(width);canvas.setHeight(height);return canvas;
    }
    static void run() throws Exception {
        EJPluginFormProperties form=new EJPluginFormProperties("presentation",null);form.setNumCols(1);
        EJPluginCanvasProperties tabs=canvas(form,"technicalTabs",EJCanvasType.TAB,440,140);
        EJPluginTabPageProperties page=new EJPluginTabPageProperties(tabs,"firstPage");page.setPageTitle("First tab");
        tabs.getTabPageContainer().addTabPageProperties(page);
        tabs.getTabPageContainer().addTabPageProperties(new EJPluginTabPageProperties(tabs,"secondPage"));
        form.getCanvasContainer().addCanvasProperties(tabs);
        EJPluginCanvasProperties stack=canvas(form,"technicalStack",EJCanvasType.STACKED,440,120);
        EJPluginStackedPageProperties first=new EJPluginStackedPageProperties(stack,"firstStack");
        EJPluginStackedPageProperties second=new EJPluginStackedPageProperties(stack,"secondStack");
        stack.getStackedPageContainer().addStackedPageProperties(first);stack.getStackedPageContainer().addStackedPageProperties(second);
        EJPluginCanvasProperties group=canvas(form,"technicalGroup",EJCanvasType.GROUP,200,50);group.setDisplayGroupFrame(true);group.setGroupFrameTitle("Visible group title");
        first.getContainedCanvases().addCanvasProperties(group);form.getCanvasContainer().addCanvasProperties(stack);

        PreviewNode runtime=PreviewRegressionTest.builder().build(form,form,900,700);
        PreviewNode runtimeTabs=PreviewRegressionTest.find(runtime,"technicalTabs"),runtimeStack=PreviewRegressionTest.find(runtime,"technicalStack");
        List<String> text=PreviewRegressionTest.paintedText(runtime);
        check(!runtime.isDesignHintsVisible() && !runtimeStack.isDesignHintsVisible(),"form preview disables design aids per model tree");
        check(!text.contains("technicalTabs") && !text.contains("technicalStack") && !text.contains("technicalGroup"),"form painter omits internal canvas names");
        check(text.containsAll(Arrays.asList("First tab","secondPage","Visible group title")),"runtime tabs, runtime name fallback and configured group titles remain visible");
        check(!text.contains("<STACKED_COMPONENT>") && text.stream().noneMatch(s->s.startsWith("<page>:")||s.startsWith("<title>:")),"form painter omits synthetic stacked/tab labels");
        check(!runtimeTabs.isPaintContainerTitle() && runtimeTabs.getInsetTop()==runtime.getPreviewTheme().tabHeight()+1,"form tabs reserve only the real tab strip, not the canvas name band");
        check(runtimeStack.getInsetTop()==runtime.getPreviewTheme().tabHeight()+1 && runtimeStack.getInsetBottom()==1,"stacked page retains tab-strip space but drops the extra heading band");
        check(runtimeStack.getVisibleChildren().get(0).getBounds().getY()==runtimeStack.getBounds().getY()+runtimeStack.getInsetTop(),"stacked page starts directly beneath its retained navigation tabs");
        check(PreviewRegressionTest.figure(runtimeStack,null).getTabIndexAt(runtimeStack.getBounds().getX()+10,runtimeStack.getBounds().getY()+runtime.getPreviewTheme().tabHeight()/2)==0,"stacked page tabs remain clickable in form preview");
        check(runtimeStack.getTabLabels().equals(Arrays.asList("firstStack","secondStack")) && text.containsAll(Arrays.asList("firstStack","secondStack")),"stacked navigation keeps page names without debug prefixes");
        runtimeStack.setSelectedTabIndex(1);new PreviewGridLayoutSolver().layout(runtime);
        check(runtimeStack.getSelectedTabChild().getSource()==second,"switching stacked tabs changes the visible page in form preview");
        check(((org.eclipse.draw2d.Label)PreviewRegressionTest.figure(runtimeTabs,null).getToolTip()).getText().equals("technicalTabs"),"canvas identity remains available in hover tooltip");

        PreviewNode design=PreviewRegressionTest.builder().build(form,tabs,900,700);
        PreviewNode designStack=PreviewRegressionTest.find(design,"technicalStack"),designTabs=PreviewRegressionTest.find(design,"technicalTabs");
        List<String> designText=PreviewRegressionTest.paintedText(design);
        check(design.isDesignHintsVisible() && designText.contains("technicalTabs") && designText.contains("<STACKED_COMPONENT>"),"canvas design preview retains its existing headings and stacked navigator");
        check(designTabs.getTabLabels().contains("<title>: secondPage") && designStack.getTabLabels().contains("<page>: firstStack"),"canvas design preview retains its diagnostic page labels");
        check(designStack.getInsetTop()>0 && designStack.getVisibleChildren().get(0).getBounds().getY()>designStack.getBounds().getY(),"canvas design chrome retains its reserved layout space");
        PreviewNode selectedPage=PreviewRegressionTest.builder().build(form,second,900,700);
        check(PreviewRegressionTest.find(selectedPage,"technicalStack").getSelectedTabIndex()==1,"outline selection still switches the stacked design page");
        check(!org.entirej.ide.ui.editors.form.preview.FormPreviewModelBuilder.canvasDesignSelection(second,false)
            && org.entirej.ide.ui.editors.form.preview.FormPreviewModelBuilder.canvasDesignSelection(second,true),"page selection preserves runtime/design presentation instead of flipping modes");
        check(!org.entirej.ide.ui.editors.form.preview.FormPreviewModelBuilder.canvasDesignSelection(stack,false,true)
            && org.entirej.ide.ui.editors.form.preview.FormPreviewModelBuilder.canvasDesignSelection(stack,false,false),"preview-click selection echo preserves presentation while an explicit outline canvas selection enters design mode");
        PreviewNode switched=PreviewRegressionTest.builder().build(form,second,900,700,false);
        check(!switched.isDesignHintsVisible() && PreviewRegressionTest.find(switched,"technicalStack").getSelectedTabIndex()==1
            && !PreviewRegressionTest.paintedText(switched).contains("<STACKED_COMPONENT>"),"form page navigation rebuild keeps the selected page without restoring debug headings");
        check(PreviewRegressionTest.find(PreviewRegressionTest.builder().build(form,form,900,700),"technicalTabs").getTabLabels().equals(Arrays.asList("First tab","secondPage")),"design preview does not mutate form properties or later runtime previews");
        if(PreviewRegressionTest.screenshots!=null) {
            runtimeStack.setSelectedTabIndex(0);new PreviewGridLayoutSolver().layout(runtime);
            PreviewRegressionTest.render(runtime,"presentation-form.png");
            PreviewRegressionTest.render(design,"presentation-canvas.png");
        }

        for(String renderer:Arrays.asList("HtmlTable","TreeRecord","PieChartRecord")) {
            EJPluginBlockProperties block=block(renderer,"technical"+renderer);
            PreviewNode selected=PreviewRegressionTest.builder().build(block.getFormProperties(),block,900,700);
            check(!PreviewRegressionTest.paintedText(selected).contains(block.getName()),renderer+" selected renderer omits technical block-name heading");
            EJPluginCanvasProperties blockCanvas=canvas(block.getFormProperties(),"blockCanvas",EJCanvasType.BLOCK,400,180);
            block.setCanvasName("blockCanvas");block.getFormProperties().getCanvasContainer().addCanvasProperties(blockCanvas);
            PreviewNode canvasDesign=PreviewRegressionTest.builder().build(block.getFormProperties(),blockCanvas,900,700);
            check(PreviewRegressionTest.paintedText(canvasDesign).contains(block.getName()),renderer+" canvas design placeholder heading is unchanged");
        }
        EJPluginBlockProperties columns=block("MultiRecord","technicalTable");
        PreviewNode selected=PreviewRegressionTest.builder().build(columns.getFormProperties(),columns,900,700);
        check(PreviewRegressionTest.paintedText(selected).contains("Runtime column"),"real table column captions are preserved");
    }
    static EJPluginBlockProperties block(String renderer,String name) throws Exception {
        String xml="<block name='"+name+"'><blockRendererName>"+renderer+"</blockRendererName><itemList><item name='column'><itemRendererName>TextItem</itemRendererName></item></itemList>"
            +"<mainScreen><itemGroupList><itemGroup name='columns'><itemGroupItemList><item referencedItemName='column'><label>Runtime column</label></item></itemGroupItemList></itemGroup></itemGroupList></mainScreen></block>";
        return PreviewRegressionTest.loadBlock(Paths.get("renderer-presentation.form"),DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml))).getDocumentElement());
    }
}
