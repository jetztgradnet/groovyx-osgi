/*
 * Copyright 2009-2010 Wolfgang Schell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovyx.osgi.test

import java.util.Arrays;
import java.util.jar.Manifest
import java.util.jar.Attributes

import org.osgi.framework.Constants

import org.springframework.util.StringUtils

import org.eclipse.gemini.blueprint.test.AbstractConfigurableBundleCreatorTests;
import org.eclipse.gemini.blueprint.test.provisioning.ArtifactLocator;

abstract class AbstractGeminiBlueprintTests extends
		AbstractConfigurableBundleCreatorTests {
	public AbstractGeminiBlueprintTests() {
		// do not check dependencies in create Spring ApplicationContext,
		// as this doesn't seem to work with Groovy's MetaClass property
		setDependencyCheck(false)
	}
	
	/**
	 * Set ArtifactLocator for local Gradle cache.
	 */
	@Override
	protected ArtifactLocator getLocator() {
		return new LocalFileSystemGradleRepository()
	}
	
	/**
	 * Set location of test classes according to Gradle conventions.
	 */
	@Override
	protected String getRootPath() {
		return "file:./build/classes/test/";
	}
	
	/**
	 * The generated Manifest contains some invalid imported packages. This method
	 * removes or corrects invalid imports.
	 */
	@Override
	protected Manifest createDefaultManifest() {
		Manifest manifest = super.createDefaultManifest();
		
		// filter packages staring with [L from list of imported packages
		Attributes attributes = manifest.getMainAttributes();
		String importedPackages = attributes.getValue(Constants.IMPORT_PACKAGE)
		String[] packages = importedPackages.split(",")
		def imports = new HashSet()
		packages.each { String pkg ->
			if (pkg.startsWith("[L")) {
				pkg -= "[L"
			}
			if (pkg.startsWith("java.")) {
				return
			}
			if (pkg.startsWith("file:")) {
				// remaining part from our root path
				return
			}
			imports << pkg
		}
		
		attributes.putValue(Constants.IMPORT_PACKAGE, StringUtils.collectionToCommaDelimitedString(imports));
		
		return manifest;
	}
}
