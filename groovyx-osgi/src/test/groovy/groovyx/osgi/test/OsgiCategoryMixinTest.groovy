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

import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceRegistration

import groovyx.osgi.OsgiCategory
import groovyx.osgi.ServiceWrapper
import groovyx.osgi.ServiceFinder

/**
 * Integration tests for OsgiCategory.
 *
 * NOTE: the tests require the main classes to be jar'ed up and
 * placed at ./build/libs/groovyx.osgi-version.jar!!! See {@link #getTestBundles()}
 * 
 * TODO mixing in this does not currently work (see {@link #onSetUp()}), therefore
 * it's abstract to prevent execution...
 *
 * @author Wolfgang Schell
 */
class OsgiCategoryMixinTest extends AbstractGroovyxOsgiTests {
	
	protected Map<String, Object> registeredServices
	protected List<ServiceRegistration> serviceRegistrations
	
	protected void onSetUp() throws Exception {
		super.onSetUp();
		
		// create some services
		registeredServices = [:]
		serviceRegistrations = []
		[ 
			// property 'since': year of invention
			[ "Groovy", 54, [ level: 'easy', jvm: true, since: 2003 ] ],
			[ "JRuby", 74, [ level: 'easy', jvm: true, since: 2001 ]  ],
			[ "Scala", 90, [ level: 'advanced', jvm: true, since: 2003 ]  ],
			[ "Java", 2006, [ level: 'medium', jvm: true, since: 1995 ]  ],
			[ "C++", 2010, [ level: 'dificult', jvm: false, since: 1979 ]  ],
		].collect { data ->
			String name
			int number
			Map properties
			(name, number, properties) = data
			MyService service = new MyService(name, number)
			ServiceRegistration registration = bundleContext.registerService(MyService.class.name, 
																				service, 
																				properties ? OsgiCategory.dictionaryFromMap(properties) : null)
			
			registeredServices[service.name] = service
			serviceRegistrations << registration
		}
	}
	
	@Override
	protected void onTearDown() throws Exception {
		serviceRegistrations.each { ServiceRegistration registration ->
			registration.unregister()
		}
		serviceRegistrations.clear()
		registeredServices.clear()
		
		super.onTearDown();
	}
	
	public void testLoadGroovyxOsgi() throws Exception {
		assertNotNull(bundleContext)
		def found = bundleContext.bundles.find { bundle -> 'groovyx.osgi' == bundle.symbolicName  }
		assertNotNull('Bundle groovyx-osgi is not loaded', found)
		
		assertEquals('Bundle groovyx-osgi is not active', "ACTIVE", found.stateAsText)
	}
	
	public void testFindServiceNoResult() throws Exception {
		ServiceWrapper wrapper
		List results
		wrapper = bundleContext.findService(MyOtherService)
		assertNotNull('Service wrapper should exist', wrapper)
		assertNotNull('BundleContext should be available', wrapper.bundleContext)
		assertEquals(0, wrapper.size())
		assertEquals(0, wrapper.serviceCount)
		assertNull(wrapper.serviceReference)
		assertNotNull(wrapper.serviceReferences)
		assertEquals(0, wrapper.serviceReferences.length)
	}
	
	public void testFindServicesNoResult() throws Exception {
		ServiceWrapper wrapper
		List results
		wrapper = bundleContext.findServices(MyOtherService)
		assertNotNull('Service wrapper should exist', wrapper)
		assertNotNull('BundleContext should be available', wrapper.bundleContext)
		assertEquals(0, wrapper.size())
		assertEquals(0, wrapper.serviceCount)
		assertNull(wrapper.serviceReference)
		assertNotNull(wrapper.serviceReferences)
		assertEquals(0, wrapper.serviceReferences.length)
	}
	
	public void testFindSingleService() throws Exception {
		def result
		result = bundleContext.findService(MyService).withService() { MyService srv ->
			[ srv.name, srv.doSomething() ]
		}
		String name
		def data
		(name, data) = result
		assertNotNull('Service result should exist', result)
		assertEquals(registeredServices[name]?.number + 42, data)
	}
	
	public void testFindMultipleServices() throws Exception {
		ServiceWrapper wrapper
		wrapper = bundleContext.findServices(MyService)
		assertNotNull('Service wrapper should exist', wrapper)
		assertNotNull('BundleContext should be available', wrapper.bundleContext)
		assertEquals('wrapper should reference services from abvove', registeredServices.size(), wrapper.serviceCount)
		assertEquals('wrapper should reference services from abvove', registeredServices.size(), wrapper.size())
		assertNotNull('first service reference should exists', wrapper.serviceReference)
		assertNotNull('service references should be valid', wrapper.serviceReferences)
		assertEquals('service references should match service from above', registeredServices.size(), wrapper.serviceReferences.length)
	}
	
	public void testFindMultipleServicesWithResult() throws Exception {
		List results
		results = bundleContext.findServices(MyService).withEachService() { MyService srv ->
			srv.doSomething()
		}
		assertNotNull('Service results should exist', results)
		assertFalse('Service results should not be empty', results.isEmpty())
		assertEquals('There should be one result per service', registeredServices.size(), results.size())
	}
	
	public void testFindMultipleServicesFiltered() throws Exception {
		ServiceWrapper wrapper
		wrapper = bundleContext.findServices(MyService, "(since <= 2000)")
		assertNotNull('Service wrapper should exist', wrapper)
		assertNotNull('BundleContext should be available', wrapper.bundleContext)
		assertEquals('wrapper should reference filtered services from abvove', 2, wrapper.serviceCount)
		assertEquals('wrapper should reference filtered services from abvove', 2, wrapper.size())
		assertNotNull('first service reference should exists', wrapper.serviceReference)
		assertNotNull('service references should be valid', wrapper.serviceReferences)
		assertEquals('service references should match service from above', 2, wrapper.serviceReferences.length)
	}
	
	public void testFindMultipleServicesFilteredWithResult() throws Exception {
		List results
		results = bundleContext.findServices(MyService, "(since <= 2000)").withEachService() { MyService srv ->
			srv.doSomething()
		}
		assertNotNull('Service results should exist', results)
		assertFalse('Service results should not be empty', results.isEmpty())
		assertEquals('There should be one result per service', 2, results.size())
	}
	
	public void testFindMultipleServicesFilterDSL() throws Exception {
		ServiceWrapper wrapper
		wrapper = bundleContext.findServices(MyService) {
			filter {
				lte('since', 2000) 
			}
		}
		assertNotNull('Service wrapper should exist', wrapper)
		assertNotNull('BundleContext should be available', wrapper.bundleContext)
		assertEquals('wrapper should reference filtered services from abvove', 2, wrapper.serviceCount)
		assertEquals('wrapper should reference filtered services from abvove', 2, wrapper.size())
		assertNotNull('first service reference should exists', wrapper.serviceReference)
		assertNotNull('service references should be valid', wrapper.serviceReferences)
		assertEquals('service references should match service from above', 2, wrapper.serviceReferences.length)
	}
	
	public void testFindMultipleServicesFilterDSLWithResult() throws Exception {
		List results
		results = bundleContext.findServices(MyService) {
			filter {
				lte('since', 2000) 
			}
		}.withEachService() { MyService srv ->
			srv.doSomething()
		}
		assertNotNull('Service results should exist', results)
		assertFalse('Service results should not be empty', results.isEmpty())
		assertEquals('There should be one result per service', 2, results.size())
	}
}
