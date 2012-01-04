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
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarFinder {
	boolean searchCompleted = false;
	List<String> matches = new ArrayList<String>();
	String target;
	String targetClass;
	String searchPath;
	
	public JarFinder(String target, String searchPath) {
		this.target = target;
		this.targetClass = target.replaceAll("[.]", "/") + ".class";
		this.searchPath = searchPath;
	}
	
	public static void printUsage() {
		System.out.println(String.format("Usage: %s classname dir [ dir2 ] [ dir3 ] [ ... ]", JarFinder.class.getCanonicalName()));
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 2) {
			printUsage();
			System.exit(-1);
		}
		
		boolean found = false;
		String target = args[0];
		for (int i = 1; i < args.length; i++) {
			String searchPath = args[i];
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
		
		for(String match : matches) {
			System.out.println(String.format("%s contains the class '%s'", match, target));
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
	
	public void walk(File dir) {
		String pattern = ".jar";

		for (File listFile : dir.listFiles()) {
			if(listFile.isDirectory()) {
				walk(listFile);
			} else {
				if(listFile.getName().endsWith(pattern)) {
					if (searchJar(listFile)) {
						matches.add(listFile.getPath());
					}
				}
			}
		}
	}

	public boolean searchJar(File listFile) {
		JarFile jarFile;
		boolean found = false;
		try {
			jarFile = new JarFile(listFile);

			JarEntry targetJarEntry = jarFile.getJarEntry(targetClass);
			if (targetJarEntry != null) {
				found = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return found;
	}
}
