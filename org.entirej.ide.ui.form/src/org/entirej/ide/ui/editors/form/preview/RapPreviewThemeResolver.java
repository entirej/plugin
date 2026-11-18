/*******************************************************************************
 * Copyright 2026 CRESOFT AG
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by law or agreed in writing, software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND.
 ******************************************************************************/
package org.entirej.ide.ui.editors.form.preview;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.entirej.ide.ui.editors.preview.PreviewTheme;

/** Resolves only local source resources; never instantiates an application's launcher. */
public final class RapPreviewThemeResolver
{
    public static PreviewTheme forProject(IJavaProject project)
    {
        if (project == null) return PreviewTheme.rapDefault();
        try
        {
            Properties settings = new Properties();
            IFile file = project.getProject().getFile(".settings/org.entirej.preview.properties");
            if (file.exists()) try (InputStream input = file.getContents()) { settings.load(input); }
            String theme = settings.getProperty("theme");
            String base = settings.getProperty("baseTheme");
            if (theme == null)
            {
                // Conventional launchers are cheap to inspect and cover the existing RAP templates.
                // Ambiguous or computed runtime choices intentionally fall back to RAP defaults.
                Set<String> themes = new LinkedHashSet<String>();
                Set<String> bases = new LinkedHashSet<String>();
                for (IPackageFragment fragment : project.getPackageFragments())
                {
                    if (fragment.getKind() != IPackageFragmentRoot.K_SOURCE) continue;
                    for (ICompilationUnit unit : fragment.getCompilationUnits())
                    {
                        if (!unit.getElementName().endsWith("Launcher.java")) continue;
                        for (IType type : unit.getAllTypes()) for (IMethod method : type.getMethods())
                        {
                            if ("getThemeCSSLocation".equals(method.getElementName()))
                            {
                                String literal = literalReturn(method.getSource());
                                if (literal != null) themes.add(literal);
                            }
                            else if ("getBaseThemeCSSLocation".equals(method.getElementName()))
                            {
                                String literal = literalReturn(method.getSource());
                                if (literal != null) bases.add(literal);
                            }
                        }
                    }
                }
                if (themes.size() == 1) theme = themes.iterator().next();
                if (base == null && bases.size() == 1) base = bases.iterator().next();
            }
            return PreviewTheme.fromCss(readResource(project, base), readResource(project, theme));
        }
        catch (CoreException | java.io.IOException e)
        {
            java.util.logging.Logger.getLogger(RapPreviewThemeResolver.class.getName()).log(java.util.logging.Level.FINE,
                    "Preview theme unavailable; using RAP metrics", e);
            return PreviewTheme.rapDefault();
        }
    }

    public static String literalReturn(String source)
    {
        if (source == null) return null;
        source = source.replaceAll("(?s)/\\*.*?\\*/|//[^\\r\\n]*", "");
        Matcher match = Pattern.compile("(?s)^[^{]*\\{\\s*return\\s+\"([^\"\\\\]+\\.css)\"\\s*;\\s*}\\s*$").matcher(source);
        return match.matches() ? match.group(1) : null;
    }

    private static String readResource(IJavaProject project, String location) throws CoreException, java.io.IOException
    {
        if (location == null || location.isBlank()) return null;
        Path path = new Path(location.trim());
        if (path.isAbsolute() || location.contains("..") || location.contains(":")) return null;
        for (IPackageFragmentRoot root : project.getPackageFragmentRoots())
        {
            if (root.getKind() != IPackageFragmentRoot.K_SOURCE || !(root.getResource() instanceof IContainer)) continue;
            IFile resource = ((IContainer) root.getResource()).getFile(path);
            if (resource.exists()) try (InputStream stream = resource.getContents())
            {
                return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
        return null;
    }
}
