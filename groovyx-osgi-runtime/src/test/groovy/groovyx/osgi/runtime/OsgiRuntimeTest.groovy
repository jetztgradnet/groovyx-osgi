package groovyx.osgi.runtime;

import org.codehaus.groovy.osgi.runtime.equinox.EquinoxRuntimeFactory;
import org.codehaus.groovy.osgi.runtime.equinox.EquinoxRuntime;
import org.codehaus.groovy.osgi.runtime.felix.FelixRuntimeFactory;
import org.codehaus.groovy.osgi.runtime.felix.FelixRuntime;

import org.osgi.framework.BundleContext;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*

public class OsgiRuntimeTest {
	OsgiRuntimeBuilder builder
	
	@Before
	void setUp() {
		builder = new OsgiRuntimeBuilder()
	}
	
	@Test
	void testStartEquinox() throws Exception {
		OsgiRuntime runtime = builder.build {
			// test alias
			framework 'equinox'

			runtimeDir 'system'

			args {
				resolverLogLevel = "debug"
			}

			bundle 'mvn:org.apache.felix:org.apache.felix.fileinstall:3.0.2'
			bundle group: 'org.apache.felix', name:'org.apache.felix.configadmin', version:'1.2.4'
		}
		assertNotNull(runtime)
		assertEquals(EquinoxRuntime.class, runtime.class)
		
		BundleContext context = runtime.startBundle()
		assertNotNull(context)
		assertTrue(runtime.isRunning())
		
		// TODO do something with bundle context		
		
		runtime.stop()
		assertFalse(runtime.isRunning())
		
		context = runtime.bundleContext
		assertNull(context)
	}
}
