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
- **Maven:** 3.9+ (Maven wrapper included for 3.9.6)
- **Build System:** Apache Maven with Eclipse Tycho 4.0.8
- **Target Platform:** Eclipse 4.33 (2024-09)

## Quick Start

```bash
# Full build using Maven wrapper (recommended)
./mvnw clean install

# Skip tests
./mvnw clean install -DskipTests

# Build with JAR signing (release)
./mvnw clean install -Prelease-sign -DBUILD.SIGN.PATH=/path/to/keystore -DBUILD.SIGN.PASS=password
```

---

## Architecture Overview

The EntireJ Eclipse plugin follows a **modular SPI (Service Provider Interface)** pattern with clearly defined extension points for extensibility.

### Core Components

```
┌─────────────────────────────────────────────────────────────────────┐
│                        Update Sites                                  │
│   org.entirej.ide.site          org.entirej.ide.report.site         │
└─────────────────────────────────────────────────────────────────────┘
                                    │
┌─────────────────────────────────────────────────────────────────────┐
│                          Features                                    │
│   org.entirej.ide.feature       org.entirej.ide.report.feature      │
└─────────────────────────────────────────────────────────────────────┘
                                    │
┌─────────────────────────────────────────────────────────────────────┐
│                        UI Layer                                      │
│  ┌──────────────┐  ┌──────────────────┐  ┌────────────────────┐    │
│  │org.entirej.  │  │org.entirej.ide.  │  │org.entirej.ide.    │    │
│  │ide.ui        │  │ui.form           │  │ui.report           │    │
│  │(Perspectives,│  │(Form Editor,     │  │(Report Editor,     │    │
│  │ Wizards)     │  │ GEF Canvas)      │  │ GEF Preview)       │    │
│  └──────────────┘  └──────────────────┘  └────────────────────┘    │
└─────────────────────────────────────────────────────────────────────┘
                                    │
┌─────────────────────────────────────────────────────────────────────┐
│                      Core Layer                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ org.entirej.ide.core                                          │   │
│  │ - Project Natures (EJNature, EJReportNature)                  │   │
│  │ - Builders (5 consistency checkers)                           │   │
│  │ - Classpath Containers                                        │   │
│  │ - SPI Extension Points (8 defined)                            │   │
│  └──────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
                                    │
┌─────────────────────────────────────────────────────────────────────┐
│                   Provider Layer (SPI Implementations)               │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐                 │
│  │Client       │  │Database     │  │Block Service│                 │
│  │Frameworks   │  │Providers    │  │Providers    │                 │
│  │- RWT        │  │- MySQL      │  │- Table      │                 │
│  │- Swing      │  │- Oracle     │  │- Statement  │                 │
│  │- JavaFX     │  │- H2/HSQL    │  │- Custom     │                 │
│  └─────────────┘  └─────────────┘  └─────────────┘                 │
└─────────────────────────────────────────────────────────────────────┘
                                    │
┌─────────────────────────────────────────────────────────────────────┐
│                      Runtime Layer                                   │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │ org.entirej.core.runtime                                      │   │
│  │ - entirej-core.jar                                            │   │
│  │ - entirej-development.jar                                     │   │
│  │ - entirej-report.jar                                          │   │
│  └──────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────┘
```

---

## Project Structure

```
plugin/
├── org.entirej.ide.parent/          # Parent POM (build entry point)
├── org.entirej.ide.target/          # Eclipse target platform definition
├── org.entirej.ide.libs/            # External dependency management
│
├── CORE PLUGINS
│   ├── org.entirej.core.runtime/    # Core runtime framework JARs
│   ├── org.entirej.ide.core/        # Natures, builders, SPI extension points
│   ├── org.entirej.ide.compatibility/# FreeMarker compatibility layer
│   ├── org.entirej.ide.ui/          # Main UI - perspectives, wizards
│   ├── org.entirej.ide.ui.form/     # Form editor with GEF canvas
│   └── org.entirej.ide.ui.report/   # Report editor with GEF preview
│
├── CLIENT FRAMEWORK PROVIDERS
│   ├── org.entirej.ide.cf.rwt/      # Eclipse RAP/RWT web client
│   ├── org.entirej.ide.cf.fx/       # JavaFX client (not in default build)
│   └── org.entirej.ide.cf.swing/    # Swing desktop client
│
├── DATABASE PROVIDERS
│   ├── org.entirej.ext.oracle/      # Oracle DB support
│   ├── org.entirej.ext.mysql/       # MySQL support
│   └── org.entirej.ext.hsql/        # H2/HSQL embedded DB
│
├── FEATURES & UPDATE SITES
│   ├── org.entirej.ide.feature/     # Main IDE feature
│   ├── org.entirej.ide.report.feature/ # Report feature
│   ├── org.entirej.ide.site/        # P2 update site
│   └── org.entirej.ide.report.site/ # Report update site
│
└── UTILITIES
    ├── org.entirej.ext.jasper/      # JasperReports integration
    └── org.entirej.ora.gen/         # Oracle PL/SQL generation
```

