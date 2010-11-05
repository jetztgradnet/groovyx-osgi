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

package groovyx.osgi

import static org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

import org.ops4j.pax.exam.junit.JUnit4TestRunner
import org.ops4j.pax.exam.junit.Configuration
import org.ops4j.pax.exam.Option
import org.ops4j.pax.exam.Inject
import static org.ops4j.pax.exam.CoreOptions.*

import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceReference
import org.osgi.framework.InvalidSyntaxException


@RunWith (JUnit4TestRunner)
class OsgiCategoryTests {
	@Inject
	BundleContext bundleContext
	
	@Configuration
	public Option[] configure() {
		[
			equinox(),
			provision(
				mavenBundle().groupId('org.codehaus.groovy').artifactId('groovy-all').version('1.7.5'),
				// TODO automatically determine path and file name, e.g. from system property set in build.gradle 
				bundle(new File('./build/libs/groovyx.osgi-0.1.jar').toURI().toString())
			)
		] as Option[]
	}

	@Before
	public void setUp() throws Exception {
		def props = [ name: 'osgitest', numval: 42 ]
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testFrameworkLoaded() throws Exception {
		assertNotNull("BundleContext is not available", bundleContext)
	}
	
	@Test
	public void testLoadGroovyxOsgi() throws Exception {
		def found = bundleContext.bundles.find { bundle -> 'groovyx.osgi' == bundle.symbolicName  }
		assertNotNull('Bundle groovyx-osgi is not loaded', found)
	}
	
	@Test
	public void testFindService() throws Exception {
		String service = "ThisIsAService"
		def reg = bundleContext.registerService(String.class.getName(), service, null)
		def result
		use (OsgiCategory) {
			result = bundleContext.withService(String.class.getName()) { srv ->
				srv.toUpperCase()
			}
		}
		assertNotNull('Service result should exist', result)
		assertEquals(service.toUpperCase(), result)
	}
}
