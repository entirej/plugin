import java.nio.file.*;
import java.util.*;
import org.w3c.dom.*;
import org.entirej.framework.plugin.framework.properties.*;

/** Read-only local resource resolution for corpus fixtures, with cycle guards and explicit gaps. */
public class PreviewFixtureRepository {
    final Path blocks,groups;
    final List<String> gaps=new ArrayList<>(),resolved=new ArrayList<>();
    final Set<Path> loading=new HashSet<>();
    static final Map<Path,Document> documents=new HashMap<>();

    PreviewFixtureRepository(Path form) throws Exception {
        Path source=form.toAbsolutePath().getParent();
        while(source!=null && !Files.isRegularFile(source.resolve("application.ejprop")))source=source.getParent();
        if(source!=null) {
            Document app=parse(source.resolve("application.ejprop"));
            Element root=app.getDocumentElement();
            Element props=PreviewRegressionTest.child(root,"application");
            if(props==null)props=root;
            blocks=source.resolve(PreviewRegressionTest.text(props,"reusableBlocksLocation","ch/cresoft/temos/forms/referenced/blocks")).normalize();
            groups=source.resolve(PreviewRegressionTest.text(props,"objectGroupDefinitionLocation","ch/cresoft/temos/forms/referenced/objectgroups")).normalize();
        } else {
            Path parent=form.toAbsolutePath().getParent();blocks=parent.resolve("referenced/blocks");groups=parent.resolve("referenced/objectgroups");
        }
    }
    static Document parse(Path path) throws Exception {
        path=path.toAbsolutePath().normalize();
        Document document=documents.get(path);
        if(document==null){document=PreviewRegressionTest.parse(path);documents.put(path,document);}
        return document;
    }
    void loadInto(Path path,Document xml,EJPluginFormProperties form) throws Exception {
        path=path.toAbsolutePath().normalize();
        if(!loading.add(path))throw new IllegalArgumentException("Shared definition cycle: "+path);
        try {
            Element source=PreviewRegressionTest.child(xml.getDocumentElement(),"form");
            form.setFormTitle(PreviewRegressionTest.text(source,"formTitle",""));
            form.setFormWidth(PreviewRegressionTest.integer(source,"formWidth",0));form.setFormHeight(PreviewRegressionTest.integer(source,"formHeight",0));
            form.setNumCols(PreviewRegressionTest.integer(source,"numCols",1));
            Element list=PreviewRegressionTest.child(source,"blockList");
            if(list!=null) {
                NodeList definitions=list.getElementsByTagName("block");
                Map<String,Element> local=new HashMap<>();
                for(int i=0;i<definitions.getLength();i++){Element block=(Element)definitions.item(i);local.put(block.getAttribute("name"),block);}
                for(int i=0;i<definitions.getLength();i++) {
                    Element block=(Element)definitions.item(i);
                    Element effective=resolveBlock(block,local,new HashSet<>());
                    EJPluginBlockProperties props=PreviewRegressionTest.loadBlock(path,effective,form);
                    if(!block.getAttribute("objectgroup").isBlank())props.setReferencedObjectGroupName(block.getAttribute("objectgroup"));
                }
            }
            PreviewFormFixture.canvases(PreviewRegressionTest.child(source,"canvasList"),form.getCanvasContainer(),form);
            for(Element ref:PreviewRegressionTest.children(PreviewRegressionTest.child(source,"objGroupDefinitionList"),"objGroupDefinition")) {
                String name=ref.getAttribute("name");Path resource=resource(groups,name,".objgroup");
                if(resource==null || !Files.isRegularFile(resource)){gaps.add("missing object group "+name);continue;}
                if(loading.contains(resource)){gaps.add("object group cycle "+name);continue;}
                EJPluginObjectGroupProperties group=new EJPluginObjectGroupProperties(name,null);
                loadInto(resource,parse(resource),group);group.setInitialized(true);
                form.getObjectGroupContainer().addObjectGroupProperties(group);
                // Use production import logic, including host canvas overrides and block replacement.
                group.importObjectsToForm(form);resolved.add("object group "+name);
            }
            for(EJPluginBlockProperties block:form.getBlockContainer().getAllBlockProperties())
                if(block.getBlockRendererName()==null || block.getBlockRendererName().isBlank())gaps.add("unresolved block "+block.getName());
        } finally {loading.remove(path);}
    }
    Element resolveBlock(Element source,Map<String,Element> local,Set<String> stack) throws Exception {
        String name=source.getAttribute("name");
        String key=source.getOwnerDocument().getDocumentURI()+"#"+name;
        if(!stack.add(key)){gaps.add("mirror/reference cycle "+name);return source;}
        try {
            String reusable=source.getAttribute("referencedBlockName");
            String mirror=PreviewRegressionTest.text(source,"mirrorParent","");
            Element base=null;
            if(PreviewRegressionTest.boolAttribute(source,"referenced") && source.getAttribute("objectgroup").isBlank()) {
                if(reusable.isBlank())reusable=name;
                Path file=resource(blocks,reusable,".block");
                if(file!=null && Files.isRegularFile(file)) {
                    Element form=PreviewRegressionTest.child(parse(file).getDocumentElement(),"form");
                    Element list=PreviewRegressionTest.child(form,"blockList");
                    if(list!=null && list.getElementsByTagName("block").getLength()>0) {
                        base=resolveBlock((Element)list.getElementsByTagName("block").item(0),Map.of(),stack);
                        resolved.add("reusable block "+reusable+" as "+name);
                    }
                } else gaps.add("missing reusable block "+reusable);
            } else if(!mirror.isBlank() && PreviewRegressionTest.child(source,"itemList")==null && local.containsKey(mirror))
                base=resolveBlock(local.get(mirror),local,stack);
            if(base==null)return source;
            Element result=(Element)base.cloneNode(true);merge(result,source);result.setAttribute("name",name);return result;
        } finally {stack.remove(key);}
    }
    static Path resource(Path folder,String name,String suffix) {
        if(name==null || name.isBlank() || name.contains("/") || name.contains("\\") || name.contains(":"))return null;
        Path path=folder.resolve(name+suffix).toAbsolutePath().normalize();
        return path.startsWith(folder.toAbsolutePath().normalize())?path:null;
    }
    static void merge(Element target,Element override) {
        NamedNodeMap attributes=override.getAttributes();
        for(int i=0;i<attributes.getLength();i++)target.setAttribute(attributes.item(i).getNodeName(),attributes.item(i).getNodeValue());
        for(Node n=override.getFirstChild();n!=null;n=n.getNextSibling())if(n instanceof Element) {
            Element value=(Element)n;Element existing=null;
            // References serialize an empty itemList: the items still belong to the reusable block.
            if(value.getTagName().equals("itemList") && value.getElementsByTagName("item").getLength()==0)continue;
            for(Element candidate:PreviewRegressionTest.children(target,value.getTagName()))
                if(!value.hasAttribute("name") || value.getAttribute("name").equals(candidate.getAttribute("name"))){existing=candidate;break;}
            if(existing!=null && (value.getTagName().equals("mainScreenProperties") || value.getTagName().endsWith("RendererProperties") || value.getTagName().equals("propertyGroup")))merge(existing,value);
            else {
                Node imported=target.getOwnerDocument().importNode(value,true);
                if(existing==null)target.appendChild(imported);else target.replaceChild(imported,existing);
            }
        }
    }
}
