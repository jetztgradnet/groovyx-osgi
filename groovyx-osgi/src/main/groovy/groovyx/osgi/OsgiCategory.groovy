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

import java.util.Map;

import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceReference
import org.osgi.framework.ServiceRegistration

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
	
	
	static Object withService(ServiceReference serviceReference, BundleContext bundleContext, Map options, Closure closure) {
		return withService(bundleContext, serviceReference, options, closure)
	}
	
	static Object withService(BundleContext bundleContext, ServiceReference serviceReference, Map options, Closure closure) {
		Object result
		if (serviceReference != null) {
			// get service
			Object service = bundleContext.getService(serviceReference);
			try {
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
		case BundleContext.UNINSTALLED: return "UNINSTALLED"
		case BundleContext.INSTALLED:   return "INSTALLED"  
		case BundleContext.RESOLVED:    return "RESOLVED"   
		case BundleContext.STARTING:    return "STARTING"   
		case BundleContext.STOPPING:    return "STOPPING"   
		case BundleContext.ACTIVE:      return "ACTIVE"     
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
}
