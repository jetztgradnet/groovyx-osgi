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

import groovy.lang.Closure;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceReference
import org.osgi.framework.ServiceRegistration

/**
 * The Osgi Category provides many convenience methods for instances
 * of {@link Bundle}, {@link BundleContext}, and {@link ServiceRegistration}, 
 * mainly regarding searching for and working with services.
 * 
 * <p>
 * {@link #findService(BundleContext, Class)} and its variants can be used to 
 * get services from the OSGi service registry. Search parameters like filters
 * can be set using a DSL. The return value is an instance of {@link ServiceWrapper},
 * which wraps the {@link BundleContext} and all found services (or rather their
 * corresponding {@link ServiceReference}).
 * </p>
 * 
 * <p>
 * {@link ServiceWrapper} provides some convenient methods to work with the service. 
 * It transparently handles getting and returning the service instance from the 
 * {@link ServiceRegistration} and make sure, that no resources are leaked. The
 * service and optionally the service properties are handed to a user-provided
 * {@link Closure}, which performs some service-related action.
 * </p>
 * 
 * <p>Example:
 * <pre>
 * use(OsgiCategory) {
 * 	def services = bundleContext.findServices(MyServiceClass, "someprop=42")
 * 	services.withEachService(callForNullService: true) { service, props ->
 * 		println "Service properties: " + props
 * 		service?.doSomething()
 *	}
 * }
 * </pre>
 * </p>
 * 
 * @author Wolfgang Schell
 */
class OsgiCategory {
	
	static ServiceWrapper findService(BundleContext bundleContext, Closure closure) {
		new ServiceFinder(bundleContext)
				.single()
				.configure(closure)
				.find()
	}
	
	static ServiceWrapper findService(BundleContext bundleContext, Map options, Closure closure) {
		new ServiceFinder(bundleContext)
				.single()
				.configure(options, closure)
				.find()
	}
	
	static ServiceWrapper findService(BundleContext bundleContext, String className) {
		new ServiceFinder(bundleContext)
				.single()
				.setClassName(className)
				.find()
	}
	
	static ServiceWrapper findService(BundleContext bundleContext, String className, String filter) {
		new ServiceFinder(bundleContext)
				.single()
				.setClassName(className)
				.setFilter(filter)
				.find()
	}
	
	static ServiceWrapper findService(BundleContext bundleContext, Class clazz) {
		new ServiceFinder(bundleContext)
				.single()
				.setClassName(clazz)
				.find()
	}
	
	static ServiceWrapper findService(BundleContext bundleContext, Class clazz, String filter) {
		new ServiceFinder(bundleContext)
				.single()
				.setClassName(clazz)
				.setFilter(filter)
				.find()
	}
	
	static ServiceWrapper findService(BundleContext bundleContext, Map options) {
		new ServiceFinder(bundleContext)
				.single()
				.configure(options)
				.find()
	}
	
	static ServiceWrapper findServices(BundleContext bundleContext, Closure closure) {
		new ServiceFinder(bundleContext)
				.multiple()
				.configure(closure)
				.find()
	}
	
	static ServiceWrapper findServices(BundleContext bundleContext, Map options, Closure closure) {
		new ServiceFinder(bundleContext)
				.multiple()
				.configure(options, closure)
				.find()
	}
	
	static ServiceWrapper findServices(BundleContext bundleContext, String className) {
		new ServiceFinder(bundleContext)
				.multiple()
				.setClassName(className)
				.find()
	}
	
	static ServiceWrapper findServices(BundleContext bundleContext, String className, String filter) {
		new ServiceFinder(bundleContext)
				.multiple()
				.setClassName(className)
				.setFilter(filter)
				.find()
	}
	
	static ServiceWrapper findServices(BundleContext bundleContext, Class clazz) {
		new ServiceFinder(bundleContext)
				.multiple()
				.setClassName(clazz)
				.find()
	}
	
	static ServiceWrapper findServices(BundleContext bundleContext, Class clazz, String filter) {
		new ServiceFinder(bundleContext)
				.multiple()
				.setClassName(clazz)
				.setFilter(filter)
				.find()
	}
	
