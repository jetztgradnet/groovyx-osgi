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

import org.eclipse.gemini.blueprint.test.platform.Platforms
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource

/**
 * Abstract base class for all integration tests. This class sets up
 * the OSGi framework, and loads the bundles 'groovy-all' and 'groovyx.osgi'
 */
abstract class AbstractGroovyxOsgiTests extends AbstractGeminiBlueprintTests {
	@Override
	protected Resource[] getTestBundles() {
		def resources = []
		
		Resource[] testBundles = super.getTestBundles()
		resources.addAll(testBundles)

		// add local test bundle
		// TODO automatically determine version, path and file name, e.g. from system property set in build.gradle
		resources << new FileSystemResource(new File('./build/libs/groovyx.osgi-0.1.jar'))		
		
		return resources as Resource[]
	}
	
	@Override
	protected String[] getTestBundlesNames() {
		// bundles will be resolved from Gradle cache
		return [
				// TODO automatically determine version 
				"org.codehaus.groovy, groovy-all, 1.7.5",
			] as String[]
	}
	
	@Override
	protected String getPlatformName() {
		return Platforms.EQUINOX // .FELIX
	}

}