---

## Core Plugin Details

### org.entirej.ide.core

The foundation plugin providing project infrastructure.

#### Project Natures

| Nature | ID | Purpose |
|--------|-----|---------|
| **EJNature** | `org.entirej.ide.EJNature` | Form projects - adds form builders |
| **EJReportNature** | `org.entirej.ide.EJReportNature` | Report projects - adds report builders |

**EJNature Configuration:**
- Adds builders: Maven2Builder, EJFormConstBuilder, EJPropertiesBuilder, EJFormBuilder
- Default properties file: `src/application.ejprop`
- Default renderer file: `src/renderers.ejprop`

**EJReportNature Configuration:**
- Adds builders: Maven2Builder, EJReportPropertiesBuilder, EJReportConstBuilder, EJReportBuilder
- Default properties file: `src/report.ejprop`

#### Builders

| Builder ID | Purpose |
|------------|---------|
| `org.entirej.ide.EJPropertiesBuilder` | Validates application.ejprop |
| `org.entirej.ide.EJFormBuilder` | Validates .form files |
| `org.entirej.ide.EJFormConstBuilder` | Generates form constants |
| `org.entirej.ide.EJReportPropertiesBuilder` | Validates report.ejprop |
| `org.entirej.ide.EJReportBuilder` | Validates .ejreport files |
| `org.entirej.ide.EJReportConstBuilder` | Generates report constants |

#### Classpath Containers

| Container ID | Purpose |
|--------------|---------|
| `EJ_CORE_CONTAINER` | EntireJ core runtime libraries |
| `EJ_REPORT_CONTAINER` | Report-specific libraries |
| `EJ_DEV_CONTAINER` | Development runtime libraries |

#### SPI Extension Points

```xml
<!-- Client Framework Provider -->
org.entirej.ide.core.spi.clientframework.exportProvider
  Interface: ClientFrameworkProvider
  Methods: addEntireJNature(), getProviderName(), getProviderId()

<!-- Database Connectivity Provider -->
org.entirej.ide.core.spi.databaseconnectivity.exportProvider
  Interface: DBConnectivityProvider
  Methods: addEntireJNature(), addEntireJReportNature()

<!-- Block Service Content Provider -->
org.entirej.ide.core.spi.blockservicecontent.provider
  Interface: BlockServiceContentProvider
  Implementations: Table, Statement, Custom

<!-- Validation Providers -->
org.entirej.ide.core.spi.ejpropertiesvalidate.provider
org.entirej.ide.core.spi.ejformvalidate.provider
org.entirej.ide.core.spi.ejreportpropertiesvalidate.provider
org.entirej.ide.core.spi.ejreportvalidate.provider

<!-- Feature Configuration Provider -->
org.entirej.ide.core.spi.featureconfig.provider
  Implementations: SpringFeatureConfigProvider, SpringKerberosFeatureConfigProvider
```

---

### org.entirej.ide.ui & org.entirej.ide.ui.form

Form editing environment with visual canvas.

#### Perspective

- **EJPerspective** (`org.entirej.ide.ui.perspective`) - Main form development perspective

#### Editors

| Editor ID | Extension | Purpose |
|-----------|-----------|---------|
| `org.entirej.ide.ui.editors.ej.properties` | `application.ejprop` | Application properties |
| `org.entirej.ide.ui.editors.ej.form` | `.form` | Form definitions |
| `org.entirej.ide.ui.editors.ej.block` | `.block` | Block references |
| `org.entirej.ide.ui.editors.ej.objgroup` | `.objgroup` | Object groups |
| `org.entirej.ide.ui.editors.ej.lov` | `.lovdef` | LOV definitions |

#### Form Editor Structure

```
EJFormEditor (Multi-page)
├── Tree View Page (FormDesignTreeSection)
│   ├── BlockGroupNode
│   │   └── BlockItemsGroupNode
│   ├── LovGroupNode
│   ├── RelationsGroupNode
│   └── ObjectGroupNode
├── Canvas Visual Editor (CanvasGroupNode + GEF)
├── Form Reference Page
└── Form Usage Page
```