	static ServiceWrapper findServices(BundleContext bundleContext, Map options) {
		new ServiceFinder(bundleContext)
				.multiple()
				.configure(options)
				.find()
	}
	
	/**
	 * Perform action for a service. If a service is not available, the closure
	 * will not be called. This behavior can be changed by calling 
	 * {@link #withService(ServiceReference, BundleContext, Map, Closure)} with 
	 * option <code>callForNullService</code> set to <code>true</code>.
	 * 
	 * <p>
	 * The provided closure may receive one, two, or three args:
	 * 
	 * <ol>
	 * <li>service instance</li>
	 * <li>service properties ({@link Map})</li>
	 * <li>user options ({@link Map})</li>
	 * </ol>
	 * </p>
	 * 
	 * <p>Example:
	 * <pre>
	 * use(OsgiCategory) {
	 * 	ServiceReference serviceReference = ...
	 * 	serviceReference.withService(bundleContext) { service, props ->
	 * 		println "Service properties: " + props
	 * 		service?.doSomething()
	 *	}
	 * }
	 * </pre>
	 * </p>
	 * 
	 * @param serviceReference references to service
	 * @param bundleContext	bundle context
	 * @param closure service action closure
	 * 
	 * @return result of service action
	 */
	static Object withService(ServiceReference serviceReference, BundleContext bundleContext, Closure closure) {
		return withService(bundleContext, serviceReference, closure)
	}
	
	/**
	 * Perform action for a service.
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
	 * 	ServiceReference serviceReference = ...
	 * 	serviceReference.withService(bundleContext, callForNullService: true) { service, props ->
	 * 		println "Service properties: " + props
	 * 		service?.doSomething()
	 *	}
	 * }
	 * </pre>
	 * </p>
	 * 
	 * @param serviceReference references to service
	 * @param bundleContext	bundle context
	 * @param options map of options. See {@link #withEachService(BundleContext, ServiceReference[], Map, Closure)} for supported options
	 * @param closure service action closure
	 * 
	 * @return result of service action
	 */
	static Object withService(ServiceReference serviceReference, BundleContext bundleContext, Map options, Closure closure) {
		return withService(bundleContext, serviceReference, options, closure)
	}
	
	/**
	 * Perform action for a service. If a service is not available, the closure
	 * will not be called. This behavior can be changed by calling 
	 * {@link #withService(BundleContext, ServiceReference, Map, Closure)} with 
	 * option <code>callForNullService</code> set to <code>true</code>.
	 * 
	 * <p>
	 * The provided closure may receive one, two, or three args:
	 * 
	 * <ol>
	 * <li>service instance</li>
	 * <li>service properties ({@link Map})</li>
	 * <li>user options ({@link Map})</li>
	 * </ol>
	 * </p>
	 * 
	 * <p>Example:
	 * <pre>
	 * use(OsgiCategory) {
	 * 	ServiceReference serviceReference = ...
	 * 	bundleContext.withService(serviceReference) { service, props ->
	 * 		println "Service properties: " + props
	 * 		service?.doSomething()
	 *	}
	 * }
	 * </pre>
	 * </p>
	 * 
	 * @param bundleContext	bundle context
	 * @param serviceReference references to service
	 * @param closure service action closure
	 * 
	 * @return result of service action
	 */
	static Object withService(BundleContext bundleContext, ServiceReference serviceReference, Closure closure) {
		return withService(bundleContext, serviceReference, Collections.EMPTY_MAP, closure)
	}
	
