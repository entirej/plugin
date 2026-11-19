import java.nio.file.*;
import java.nio.file.Path;
import java.util.*;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.*;
import org.w3c.dom.*;
import org.entirej.framework.core.enumerations.*;
import org.entirej.framework.core.extensions.properties.*;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.definitions.EJPropertyDefinitionType;
import org.entirej.framework.dev.renderer.definition.*;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevBlockRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevItemRendererDefinition;
import org.entirej.framework.plugin.framework.properties.*;
import org.entirej.framework.plugin.framework.properties.containers.*;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginScreenItemProperties;
import org.entirej.ide.ui.editors.form.preview.*;
import org.entirej.ide.ui.editors.preview.*;
import org.eclipse.draw2d.SWTGraphics;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.widgets.Display;

/** Standalone regression runner; no application/database or Eclipse workspace mutations. */
public class PreviewRegressionTest {
    static int passed, failed;
    static Display display;
    static Path screenshots;
    static PreviewTheme testTheme;
    static FormPreviewModelBuilder builder(){return new FormPreviewModelBuilder(new FormPreviewDescriptorResolver(),testTheme);}
    static final XPath XPATH = XPathFactory.newInstance().newXPath();
    static void check(boolean value, String description) {
        if (value) { passed++; System.out.println("PASS " + description); }
        else { failed++; System.out.println("FAIL " + description); }
    }
    static PreviewNode node(EJDevPreviewKind kind, int width, int height) {
        PreviewNode n = new PreviewNode(null, EJDevPreviewDescriptor.create(kind, kind.name(), kind.name(), null, width, height));
        n.setPaintBorder(false); n.setPaintContainerTitle(false); n.setCompactLayout(true);
        n.setConstraint(PreviewGridConstraint.defaults().setGrabHorizontal(false).setGrabVertical(false));
        return n;
    }
    static boolean inside(PreviewNode child, PreviewNode parent) {
        PreviewBounds c=child.getBounds(),p=parent.getBounds();
        return c.getX()>=p.getX() && c.getY()>=p.getY()
            && c.getX()+c.getWidth()<=p.getX()+p.getWidth() && c.getY()+c.getHeight()<=p.getY()+p.getHeight();
    }
    public static void main(String[] args) throws Exception {
        if(args.length>3)testTheme=PreviewTheme.fromCss(Files.readString(Paths.get(args[3])));
        else testTheme=PreviewTheme.rapDefault();
        PreviewNode root=node(EJDevPreviewKind.FORM_CONTAINER,0,0);root.setBounds(0,0,900,700);
        PreviewNode group=node(EJDevPreviewKind.GROUP,0,0);root.addChild(group);
        for(int i=0;i<4;i++)group.addChild(node(EJDevPreviewKind.TEXT_FIELD,180,36));
        PreviewGridLayoutSolver solver=new PreviewGridLayoutSolver();solver.layout(root);
        check(group.getBounds().getHeight()>=4*36+3*5+10,"auto group measures every child row");
        check(inside(group.getChildren().get(3),group),"last child is not clipped by auto group");
        check(group.getBounds().getWidth()>=190,"auto group measures child width");
        PreviewNode nested=node(EJDevPreviewKind.GROUP,0,0);group.addChild(nested);
        nested.addChild(node(EJDevPreviewKind.TEXT_AREA,210,90));solver.layout(root);
        check(inside(nested,group)&&inside(nested.getChildren().get(0),nested),"nested auto groups measure bottom-up on repeated layout");
        PreviewNode explicit=node(EJDevPreviewKind.GROUP,200,30);explicit.addChild(node(EJDevPreviewKind.TEXT_AREA,300,100));
        root.addChild(explicit);solver.layout(root);
        check(explicit.getBounds().getHeight()==30,"explicit group height is preserved");
        PreviewNode radio=node(EJDevPreviewKind.RADIO_GROUP,0,0);
        radio.setOptionLabels(Arrays.asList("",null,"Yes"," "));
        check(radio.getOptionLabels().size()==4,"blank radio labels retain their button slots");
        PreviewNode minimum=node(EJDevPreviewKind.GROUP,0,0);
        minimum.getConstraint().setMinimumWidth(40).setMinimumHeight(20);
        minimum.addChild(node(EJDevPreviewKind.TEXT_AREA,210,90));root.addChild(minimum);solver.layout(root);
        check(minimum.getBounds().getWidth()>=220&&minimum.getBounds().getHeight()>=100,"minimum constraints do not replace natural size");
        PreviewNode tabs=node(EJDevPreviewKind.TAB_FOLDER,0,0);
        tabs.addChild(node(EJDevPreviewKind.TAB_PAGE,180,40));tabs.addChild(node(EJDevPreviewKind.TAB_PAGE,180,100));root.addChild(tabs);
        solver.layout(root);int firstHeight=tabs.getBounds().getHeight();tabs.setSelectedTabIndex(1);solver.layout(root);
        check(tabs.getBounds().getHeight()>firstHeight,"selected tab natural size is remeasured");
        PreviewNode split=node(EJDevPreviewKind.SPLIT,0,0);split.setColumns(2);split.setBounds(0,0,600,300);
        PreviewNode left=node(EJDevPreviewKind.CANVAS,100,100),right=node(EJDevPreviewKind.CANVAS,300,100);
        left.getConstraint().setGrabHorizontal(true);right.getConstraint().setGrabHorizontal(true);split.addChild(left);split.addChild(right);solver.layout(split);
        check(right.getBounds().getWidth()>left.getBounds().getWidth()&&inside(right,split),"weighted split sizing remains intact");
        display=new Display();
        if(args.length>1 && !"-".equals(args[1]))screenshots=Paths.get(args[1]);
        rendererRegressions();
        PreviewSpacingTest.run();
        PreviewPresentationTest.run();
        PreviewInteractionTest.run();
        if(args.length>0)for(String form:Arrays.asList("TM03.2","TM03.4","TM03.5","TM04.2"))testForm(Paths.get(args[0],form+".form"));
        if(args.length>2 && "corpus".equals(args[2]))PreviewCorpusTest.run(Paths.get(args[0]));
        if(display!=null)display.dispose();
        System.out.println("RESULT passed="+passed+" failed="+failed);
        if(failed>0)throw new AssertionError(failed+" preview regressions failed");
    }
    static void testForm(Path path) throws Exception {
        Document xml=parse(path);
        String blockName=path.getFileName().toString().startsWith("TM04")?"TM04ARequest":path.getFileName().toString().substring(0,6).replace(".","")+"CProductType";
        Element blockXml=(Element)XPATH.evaluate("//block[@name='"+blockName+"']",xml,XPathConstants.NODE);
        if(blockXml==null)throw new AssertionError("Missing block "+blockName);
        EJPluginBlockProperties block=loadBlock(path,blockXml);
        EJPluginFormProperties form=block.getFormProperties();
        PreviewNode preview=builder().build(form,block,900,700);
        validateContainers(preview,path.getFileName().toString());
        PreviewNode radio=findRadio(preview);
        check(radio!=null,path.getFileName()+" contains radio preview");
        check(preview.getBounds().getWidth()>=900,path.getFileName()+" root can scroll natural-width content");
        if(radio!=null && blockName.equals("TM04ARequest")) {
            check(radio.getOptionLabels().size()==2 && radio.getOptionLabels().get(0).isEmpty(),"TM04.2 preserves two unlabelled excess choices");
            check(!contains(preview,"criticalExcess"),"TM04.2 explicitly hidden fields stay hidden");
        }
        if(radio!=null && !blockName.equals("TM04ARequest")) {
            int longest=radio.getOptionLabels().stream().mapToInt(s->org.eclipse.draw2d.FigureUtilities.getTextWidth(s,testTheme.font("Button[RADIO]",null))).max().orElse(0);
            check(radio.getBounds().getWidth()>=longest+23,path.getFileName()+" radio options fit intrinsic width");
        }
        System.out.println("FORM "+path.getFileName()+" block="+blockName);
        if(screenshots!=null)render(preview,path.getFileName().toString()+".png");
    }
    static Document parse(Path path) throws Exception {
        DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl",true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities",false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities",false);
        return factory.newDocumentBuilder().parse(path.toFile());
    }
    static void rendererRegressions() throws Exception {
        // Anonymous, non-customer fixtures cover visible/blank captions and explicit size overrides.
        StringBuilder xml=new StringBuilder("<block name='controls'><blockRendererName>SingleRecord</blockRendererName><mainScreenProperties><numCols>1</numCols></mainScreenProperties><itemList>");
        String[] renderers={"LabelItem","CheckBoxItem","StackedItem","DateTimeItem","ButtonItem","LabelItem","CheckBoxItem"};
        for(int i=0;i<renderers.length;i++)xml.append("<item name='i").append(i).append("'><itemRendererName>").append(renderers[i])
            .append("</itemRendererName><itemRendererProperties><property name='ALIGNMENT'>RIGHT</property></itemRendererProperties></item>");
        xml.append("</itemList><mainScreen><itemGroupList><itemGroup name='group'><numCols>2</numCols><itemGroupItemList>");
        for(int i=0;i<renderers.length;i++)xml.append("<item referencedItemName='i").append(i).append("'><label>")
            .append(i<2?"A long visible caption that must not be clipped":"")
            .append("</label><blockRendererRequiredProperties>").append(i==5||i==6?"<property name='DISPLAYED_WIDTH'>10</property>":"")
            .append("</blockRendererRequiredProperties></item>");
        xml.append("<item referencedItemName='hidden'><visible>false</visible></item></itemGroupItemList></itemGroup></itemGroupList></mainScreen></block>");
        Document document=DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new org.xml.sax.InputSource(new java.io.StringReader(xml.toString())));
        EJPluginBlockProperties block=loadBlock(Paths.get("renderer-fixtures.form"),document.getDocumentElement());
        PreviewNode root=builder().build(block.getFormProperties(),block,900,700);
        PreviewNode label=find(root,"i0"),checkbox=find(root,"i1");
        int captionWidth=org.eclipse.draw2d.FigureUtilities.getTextWidth("A long visible caption that must not be clipped",testTheme.font("Label",null));
        check(label.getConstraint().getPreferredWidth()>=captionWidth,"auto label measures caption rather than internal item name");
        check(checkbox.getConstraint().getPreferredWidth()>=captionWidth+23,"auto checkbox reserves caption and checkbox glyph");
        check(label.getTextAlignment()==PreviewNode.TEXT_ALIGN_RIGHT,"label renderer ALIGNMENT is honored");
        check(find(root,"i3").getKind()==EJDevPreviewKind.DATE_TIME_FIELD,"date-time renderer is not mistaken for date only");
        check(find(root,"i5").getConstraint().getPreferredWidth()==11*testTheme.characterWidth("Label",null),"explicit label width keeps character units");
        check(find(root,"i6").getConstraint().getPreferredWidth()==10,"explicit checkbox width keeps pixel units");
        check(!contains(root,"hidden"),"hidden fixture stays hidden");
        List<String> painted=paintedText(root);
        check(painted.contains("STACK-TEXT-MODE")&&!painted.contains("<STACKED_COMPONENT>")&&!painted.contains("Tab"),
            "stacked item paints a field placeholder, not clipped canvas tabs");
        check(!painted.contains("i4")&&!painted.contains("i5")&&!painted.contains("i6"),"blank inline captions do not expose internal item names");
        PreviewNode canvas=node(EJDevPreviewKind.STACKED,300,200);canvas.setPaintContainerTitle(true);canvas.setTabLabels(Arrays.asList("First","Second"));
        canvas.addChild(node(EJDevPreviewKind.TAB_PAGE,200,80));canvas.addChild(node(EJDevPreviewKind.TAB_PAGE,200,80));
        canvas.setBounds(0,0,300,200);new PreviewGridLayoutSolver().layout(canvas);
        check(paintedText(canvas).containsAll(Arrays.asList("<STACKED_COMPONENT>","First","Second")),"stacked canvases retain their page tabs");
    }
    static List<String> paintedText(PreviewNode model) {
        if(display==null)display=new Display();
        Image image=new Image(display,Math.max(1,model.getBounds().getWidth()),Math.max(1,model.getBounds().getHeight()));
        GC gc=new GC(image);List<String> result=new ArrayList<>();
        SWTGraphics graphics=new SWTGraphics(gc) {
            @Override public void drawText(String text,int x,int y){result.add(text);super.drawText(text,x,y);}
        };
        try{PreviewNodeFigure f=figure(model,null);f.validate();f.paint(graphics);return result;}
        finally{graphics.dispose();gc.dispose();image.dispose();}
    }
    static PreviewNode find(PreviewNode n,String name){if(name.equals(n.getDescriptor().getName()))return n;for(PreviewNode c:n.getChildren()){PreviewNode r=find(c,name);if(r!=null)return r;}return null;}
    static EJPluginBlockProperties loadBlock(Path path,Element blockXml) {
        EJPluginFormProperties form=new EJPluginFormProperties(path.getFileName().toString(),null);
        return loadBlock(path,blockXml,form);
    }
    static EJPluginBlockProperties loadBlock(Path path,Element blockXml,EJPluginFormProperties form) {
        String blockName=blockXml.getAttribute("name");
        final String blockRenderer=text(blockXml,"blockRendererName","");
        EJPluginBlockProperties block=new EJPluginBlockProperties(form,blockName,true) {
            @Override public String getBlockRendererName(){return blockRenderer;}
            @Override public EJDevBlockRendererDefinition getBlockRendererDefinition(){return null;}
        };
        form.getBlockContainer().addBlockProperties(block);
        block.setCanvasName(text(blockXml,"canvasName",""));
        block.setBlockRendererProperties(properties(child(blockXml,"blockRendererProperties")));
        Element main=child(blockXml,"mainScreenProperties");
        block.getMainScreenProperties().setNumCols(integer(main,"numCols",1));
        block.getMainScreenProperties().setWidth(integer(main,"width",0));
        block.getMainScreenProperties().setHeight(integer(main,"height",0));
        block.getMainScreenProperties().setDisplayFrame(bool(main,"displayFrame",false));
        block.getMainScreenProperties().setFrameTitle(text(main,"frameTitle",""));
        block.getMainScreenProperties().setHorizontalSpan(integer(main,"horizontalSpan",1));
        block.getMainScreenProperties().setVerticalSpan(integer(main,"verticalSpan",1));
        block.getMainScreenProperties().setExpandHorizontally(bool(main,"expandHorizontally",false));
        block.getMainScreenProperties().setExpandVertically(bool(main,"expandVertically",false));
        for(Element itemXml:children(child(blockXml,"itemList"),"item")) {
            final String renderer=text(itemXml,"itemRendererName","");
            final EJCoreFrameworkExtensionProperties props=properties(child(itemXml,"itemRendererProperties"));
            EJPluginBlockItemProperties item=new EJPluginBlockItemProperties(block,itemXml.getAttribute("name"),true) {
                @Override public String getItemRendererName(){return renderer;}
                @Override public EJCoreFrameworkExtensionProperties getItemRendererProperties(){return props;}
                @Override public EJDevItemRendererDefinition getItemRendererDefinition(){return null;}
            };
            block.getItemContainer().addItemProperties(item);
        }
        addGroups(child(child(blockXml,"mainScreen"),"itemGroupList"),block.getMainScreenItemGroupDisplayContainer());
        return block;
    }
    static void render(PreviewNode model,String name) throws Exception {
        if(display==null)display=new Display();Files.createDirectories(screenshots);
        Image image=new Image(display,model.getBounds().getWidth(),model.getBounds().getHeight());
        GC gc=new GC(image);SWTGraphics graphics=new SWTGraphics(gc);
        try {
            gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));gc.fillRectangle(image.getBounds());
            PreviewNodeFigure figure=figure(model,null);figure.validate();figure.paint(graphics);
            ImageLoader loader=new ImageLoader();loader.data=new ImageData[]{image.getImageData()};
            loader.save(screenshots.resolve(name).toString(),SWT.IMAGE_PNG);
        } finally {graphics.dispose();gc.dispose();image.dispose();}
    }
    static PreviewNodeFigure figure(PreviewNode model,PreviewNode parent) {
        PreviewBounds b=model.getBounds();PreviewNodeFigure f=new PreviewNodeFigure(model);if(model.getPreviewTheme()==null)f.setFont(display.getSystemFont());
        int x=b.getX()-(parent==null?0:parent.getBounds().getX()),y=b.getY()-(parent==null?0:parent.getBounds().getY());
        f.setBounds(new Rectangle(x,y,b.getWidth(),b.getHeight()));
        for(PreviewNode child:model.getVisibleChildren()) {
            PreviewNodeFigure childFigure=figure(child,model);f.add(childFigure,childFigure.getBounds().getCopy());
        }
        return f;
    }
    static void validateContainers(PreviewNode n,String name) {
        if(n.getKind()==EJDevPreviewKind.RADIO_GROUP) {
            EJPluginScreenItemProperties source=(EJPluginScreenItemProperties)n.getSource();
            String orientation=source.getBlockItemDisplayProperties().getItemRendererProperties().getStringProperty("ORIENTATION");
            System.out.println("RADIO "+source.getName()+" orientation="+orientation+" vertical="+n.isVerticalOrientation()+" size="+n.getBounds().getWidth()+"x"+n.getBounds().getHeight());
            check(n.isVerticalOrientation()!= "HORIZONTAL".equals(orientation),name+" radio orientation matches renderer properties");
        }
        for(PreviewNode c:n.getVisibleChildren()) {
            check(inside(c,n),name+" child fits "+n.getDisplayName()+" -> "+c.getDisplayName());
            validateContainers(c,name);
        }
    }
    static PreviewNode findRadio(PreviewNode n){if(n.getKind()==EJDevPreviewKind.RADIO_GROUP)return n;for(PreviewNode c:n.getChildren()){PreviewNode r=findRadio(c);if(r!=null)return r;}return null;}
    static boolean contains(PreviewNode n,String name){if(name.equals(n.getDescriptor().getName()))return true;for(PreviewNode c:n.getChildren())if(contains(c,name))return true;return false;}
    static void addGroups(Element list,EJPluginItemGroupContainer container) {
        for(Element e:children(list,"itemGroup")) {
            EJPluginItemGroupProperties g=new EJPluginItemGroupProperties(e.getAttribute("name"),container);
            g.setNumCols(integer(e,"numCols",1));g.setWidth(integer(e,"width",0));g.setHeight(integer(e,"height",0));
            g.setXspan(integer(e,"xspan",1));g.setYspan(integer(e,"yspan",1));
            g.setExpandHorizontally(bool(e,"expandHorizontally",false));g.setExpandVertically(bool(e,"expandVertically",false));
            g.setDisplayGroupFrame(bool(e,"displayFrame",false));g.setFrameTitle(text(e,"frameTitle",""));
            g.setHorizontalAlignment(EJItemGroupAlignment.valueOf(text(e,"horizontalAlignment","FILL")));
            g.setVerticalAlignment(EJItemGroupAlignment.valueOf(text(e,"verticalAlignment","FILL")));
            g.setRendererProperties(properties(child(e,"rendererProperties")));
            g.setSeparator(boolAttribute(e,"isSeparator"));
            if(e.hasAttribute("separatorOrientation"))g.setSeparatorOrientation(EJSeparatorOrientation.valueOf(e.getAttribute("separatorOrientation")));
            container.addItemGroupProperties(g);
            for(Element si:children(child(e,"itemGroupItemList"),"item")) {
                EJPluginMainScreenItemProperties item=new EJPluginMainScreenItemProperties(g,false,boolAttribute(si,"isSpacerItem")) {
                    @Override public void refreshBlockRendererRequiredProperties(boolean defaults){}
                    @Override public void refreshLovRendererRequiredProperties(){}
                };
                item.setReferencedItemName(si.getAttribute("referencedItemName"));item.setLabel(text(si,"label",""));
                item.setVisible(bool(si,"visible",true));item.setBlockRendererRequiredProperties(properties(child(si,"blockRendererRequiredProperties")));
                item.setSeparator(boolAttribute(si,"isSeparator"));
                if(si.hasAttribute("separatorOrientation"))item.setSeparatorOrientation(EJSeparatorOrientation.valueOf(si.getAttribute("separatorOrientation")));
                g.addItemProperties(item);
            }
            addGroups(child(e,"itemGroupList"),g.getChildItemGroupContainer());
        }
    }
    static EJCoreFrameworkExtensionProperties properties(Element e) {
        EJCoreFrameworkExtensionProperties p=new EJCoreFrameworkExtensionProperties(null,null,e!=null&&e.hasAttribute("name")?e.getAttribute("name"):"test",null);
        for(Element v:children(e,"property")) {
            EJCoreFrameworkExtensionProperty value=new EJCoreFrameworkExtensionProperty(EJPropertyDefinitionType.STRING,v.getAttribute("name"));
            value.setValue(v.getTextContent().trim());p.addProperty(value);
        }
        for(Element l:children(e,"propertyList")) {
            EJCoreFrameworkExtensionPropertyList list=new EJCoreFrameworkExtensionPropertyList(l.getAttribute("name"));
            for(Element en:children(l,"listEntry")){EJCoreFrameworkExtensionPropertyListEntry entry=new EJCoreFrameworkExtensionPropertyListEntry();for(Element v:children(en,"property"))entry.addProperty(v.getAttribute("name"),v.getTextContent().trim());list.addListEntry(entry);}
            p.addPropertyList(list);
        }
        for(Element g:children(e,"propertyGroup"))p.addPropertyGroup(properties(g));
        return p;
    }
    static List<Element> children(Element e,String name){List<Element> result=new ArrayList<>();if(e!=null)for(Node n=e.getFirstChild();n!=null;n=n.getNextSibling())if(n instanceof Element && n.getNodeName().equals(name))result.add((Element)n);return result;}
    static Element child(Element e,String name){List<Element> list=children(e,name);return list.isEmpty()?null:list.get(0);}
    static String text(Element e,String name,String fallback){Element c=child(e,name);return c==null||c.getTextContent().isBlank()?fallback:c.getTextContent().trim();}
    static int integer(Element e,String name,int fallback){return Integer.parseInt(text(e,name,Integer.toString(fallback)));}
    static boolean bool(Element e,String name,boolean fallback){return Boolean.parseBoolean(text(e,name,Boolean.toString(fallback)));}
    static boolean boolAttribute(Element e,String name){return Boolean.parseBoolean(e.getAttribute(name));}
}
