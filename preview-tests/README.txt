GEF preview regression checks
=============================
Build the Tycho reactor first so bundle dependencies exist under target/classes.
Use Java 21 and a local Eclipse installation with SWT/Draw2D/GEF plugins.

PowerShell example (from the repository root):
  .\preview-tests\run.ps1 -JavaHome <jdk21> -EclipseHome <eclipse>

Optional real-form checks (read-only, no database/application startup):
  .\preview-tests\run.ps1 -JavaHome <jdk21> -FormsDirectory <temos-forms-directory>
  Add -ScreenshotsDirectory <output-directory> for SWT/Draw2D PNGs.
  Add -Packaged to test the built UI and form UI JARs rather than compiling sources.
  Add -Corpus to check each full main form first, then every local block main screen.
  Add -ThemeFile <CSS file> to repeat the corpus with the actual application theme.
  -SourceDirectory can select an exported previous source revision for comparison.

The focused tests retain the TM03.2, TM03.4, TM03.5 and TM04.2 regression cases.
Corpus mode also reconstructs local main-form canvas trees. No customer forms
are copied into this repository. It uses the real model builder, descriptor
fallback, layout solver and figure painter. Renderer definition loading is stubbed
out (no live Eclipse project/extension registry), so this is not an end-to-end
workbench test. Shared .objgroup and .block resources are resolved read-only using
the source application's configured resource paths. Object groups use the real
plugin import logic, including host canvas overrides. Cycles and missing resources
are reported. It does not execute actions that change visibility at runtime.
Screenshots are off-screen native SWT images;
no editor is opened or modified. The form files are never written.

Coverage: recursive auto sizing, explicit sizes/minima, nested groups, selected
tabs, weighted splits, radio caption/slot preservation, orientation, clipping,
long option text, and preservation of explicitly hidden form fields.
Additional fixtures cover inline caption sizing, blank captions, label alignment,
character-vs-pixel width hints, date-time inference and stacked item painting
without changing stacked canvas tab painting. Native SWT/Draw2D drawing calls are
recorded to check the painter as well as the layout model.

Corpus mode builds and paints each complete locally defined main screen first,
then every declared popup canvas (including imported object-group popups),
then visits every tab/stack page (including nested pages) and individually builds
AND paints every local/resolved shared SingleRecord block, including empty main screens. Other local
block main screens keep their model checks and representative renders. The loader
includes blocks nested in organizational blockGroup elements, real main-screen
frame/size/span/expand properties and local nested canvas/page properties.
Every scroll region is painted in bounded 1600x1000 tiles, avoiding oversized
native image allocations. With -ScreenshotsDirectory all main/page/single-record
tiles and coverage.txt are saved, not just one example per renderer. Without it,
all these painting checks still run but the generated images are not saved.

Individual block checks cover visible controls, renderer kinds, natural caption
sizes, alignment and containment. CONFIGURED-CLIP entries identify the nearest
explicitly sized ancestor; its constraints can also shrink auto-sized descendants
as native SWT does. They remain warnings, not silently repaired customer forms.
MAIN-CONTAINMENT entries flag main-form layout regions needing visual
review; these can depend on form dimensions/splits/runtime state and are not
automatically classified as source bugs or silently corrected. REFERENCE-GAP
entries identify missing shared definitions, embedded referenced forms or unbound block canvases
that this local reconstruction cannot verify. Such gaps are NOT claimed as tested
runtime content. Each tab/stack page is visited with its ancestors selected; this
does not enumerate the Cartesian product of unrelated page selections.
Runtime-created dialogs and action/data-driven runtime state
remain outside this runner. Custom table/chart HTML/data remain schematic.
Generated test classes are kept under the form UI module's ignored target folder.

RAP spacing and theme metrics
----------------------------
Form previews now use RAP runtime layout metrics rather than the legacy SWT
preview's zero-gap item grids: plain groups have zero margins and 5px/1px
horizontal/vertical gaps; block bodies are margin-free. Framed groups retain
their inner grid margins, with theme frame/title insets. Application hideMargin
continues to apply. Independent gaps and per-side insets are supported by the
shared solver; models without a PreviewTheme keep the legacy painter defaults.

The editor reads local source-classpath CSS returned as a literal from
getThemeCSSLocation/getBaseThemeCSSLocation in *Launcher.java source files.
Application classes are never loaded or executed. A unique literal theme is
used; multiple/computed choices fall back to RAP defaults. For such projects,
an optional .settings/org.entirej.preview.properties can select classpath paths:
  theme=ch/example/theme/application.css
  baseTheme=theme/custom-base.css
No settings file is created automatically. Themes are read again on rebuild.

Supported CSS is intentionally limited to flat widget/sub-element selectors,
SWT attributes, single custom variants and states; font (px), padding, margin,
border width and spacing. Specificity/source order and !important are honored.
Unsupported selectors, imports, calculated lengths, other units and dynamic
launcher expressions are not executed or interpreted. Negative margins are
clamped to zero for preview client-area safety. This is not a browser engine:
runtime HTML, visual-attribute fonts, image intrinsic sizes, theme animations,
widget state changes and platform-specific trim remain schematic.

Geometry tests compare real SWT GridLayout cells to the GEF solver, check
asymmetric insets, spans, title/tab chrome, CSS cascade and project resource
resolution, and preserve the earlier caption/radio/stacked-item regressions.
They include constrained/zero-width grids, explicit minima, mixed fixed/grabbing
tracks, spanning-only grids, unequal row spans, and pixel rounding. Grab tracks
shrink to minima; spanning growth does not inflate unrelated fixed tracks.
Theme fonts come from the JFace font registry (registry-owned/disposed); the
model builder and painter share font measurements to avoid caption clipping.

Form/block versus canvas presentation
------------------------------------
Form/block previews omit technical canvas/block headings and generated <title>:
or <page>: prefixes; configured group titles and real tab captions remain.
Stacked pages KEEP their clickable editor navigation tabs, using plain page
names, without the extra <STACKED_COMPONENT> heading band. Navigation and the
preview-to-outline selection echo preserve the current presentation mode.
Explicitly selecting a canvas in the outline keeps the existing design aids;
page selection within that mode keeps the design presentation. Technical names
remain model/selection identities and are available in hover tooltips.
Presentation tests cover both modes, page switching, reserved title space,
selection rebuilds, technical table/chart headings and real column captions.
