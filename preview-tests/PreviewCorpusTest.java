import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import org.w3c.dom.*;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.dev.renderer.definition.EJDevPreviewKind;
import org.entirej.framework.core.enumerations.EJCanvasType;
import org.entirej.framework.plugin.framework.properties.*;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginScreenItemProperties;
import org.entirej.ide.ui.editors.form.preview.FormPreviewModelBuilder;
import org.entirej.ide.ui.editors.preview.*;

/** Read-only audit of every locally defined main-screen block in a customer forms directory. */
public class PreviewCorpusTest {
    static int forms, blocks, controls, explicitClips, mainForms, popupScreens, pageStates, singleRecords, singleExpected, renderTiles, mainClips, gaps, resolvedReferences;
    static final List<String> coverage=new ArrayList<>();
    static final Map<String,Integer> renderers=new TreeMap<>();
    static final Set<String> screenshots=new HashSet<>();

    static void run(Path directory) throws Exception {
        PreviewFormFixture.verify();
        List<Path> paths;
        try(Stream<Path> files=Files.list(directory)) {
            paths=files.filter(p->p.toString().endsWith(".form")).sorted().toList();
        }
        for(Path path:paths) {
            Document xml=PreviewRegressionTest.parse(path);forms++;
            try {
                PreviewFormFixture.Loaded loaded=PreviewFormFixture.resolved(path,xml);
                EJPluginFormProperties form=loaded.form();
                PreviewNode root=PreviewRegressionTest.builder().build(form,form,900,700,false);
                renderMain(root,path.getFileName()+"/main");mainForms++;
                visitPages(root,root,path.getFileName()+"/pages");
                findGaps(root,path.getFileName().toString());
                for(org.entirej.framework.core.properties.interfaces.EJCanvasProperties canvas:
                        org.entirej.framework.plugin.utils.EJPluginCanvasRetriever.retriveAllCanvases(form)) {
                    if(canvas.getType()!=EJCanvasType.POPUP)continue;
                    PreviewNode popup=PreviewRegressionTest.builder().build(form,canvas,900,700,false);
                    String context=path.getFileName()+"/popup/"+canvas.getName();
                    renderMain(popup,context);visitPages(popup,popup,context);findGaps(popup,context);popupScreens++;
                }
                for(String unresolved:loaded.repository().gaps)gap(path.getFileName()+" "+unresolved);
                for(String resolved:loaded.repository().resolved){resolvedReferences++;coverage.add("RESOLVED "+path.getFileName()+" "+resolved);}
                Set<String> localSingles=new HashSet<>();NodeList definitions=xml.getElementsByTagName("block");
                for(int j=0;j<definitions.getLength();j++) {
                    Element block=(Element)definitions.item(j);
                    if(PreviewRegressionTest.text(block,"blockRendererName","").equalsIgnoreCase("SingleRecord"))localSingles.add(block.getAttribute("name"));
                }
                for(EJPluginBlockProperties block:form.getBlockContainer().getAllBlockProperties()) {
                    if(!"SingleRecord".equalsIgnoreCase(block.getBlockRendererName()) || localSingles.contains(block.getName()))continue;
                    singleExpected++;
                    PreviewNode single=PreviewRegressionTest.builder().build(form,block,900,700,false);
                    audit(single,path.getFileName()+"/shared/"+block.getName());paintAll(single,"shared-single-"+path.getFileName()+"-"+block.getName());singleRecords++;
                    coverage.add("PASS shared-single "+path.getFileName()+"/"+block.getName());
                }
            } catch(Exception e) {
                e.printStackTrace(System.out);PreviewRegressionTest.check(false,path.getFileName()+" full main preview exception: "+e);
                coverage.add("FAIL main "+path.getFileName()+" "+e);
            }
            NodeList nodes=xml.getElementsByTagName("block");
            for(int i=0;i<nodes.getLength();i++) {
                Element blockXml=(Element)nodes.item(i);
                Element main=PreviewRegressionTest.child(blockXml,"mainScreen");
                boolean single=PreviewRegressionTest.text(blockXml,"blockRendererName","").replace("_","").replace(" ","").equalsIgnoreCase("SingleRecord");
                if(single)singleExpected++;
                if(!single && (main==null || main.getElementsByTagName("itemGroup").getLength()==0))continue;
                String context=path.getFileName()+"/"+blockXml.getAttribute("name");
                try {
                    EJPluginBlockProperties block=PreviewRegressionTest.loadBlock(path,blockXml);
                    PreviewNode root=PreviewRegressionTest.builder().build(block.getFormProperties(),block,900,700);
                    blocks++;
                    audit(root,context);
                    if(single) {
                        paintAll(root,"single-"+i+"-"+context);singleRecords++;
                        coverage.add("PASS single "+context+(main==null || main.getElementsByTagName("itemGroup").getLength()==0?" [empty main screen]":""));
                    }
                    // Render a representative sample of real single-record layouts, once per renderer.
                    Set<String> inBlock=new TreeSet<>();collectRenderers(root,inBlock);
                    boolean fresh=screenshots.addAll(inBlock);
                    boolean sample=context.equals("TA01.1.form/WelcomeMessage") || context.equals("TC01.form/ExtraInformation")
                        || context.equals("TC01.form/TC01CEditQuestionDetailValueRequest");
                    if(!single && PreviewRegressionTest.screenshots!=null && (fresh || sample) && root.getBounds().getWidth()<5000 && root.getBounds().getHeight()<5000)
                        PreviewRegressionTest.render(root,"corpus-"+context.replaceAll("[^A-Za-z0-9._-]","_")+".png");
                    PreviewRegressionTest.check(true,context+" builds and lays out");
                } catch(Exception e) {
                    e.printStackTrace(System.out);
                    PreviewRegressionTest.check(false,context+" preview exception: "+e);
                }
            }
        }
        PreviewRegressionTest.check(mainForms==forms,"every form main screen was built and painted");
        PreviewRegressionTest.check(singleRecords==singleExpected,"every single-record block was built and painted individually");
        System.out.println("CORPUS forms="+forms+" mainScreens="+blocks+" controls="+controls+" explicitSizeClips="+explicitClips);
        System.out.println("CORPUS renderer coverage="+renderers);
        System.out.println("LOCAL COVERAGE mainForms="+mainForms+"/"+forms+" popups="+popupScreens+" pageStates="+pageStates+" individualSingleRecords="+singleRecords+"/"+singleExpected
            +" paintTiles="+renderTiles+" mainContainmentWarnings="+mainClips+" unresolvedReferences="+gaps+" resolvedReferences="+resolvedReferences);
        if(PreviewRegressionTest.screenshots!=null) {
            coverage.add(0,"Local and resolved shared definitions: mainForms="+mainForms+"/"+forms+", popups="+popupScreens+", pageStates="+pageStates+", individualSingleRecords="+singleRecords+"/"+singleExpected
                +", paintedTiles="+renderTiles+", mainContainmentWarnings="+mainClips+", referenceGaps="+gaps);
            Files.write(PreviewRegressionTest.screenshots.resolve("coverage.txt"),coverage);
        }
    }

