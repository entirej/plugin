# EntireJ IDE Eclipse Plugin Project

This project provides Eclipse RCP plugins for the EntireJ Framework - a model-driven application development platform that enables developers to create GUI applications without writing GUI-specific Java code.

## Project Overview

| Property | Value |
|----------|-------|
| **Group ID** | `org.entirej.ide` |
| **Version** | `5.1.0-SNAPSHOT` |
| **License** | Apache License 2.0 |
| **Provider** | CRESOFT AG |
| **Website** | http://www.entirej.com/ |

## Build Requirements

- **Java:** JDK 17+ (required)
- **Maven:** 3.6+ (Maven wrapper included for 3.9.6)
- **Build System:** Apache Maven with Eclipse Tycho 3.0.5
- **Target Platform:** Eclipse 4.33 (2024-09)

## Quick Start

### Full Build (using Maven wrapper)
```bash
./mvnw clean install
```

### Full Build (from parent directory)
```bash
cd org.entirej.ide.parent
mvn clean install
```

### Build with JAR Signing (Release)
```bash
./mvnw clean install -Prelease-sign -DBUILD.SIGN.PATH=/path/to/keystore -DBUILD.SIGN.PASS=password
```

### Update External Libraries
```bash
cd org.entirej.ide.libs
mvn org.apache.maven.plugins:maven-dependency-plugin:2.8:copy
```

## Project Structure

```
plugin/
├── org.entirej.ide.parent/          # Parent POM (build entry point)
├── org.entirej.ide.libs/            # External dependency management
├── org.entirej.ide.target/          # Eclipse target platform definition
│
├── CORE PLUGINS
│   ├── org.entirej.core.runtime/    # Core runtime framework & bundled JARs
│   ├── org.entirej.ide.core/        # IDE core - natures, builders, extension points
│   ├── org.entirej.ide.compatibility/# Legacy framework compatibility layer
│   ├── org.entirej.ide.ui/          # Main UI plugin - editors, views
│   ├── org.entirej.ide.ui.form/     # Form editor components
│   └── org.entirej.ide.ui.report/   # Report editor components
│
├── CLIENT FRAMEWORK PLUGINS
│   ├── org.entirej.ide.cf.rwt/      # RAP/RWT web client support
│   ├── org.entirej.ide.cf.fx/       # JavaFX client support (disabled)
│   └── org.entirej.ide.cf.swing/    # Swing client support
│
├── DATABASE EXTENSIONS
│   ├── org.entirej.ext.oracle/      # Oracle DB support (OJDBC8)
│   ├── org.entirej.ext.mysql/       # MySQL support (Connector 5.1.22)
│   └── org.entirej.ext.hsql/        # H2/HSQL support
│
├── REPORT EXTENSIONS
│   └── org.entirej.ext.jasper/      # JasperReports integration
│
├── FEATURES & UPDATE SITES
│   ├── org.entirej.ide.feature/     # Main IDE feature
│   ├── org.entirej.ide.report.feature/ # Report feature
│   ├── org.entirej.ide.site/        # P2 update site (IDE tools)
│   └── org.entirej.ide.report.site/ # P2 update site (reports)
│
└── UTILITIES
    ├── org.entirej.ide.upload/      # Deployment utilities
    ├── org.entirej.ide.report.upload/
    ├── org.entirej.ora.gen/         # Oracle generation tools
    └── org.entirej.development/     # Development framework
```

## Module Details

### Core Plugins

| Module | Description | Packaging |
|--------|-------------|-----------|
| `org.entirej.core.runtime` | Bundles core JARs (entirej-core, entirej-development, entirej-report) | eclipse-plugin |
| `org.entirej.ide.core` | Defines project natures, builders, classpath containers, extension points | eclipse-plugin |
| `org.entirej.ide.compatibility` | Compatibility layer with Freemarker 2.3.23 for template processing | eclipse-plugin |
| `org.entirej.ide.ui` | GEF-based editors, Eclipse perspectives, form wizards | eclipse-plugin |
| `org.entirej.ide.ui.form` | Form-specific editors (.form, .block, .objgroup, .lovdef) | eclipse-plugin |
| `org.entirej.ide.ui.report` | Report-specific editors and views | eclipse-plugin |

### Eclipse Extension Points

The project defines several SPI extension points in `org.entirej.ide.core`:

- `org.entirej.ide.core.spi.clientframework.exportProvider` - Client framework implementations
- `org.entirej.ide.core.spi.databaseconnectivity.exportProvider` - Database connectivity providers
- `org.entirej.ide.core.spi.blockservicecontent.provider` - Block service content providers
- `org.entirej.ide.core.spi.ejpropertiesvalidate.provider` - Properties validation
- `org.entirej.ide.core.spi.ejformvalidate.provider` - Form validation
- `org.entirej.ide.core.spi.ejreportpropertiesvalidate.provider` - Report properties validation
- `org.entirej.ide.core.spi.ejreportvalidate.provider` - Report validation
- `org.entirej.ide.core.spi.featureconfig.provider` - Feature configuration

### Classpath Containers

- `EJ_CORE_CONTAINER` - EntireJ Core libraries
- `EJ_REPORT_CONTAINER` - EntireJ Report libraries
- `EJ_DEV_CONTAINER` - EntireJ Development libraries

### Project Natures

- `EJNature` - EntireJ Form Project
- `EJReportNature` - EntireJ Report Project

## P2 Repositories (Target Platform)

```xml
<repository location="https://download.eclipse.org/releases/2024-09"/>
<repository location="https://download.eclipse.org/tools/gef/classic/release/latest"/>
```

**GEF Version:** 3.14.0+ (GEF Classic)