	/**
	 * Perform action for a service.
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
	 * 	ServiceReference serviceReference = ...
	 * 	bundleContext.withService(serviceReference, callForNullService: true) { service, props ->
	 * 		println "Service properties: " + props
	 * 		service?.doSomething()
	 *	}
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param bundleContext	bundle context
	 * @param serviceReference references to service
	 * @param options map of options. See {@link #withEachService(BundleContext, ServiceReference[], Map, Closure)} for supported options
	 * @param closure service action closure
	 * 
	 * @return result of service action
	 */
	static Object withService(BundleContext bundleContext, ServiceReference serviceReference, Map options, Closure closure) {
		Object result
		
		if (options == null) {
			options = Collections.EMPTY_MAP
		}
		
		if (serviceReference != null) {
			// get service
			Object service = bundleContext.getService(serviceReference);
			try {
				if ((service != null) || options.callForNullService) {
					// call closure with service [and properties] as arg(s)
					switch (closure.getMaximumNumberOfParameters()) {
						case 3:
							// params: service, service properties, options
							result = closure.call(service, mapFromServiceProperties(serviceReference), options ?: [:])
							break
						case 2:
							// params: service, service properties
							result = closure.call(service, mapFromServiceProperties(serviceReference))
							break
						case 1:
						default:
							// params: service
							result = closure.call(service)
							break
					}
				}
			}
			finally {
				if (service != null) {
					bundleContext.ungetService(serviceReference);
				}
			}
		}
		else {
			return null
		}
		
		return result
	}
	
	/**
	 * Perform action for each service. If a service is not available, the closure
	 * will not be called. This behavior can be changed by calling 
	 * {@link #withEachService(ServiceReference[], BundleContext, Map, Closure)} with 
	 * option <code>callForNullService</code> set to <code>true</code>.
	 * 
	 * <p>This is the same as {@link #withEachService(ServiceReference[], BundleContext, Closure)}.</p>
	 * 
	 * <p>
	 * The provided closure may receive one, two, or three args:
	 * 
	 * <ol>
	 * <li>service instance</li>
	 * <li>service properties ({@link Map})</li>
	 * <li>user options ({@link Map})</li>
	 * </ol>
	 * </p>
	 * 
	 * <p>Example:
	 * <pre>
	 * use(OsgiCategory) {
	 * 	ServiceReference[] serviceReferences = ...
	 * 	serviceReferences.withEachService(bundleContext) { service, props ->
	 * 		println "Service properties: " + props
	 * 		service?.doSomething()
	 *	}
	 * }
	 * </pre>
	 * </p>
	 * 
	 * @param serviceReferences references to services
	 * @param bundleContext	bundle context
	 * @param closure service action closure
	 * 
	 * @return list of results of service action
	 */
	static List eachService(ServiceReference[] serviceReferences, BundleContext bundleContext, Closure closure) {
		return withEachService(bundleContext, serviceReferences, closure)
	}
	
	/**
	 * Perform action for each service.
	 * 
	 * <p>This is the same as {@link #withEachService(ServiceReference[], BundleContext, Map, Closure)}.</p>
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
	 * 	ServiceReference[] serviceReferences = ...
	 * 	serviceReferences.withEachService(bundleContext, callForNullService: true) { service, props ->
	 * 		println "Service properties: " + props
	 * 		service?.doSomething()
	 *	}
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param bundleContext	bundle context
	 * @param serviceReferences references to services
	 * @param options map of options. See {@link #withEachService(BundleContext, ServiceReference[], Map, Closure)} for supported options
	 * @param closure service action closure
	 *
	 * @return list of results of service action
	 */
	static List eachService(ServiceReference[] serviceReferences, BundleContext bundleContext, Map options, Closure closure) {
		return withEachService(bundleContext, serviceReferences, options, closure)
	}
	
	/**
	 * Perform action for each service. If a service is not available, the closure
	 * will not be called. This behavior can be changed by calling 
	 * {@link #withEachService(BundleContext, ServiceReference[], Map, Closure)} with 
	 * option <code>callForNullService</code> set to <code>true</code>.
	 * 
	 * <p>This is the same as {@link #withEachService(BundleContext, ServiceReference[], Closure)}.</p>
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
	 * 	ServiceReference[] serviceReferences = ...
	 * 	bundleContext.withEachService(serviceReferences) { service, props ->
	 * 		println "Service properties: " + props
	 * 		service?.doSomething()
	 *	}
	 * }
	 * </pre>
	 * </p>
	 * 
	 * @param bundleContext	bundle context
	 * @param serviceReferences references to services
	 * @param closure service action closure
	 * 
	 * @return list of results of service action
	 */
	static List eachService(BundleContext bundleContext, ServiceReference[] serviceReferences, Closure closure) {
		return withEachService(bundleContext, serviceReferences, Collections.EMPTY_MAP, closure)
	}
	
