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
}