    static void renderMain(PreviewNode root,String context) throws Exception {
        auditMain(root,context);paintAll(root,"main-"+context);
        PreviewRegressionTest.check(!root.isDesignHintsVisible(),context+" main form builds/paints without design headings");
        coverage.add("PASS main "+context);
    }
    static void visitPages(PreviewNode root,PreviewNode node,String path) throws Exception {
        if((node.getKind()==EJDevPreviewKind.STACKED || node.getKind()==EJDevPreviewKind.TAB_FOLDER) && !node.getChildren().isEmpty()) {
            int original=node.getSelectedTabIndex();
            for(int i=0;i<node.getChildren().size();i++) {
                node.setSelectedTabIndex(i);new PreviewGridLayoutSolver().layout(root);
                String state=path+"/"+node.getDescriptor().getName()+"/"+i;
                PreviewRegressionTest.check(node.getSelectedTabChild()==node.getChildren().get(i),state+" page switches correctly");
                renderMain(root,state);pageStates++;visitPages(root,node.getChildren().get(i),state);
            }
            node.setSelectedTabIndex(original);new PreviewGridLayoutSolver().layout(root);
        } else for(PreviewNode child:node.getVisibleChildren())visitPages(root,child,path);
    }
    static void auditMain(PreviewNode node,String context) {
        for(PreviewNode child:node.getVisibleChildren()) {
            if(!PreviewRegressionTest.inside(child,node)) {
                mainClips++;String warning="MAIN-CONTAINMENT "+context+" "+node.getDisplayName()+" -> "+child.getDisplayName()+constraintContext(node);
                System.out.println(warning);coverage.add(warning);
            }
            auditMain(child,context);
        }
    }
    static void gap(String context) {gaps++;coverage.add("REFERENCE-GAP "+context);System.out.println("REFERENCE-GAP "+context);}
    static void findGaps(PreviewNode node,String context) {
        if(node.getSource() instanceof EJPluginCanvasProperties) {
            EJPluginCanvasProperties canvas=(EJPluginCanvasProperties)node.getSource();
            if(canvas.getType()==EJCanvasType.FORM)gap(context+" referred form "+canvas.getReferredFormId()+" is schematic");
            if(canvas.getType()==EJCanvasType.BLOCK && canvas.getPluginBlockProperties()==null)gap(context+" unbound block canvas "+canvas.getName());
        }
        for(PreviewNode child:node.getChildren())findGaps(child,context);
    }
    /** Paint every scroll region in bounded tiles, avoiding one giant native image per form. */
    static void paintAll(PreviewNode model,String name) throws Exception {
        int width=model.getBounds().getWidth(),height=model.getBounds().getHeight();
        if((long)width*height>100_000_000L)throw new IllegalArgumentException("Preview exceeds 100 million pixels: "+width+"x"+height);
        PreviewNodeFigure figure=PreviewRegressionTest.figure(model,null);figure.validate();
        for(int y=0;y<height;y+=1000)for(int x=0;x<width;x+=1600) {
            org.eclipse.swt.graphics.Image image=new org.eclipse.swt.graphics.Image(PreviewRegressionTest.display,Math.min(1600,width-x),Math.min(1000,height-y));
            org.eclipse.swt.graphics.GC gc=new org.eclipse.swt.graphics.GC(image);org.eclipse.draw2d.SWTGraphics graphics=new org.eclipse.draw2d.SWTGraphics(gc);
            try {
                gc.setBackground(PreviewRegressionTest.display.getSystemColor(org.eclipse.swt.SWT.COLOR_WHITE));gc.fillRectangle(image.getBounds());
                graphics.translate(-x,-y);figure.paint(graphics);renderTiles++;
                if(PreviewRegressionTest.screenshots!=null) {
                    Files.createDirectories(PreviewRegressionTest.screenshots);
                    org.eclipse.swt.graphics.ImageLoader loader=new org.eclipse.swt.graphics.ImageLoader();loader.data=new org.eclipse.swt.graphics.ImageData[]{image.getImageData()};
                    loader.save(PreviewRegressionTest.screenshots.resolve(name.replaceAll("[^A-Za-z0-9._-]","_")+"-"+x+"-"+y+".png").toString(),org.eclipse.swt.SWT.IMAGE_PNG);
                }
            } finally {graphics.dispose();gc.dispose();image.dispose();}
        }
    }