## Maven Repositories

| Repository | URL | Purpose |
|------------|-----|---------|
| EntireJ Maven | https://raw.github.com/entirej/mavenrepo/gh-pages/maven/development/ | Framework artifacts |
| Sonatype Snapshots | https://oss.sonatype.org/content/repositories/snapshots/ | Snapshot dependencies |

## Build Artifacts

After a successful build:

- **Update Site (IDE):** `org.entirej.ide.site/target/site/`
- **Update Site (Reports):** `org.entirej.ide.report.site/target/site/`
- **Distribution ZIP:** `org.entirej.ide.site/target/org.entirej.site-5.1.0-SNAPSHOT-site.zip`
- **Plugin JARs:** `[module]/target/[module]-5.1.0-SNAPSHOT.jar`

## File Extensions Handled

| Extension | Editor | Description |
|-----------|--------|-------------|
| `.ej.properties` | EJ Properties Editor | EntireJ project configuration |
| `.form` | EJ Form Editor | Form definitions |
| `.block` | EJ Block Reference Editor | Block references |
| `.objgroup` | EJ Object Group Editor | Object group definitions |
| `.lovdef` | EJ LOV Reference Editor | List of Values definitions |

## Development Setup

### Import into Eclipse

1. Install Eclipse IDE for RCP Developers (2024-09 or compatible)
2. Install GEF Classic SDK 3.14.0+
3. Import as existing Maven projects
4. Set target platform from `org.entirej.ide.target/entirej-2024-09.target`

### Running the Build

The build uses Maven Tycho which requires specific configuration:

```bash
# Using Maven wrapper (recommended)
./mvnw clean install

# Standard build (from parent directory)
mvn clean install -f org.entirej.ide.parent/pom.xml

# Skip tests
./mvnw clean install -DskipTests

# Verbose output
./mvnw clean install -X
```

### Common Build Issues

1. **Platform-dependent warnings:** The build shows warnings about missing explicit target runtime. This is expected and doesn't affect the build.

2. **Missing Tycho plugin versions:** Warnings about `tycho-packaging-plugin` and `tycho-p2-plugin` missing versions in site modules - these inherit from parent.

3. **Locally built units:** Warning about locally built FX feature units when resolving site dependencies - requires prior FX feature build if FX support is needed.

## Key Dependencies

### EntireJ Framework Libraries
- `entirej-core.jar` - Core framework
- `entirej-development.jar` - Development tools
- `entirej-report.jar` - Report framework
- `entirej-rwt.jar` - RWT client
- `entirej-tabris.jar` - Tabris mobile client
- `entirej-fx.jar` - JavaFX client

### Third-Party
- FreeMarker 2.3.23 (template engine)
- SLF4J API 1.7.3 (logging facade)
- Oracle OJDBC8 (Oracle driver)
- MySQL Connector 5.1.22
- H2 Database 1.3.169
- JFXtras Controls/Common (JavaFX extras)

## Version Management

Version is defined in `org.entirej.ide.parent/pom.xml`:
```xml
<version>5.1.0-SNAPSHOT</version>
```

All modules inherit this version. The OSGi qualifier is auto-generated during build (e.g., `5.1.0.202602051532`).

## Release Process

1. Update version in parent POM
2. Update `MANIFEST.MF` Bundle-Version in all plugins
3. Update `feature.xml` versions in features
4. Build with signing profile:
   ```bash
   mvn clean install -Prelease-sign -DBUILD.SIGN.PATH=/path/to/keystore -DBUILD.SIGN.PASS=password
   ```
5. Deploy update site ZIP to distribution server

## Architecture Notes

- **Model-Driven Design:** UI definitions stored in XML, interpreted at runtime
- **SWT/JFace Based:** All Eclipse-native UI components
- **GEF Integration:** Graphical editing capabilities for form design
- **OSGi Compliant:** Full OSGi bundle metadata with lazy activation
- **Extension Point Architecture:** Pluggable database connectors, client frameworks, validators

## Troubleshooting

### Build Fails with Resolution Errors
Ensure target platform repositories are accessible:
- https://download.eclipse.org/releases/2024-09
- https://download.eclipse.org/tools/gef/classic/release/latest

### Missing Core Libraries
Run the library copy task first:
```bash
cd org.entirej.ide.libs
./mvnw dependency:copy
```

### Eclipse Import Issues
1. Clean all projects
2. Update Maven projects (Alt+F5)
3. Reload target platform
4. Rebuild workspace

## Upgrade History

### Java 17 & Eclipse 2024-09 Upgrade (February 2026)

The project was upgraded from Java 11/Eclipse 4.11 to Java 17/Eclipse 2024-09:

**Changes Made:**
- Tycho version: 1.4.0 → 3.0.5
- Target platform: Eclipse 4.11 → Eclipse 4.33 (2024-09)
- Java version: 11 → 17
- GEF: 3.10.1 (legacy) → GEF Classic 3.14.0+
- Added Maven wrapper (version 3.9.6)

**API Compatibility Fixes:**
- `ZoomListener` moved from `org.eclipse.gef.editparts` to `org.eclipse.draw2d.zoom`
- `createSelectionHandles()` return type changed from `List<?>` to `List<Handle>` in GEF

**Files Modified:**
- All MANIFEST.MF files: `Bundle-RequiredExecutionEnvironment: JavaSE-17`
- `org.entirej.ide.parent/pom.xml`: Updated Tycho and repository configuration
- `org.entirej.ide.target/entirej-2024-09.target`: New target platform definition
- `feature.xml` files: Updated GEF import version
- GEF policy classes: Fixed API compatibility issues
