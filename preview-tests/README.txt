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
  Add -Corpus to audit all locally defined main screens in every *.form file.
  -SourceDirectory can select an exported previous source revision for comparison.

The runner constructs plugin property models from the main-screen/item subset of
the specified TM03.2, TM03.4, TM03.5 and TM04.2 form XML files. No customer forms
are copied into this repository. It uses the real model builder, descriptor
fallback, layout solver and figure painter. Renderer definition loading is stubbed
out (no live Eclipse project/extension registry), so this is not an end-to-end
workbench test. It does not resolve referenced object groups or execute actions
that change visibility at runtime. Screenshots are off-screen native SWT images;
no editor is opened or modified. The form files are never written.

Coverage: recursive auto sizing, explicit sizes/minima, nested groups, selected
tabs, weighted splits, radio caption/slot preservation, orientation, clipping,
long option text, and preservation of explicitly hidden form fields.
Additional fixtures cover inline caption sizing, blank captions, label alignment,
character-vs-pixel width hints, date-time inference and stacked item painting
without changing stacked canvas tab painting. Native SWT/Draw2D drawing calls are
recorded to check the painter as well as the layout model.

Corpus mode builds every main screen with local item groups. It checks visible
controls, known renderer kinds, natural label/check-box caption widths and label
alignment. Representative screens are rendered when a screenshots directory is
supplied. CONFIGURED-CLIP entries are informational: explicit size hints may be
smaller than the static content, so these are not classified as auto-size defects.
It does not reconstruct full form canvases, dialogs, referenced object groups or
application state. Custom table/chart renderers are schematic placeholders; their
HTML, data and runtime appearance are not verified by this runner.
Generated test classes are kept under the form UI module's ignored target folder.
