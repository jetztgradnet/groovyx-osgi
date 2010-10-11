package groovyx.osgi.runtime;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*

public class OsgiBuilderTest {
	OsgiBuilder builder
	
	@Before
	def setUp() {
		builder = new OsgiBuilder()
	}
	
	@Test
	public void testSimpleBuilder() throws Exception {
		def springVersion = '3.0.3.RELEASE'
		builder.bundles {
			bundle "org.springframework:org.springframework.aop:$springVersion"
		}
		
		def bundles = builder.bundles
		assertEquals(1, bundles.size())
	}
}
