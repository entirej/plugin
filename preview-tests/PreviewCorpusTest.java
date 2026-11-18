import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;
import org.w3c.dom.*;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.dev.renderer.definition.EJDevPreviewKind;
import org.entirej.framework.plugin.framework.properties.*;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginScreenItemProperties;
import org.entirej.ide.ui.editors.form.preview.FormPreviewModelBuilder;
import org.entirej.ide.ui.editors.preview.*;

/** Read-only audit of every locally defined main-screen block in a customer forms directory. */
public class PreviewCorpusTest {
    static int forms, blocks, controls, explicitClips;
    static final Map<String,Integer> renderers=new TreeMap<>();
    static final Set<String> screenshots=new HashSet<>();

    static void run(Path directory) throws Exception {
        List<Path> paths;
        try(Stream<Path> files=Files.list(directory)) {
            paths=files.filter(p->p.toString().endsWith(".form")).sorted().toList();
        }
        for(Path path:paths) {
            Document xml=PreviewRegressionTest.parse(path);forms++;
            NodeList nodes=xml.getElementsByTagName("block");
            for(int i=0;i<nodes.getLength();i++) {
                Element blockXml=(Element)nodes.item(i);
                Element main=PreviewRegressionTest.child(blockXml,"mainScreen");
                if(main==null || main.getElementsByTagName("itemGroup").getLength()==0)continue;
                String context=path.getFileName()+"/"+blockXml.getAttribute("name");
                try {
                    EJPluginBlockProperties block=PreviewRegressionTest.loadBlock(path,blockXml);
                    PreviewNode root=new FormPreviewModelBuilder().build(block.getFormProperties(),block,900,700);
                    blocks++;
                    audit(root,context);
                    // Render a representative sample of real single-record layouts, once per renderer.
                    Set<String> inBlock=new TreeSet<>();collectRenderers(root,inBlock);
                    boolean fresh=screenshots.addAll(inBlock);
                    boolean sample=context.equals("TA01.1.form/WelcomeMessage") || context.equals("TC01.form/ExtraInformation")
                        || context.equals("TC01.form/TC01CEditQuestionDetailValueRequest");
                    if(PreviewRegressionTest.screenshots!=null && (fresh || sample) && root.getBounds().getWidth()<5000 && root.getBounds().getHeight()<5000)
                        PreviewRegressionTest.render(root,"corpus-"+context.replaceAll("[^A-Za-z0-9._-]","_")+".png");
                    PreviewRegressionTest.check(true,context+" builds and lays out");
                } catch(Exception e) {
                    e.printStackTrace(System.out);
                    PreviewRegressionTest.check(false,context+" preview exception: "+e);
                }
            }
        }
        System.out.println("CORPUS forms="+forms+" mainScreens="+blocks+" controls="+controls+" explicitSizeClips="+explicitClips);
        System.out.println("CORPUS renderer coverage="+renderers);
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
                int minimum=item.getLabel().length()*7+(n.getKind()==EJDevPreviewKind.CHECKBOX?24:0);
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
                if(n.getDescriptor().getPreferredWidth()>0 || n.getDescriptor().getPreferredHeight()>0 ||
                   n.getConstraint().getPreferredWidth()>0 || n.getConstraint().getPreferredHeight()>0) {
                    explicitClips++;
                    System.out.println("CONFIGURED-CLIP "+context+" "+n.getDisplayName()+" -> "+child.getDisplayName());
                }
                else PreviewRegressionTest.check(false,context+" auto container clips "+n.getDisplayName()+" -> "+child.getDisplayName());
            }
            audit(child,context);
        }
    }
}
