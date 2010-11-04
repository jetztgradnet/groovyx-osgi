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

import java.util.Collections;

import groovy.lang.Closure;

import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceReference
import org.osgi.framework.ServiceRegistration


class ServiceWrapper {
	private final BundleContext bundleContext
	private final List<ServiceReference> serviceReferences
	
	ServiceWrapper(BundleContext bundleContext, ServiceReference serviceReference) {
		this.bundleContext = bundleContext
		this.serviceReferences = serviceReference ? Collections.unmodifiableList([serviceReference]) : Collections.EMPTY_LIST
	}
	
	ServiceWrapper(BundleContext bundleContext, ServiceReference[] serviceReferences) {
		this.bundleContext = bundleContext
		this.serviceReferences = Collections.unmodifiableList(serviceReferences as List)
	}
	
	/**
	 * Get wrapped {@link BundleContext}.
	 * 
	 * @return {@link BundleContext}
	 */
	BundleContext getBundleContext() {
		return bundleContext
	}
	
	/**
	 * Get {@link ServiceReference}. If the wrapper contains more 
	 * than one reference, the first reference is returned.
	 * 
	 * @return {@link ServiceReference} or <code>null</code>, if there are
	 * 				no service references
	 */
	ServiceReference getServiceReference() {
		if (serviceReferences.size()) {
			serviceReferences[0]
		}
		null
	}
	
	/**
	 * Get {@link List} of {@link ServiceReference}s.
	 * 
	 * @return unmodifiable {@link List}  of {@link ServiceReference}. 
	 * 			The list may be empty, but never <code>null</code>
	 * 
	 * @return
	 */
	List<ServiceReference> getServiceReferences() {
		serviceReferences
	}

	/**
	 * Perform service action. The provided closure may receive one, 
	 * two, or three args:
	 * 
	 * <ol>
	 * <li>service instance. May be <code>null</code>, if the service is (no longer) available</li>
	 * <li>service properties ({@link Map})</li>
	 * <li>user options ({@link Map})</li>
	 * </ol>
	 * 
	 * <p>Example:
	 * <pre>
	 * use(OsgiCategory) {
	 * 	bundleContext.findService(MyService.class).withService { service, props ->
	 * 		println "Service properties: " + props
	 * 		service?.doSomething()
	 * 	}
	 * }
	 * </pre>
	 * </p>
	 * 
	 * @param closure service action closure
	 * 
	 * @return result of service action
	 */
	Object withService(Closure closure) {
		return withService(Collections.EMPTY_MAP, closure)
	}
	
	Object withService(Map options, Closure closure) {
		if (!serviceReferences) {
			return null
		}
		ServiceReference serviceReference = serviceReferences[0]
		return OsgiCategory.withService(bundleContext, serviceReference, options ?: Collections.EMPTY_MAP, closure)
	}
	
	List withEachService(Closure closure) {
		return withEachService(Collections.EMPTY_MAP, closure)
	}
	
	List withEachService(Map options, Closure closure) {
		List results = []
		
		serviceReferences.each { ServiceReference serviceReference ->
			def result = OsgiCategory.withService(bundleContext, serviceReference, options ?: Collections.EMPTY_MAP, closure)
			if (result) {
				results << result
			}
		}
		
		return results
	}
}