    static void collectRenderers(PreviewNode n,Set<String> result) {
        if(isControl(n) && n.getDescriptor().getRendererName()!=null)result.add(n.getDescriptor().getRendererName());
        for(PreviewNode c:n.getVisibleChildren())collectRenderers(c,result);
    }
    static boolean isControl(PreviewNode n) {
        return n.getSource() instanceof EJPluginScreenItemProperties && !((EJPluginScreenItemProperties)n.getSource()).isSpacerItem()
            && !((EJPluginScreenItemProperties)n.getSource()).isSeparator() && !n.getDescriptor().getName().endsWith(".label");
    }
    static void audit(PreviewNode n,String context) {
        if(isControl(n)) {
            controls++;
            EJPluginScreenItemProperties item=(EJPluginScreenItemProperties)n.getSource();
            String renderer=n.getDescriptor().getRendererName();
            if(renderer!=null)renderers.merge(renderer,1,Integer::sum);
            PreviewRegressionTest.check(item.isVisible(),context+" hidden item not emitted: "+item.getName());
            PreviewRegressionTest.check(n.getKind()!=EJDevPreviewKind.UNKNOWN,context+" recognized renderer: "+renderer);
            EJFrameworkExtensionProperties props=((EJPluginMainScreenItemProperties)item).getBlockRendererRequiredProperties();
            boolean autoWidth=props==null || props.getIntProperty("DISPLAYED_WIDTH",0)<=0;
            if(autoWidth && (n.getKind()==EJDevPreviewKind.CHECKBOX || n.getKind()==EJDevPreviewKind.LABEL)) {
                int minimum=org.eclipse.draw2d.FigureUtilities.getTextWidth(item.getLabel(),n.getPreviewTheme().font(n.getThemeSelector(),n.getThemeVariant()))
                    +(n.getKind()==EJDevPreviewKind.CHECKBOX?23:0);
                PreviewRegressionTest.check(n.getConstraint().getPreferredWidth()>=minimum,
                    context+" natural caption fits: "+item.getName()+" ["+n.getDisplayName()+"]");
            }
            if(n.getKind()==EJDevPreviewKind.LABEL && item.getBlockItemDisplayProperties()!=null) {
                EJFrameworkExtensionProperties rendererProps=item.getBlockItemDisplayProperties().getItemRendererProperties();
                String alignment=rendererProps==null?null:rendererProps.getStringProperty("ALIGNMENT");
                int expected="RIGHT".equals(alignment)?PreviewNode.TEXT_ALIGN_RIGHT:"CENTER".equals(alignment)?PreviewNode.TEXT_ALIGN_CENTER:PreviewNode.TEXT_ALIGN_LEFT;
                PreviewRegressionTest.check(n.getTextAlignment()==expected,context+" label alignment: "+item.getName());
            }
        }
        for(PreviewNode child:n.getVisibleChildren()) {
            if(!PreviewRegressionTest.inside(child,n)) {
                // Explicit form/group sizes are user configuration, not a reason to rewrite forms.
                if(!constraintContext(n).isEmpty()) {
                    explicitClips++;
                    System.out.println("CONFIGURED-CLIP "+context+" "+n.getDisplayName()+" -> "+child.getDisplayName()+constraintContext(n));
                }
                else PreviewRegressionTest.check(false,context+" auto container clips "+n.getDisplayName()+" -> "+child.getDisplayName());
            }
            audit(child,context);
        }
    }
    static String constraintContext(PreviewNode node) {
        for(PreviewNode current=node;current!=null;current=current.getParent()) {
            int width=Math.max(current.getDescriptor().getPreferredWidth(),current.getConstraint().getPreferredWidth());
            int height=Math.max(current.getDescriptor().getPreferredHeight(),current.getConstraint().getPreferredHeight());
            if(width>0 || height>0)return " [configured ancestor="+current.getDescriptor().getName()+" hints="+width+"x"+height+"]";
        }
        return "";
    }
}