#### Wizards

| Wizard | Purpose |
|--------|---------|
| NewEntireJProjectWizard | Create EntireJ form project |
| NewEntireJFormWizard | Add new form |
| NewEntireJRefBlockWizard | Create reusable block |
| NewEJPojoServiceWizard | Create service class |
| NewEntireJRefLovWizard | Create reusable LOV |
| NewEntireJObjectGroupWizard | Create object group |

#### Refactoring Support

11 rename/move/delete participants ensure metadata stays synchronized with Java code changes.

---

### org.entirej.ide.ui.report

Report editing environment with GEF preview.

#### Perspective

- **EJReportPerspective** (`org.entirej.ide.ui.report.perspective`)

#### Editors

| Editor ID | Extension | Purpose |
|-----------|-----------|---------|
| `org.entirej.ide.ui.editors.ej.report` | `.ejreport` | Report definitions |
| `org.entirej.ide.ui.editors.ej.report.properties` | `report.ejprop` | Report properties |

#### GEF Integration

```
ReportEditPartFactory
├── ReportCanvasPart (main container)
├── ReportBlockSectionCanvasPart
├── ReportBlockPart
├── ReportBlockColumnPart
├── ReportFormScreenPart
├── ReportTableScreenCanvasPart
└── ReportFormScreenItemPart

Policies (Edit behavior):
├── ColumnResizableEditPolicy
├── ReportBlockResizableEditPolicy
├── ScreenItemResizableEditPolicy
└── ScreenResizableEditPolicy

Ruler Support:
├── ReportRuler / ReportRulerProvider
└── Guide commands (Create, Delete, Move)
```

---

### org.entirej.ide.cf.rwt

RWT (Eclipse RAP) client framework provider.

#### Providers

| Provider | ID | Description |
|----------|-----|-------------|
| RWTClientFrameworkProvider | `org.entirej.framework.cf.rwt_rap` | Eclipse RAP web apps |
| RWTSpringClientFrameworkProvider | - | Spring-integrated RAP |
| ReactClientFrameworkProvider | - | React frontend |

#### Project Creation Process

1. Creates `src` directory
2. Copies templates:
   - `application.ejprop` - RWT app config
   - `renderers.ejprop` - RAP renderer definitions
   - `ApplicationLauncher.java` - App launcher
   - `pom.xml` - Maven POM with Jakarta EE dependencies
   - `web.xml` - Servlet configuration (Jakarta EE 6.0)
   - `index.html` - Web entry point
3. Configures WEB module facets
4. Adds natures: Maven2, Java, JEM, ModuleCore
5. Sets up classpath containers

#### Templates Location

```
templates/rwt/
├── application.ejprop
├── renderers.ejprop
├── ApplicationLauncher.java
├── pom.xml                    # Jakarta Servlet API 6.1.0
├── web.xml                    # Jakarta EE 6.0 namespace
├── web.tabris.xml
├── index.html
├── login.html
├── 403.html
├── LoginServlet.java          # jakarta.servlet imports
├── AccessDeniedServlet.java   # jakarta.servlet imports
├── EJSecurityConfig.java
└── EJAuthenticationProvider.java
```

---

### Database Providers

#### MySQL (org.entirej.ext.mysql)

- **Provider ID:** `org.entirej.framework.dbn.mysql`
- **Container:** `EJ_MYSQL_CONTAINER`
- **Template:** `/templates/mysqlOptions/Connection.properties`

#### Oracle (org.entirej.ext.oracle)

- **Provider ID:** `org.entirej.framework.dbn.oracle`
- **Container:** `EJ_ORACLE_CONTAINER`
- **Template:** `/templates/oracleOptions/Connection.properties`
- **Extra:** OraTypeBlockServiceContentProvider for PL/SQL packages

#### H2/HSQL (org.entirej.ext.hsql)

- **Provider ID:** `org.entirej.framework.dbn.hsql`
- **Container:** `EJ_HSQL_CONTAINER`
- **Template:** `/templates/hsqlOptions/Connection.properties`
- **Extra:** Embedded database demo file

---

## File Formats

### application.ejprop

```xml
<?xml version="1.0" encoding="UTF-8"?>
<entirejFramework>
  <version>2.2</version>
  <applicationManager>org.entirej...EJRWTApplicationManager</applicationManager>
  <connectionFactoryClassName>...</connectionFactoryClassName>
  <reusableBlocksLocation>...</reusableBlocksLocation>
  <reusableLovDefinitionLocation>...</reusableLovDefinitionLocation>
  <applicationDefinedProperties>
    <property name="..." multilingual="boolean" propertyType="TYPE"/>
    <propertyGroup name="...">...</propertyGroup>
  </applicationDefinedProperties>
  <renderer>...</renderer>
</entirejFramework>
```

