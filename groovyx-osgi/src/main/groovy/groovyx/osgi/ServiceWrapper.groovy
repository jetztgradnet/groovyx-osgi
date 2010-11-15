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

/**
 * The {@link ServiceWrapper} provides convenience methods to
 * handle obtaining services from the OSGi service registry.
 * ServiceWrapper is used by {@link OsgiCategory}, when 
 * handling services with {@link BundleContext}.withEachService()
 * and variants.
 * 
 * <p>Example:
 * <pre>
 * def finder = new ServiceFinder(bundleContext)
 * ServiceWrapper services = finder.find {
 * 		className MyClass
 * 		filter "(&(name=groovy)(count=42))"
 * 		multiple()
 * }
 * services.withEachService { service, props -> 
 * 		println "Service properties: " + props
 * 		service?.doSomething()
 * }
 * </pre>
 * </p>
 * 
 * @author Wolfgang Schell
 */
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
	 * Get wrapped {@link ServiceReference}. If the wrapper contains more 
	 * than one reference, only the first reference is returned.
	 * 
	 * @return {@link ServiceReference} or <code>null</code>, if there are
	 * 				no service references
	 */
	ServiceReference getServiceReference() {
		if (serviceReferences.size()) {
			return serviceReferences[0]
		}
		null
	}
	
	/**
	 * Get wrapped array of {@link ServiceReference}s.
	 * 
	 * @return array of {@link ServiceReference}. 
	 * 			The array may be empty, but never <code>null</code>
	 * 
	 * @return
	 */
	ServiceReference[] getServiceReferences() {
		serviceReferences as ServiceReference[]
	}
	
	/**
	 * Determine whether this wrapper actually contains services (or rather
	 * {@link ServiceReference}s).
	 * 
	 * @return <code>true</code>, of the wrapper actually contains services, 
	 * 			<code>false</code> otherwise
	 */
	boolean hasServices() {
		serviceReferences.size() > 0
	}
	
	/**
	 * Get number of wrapped services (or rather {@link ServiceReference}s).
	 * 
	 * @return number of wrapped services
	 */
	int size() {
		serviceReferences.size()
	}
	
	/**
	 * Get number of wrapped services (or rather {@link ServiceReference}s).
	 * 
	 * @return number of wrapped services
	 */
	int getServiceCount() {
		serviceReferences.size()
	}

	/**
	 * Perform service action.
	 * 
	 * <p>
	 * The provided closure may receive one, two, or three args:
	 * 
	 * <ol>
	 * <li>service instance. May be <code>null</code>, if the service is (no longer) available</li>
	 * <li>service properties ({@link Map})</li>
	 * <li>user options ({@link Map})</li>
	 * </ol>
	 * </p>
	 * 
	 * <p>Example:
	 * <pre>
	 * use(OsgiCategory) {
	 * 	bundleContext.findService(MyService.class).withService { service, props ->
	 * 		println "Service properties: " + props
	 * 		service?.doSomething()
	 *	}
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
	
	/**
	 * Perform service action. This is the same as {@link #withEachService(Closure)}.
	 * 
	 * <p>
	 * The provided closure may receive one, two, or three args:
	 * 
	 * <ol>
	 * <li>service instance. May be <code>null</code>, if the service is (no longer) available</li>
	 * <li>service properties ({@link Map})</li>
	 * <li>user options ({@link Map})</li>
	 * </ol>
	 * </p>
	 * 
	 * <p>Example:
	 * <pre>
	 * use(OsgiCategory) {
	 * 	bundleContext.findService(MyService.class).withService { service, props ->
	 * 		println "Service properties: " + props
	 * 		service?.doSomething()
	 *	}
	 * }
	 * </pre>
	 * </p>
	 * 
	 * @param options map of options. See {@link OsgiCategory#withEachService(BundleContext, ServiceReference[], java.util.Map, Closure)} 
	 * 			for supported values
	 * @param closure service action closure
	 * 
	 * @return result of service action
	 */
	Object eachService(Map options, Closure closure) {
		if (!serviceReferences) {
			return null
		}
		ServiceReference serviceReference = serviceReferences[0]
		return OsgiCategory.withService(bundleContext, serviceReference, options ?: Collections.EMPTY_MAP, closure)
	}
	
	/**
	 * Perform action for each wrapped service.
	 * 
	 * <p>
	 * The provided closure may receive one, two, or three args:
	 * 
	 * <ol>
	 * <li>service instance. May be <code>null</code>, if the service is (no longer) available</li>
	 * <li>service properties ({@link Map})</li>
	 * <li>user options ({@link Map})</li>
	 * </ol>
	 * </p>
	 * 
	 * <p>Example:
	 * <pre>
	 * use(OsgiCategory) {
	 * 	bundleContext.findServices(MyService.class).withEachService { service, props ->
	 * 		println "Service properties: " + props
	 * 		service?.doSomething()
	 *	}
	 * }
	 * </pre>
	 * </p>
	 * 
	 * @param options map of options. See {@link OsgiCategory#withEachService(BundleContext, ServiceReference[], java.util.Map, Closure)} 
	 * 			for supported values
	 * @param closure service action closure
	 * 
	 * @return result of service action
	 */
	List eachService(Closure closure) {
		return withEachService(Collections.EMPTY_MAP, closure)
	}
	
	/**
	 * Perform service action. This is the same as {@link #withEachService(Map, Closure)}.
	 * 
	 * <p>
	 * The provided closure may receive one, two, or three args:
	 * 
	 * <ol>
	 * <li>service instance. May be <code>null</code>, if the service is (no longer) available</li>
	 * <li>service properties ({@link Map})</li>
	 * <li>user options ({@link Map})</li>
	 * </ol>
	 * </p>
	 * 
	 * <p>Example:
	 * <pre>
	 * use(OsgiCategory) {
	 * 	bundleContext.findService(MyService.class).withService { service, props ->
	 * 		println "Service properties: " + props
	 * 		service?.doSomething()
	 *	}
	 * }
	 * </pre>
	 * </p>
	 * 
	 * @param options map of options. See {@link OsgiCategory#withEachService(BundleContext, ServiceReference[], java.util.Map, Closure)} 
	 * 			for supported values
	 * @param closure service action closure
	 * 
	 * @return result of service action
	 */
	Object withService(Map options, Closure closure) {
		if (!serviceReferences) {
			return null
		}
		ServiceReference serviceReference = serviceReferences[0]
		return OsgiCategory.withService(bundleContext, serviceReference, options ?: Collections.EMPTY_MAP, closure)
	}
	
	/**
	 * Perform action for each wrapped service.
	 * 
	 * <p>
	 * The provided closure may receive one, two, or three args:
	 * 
	 * <ol>
	 * <li>service instance. May be <code>null</code>, if the service is (no longer) available</li>
	 * <li>service properties ({@link Map})</li>
	 * <li>user options ({@link Map})</li>
	 * </ol>
	 * </p>
	 * 
	 * <p>Example:
	 * <pre>
	 * use(OsgiCategory) {
	 * 	bundleContext.findServices(MyService.class).withEachService { service, props ->
	 * 		println "Service properties: " + props
	 * 		service?.doSomething()
	 *	}
	 * }
	 * </pre>
	 * </p>
	 * 
	 * @param options map of options. See {@link OsgiCategory#withEachService(BundleContext, ServiceReference[], java.util.Map, Closure)} 
	 * 			for supported values
	 * @param closure service action closure
	 * 
	 * @return result of service action
	 */
	List withEachService(Closure closure) {
		return withEachService(Collections.EMPTY_MAP, closure)
	}
	
	/**
	 * Perform action for each wrapped service. 
	 * 
	 * <p>
	 * The provided closure may receive one, two, or three args:
	 * 
	 * <ol>
	 * <li>service instance. May be <code>null</code>, if the service is (no longer) available</li>
	 * <li>service properties ({@link Map})</li>
	 * <li>user options ({@link Map})</li>
	 * </ol>
	 * </p>
	 * 
	 * <p>Example:
	 * <pre>
	 * use(OsgiCategory) {
	 * 	bundleContext.findServices(MyService.class).withEachService { service, props ->
	 * 		println "Service properties: " + props
	 * 		service?.doSomething()
	 *	}
	 * }
	 * </pre>
	 * </p>
	 * 
	 * @param options map of options. See {@link OsgiCategory#withEachService(BundleContext, ServiceReference[], java.util.Map, Closure)} 
	 * 			for supported values
	 * @param closure service action closure
	 * 
	 * @return result of service action
	 */
	List withEachService(Map options, Closure closure) {
		return OsgiCategory.withEachService(bundleContext, serviceReferences as ServiceReference[], options ?: Collections.EMPTY_MAP, closure)
	}
}
