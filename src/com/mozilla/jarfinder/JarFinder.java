/*
 * Copyright 2012 Mozilla Foundation
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mozilla.jarfinder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFinder {
    boolean searchCompleted = false;
    List<JarMatch> matches = new ArrayList<JarMatch>();
    String target;
    String targetClass;
    String targetSource;
    String searchPath;

    public JarFinder(String target, String searchPath) {
        this.target = target;
        this.targetClass = target.replaceAll("[.]", "/") + ".class";
        this.targetSource = this.targetClass.replaceFirst("[.]class$", ".java");
        this.searchPath = searchPath;
    }

    public static void printUsage() {
        System.out.println(String.format("Usage: %s classname [ dir ] [ dir2 ] [ dir3 ] [ ... ]", JarFinder.class.getCanonicalName()));
        String pwd = "./";
        try {
            pwd = new File(".").getCanonicalPath();
        } catch (IOException e) {

        }
        System.out.println(String.format("If no directories are specified, the current directory '%s' will be searched.", pwd));
    }

    public static void main(String[] args) {
        if (args == null || args.length < 1) {
            printUsage();
            System.exit(-1);
        }

        List<String> argList = new ArrayList<String>();
        argList.addAll(Arrays.asList(args));
        if (args.length == 1)
            argList.add("./"); // default to searching the current directory.

        boolean found = false;
        String target = argList.remove(0);
        for (String searchPath : argList) {
            JarFinder finder = new JarFinder(target, searchPath);
            finder.doSearch();
            if (finder.matchCount() > 0) {
                finder.printMatches();
                found = true;
            }
        }

        if (!found) {
            System.out.println(String.format("Could not find class '%s' in any jar in the given path(s)", target));
        }
    }

    public void printMatches() {
        if (!searchCompleted) {
            doSearch();
        }

        System.out.println(String.format("Search results for '%s':", target));
        for(JarMatch match : matches) {
            System.out.println(String.format("%s contains the class '%s' (%s)", match.getJarName(), match.getClassName(), match.getMatchType()));
        }
    }

    public int matchCount() {
        if (!searchCompleted) {
            doSearch();
        }

        return matches.size();
    }

    public void refreshSearch() {
        searchCompleted = false;
        doSearch();
    }

    public void doSearch() {
        if (!searchCompleted) {
            File searchFile = new File(searchPath);
            walk(searchFile);
            searchCompleted = true;
        }
    }

    public void walk(File fileOrDir) {
        String pattern = ".jar";

        if (fileOrDir == null)
            throw new IllegalArgumentException("Can't search null...");

        if (fileOrDir.isFile() && fileOrDir.getName().endsWith(pattern)) {
            matches.addAll(searchJar(fileOrDir));
        } else if (fileOrDir.isDirectory()) {
            for (File listFile : fileOrDir.listFiles()) {
                if(listFile.isDirectory()) {
                    walk(listFile);
                } else {
                    if(listFile.getName().endsWith(pattern)) {
                        matches.addAll(searchJar(listFile));
                    }
                }
            }
        } else {
            throw new IllegalArgumentException(String.format("Error, '%s' is neither a jar file nor a directory.", fileOrDir.getName()));
        }
    }

    public List<JarMatch> searchJar(File listFile) {
        JarFile jarFile;
        List<JarMatch> matches = new ArrayList<JarMatch>();
        try {
            if (listFile.length() > 0) {
                jarFile = new JarFile(listFile);

                JarEntry targetJarEntry = jarFile.getJarEntry(targetClass);
                if (targetJarEntry != null) {
                    matches.add(new JarMatch(listFile.getCanonicalPath(), target, "compiled"));
                } else {
                    targetJarEntry = jarFile.getJarEntry(targetSource);
                    if (targetJarEntry != null) {
                        matches.add(new JarMatch(listFile.getCanonicalPath(), target, "source"));
                    }
                    // If we just have a bare class name, search the whole jar
                    if (target.matches("^[^.]+$")) {
                        // No package names.  Search the whole thing.
                        Enumeration<JarEntry> entries = jarFile.entries();
                        while (entries.hasMoreElements()) {
                            JarEntry entry = entries.nextElement();
                            String name = entry.getName();
                            if (name.endsWith("/" + targetClass)) {
                                //System.out.println("Found name: " + name + " in jar " + listFile.getCanonicalPath());
                                matches.add(new JarMatch(listFile.getCanonicalPath(), pathToClassName(name), "compiled"));
                            } else if (name.endsWith("/" + targetSource)) {
                                matches.add(new JarMatch(listFile.getCanonicalPath(), pathToClassName(name), "source"));
                            }
                        }
                    }
                }
            } else {
                // Skip empty jar file.
                //System.out.println(String.format("Skipping empty jar file '%s'", listFile.getName()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return matches;
    }

    private String pathToClassName(String name) {
        if (name == null) return null;
        String className = name.replaceAll("[/]", ".");
        className = className.replaceFirst("^\\.", "");
        className = className.replaceFirst("\\.class$", "");
        className = className.replaceFirst("\\.java$", "");
        return className;
    }
}

class JarMatch {
    private final String jarName;
    private final String className;
    private final String matchType;

    public String getJarName() { return jarName; }
    public String getClassName() { return className; }
    public String getMatchType() { return matchType; }

    public JarMatch(String jarName, String className, String matchType) {
        this.jarName = jarName;
        this.className = className;
        this.matchType = matchType;
    }
}
