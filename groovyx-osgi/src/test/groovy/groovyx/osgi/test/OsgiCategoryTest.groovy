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
import org.osgi.framework.ServiceRegistration
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource


import groovyx.osgi.OsgiCategory

/**
 * Integration tests for OsgiCategory.
 *
 * NOTE: the tests require the main classes to be jar'ed up and
 * placed at ./build/libs/groovyx.osgi-version.jar!!! See {@link #getTestBundles()}
 *
 * @author Wolfgang Schell
 */
class OsgiCategoryTest extends AbstractGeminiBlueprintTests {
	
	@Override
	protected Resource[] getTestBundles() {
		def resources = []
		
		Resource[] testBundles = super.getTestBundles()
		resources.addAll(testBundles)

		// add local test bundle
		// TODO automatically determine path and file name, e.g. from system property set in build.gradle
		resources << new FileSystemResource(new File('./build/libs/groovyx.osgi-0.1.jar'))		
		
		return resources as Resource[]
	}
	
	@Override
	protected String[] getTestBundlesNames() {
		// bundles will be resolved from Gradle cache
		return [ 
				"org.codehaus.groovy, groovy-all, 1.7.5",
			] as String[]
	}
	
	@Override
	protected String getPlatformName() {
		return Platforms.EQUINOX // .FELIX
	}
	
	public void testSpringOsgiTest() throws Exception {
		assertNotNull(bundleContext)
	}
	
	public void testLoadGroovyxOsgi() throws Exception {
		def found = bundleContext.bundles.find { bundle -> 'groovyx.osgi' == bundle.symbolicName  }
		assertNotNull('Bundle groovyx-osgi is not loaded', found)
	}
	
	public void testFindService() throws Exception {
		String service = "ThisIsAService"
		ServiceRegistration reg = bundleContext.registerService(String.class.getName(), service, null)
		try {
			def result
			use(OsgiCategory) {
				result = bundleContext.findService(String.class.getName()).withService() { String srv ->
					srv.toUpperCase()
				}
			}
			assertNotNull('Service result should exist', result)
			assertEquals(service.toUpperCase(), result)
		}
		finally {
			reg.unregister()
		}
	}
}
