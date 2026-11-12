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
Generated test classes are kept under the form UI module's ignored target folder.