	/**
	 * Perform action for each service.
	 * 
	 * <p>This is the same as {@link #withEachService(BundleContext, ServiceReference[], Map, Closure)}.</p>
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
	 * <p>
	 * Supported options: TODO
	 * <ul>
	 * <li><b>collectNullResults</b>: if <code>true</code>, <code>null</code> 
	 * 		values returned by the service action are put into the result list. 
	 * 		By default, <code>null</code> results are skipped.</li>
	 * <li><b>callForNullService</b>: if <code>true</code>, the service action
	 * 		closure will be called, if the service reference returns a <code>null</code>
	 * 		service, e.g. because the service is no longer available. By default,
	 * 		the service action closure will not be called, if the service resolves
	 * 		to <code>null</code>.</li>
	 * </ul>
	 * </p>
	 *
	 * <p>Example:
	 * <pre>
	 * use(OsgiCategory) {
	 * 	ServiceReference[] serviceReferences = ...
	 * 	bundleContext.withEachService(serviceReferences, callForNullService: true) { service, props ->
	 * 		println "Service properties: " + props
	 * 		service?.doSomething()
	 *	}
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param bundleContext	bundle context
	 * @param serviceReferences references to services
	 * @param options map of options. See above for supported options
	 * @param closure service action closure
	 *
	 * @return list of results of service action
	 */
	static List eachService(BundleContext bundleContext, ServiceReference[] serviceReferences, Map options, Closure closure) {
		withEachService(bundleContext, serviceReferences, options, closure)
	}
	
	/**
	 * Perform action for each service. If a service is not available, the closure
	 * will not be called. This behavior can be changed by calling 
	 * {@link #withEachService(ServiceReference[], BundleContext, Map, Closure)} with 
	 * option <code>callForNullService</code> set to <code>true</code>. 
	 * 
	 * <p>
	 * The provided closure may receive one, two, or three args:
	 * 
	 * <ol>
	 * <li>service instance</li>
	 * <li>service properties ({@link Map})</li>
	 * <li>user options ({@link Map})</li>
	 * </ol>
	 * </p>
	 * 
	 * <p>Example:
	 * <pre>
	 * use(OsgiCategory) {
	 * 	ServiceReference[] serviceReferences = ...
	 * 	serviceReferences.withEachService(bundleContext) { service, props ->
	 * 		println "Service properties: " + props
	 * 		service?.doSomething()
	 *	}
	 * }
	 * </pre>
	 * </p>
	 * 
	 * @param serviceReferences references to services
	 * @param bundleContext	bundle context
	 * @param closure service action closure
	 * 
	 * @return list of results of service action
	 */
	static List withEachService(ServiceReference[] serviceReferences, BundleContext bundleContext, Closure closure) {
		return withEachService(bundleContext, serviceReferences, closure)
	}
	
	/**
	 * Perform action for each service.
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
	 * 	ServiceReference[] serviceReferences = ...
	 * 	serviceReferences.withEachService(bundleContext, callForNullService: true) { service, props ->
	 * 		println "Service properties: " + props
	 * 		service?.doSomething()
	 *	}
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param bundleContext	bundle context
	 * @param serviceReferences references to services
	 * @param options map of options. See {@link #withEachService(BundleContext, ServiceReference[], Map, Closure)} for supported options
	 * @param closure service action closure
	 *
	 * @return list of results of service action
	 */
	static List withEachService(ServiceReference[] serviceReferences, BundleContext bundleContext, Map options, Closure closure) {
		return withEachService(bundleContext, serviceReferences, options, closure)
	}
	
