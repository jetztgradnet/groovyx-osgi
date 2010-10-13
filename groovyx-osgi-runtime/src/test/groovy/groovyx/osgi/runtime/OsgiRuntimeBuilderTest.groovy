package groovyx.osgi.runtime;

import org.codehaus.groovy.osgi.runtime.equinox.EquinoxRuntimeFactory;
import org.codehaus.groovy.osgi.runtime.equinox.EquinoxRuntime;
import org.codehaus.groovy.osgi.runtime.felix.FelixRuntimeFactory;
import org.codehaus.groovy.osgi.runtime.felix.FelixRuntime;

import org.osgi.framework.BundleContext;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*

public class OsgiRuntimeBuilderTest {
	OsgiRuntimeBuilder builder
	
	@Before
	void setUp() {
		builder = new OsgiRuntimeBuilder()
	}
	
	@Test
	void testSimpleBuilder() throws Exception {
		def springVersion = '3.0.3.RELEASE'
		builder.bundles {
			bundle "org.springframework:org.springframework.aop:$springVersion"
		}
		
		def bundles = builder.bundles
		assertEquals(1, bundles.size())
		assertEquals("org.springframework:org.springframework.aop:$springVersion".toString(), bundles[0])
	}
		
	@Test
	void testEquinoxFactoryAlias() throws Exception {
		builder.configure {
			// test alias
			framework 'equinox'

			bundle 'mvn:org.apache.felix:org.apache.felix.fileinstall:3.0.2'
			bundle group: 'org.apache.felix', name:'org.apache.felix.configadmin', version:'1.2.4'
		}
		OsgiRuntimeFactory factory = builder.getOsgiRuntimeFactory(builder.framework)
		
		def bundles = builder.bundles
		assertEquals(2, bundles.size())
		assertEquals(EquinoxRuntimeFactory.class, factory.class)
	}
	
	@Test
	void testEquinoxFactoryClass() throws Exception {
		builder.configure {
			// test alias
			framework EquinoxRuntimeFactory.class
		}
		OsgiRuntimeFactory factory = builder.getOsgiRuntimeFactory(builder.framework)
		
		assertEquals(EquinoxRuntimeFactory.class, factory.class)
	}
	
	@Test
	void testEquinoxFactoryClassName() throws Exception {
		builder.configure {
			// test alias
			framework EquinoxRuntimeFactory.class.name
		}
		OsgiRuntimeFactory factory = builder.getOsgiRuntimeFactory(builder.framework)
		
		assertEquals(EquinoxRuntimeFactory.class, factory.class)
	}
	
	@Test
	void testFelixFactoryAlias() throws Exception {
		builder.configure {
			// test alias
			framework 'Felix'
		}
		OsgiRuntimeFactory factory = builder.getOsgiRuntimeFactory(builder.framework)
		
		assertEquals(FelixRuntimeFactory.class, factory.class)
	}
	
	@Test
	void testFelixFactoryClass() throws Exception {
		builder.configure {
			// test alias
			framework FelixRuntimeFactory.class
		}
		OsgiRuntimeFactory factory = builder.getOsgiRuntimeFactory(builder.framework)
		
		assertEquals(FelixRuntimeFactory.class, factory.class)
	}
	
	@Test
	void testFelixFactoryClassName() throws Exception {
		builder.configure {
			// test alias
			framework FelixRuntimeFactory.class.name
		}
		OsgiRuntimeFactory factory = builder.getOsgiRuntimeFactory(builder.framework)
		
		assertEquals(FelixRuntimeFactory.class, factory.class)
	}
}
