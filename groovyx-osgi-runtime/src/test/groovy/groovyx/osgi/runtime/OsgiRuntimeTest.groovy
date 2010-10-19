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

package groovyx.osgi.runtime

import java.io.File
import org.junit.*
import static org.junit.Assert.*

import org.osgi.framework.BundleContext

import org.codehaus.groovy.osgi.runtime.equinox.*
import org.codehaus.groovy.osgi.runtime.external.*


public class OsgiRuntimeTest {
	OsgiRuntimeBuilder builder
	File tempDir
	
	@Before
	void setUp() {
		builder = new OsgiRuntimeBuilder()
		File cwd = new File(System.getProperty('user.dir'))
		tempDir = new File(cwd, 'dropins')
		tempDir.mkdirs()
	}
	
	@After
	void tearDown() {
		if (tempDir && tempDir.isDirectory()) {
			tempDir.deleteDir()
		}
	}
	
	@Test
	void testStartEquinox() throws Exception {
		OsgiRuntime runtime = builder.build {
			// test alias
			framework 'equinox'

			runtimeDir 'system'

			args {
				resolverLogLevel = "warn"
			}

			bundle 'mvn:org.apache.felix:org.apache.felix.fileinstall:3.0.2'
			bundle group: 'org.apache.felix', name:'org.apache.felix.configadmin', version:'1.2.4'
		}
		assertNotNull(runtime)
		assertEquals(EquinoxRuntime.class, runtime.class)
		
		BundleContext context = runtime.start()
		assertNotNull(context)
		assertTrue(runtime.isRunning())
		
		// TODO do something with bundle context		
		
		runtime.stop()
		assertFalse(runtime.isRunning())
		
		context = runtime.bundleContext
		assertNull(context)
	}
	
	@Test
	void testExternalRuntime() throws Exception {
		def tempDir = this.tempDir
		
		OsgiRuntime runtime = builder.build {
			// test alias
			framework 'external'

			dropinsDir tempDir

			args {
				resolverLogLevel = "warn"
			}

			bundle 'mvn:org.apache.felix:org.apache.felix.fileinstall:3.0.2'
			bundle group: 'org.apache.felix', name:'org.apache.felix.configadmin', version:'1.2.4'
		}
		assertNotNull(runtime)
		assertEquals(ExternalRuntime.class, runtime.class)
		
		assertFalse(runtime.canStart())
		BundleContext context = runtime.start()
		assertNull(context)
		assertTrue(runtime.isRunning())
		
		// ensure that the bundle files have been installed into dropins folder
		assertTrue(new File(tempDir, 'org.apache.felix.fileinstall-3.0.2.jar').exists())
		assertTrue(new File(tempDir, 'org.apache.felix.configadmin-1.2.4.jar').exists())
		
		assertFalse(runtime.canStop())
		
		context = runtime.bundleContext
		assertNull(context)
	}
	
	@Test
	void testGroovyScript() throws Exception {
		OsgiRuntimeBuilder.run("""
allBundles = []
allBundles << 'mvn:org.apache.felix:org.apache.felix.configadmin:1.2.4'

configure {
	// test alias
	framework 'equinox'

	runtimeDir 'system'

	args {
		resolverLogLevel = "warn"
	}
	
	println "bundles to install: " + allBundles
        
	allBundles.each { bdl ->
		println "Installing bundle " + bdl
		bundle bdl
	}
	
	doRun = {
		// return immediately, so that the runtime stops
	}
}
""")
	}
}