	/**
	 * Perform action for each service. If a service is not available, the closure
	 * will not be called. This behavior can be changed by calling 
	 * {@link #withEachService(BundleContext, ServiceReference[], Map, Closure)} with 
	 * option <code>callForNullService</code> set to <code>true</code>.
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
	 * 	ServiceReference[] serviceReferences = ...
	 * 	bundleContext.withEachService(serviceReferences) { service, props ->
	 * 		println "Service properties: " + props
	 * 		service?.doSomething()
	 *	}
	 * }
	 * </pre>
	 * </p>
	 * 
	 * @param bundleContext	bundle context
	 * @param serviceReferences references to services
	 * @param closure service action closure
	 * 
	 * @return list of results of service action
	 */
	static List withEachService(BundleContext bundleContext, ServiceReference[] serviceReferences, Closure closure) {
		return withEachService(bundleContext, serviceReferences, Collections.EMPTY_MAP, closure)
	}
	
	/**
	 * Perform action for each service.
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
	 * <p>
	 * Supported options: TODO
	 * <ul>
	 * <li><b>collectNullResults</b>: if <code>true</code>, <code>null</code> 
	 * 		values returned by the service action are put into the result list. 
	 * 		By default, <code>null</code> results are skipped.</li>
	 * <li><b>callForNullService</b>: if <code>true</code>, the service action
	 * 		closure will be called, if the service reference returns a <code>null</code>
	 * 		service, e.g. because the service is no longer available. By default,
	 * 		the service action closure will not be called, if the service resolves
	 * 		to <code>null</code>.</li>
	 * </ul>
	 * </p>
	 *
	 * <p>Example:
	 * <pre>
	 * use(OsgiCategory) {
	 * 	ServiceReference[] serviceReferences = ...
	 * 	bundleContext.withEachService(serviceReferences, callForNullService: true) { service, props ->
	 * 		println "Service properties: " + props
	 * 		service?.doSomething()
	 *	}
	 * }
	 * </pre>
	 * </p>
	 *
	 * @param bundleContext	bundle context
	 * @param serviceReferences references to services
	 * @param options map of options. See above for supported options
	 * @param closure service action closure
	 *
	 * @return list of results of service action
	 */
	static List withEachService(BundleContext bundleContext, ServiceReference[] serviceReferences, Map options, Closure closure) {
		List results = []
		
		if (options == null) {
			options = Collections.EMPTY_MAP
		}
		
		serviceReferences.each { ServiceReference serviceReference ->
			def result = withService(bundleContext, serviceReference, options, closure)
			if (result || options?.collectNullResults) {
				results << result
			}
		}
		
		return results
	}
	
	/**
	 * Get bundle state as text.
	 * 
	 * @param bundleContext context of bundle of which to get state
	 * 
	 * @return state as text
	 */
	static String getStateAsText(BundleContext bundleContext) {
		return getStateAsText(bundleContext.bundle)
	}
	
	/**
	 * Get bundle state as text.
	 *
	 * @param bundle bundle of which to get state
	 *
	 * @return state as text
	 */
	static String getStateAsText(Bundle bundle) {
		def state = "UNKNOWN"
		switch (bundle.state) {
		case Bundle.UNINSTALLED: return "UNINSTALLED"
		case Bundle.INSTALLED:   return "INSTALLED"  
		case Bundle.RESOLVED:    return "RESOLVED"   
		case Bundle.STARTING:    return "STARTING"   
		case Bundle.STOPPING:    return "STOPPING"   
		case Bundle.ACTIVE:      return "ACTIVE"     
		}
	}
	
	/**
	 * Get map of service properties from {@link ServiceReference}.
	 * 
	 * @param serviceReference reference of which to get properties
	 * 
	 * @return map of service properties
	 */
	static Map mapFromServiceProperties(ServiceReference serviceReference) {
		Map props = [:]
		
		serviceReference.getPropertyKeys().each { name ->
			props[(name)] = serviceReference.getProperty(name)
		}
		
		props
	}
	
	/**
	 * Create a {@link Dictionary} from a {@link Map}. The {@link Dictionary}
	 * is used for specifying service properties.
	 * 
	 * @param properties properties map
	 * 
	 * @return {@link Dictionary} containing the map's data
	 */
	static Dictionary dictionaryFromMap(Map params) {
		return new Hashtable(params)
	}
}
