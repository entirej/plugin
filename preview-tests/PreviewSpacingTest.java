import java.io.StringReader;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.entirej.framework.dev.renderer.definition.EJDevPreviewKind;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.ide.ui.editors.form.preview.*;
import org.entirej.ide.ui.editors.preview.*;

/** Geometry checks independent of customer XML, including a native SWT GridLayout comparison. */
public class PreviewSpacingTest {
    static void check(boolean value,String name){PreviewRegressionTest.check(value,name);}
    static PreviewNode node(EJDevPreviewKind kind,int width,int height){return PreviewRegressionTest.node(kind,width,height);}
    static void run() throws Exception {
        PreviewNode grid=node(EJDevPreviewKind.FORM_CONTAINER,0,0);grid.setBounds(0,0,1,1);grid.setColumns(2);
        grid.setLayoutInsets(2,4,6,8);grid.setHorizontalGap(5);grid.setVerticalGap(1);
        for(int i=0;i<4;i++)grid.addChild(node(EJDevPreviewKind.TEXT_FIELD,30,20));
        new PreviewGridLayoutSolver().layout(grid);
        check(grid.getBounds().getWidth()==77 && grid.getBounds().getHeight()==49,"auto measurement includes four-sided insets and independent gaps");
        check(grid.getChildren().get(1).getBounds().getX()==43 && grid.getChildren().get(2).getBounds().getY()==23,"placement uses horizontal 5 and vertical 1 without mixing axes");
        PreviewNode span=node(EJDevPreviewKind.FORM_CONTAINER,0,0);span.setBounds(0,0,1,1);span.setLayoutMargin(0);span.setVerticalGap(1);
        PreviewNode tall=node(EJDevPreviewKind.TEXT_AREA,30,41);tall.getConstraint().setVerticalSpan(2);span.addChild(tall);
        new PreviewGridLayoutSolver().layout(span);
        check(span.getBounds().getHeight()==41,"row spans count the internal vertical gap exactly once");
        Shell shell=new Shell(PreviewRegressionTest.display);
        try {
            Composite nativeGrid=new Composite(shell,SWT.NONE);GridLayout layout=new GridLayout(2,false);
            layout.marginHeight=layout.marginWidth=0;layout.horizontalSpacing=5;layout.verticalSpacing=1;nativeGrid.setLayout(layout);
            PreviewNode preview=node(EJDevPreviewKind.FORM_CONTAINER,0,0);preview.setBounds(0,0,1,1);preview.setColumns(2);
            preview.setLayoutMargin(0);preview.setHorizontalGap(5);preview.setVerticalGap(1);
            for(int i=0;i<4;i++){Label label=new Label(nativeGrid,SWT.NONE);GridData data=new GridData();data.widthHint=30;data.heightHint=20;label.setLayoutData(data);preview.addChild(node(EJDevPreviewKind.TEXT_FIELD,30,20));}
            nativeGrid.pack();nativeGrid.layout();new PreviewGridLayoutSolver().layout(preview);
            for(int i=0;i<4;i++){
                org.eclipse.swt.graphics.Rectangle actual=nativeGrid.getChildren()[i].getBounds();PreviewBounds expected=preview.getChildren().get(i).getBounds();
                check(actual.x==expected.getX()&&actual.y==expected.getY()&&actual.width==expected.getWidth()&&actual.height==expected.getHeight(),"RAP item-grid geometry matches native SWT grid cell "+i);
            }
        } finally{shell.dispose();}
        constrainedGrids();
        gridMatrix();
        PreviewTheme theme=PreviewTheme.fromCss("""
            * { font: 12px Verdana; padding: 0; }
            Group-Frame { padding: 0; margin: 0; border: 0px solid gray; }
            Group-Label { padding: 0; margin: 0 0 0 10px; border: none; }
            Button.compact { padding: 1px 2px; }
            CTabItem { padding: 4px 8px; }
            CTabItem:selected { padding: 2px 8px; }
            Composite[BORDER].itemgroupclear { border: 1px solid gray; }
            """);
        check(Arrays.equals(theme.box("Text[BORDER]",null,"padding"),new int[]{2,2,2,2}),"universal TEMOS reset does not override a more specific RAP widget rule");
        check(Arrays.equals(theme.box("Button[PUSH]","compact","padding"),new int[]{1,2,1,2}),"custom variant padding overrides widget defaults");
        check(Arrays.equals(PreviewTheme.fromCss("Text {padding: calc(2px + 2px);}").box("Text",null,"padding"),new int[]{2,2,2,2}),"unsupported calculated CSS lengths retain the last supported metric");
        check(theme.border("Group-Frame",null)==0 && theme.border("Composite[BORDER]","itemgroupclear")==1,"theme removes group chrome without removing an explicitly styled variant border");
        check(theme.tabHeight()>=theme.textHeight("CTabItem",null)+8,"tab strip reserves the tallest normal/selected padding");
        check("ch/example/theme.css".equals(RapPreviewThemeResolver.literalReturn("protected String getThemeCSSLocation(){ return \"ch/example/theme.css\"; // note\n}")),"literal theme path is read without launching application code");
        check(RapPreviewThemeResolver.literalReturn("String getThemeCSSLocation(){ return selectedTheme(); }")==null,"computed launcher themes safely fall back instead of executing code");
        resolverIntegration();
        applicationLayout();
        String xml="""
            <block name='spacing'><blockRendererName>SingleRecord</blockRendererName><mainScreenProperties><numCols>1</numCols></mainScreenProperties>
            <itemList><item name='caption'><itemRendererName>LabelItem</itemRendererName></item></itemList>
            <mainScreen><itemGroupList>
              <itemGroup name='plain'><numCols>2</numCols><itemGroupItemList><item referencedItemName='caption'><label>A caption</label></item></itemGroupItemList></itemGroup>
              <itemGroup name='framed'><displayFrame>true</displayFrame><frameTitle>Title</frameTitle><itemGroupItemList><item referencedItemName='caption'><label>A caption</label></item></itemGroupItemList></itemGroup>
            </itemGroupList></mainScreen></block>
            """;
        EJPluginBlockProperties block=PreviewRegressionTest.loadBlock(Paths.get("spacing.form"),DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new StringReader(xml))).getDocumentElement());
        PreviewNode root=new FormPreviewModelBuilder(new FormPreviewDescriptorResolver(),theme).build(block.getFormProperties(),block,900,700);
        PreviewNode plain=PreviewRegressionTest.find(root,"plain"),framed=PreviewRegressionTest.find(root,"framed");
        check(root.getLayoutMargin()==0 && root.getChildren().get(0).getLayoutMargin()==0,"selected block does not add two synthetic 5px margins");
        check(plain.getLayoutMargin()==0 && plain.getHorizontalGap()==5 && plain.getVerticalGap()==1,"real model builder assigns RAP plain-group margins and gaps");
        check(!framed.isPaintBorder() && framed.isPaintContainerTitle() && framed.getInsetTop()>=theme.textHeight("Group",null),"TEMOS framed group keeps its title while removing CSS frame padding/border");
        check(root.getPreviewTheme()==theme && plain.getPreviewTheme()==theme,"theme is inherited per model tree rather than stored globally");
        PreviewNode tabs=node(EJDevPreviewKind.TAB_FOLDER,500,180);tabs.setPreviewTheme(theme);tabs.setPaintContainerTitle(false);
        tabs.setTabLabels(Arrays.asList("First page","A considerably longer second page"));tabs.addChild(node(EJDevPreviewKind.TAB_PAGE,200,60));tabs.addChild(node(EJDevPreviewKind.TAB_PAGE,200,60));
        tabs.setBounds(0,0,500,180);theme.applyPageInsets(tabs);new PreviewGridLayoutSolver().layout(tabs);
        PreviewNodeFigure figure=PreviewRegressionTest.figure(tabs,null);figure.validate();
        check(figure.getTabIndexAt(10,theme.tabHeight()/2)==0,"themed tab hit testing matches the painted strip");
        tabs.setTabsAtBottom(true);theme.applyPageInsets(tabs);new PreviewGridLayoutSolver().layout(tabs);
        figure=PreviewRegressionTest.figure(tabs,null);figure.validate();
        check(figure.getTabIndexAt(10,175)==0 && figure.getTabIndexAt(10,5)==-1,"bottom tabs use the same theme geometry for painting and selection");
        check(PreviewRegressionTest.paintedText(tabs).containsAll(Arrays.asList("First page","A considerably longer second page")),"themed tab painter keeps both full captions with the shared width metrics");
        check(PreviewRegressionTest.inside(tabs.getChildren().get(0),tabs),"selected page remains inside themed folder after orientation change");
        check(node(EJDevPreviewKind.GROUP,1,1).getPreviewTheme()==null,"unrelated/report preview models retain legacy styling by default");
        PreviewNode bordered=node(EJDevPreviewKind.GROUP,100,60);bordered.setBounds(0,0,100,60);bordered.setPreviewTheme(PreviewTheme.rapDefault());
        bordered.setPaintBorder(true);bordered.setLayoutInsets(1,1,1,1);bordered.addChild(node(EJDevPreviewKind.TEXT_FIELD,30,20));
        new PreviewGridLayoutSolver().layout(bordered);PreviewNodeFigure painted=PreviewRegressionTest.figure(bordered,null);painted.validate();
        check(painted.getChildren().get(0).getBounds().x==1 && painted.getChildren().get(0).getBounds().y==1,"CSS border is not counted again by Draw2D XYLayout");
    }

    static void constrainedGrids() {
        Shell shell=new Shell(PreviewRegressionTest.display);
        try {
            for(boolean horizontal:new boolean[]{true,false}) {
                Composite nativeGrid=new Composite(shell,SWT.NONE);GridLayout layout=new GridLayout(horizontal?2:1,false);
                layout.marginHeight=layout.marginWidth=0;layout.horizontalSpacing=5;layout.verticalSpacing=1;nativeGrid.setLayout(layout);
                nativeGrid.setSize(horizontal?150:100,horizontal?40:25);
                PreviewNode preview=node(EJDevPreviewKind.GROUP,horizontal?150:100,horizontal?40:25);
                preview.setBounds(0,0,horizontal?150:100,horizontal?40:25);preview.setColumns(horizontal?2:1);
                preview.setLayoutMargin(0);preview.setHorizontalGap(5);preview.setVerticalGap(1);
                for(int i=0;i<2;i++) {
                    Label label=new Label(nativeGrid,SWT.NONE);GridData data=new GridData(SWT.FILL,SWT.FILL,horizontal,!horizontal);
                    data.widthHint=100;data.heightHint=20;label.setLayoutData(data);
                    PreviewNode child=node(EJDevPreviewKind.TEXT_FIELD,100,20);
                    child.getConstraint().setGrabHorizontal(horizontal).setGrabVertical(!horizontal);preview.addChild(child);
                }
                nativeGrid.layout();new PreviewGridLayoutSolver().layout(preview);
                for(int i=0;i<2;i++) {
                    org.eclipse.swt.graphics.Rectangle actual=nativeGrid.getChildren()[i].getBounds();PreviewBounds expected=preview.getChildren().get(i).getBounds();
                    check(actual.x==expected.getX()&&actual.y==expected.getY()&&actual.width==expected.getWidth()&&actual.height==expected.getHeight(),
                        "constrained "+(horizontal?"horizontal":"vertical")+" grab grid matches SWT cell "+i+" actual="+actual+" preview="+expected.getWidth()+"x"+expected.getHeight());
                }
            }
            Composite sparse=new Composite(shell,SWT.NONE);GridLayout layout=new GridLayout(3,false);
            layout.marginWidth=layout.marginHeight=0;layout.horizontalSpacing=5;sparse.setLayout(layout);
            Label label=new Label(sparse,SWT.NONE);GridData data=new GridData();data.widthHint=60;data.heightHint=20;data.horizontalSpan=3;label.setLayoutData(data);
            PreviewNode preview=node(EJDevPreviewKind.FORM_CONTAINER,0,0);preview.setBounds(0,0,1,1);preview.setColumns(3);preview.setLayoutMargin(0);
            PreviewNode child=node(EJDevPreviewKind.TEXT_FIELD,60,20);child.getConstraint().setHorizontalSpan(3);preview.addChild(child);
            new PreviewGridLayoutSolver().layout(preview);
            check(preview.getBounds().getWidth()==sparse.computeSize(SWT.DEFAULT,SWT.DEFAULT).x,"spanning-only columns have the same natural width as SWT, without phantom 120px tracks");
        } finally {shell.dispose();}
    }

    // width, height, horizontal span, vertical span, grab X/Y, minimum X/Y.
    static void gridMatrix() {
        int[][][] cases={
            {{100,20,1,1,1,1,30,8},{80,30,1,1,1,1,10,5},{90,25,1,1,1,1,0,0}},
            {{100,20,1,1,0,0,0,0},{80,30,1,1,1,1,10,5}},
            {{100,50,1,2,1,1,0,0},{80,20,1,1,1,0,0,0},{70,60,1,1,1,1,0,10}},
            {{100,20,1,1,0,0,0,0},{50,20,1,1,1,0,0,0},{200,20,2,1,1,0,30,0}},
            {{100,20,1,1,0,0,0,0},{50,20,1,1,0,0,0,0},{200,20,2,1,0,0,0,0}}
        };
        Shell shell=new Shell(PreviewRegressionTest.display);
        try {
            for(int columns:new int[]{2,3})for(int c=0;c<cases.length;c++)for(int width:new int[]{0,80,180,360}) {
                Composite composite=new Composite(shell,SWT.NONE);GridLayout grid=new GridLayout(columns,false);
                grid.marginWidth=grid.marginHeight=0;grid.horizontalSpacing=5;grid.verticalSpacing=1;composite.setLayout(grid);composite.setSize(width,65);
                PreviewNode preview=node(EJDevPreviewKind.GROUP,width,65);preview.setBounds(0,0,width,65);preview.setColumns(columns);
                preview.setLayoutMargin(0);preview.setHorizontalGap(5);preview.setVerticalGap(1);
                for(int[] spec:cases[c]) {
                    Label label=new Label(composite,SWT.NONE);GridData data=new GridData(SWT.FILL,SWT.FILL,spec[4]!=0,spec[5]!=0,spec[2],spec[3]);
                    data.widthHint=spec[0];data.heightHint=spec[1];data.minimumWidth=spec[6];data.minimumHeight=spec[7];label.setLayoutData(data);
                    PreviewNode child=node(EJDevPreviewKind.TEXT_FIELD,spec[0],spec[1]);child.getConstraint().setHorizontalSpan(spec[2]).setVerticalSpan(spec[3])
                        .setGrabHorizontal(spec[4]!=0).setGrabVertical(spec[5]!=0).setMinimumWidth(spec[6]).setMinimumHeight(spec[7]);preview.addChild(child);
                }
                composite.layout();new PreviewGridLayoutSolver().layout(preview);
                for(int i=0;i<cases[c].length;i++) {
                    org.eclipse.swt.graphics.Rectangle actual=composite.getChildren()[i].getBounds();PreviewBounds expected=preview.getChildren().get(i).getBounds();
                    check(actual.x==expected.getX()&&actual.y==expected.getY()&&actual.width==expected.getWidth()&&actual.height==expected.getHeight(),
                        "SWT grid matrix "+c+" columns="+columns+" width="+width+" cell="+i+" native="+actual+" preview="+expected.getX()+","+expected.getY()+","+expected.getWidth()+","+expected.getHeight());
                }
                composite.dispose();
            }
        } finally {shell.dispose();}
    }

    // Exercise the real project resolver using in-memory Eclipse resource/JDT interfaces.
    // No workspace is created, modified or launched by the test.
    static void resolverIntegration() {
        org.eclipse.core.resources.IFile missing=proxy(org.eclipse.core.resources.IFile.class,(m,a)->m.equals("exists")?false:null);
        org.eclipse.core.resources.IFile css=proxy(org.eclipse.core.resources.IFile.class,(m,a)->m.equals("exists")?true:
            m.equals("getContents")?new java.io.ByteArrayInputStream("* {font: 12px Verdana;} Group-Frame {border: 0px solid gray;}".getBytes(java.nio.charset.StandardCharsets.UTF_8)):null);
        org.eclipse.core.resources.IContainer source=proxy(org.eclipse.core.resources.IContainer.class,(m,a)->m.equals("getFile")&&a[0].toString().equals("ch/example/theme.css")?css:missing);
        org.eclipse.core.resources.IProject project=proxy(org.eclipse.core.resources.IProject.class,(m,a)->m.equals("getFile")?missing:null);
        org.eclipse.jdt.core.IPackageFragmentRoot root=proxy(org.eclipse.jdt.core.IPackageFragmentRoot.class,(m,a)->m.equals("getKind")?org.eclipse.jdt.core.IPackageFragmentRoot.K_SOURCE:m.equals("getResource")?source:null);
        org.eclipse.jdt.core.IMethod method=proxy(org.eclipse.jdt.core.IMethod.class,(m,a)->m.equals("getElementName")?"getThemeCSSLocation":m.equals("getSource")?"String getThemeCSSLocation(){return \"ch/example/theme.css\";}":null);
        org.eclipse.jdt.core.IType type=proxy(org.eclipse.jdt.core.IType.class,(m,a)->m.equals("getMethods")?new org.eclipse.jdt.core.IMethod[]{method}:null);
        org.eclipse.jdt.core.ICompilationUnit unit=proxy(org.eclipse.jdt.core.ICompilationUnit.class,(m,a)->m.equals("getElementName")?"ApplicationLauncher.java":m.equals("getAllTypes")?new org.eclipse.jdt.core.IType[]{type}:null);
        org.eclipse.jdt.core.IPackageFragment fragment=proxy(org.eclipse.jdt.core.IPackageFragment.class,(m,a)->m.equals("getKind")?org.eclipse.jdt.core.IPackageFragmentRoot.K_SOURCE:m.equals("getCompilationUnits")?new org.eclipse.jdt.core.ICompilationUnit[]{unit}:null);
        org.eclipse.jdt.core.IJavaProject javaProject=proxy(org.eclipse.jdt.core.IJavaProject.class,(m,a)->m.equals("getProject")?project:m.equals("getPackageFragments")?new org.eclipse.jdt.core.IPackageFragment[]{fragment}:
            m.equals("getPackageFragmentRoots")?new org.eclipse.jdt.core.IPackageFragmentRoot[]{root}:null);
        PreviewTheme resolved=RapPreviewThemeResolver.forProject(javaProject);
        check("12px Verdana".equals(resolved.property("Label",null,"font")) && resolved.border("Group-Frame",null)==0,
            "project resolver loads the literal launcher theme from a source-classpath resource");
    }
    interface Answer {Object get(String method,Object[] arguments);}
    @SuppressWarnings("unchecked") static <T>T proxy(Class<T> type,Answer answer) {
        return (T)java.lang.reflect.Proxy.newProxyInstance(type.getClassLoader(),new Class<?>[]{type},(p,m,a)->answer.get(m.getName(),a));
    }

    static void applicationLayout() {
        org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties properties=new org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties(null) {
            @Override public org.entirej.framework.plugin.framework.properties.EJPluginReusableBlockProperties loadBlock(org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties p,org.eclipse.core.resources.IProject project,org.eclipse.core.resources.IFile file,String name){return null;}
            @Override public org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties loadLovDefinition(org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties p,org.eclipse.core.resources.IProject project,org.eclipse.core.resources.IFile file,String name){return null;}
            @Override public org.entirej.framework.plugin.framework.properties.EJPluginObjectGroupProperties loadObjectGroupDefinition(org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties p,org.eclipse.core.resources.IProject project,org.eclipse.core.resources.IFile file,String name){return null;}
        };
        org.entirej.framework.core.properties.EJCoreLayoutItem.LayoutGroup group=new org.entirej.framework.core.properties.EJCoreLayoutItem.LayoutGroup();
        group.setName("hideMargin");group.setHideMargin(true);group.addItem(new org.entirej.framework.core.properties.EJCoreLayoutItem.LayoutSpace());
        properties.getLayoutContainer().addItem(group);
        PreviewNode root=new AppLayoutPreviewModelBuilder().build(properties,900,700);
        PreviewNode child=root.getChildren().get(0);
        check(!root.isPaintBorder() && !child.isPaintBorder(),"application layout does not invent CSS borders for unframed containers");
        check(child.getLayoutMargin()==0 && child.getInsetLeft()==0 && child.getInsetTop()==0,"application hideMargin remains zero after theme application");
    }
}
