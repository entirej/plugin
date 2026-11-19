import java.nio.file.Path;
import org.w3c.dom.*;
import org.entirej.framework.core.enumerations.*;
import org.entirej.framework.plugin.framework.properties.*;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginCanvasContainer;

/** Local XML fixture reconstruction; no application, project definitions or database startup. */
public class PreviewFormFixture {
    static void verify() throws Exception {
        String fixture="""
            <entirejFramework><form><formWidth>740</formWidth><formHeight>520</formHeight><numCols>2</numCols>
            <canvasList><canvas name="pages" type="TAB"><tabPageList><tabPage name="page"><pageTitle>Page</pageTitle><numCols>3</numCols>
            <canvasList><canvas name="details" type="BLOCK"/></canvasList></tabPage></tabPageList></canvas>
            <canvas name="dialog" type="POPUP"><canvasList><canvas name="popupDetails" type="BLOCK"/></canvasList></canvas></canvasList>
            <blockList><blockGroup name="organized"><block name="record"><canvasName>details</canvasName><blockRendererName>SingleRecord</blockRendererName>
            <mainScreenProperties><displayFrame>true</displayFrame><frameTitle>Details</frameTitle><numCols>2</numCols><width>144</width><height>66</height>
            <horizontalSpan>3</horizontalSpan><expandHorizontally>true</expandHorizontally></mainScreenProperties><itemList/>
            </block></blockGroup></blockList><lovDefinitionList><block name="notAMainBlock"/></lovDefinitionList>
            </form></entirejFramework>
            """;
        Document xml=javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new org.xml.sax.InputSource(new java.io.StringReader(fixture)));
        EJPluginFormProperties form=load(Path.of("loader-fixture.form"),xml);
        PreviewRegressionTest.check(form.getBlockContainer().getAllBlockProperties().size()==1,"full-form fixture includes nested block groups without importing LOV blocks into the main form");
        EJPluginCanvasProperties tabs=form.getCanvasContainer().getCanvasProperties().iterator().next();
        EJPluginTabPageProperties page=tabs.getTabPageContainer().getTabPageProperties().iterator().next();
        EJPluginBlockProperties block=page.getContainedCanvases().getCanvasProperties().iterator().next().getPluginBlockProperties();
        PreviewRegressionTest.check(block!=null && block.getName().equals("record"),"nested main-screen canvas binds to its locally defined block");
        PreviewRegressionTest.check(page.getNumCols()==3 && page.getPageTitle().equals("Page"),"full-form fixture preserves nested page layout and captions");
        PreviewRegressionTest.check(block.getMainScreenProperties().getDisplayFrame() && block.getMainScreenProperties().getWidth()==144
            && block.getMainScreenProperties().getHorizontalSpan()==3 && block.getMainScreenProperties().canExpandHorizontally(),"full-form fixture preserves real block frame, size, span and expand settings");
        PreviewRegressionTest.check(form.getFormWidth()==740 && form.getFormHeight()==520 && form.getNumCols()==2,"full-form fixture preserves configured main form dimensions");
        PreviewRegressionTest.check(form.getCanvasContainer().getCanvasProperties().get(1).getPopupCanvasContainer().getCanvasProperties().size()==1,
            "popup fixture reads the serialized canvasList rather than painting an empty dialog");
        Document reusable=javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new org.xml.sax.InputSource(new java.io.StringReader(
            "<block name='base'><itemList><item name='caption'><itemRendererName>LabelItem</itemRendererName></item></itemList><mainScreenProperties><width>90</width><height>40</height></mainScreenProperties></block>")));
        Document override=javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new org.xml.sax.InputSource(new java.io.StringReader(
            "<block name='reference'><itemList/><mainScreenProperties><width>120</width></mainScreenProperties></block>")));
        Element merged=(Element)reusable.getDocumentElement().cloneNode(true);PreviewFixtureRepository.merge(merged,override.getDocumentElement());
        PreviewRegressionTest.check(merged.getElementsByTagName("itemRendererName").getLength()==1,"empty serialized reference itemList retains reusable renderer definitions");
        Element main=PreviewRegressionTest.child(merged,"mainScreenProperties");
        PreviewRegressionTest.check(PreviewRegressionTest.integer(main,"width",0)==120 && PreviewRegressionTest.integer(main,"height",0)==40,
            "reference main-screen overrides merge without losing inherited properties");
        PreviewRegressionTest.check(PreviewRegressionTest.integer(PreviewRegressionTest.child(reusable.getDocumentElement(),"mainScreenProperties"),"width",0)==90,
            "reference expansion never mutates cached shared XML");
    }
    record Loaded(EJPluginFormProperties form,PreviewFixtureRepository repository) { }
    static EJPluginFormProperties load(Path path,Document xml) throws Exception {
        return resolved(path,xml).form();
    }
    static Loaded resolved(Path path,Document xml) throws Exception {
        PreviewFixtureRepository repository=new PreviewFixtureRepository(path);
        EJPluginFormProperties form=new EJPluginFormProperties(path.getFileName().toString(),null);
        repository.loadInto(path,xml,form);return new Loaded(form,repository);
    }
    static void canvases(Element list,EJPluginCanvasContainer target,EJPluginFormProperties form) {
        for(Element source:PreviewRegressionTest.children(list,"canvas")) {
            EJPluginCanvasProperties canvas=new EJPluginCanvasProperties(form,source.getAttribute("name"));
            canvas.setType(EJCanvasType.valueOf(source.getAttribute("type")));
            canvas.setNumCols(PreviewRegressionTest.integer(source,"numCols",1));
            canvas.setWidth(PreviewRegressionTest.integer(source,"width",0));canvas.setHeight(PreviewRegressionTest.integer(source,"height",0));
            canvas.setHorizontalSpan(PreviewRegressionTest.integer(source,"horizontalSpan",1));canvas.setVerticalSpan(PreviewRegressionTest.integer(source,"verticalSpan",1));
            canvas.setExpandHorizontally(PreviewRegressionTest.bool(source,"expandHorizontally",false));canvas.setExpandVertically(PreviewRegressionTest.bool(source,"expandVertically",false));
            canvas.setDisplayGroupFrame(PreviewRegressionTest.bool(source,"displayGroupFrame",false));canvas.setGroupFrameTitle(PreviewRegressionTest.text(source,"groupFrameTitle",""));
            canvas.setPopupPageTitle(PreviewRegressionTest.text(source,"popupPageTitle",""));canvas.setReferredFormId(PreviewRegressionTest.text(source,"referredFormId",""));
            canvas.setInitalStackedPageName(PreviewRegressionTest.text(source,"initialStackedPageName",""));
            if(PreviewRegressionTest.child(source,"splitOrientation")!=null)canvas.setSplitOrientation(EJCanvasSplitOrientation.valueOf(PreviewRegressionTest.text(source,"splitOrientation","HORIZONTAL")));
            if(PreviewRegressionTest.child(source,"tabPosition")!=null)canvas.setTabPosition(EJCanvasTabPosition.valueOf(PreviewRegressionTest.text(source,"tabPosition","TOP")));
            if(PreviewRegressionTest.child(source,"drawerPosition")!=null)canvas.setDrawerPosition(EJCanvasDrawerPosition.valueOf(PreviewRegressionTest.text(source,"drawerPosition","LEFT")));
            target.addCanvasProperties(canvas);
            for(Element page:PreviewRegressionTest.children(PreviewRegressionTest.child(source,"tabPageList"),"tabPage")) {
                EJPluginTabPageProperties tab=new EJPluginTabPageProperties(canvas,page.getAttribute("name"));
                tab.setPageTitle(PreviewRegressionTest.text(page,"pageTitle",""));tab.setNumCols(PreviewRegressionTest.integer(page,"numCols",1));
                tab.setVisible(PreviewRegressionTest.bool(page,"visible",true));tab.setEnabled(PreviewRegressionTest.bool(page,"enabled",true));
                canvas.getTabPageContainer().addTabPageProperties(tab);canvases(PreviewRegressionTest.child(page,"canvasList"),tab.getContainedCanvases(),form);
            }
            for(Element page:PreviewRegressionTest.children(PreviewRegressionTest.child(source,"stackedPageList"),"stackedPage")) {
                EJPluginStackedPageProperties stacked=new EJPluginStackedPageProperties(canvas,page.getAttribute("name"));
                stacked.setNumCols(PreviewRegressionTest.integer(page,"numCols",1));canvas.getStackedPageContainer().addStackedPageProperties(stacked);
                canvases(PreviewRegressionTest.child(page,"canvasList"),stacked.getContainedCanvases(),form);
            }
            for(Element page:PreviewRegressionTest.children(PreviewRegressionTest.child(source,"drawerPageList"),"drawerPage")) {
                EJPluginDrawerPageProperties drawer=new EJPluginDrawerPageProperties(canvas,page.getAttribute("name"));
                drawer.setPageTitle(PreviewRegressionTest.text(page,"pageTitle",""));drawer.setNumCols(PreviewRegressionTest.integer(page,"numCols",1));
                drawer.setVisible(PreviewRegressionTest.bool(page,"visible",true));drawer.setEnabled(PreviewRegressionTest.bool(page,"enabled",true));
                canvas.getDrawerPageContainer().addDrawerPageProperties(drawer);canvases(PreviewRegressionTest.child(page,"canvasList"),drawer.getContainedCanvases(),form);
            }
            canvases(PreviewRegressionTest.child(source,"groupCanvasList"),canvas.getType()==EJCanvasType.SPLIT?canvas.getSplitCanvasContainer():canvas.getGroupCanvasContainer(),form);
            canvases(PreviewRegressionTest.child(source,"splitCanvasList"),canvas.getSplitCanvasContainer(),form);
            canvases(PreviewRegressionTest.child(source,"popupCanvasList"),canvas.getPopupCanvasContainer(),form);
            if(canvas.getType()==EJCanvasType.POPUP)canvases(PreviewRegressionTest.child(source,"canvasList"),canvas.getPopupCanvasContainer(),form);
        }
    }
}
