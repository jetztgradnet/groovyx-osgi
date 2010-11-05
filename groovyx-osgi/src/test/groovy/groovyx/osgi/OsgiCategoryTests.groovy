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
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceReference
import org.osgi.framework.InvalidSyntaxException
import org.springframework.osgi.mock.MockBundleContext
import org.springframework.osgi.mock.MockServiceReference;
import org.springframework.osgi.mock.MockServiceRegistration;

class OsgiCategoryTests {
	BundleContext bundleContext

	@Before
	public void setUp() throws Exception {
		MockServiceRegistration reg1 = new MockServiceRegistration(props);
		MockServiceReference ref1 = new MockServiceReference();
		// TODO set mock BundleContext
		bundleContext = new MockBundleContext() {
			public ServiceReference[] getServiceReferences(String className, String filter) throws InvalidSyntaxException {
			}
			public Object getService(ServiceReference reference) {
			}
			public boolean ungetService(ServiceReference reference) {
			}
		}
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testFind() throws Exception {
		
	}
}
