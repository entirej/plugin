# EntireJ Eclipse Plugin Project

## Branch Baseline Before Latest Update

This working tree is based on `origin/mavenbase`, which is newer than `develop` for the Maven/Tycho migration path. The branch already contains the Maven wrapper, Java 17 migration work, Tycho 4.0.8, a PDE target-definition module, Jakarta template changes, and GEF compatibility fixes.

Verified baseline build before the latest update:

```powershell
$env:JAVA_HOME='C:\Tools\latest\eclipse\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.10.v20260205-0638\jre'
$env:MAVEN_USER_HOME='C:\Work\plugin\.maven-user'
.\mvnw.cmd -B -V -e -l C:\Work\plugin\.mavenbase-build.log -f org.entirej.ide.parent\pom.xml "-Dmaven.repo.local=C:\Work\plugin\.m2\repository" -DskipTests package
```

Result: `BUILD SUCCESS` for the Java 17 / Eclipse 2024-09 / Tycho 4.0.8 baseline.

## Module Shape

- `org.entirej.ide.parent`: Tycho reactor parent and target-platform configuration.
- `org.entirej.ide.target`: PDE target definition used by Tycho.
- `org.entirej.core.runtime`: bundled EntireJ runtime jars and plugin runtime API.
- `org.entirej.ide.core`: project model, project setup helpers, templates, and shared IDE services.
- `org.entirej.ide.ui`, `org.entirej.ide.ui.form`, `org.entirej.ide.ui.report`: Eclipse UI bundles and GEF editors.
- `org.entirej.ide.compatibility`: legacy compatibility bundle.
- `org.entirej.ide.cf.*`: client framework integrations.
- `org.entirej.ext.*`: packaged database/reporting extension jars.
- `org.entirej.ide.feature`, `org.entirej.ide.report.feature`: installable features.
- `org.entirej.ide.site`, `org.entirej.ide.report.site`: p2 update sites.

## Latest Eclipse Update Plan

1. Start from `origin/mavenbase`, not `develop`.
2. Keep the Maven wrapper and update its distribution to Maven 3.9.9.
3. Move the target platform from Eclipse 2024-09 to Eclipse 2026-06.
4. Move Tycho from 4.0.8 to 5.0.3.
5. Raise bundle execution environments from JavaSE-17 to JavaSE-21 to match the current Eclipse platform.
6. Remove stale fixed GEF feature version constraints and let the 2026-06 target resolve the matching GEF Classic feature.
7. Convert update-site modules from legacy deployable `eclipse-feature` packaging to Tycho 5 `eclipse-repository` packaging backed by `category.xml`.
8. Build the whole reactor after each meaningful step and fix only concrete failures from the current target.

## Current Modernization Edits

- `.mvn/wrapper/maven-wrapper.properties`: Maven 3.9.9.
- `org.entirej.ide.parent/pom.xml`: Tycho 5.0.3 and `org.entirej.ide.target.target`.
- `org.entirej.ide.target/org.entirej.ide.target.target`: Eclipse 2026-06 target with Platform, JDT, PDE, p2 discovery, GEF, and Draw2D.
- Bundle manifests: `Bundle-RequiredExecutionEnvironment: JavaSE-21`.
- Feature manifests: GEF feature import no longer pins an old version.
- Site modules: `eclipse-repository` packaging with `tycho-p2-repository-plugin` and self-contained dependency inclusion.

Latest verification result: `BUILD SUCCESS` on June 24, 2026. The build produced both update-site repositories and zip archives:

- `org.entirej.ide.site/target/org.entirej.site-5.1.0-SNAPSHOT-site.zip`
- `org.entirej.ide.report.site/target/org.entirej.report.site-5.1.0-SNAPSHOT-site.zip`

## Verification Loop

Use this build command from this working tree:

```powershell
$env:JAVA_HOME='C:\Tools\latest\eclipse\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.10.v20260205-0638\jre'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
$env:MAVEN_USER_HOME='C:\Work\plugin\.maven-user'
.\mvnw.cmd -B -V -e -l C:\Work\plugin\.mavenbase-latest-build.log -f org.entirej.ide.parent\pom.xml "-Dmaven.repo.local=C:\Work\plugin\.m2\repository" -DskipTests package
```
