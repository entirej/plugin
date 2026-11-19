param(
    [string]$FormsDirectory,
    [string]$ScreenshotsDirectory,
    [string]$SourceDirectory,
    [string]$ThemeFile,
    [switch]$Packaged,
    [switch]$Corpus,
    [string]$EclipseHome = 'C:\tools\latest\eclipse',
    [string]$JavaHome = $env:JAVA_HOME
)
$ErrorActionPreference='Stop'
$taskRepo=Split-Path $PSScriptRoot -Parent
if(!$SourceDirectory){$SourceDirectory=$taskRepo}
$taskOut=Join-Path $taskRepo 'org.entirej.ide.ui.form\target\preview-regressions'
if($Packaged){$taskOut+='-packaged'}
New-Item -ItemType Directory -Path $taskOut -Force | Out-Null
$taskBundles=@('org.eclipse.swt','org.eclipse.swt.win32.win32.x86_64','org.eclipse.draw2d','org.eclipse.gef',
    'org.eclipse.jface','org.eclipse.core.runtime','org.eclipse.core.resources','org.eclipse.core.jobs',
    'org.eclipse.core.expressions','org.eclipse.core.commands','org.eclipse.core.filesystem','org.eclipse.core.contenttype',
    'org.eclipse.equinox.common','org.eclipse.equinox.registry','org.eclipse.equinox.preferences','org.eclipse.osgi',
    'org.eclipse.jdt.core','org.eclipse.ui','org.eclipse.ui.workbench','org.eclipse.ui.forms','org.eclipse.ui.ide','org.eclipse.text',
    'org.osgi.service.prefs')
$taskLibraries=foreach($taskBundle in $taskBundles){
    Get-ChildItem -LiteralPath "$EclipseHome\plugins" -Filter "${taskBundle}_*.jar" |
        Sort-Object Name -Descending | Select-Object -First 1 -ExpandProperty FullName
}
$taskUi="$taskRepo\org.entirej.ide.ui\target\classes"
$taskFormUi="$taskRepo\org.entirej.ide.ui.form\target\classes"
if($Packaged){
    $taskUi="$taskRepo\org.entirej.ide.ui\target\org.entirej.ide.ui-5.1.0-SNAPSHOT.jar"
    $taskFormUi="$taskRepo\org.entirej.ide.ui.form\target\org.entirej.ide.ui.form-5.1.0-SNAPSHOT.jar"
}
$taskClasspath=(@($taskOut,$taskUi,$taskFormUi,
    "$taskRepo\org.entirej.ide.compatibility\target\classes",
    "$taskRepo\org.entirej.ide.core\target\classes",
    "$taskRepo\org.entirej.core.runtime\extlibs\*",
    "$taskRepo\org.entirej.core.runtime\extlibs-dev\*") + $taskLibraries) -join ';'
$taskSources=@('PreviewNode','PreviewBounds','PreviewGridConstraint','PreviewGridLayoutSolver','PreviewNodeFigure','PreviewTheme') | ForEach-Object {
    Join-Path $SourceDirectory "org.entirej.ide.ui\src\org\entirej\ide\ui\editors\preview\$_.java"
}
$taskSources+=Join-Path $SourceDirectory 'org.entirej.ide.ui.form\src\org\entirej\ide\ui\editors\form\preview\FormPreviewModelBuilder.java'
$taskSources+=Join-Path $SourceDirectory 'org.entirej.ide.ui.form\src\org\entirej\ide\ui\editors\form\preview\FormPreviewDescriptorResolver.java'
$taskSources+=Join-Path $SourceDirectory 'org.entirej.ide.ui.form\src\org\entirej\ide\ui\editors\form\preview\PreviewRendererNames.java'
$taskSources+=Join-Path $SourceDirectory 'org.entirej.ide.ui.form\src\org\entirej\ide\ui\editors\form\preview\RapPreviewThemeResolver.java'
$taskSources+=Join-Path $SourceDirectory 'org.entirej.ide.ui.form\src\org\entirej\ide\ui\editors\form\preview\AppLayoutPreviewModelBuilder.java'
$taskSources+=@('PreviewEditControl','PreviewEditPartFactory','PreviewNodePart','PreviewSelectionHandler') | ForEach-Object {
    Join-Path $SourceDirectory "org.entirej.ide.ui\src\org\entirej\ide\ui\editors\preview\$_.java"
}
$taskSources+=Join-Path $PSScriptRoot 'PreviewRegressionTest.java'
if($Packaged){$taskSources=@((Join-Path $PSScriptRoot 'PreviewRegressionTest.java'))}
$taskSources+=Join-Path $PSScriptRoot 'PreviewCorpusTest.java'
$taskSources+=Join-Path $PSScriptRoot 'PreviewFormFixture.java'
$taskSources+=Join-Path $PSScriptRoot 'PreviewFixtureRepository.java'
$taskSources+=Join-Path $PSScriptRoot 'PreviewSpacingTest.java'
$taskSources+=Join-Path $PSScriptRoot 'PreviewPresentationTest.java'
$taskSources+=Join-Path $PSScriptRoot 'PreviewInteractionTest.java'
$ErrorActionPreference='Continue'
& "$JavaHome\bin\javac.exe" -encoding UTF-8 -cp $taskClasspath -d $taskOut $taskSources
$ErrorActionPreference='Stop'
if($LASTEXITCODE -ne 0){throw 'Preview test compilation failed'}
$taskArguments=@('-cp',$taskClasspath,'PreviewRegressionTest')
if($FormsDirectory){$taskArguments+=$FormsDirectory}
if($FormsDirectory -and ($ScreenshotsDirectory -or $Corpus -or $ThemeFile)){$taskArguments+= $(if($ScreenshotsDirectory){$ScreenshotsDirectory}else{'-'})}
if($Corpus){if(!$FormsDirectory){throw '-Corpus requires -FormsDirectory'};$taskArguments+='corpus'}
elseif($ThemeFile){$taskArguments+='-'}
if($ThemeFile){if(!$FormsDirectory){throw '-ThemeFile requires -FormsDirectory'};$taskArguments+=$ThemeFile}
$ErrorActionPreference='Continue'
& "$JavaHome\bin\java.exe" $taskArguments
$ErrorActionPreference='Stop'
if($LASTEXITCODE -ne 0){throw 'Preview regressions failed'}