### .form Files

XML containing:
- Form metadata and structure
- Block definitions with items
- LOV mappings
- Relations between blocks
- Service class references
- Canvas layout information

### .block Files

Reusable block definition:
- Block properties (name, type, service)
- Item definitions
- Database query configuration

### .lovdef Files

List of Values definition:
- LOV name and type
- Return/display value mappings
- Data source configuration

---

## Build System

### Maven/Tycho Configuration

```xml
<tycho-version>4.0.8</tycho-version>
<target>entirej-2024-09.target</target>
```

### Target Platform

File: `org.entirej.ide.target/entirej-2024-09.target`
- Eclipse 2024-09 (4.33)
- GEF Classic 3.14.0+
- Multi-platform: Windows, Linux, macOS (x86_64, aarch64)

### P2 Repositories

```
https://download.eclipse.org/releases/2024-09
https://download.eclipse.org/tools/gef/classic/release/latest
```

### Build Artifacts

```
org.entirej.ide.site/target/
├── site/                              # P2 repository
└── org.entirej.site-5.1.0-SNAPSHOT-site.zip

org.entirej.ide.report.site/target/
├── site/                              # Report P2 repository
└── org.entirej.report.site-5.1.0-SNAPSHOT-site.zip
```

---

## Key Classes Reference

### Core Infrastructure

| Class | Package | Purpose |
|-------|---------|---------|
| `EJCorePlugin` | `org.entirej.ide.core` | Plugin activator |
| `EJCoreLog` | `org.entirej.ide.core` | Logging utility |
| `CFProjectHelper` | `org.entirej.ide.core.cf` | Project configuration utility |
| `EJPluginEntireJClassLoader` | `org.entirej.ide.core` | Custom classloader |

### Form Editor

| Class | Package | Purpose |
|-------|---------|---------|
| `AbstractEJFormEditor` | `org.entirej.ide.ui.editors.form` | Base form editor |
| `FormDesignTreeSection` | `org.entirej.ide.ui.editors.form` | Tree explorer (105KB) |
| `CanvasGroupNode` | `org.entirej.ide.ui.editors.form` | Visual canvas (187KB) |
| `FormCanvasPreviewImpl` | `org.entirej.ide.ui.editors.form` | Runtime preview |

### Report Editor

| Class | Package | Purpose |
|-------|---------|---------|
| `AbstractEJReportEditor` | `org.entirej.ide.ui.editors.report` | Base report editor |
| `ReportDesignTreeSection` | `org.entirej.ide.ui.editors.report` | Tree explorer (58KB) |
| `ReportEditPartFactory` | `org.entirej.ide.ui.editors.report.gef` | GEF part factory |

### Validation

| Class | Package | Purpose |
|-------|---------|---------|
| `EJPropertiesValidateImpl` | `org.entirej.ide.ui` | Properties validation |
| `EJFormValidateImpl` | `org.entirej.ide.ui.form` | Form validation (72KB) |
| `EJReportValidateImpl` | `org.entirej.ide.ui.report` | Report validation (18KB) |

---

## Upgrade History

### Jakarta EE Migration (February 2026)

- Templates updated from `javax.servlet` to `jakarta.servlet`
- web.xml updated to Jakarta EE 6.0 namespace
- Removed deprecated CompressingFilter
- JavaFX modules removed from default build (source preserved)

### Java 17 & Eclipse 2024-09 Upgrade (February 2026)

- Tycho: 1.4.0 → 4.0.8
- Target platform: Eclipse 4.11 → 4.33 (2024-09)
- Java: 11 → 17
- GEF: 3.10.1 (legacy) → GEF Classic 3.14.0+
- Added Maven wrapper (3.9.6)

**GEF API Fixes:**
- `ZoomListener` moved to `org.eclipse.draw2d.zoom`
- `createSelectionHandles()` return type changed to `List<Handle>`

---

## Troubleshooting

### Build Fails with Resolution Errors

Ensure repositories are accessible:
```
https://download.eclipse.org/releases/2024-09
https://download.eclipse.org/tools/gef/classic/release/latest
```

### Eclipse Import Issues

1. Clean all projects
2. Update Maven projects (Alt+F5)
3. Reload target platform
4. Rebuild workspace

### Missing Libraries

```bash
cd org.entirej.ide.libs
./mvnw dependency:copy
```
