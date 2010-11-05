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

import groovy.lang.Closure

import java.util.Collections
import java.util.Map

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext
import org.osgi.framework.Filter;
import org.osgi.framework.ServiceReference

import net.jetztgrad.groovy.osgi.Filter4OsgiBuilder


/**
 * The {@link ServiceFinder} provides convenience methods to 
 * find one or many OSGi services. It can be used as a simple
 * search DSL. ServiceFinder is used by {@link OsgiCategory},
 * when searching services with {@link BundleContext}.findServices()
 * and variants.
 * 
 * <p>Example:
 * <pre>
 * def finder = new ServiceFinder(bundleContext)
 * def services = finder.find {
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
class ServiceFinder {
	private final BundleContext bundleContext

	String className
	String filter
	boolean all = false
	boolean multiple = true
	
	ServiceFinder(BundleContext bundleContext) {
		this.bundleContext = bundleContext
	}
	
	/**
	* Get wrapped {@link BundleContext}.
	*
	* @return {@link BundleContext}
	*/
	BundleContext getBundleContext() {
		bundleContext
	}
	
	/**
	 * Configure {@link ServiceFinder} using the built-in DSL.
	 * 
	 * @param closure config closure
	 * 
	 * @return the service finder
	 */
	ServiceFinder configure(Closure closure) {
		Closure cl = closure.clone()
		cl.delegate = this
		cl()
		
		return this
	}
	
	/**
	 * Configure {@link ServiceFinder} using options from a {@link Map}.
	 * 
	 * @param options map of options
	 * 
	 * @return the service finder
	 */
	ServiceFinder configure(Map options) {
		if (!options) {
			return this
		}
		
		if (options?.single) {
			single()
		}
		
		if (options?.multiple) {
			multiple()
		}
		
		if (options?.all) {
			all()
		}
		
		// set class name
		if (options?.className) {
			setClassName(options?.className)
		}
		
		// set filter
		if (options?.filter) {
			setFilter(options?.filter)
		}
		
		return this
	}
	
	/**
	 * Configure {@link ServiceFinder} using both the built-in DSL and
	 * options from a {@link Map}.
	 * 
	 * @param options map of options
	 * @param closure config closure
	 * 
	 * @return the service finder
	 */
	ServiceFinder configure(Map options, Closure closure) {
		configure(options)
		configure(closure)
		
		return this
	}
	
	/**
	 * Determine whether to return all service instance, even those incompatible
	 * with a {@link Bundle}. See {@link BundleContext#getAllServiceReferences(String, String)}.
	 * 
	 * @return <code>true</code>, if all services are returned, <code>false</code> if
	 * 		only compatible services are returned
	 */
	boolean getAll() {
		all
	}
	
	/**
	 * Specify, that all service instance are returned, even those incompatible
	 * with a {@link Bundle}. See {@link BundleContext#getAllServiceReferences(String, String)}.
	 * 
	 * @return the service finder
	 */
	ServiceFinder all() {
		setAll(true)
		
		return this
	}

	/**
	 * Specify, whether all service instance are returned, even those incompatible
	 * with a {@link Bundle}. See {@link BundleContext#getAllServiceReferences(String, String)}.
	 * 
	 * @param all if <code>true</code>, all services are returned, if <code>false</code>
	 * 		only compatible services are returned
	 * 
	 * @return the service finder	
	 */
	ServiceFinder all(boolean all) {
		setAll(all)
		
		return this
	}
	
	/**
	 * Specify, whether all service instance are returned, even those incompatible
	 * with a {@link Bundle}. See {@link BundleContext#getAllServiceReferences(String, String)}.
	 * 
	 * @param all if <code>true</code>, all services are returned, if <code>false</code>
	 * 		only compatible services are returned
	 * 
	 * @return the service finder
	 */
	ServiceFinder setAll(boolean all) {
		// always set multiple to true
		setMultiple(true)
		
		this.all = all
		
		return this
	}
	
	/**
	 * Determine whether we are looking for a single or multiple services.
	 * 
	 * @return <code>true</code>, if only a single service is returned, 
	 * 			<code>false</code>, if multiple services are returned
	 */
	boolean isSingle() {
		!multiple
	}
	
	/**
	 * Specify, whether to look for a single or multiple services.
	 * 
	 * @param single if <code>true</code>, only a single service is returned, 
	 * 			if <code>false</code>, multiple services are returned
	 * 
	 * @return the service finder
	 */
	ServiceFinder setSingle(boolean single) {
		setMultiple(!single)
		
		return this
	}
	
	/**
	 * Specify, whether to look for a single or multiple services.
	 * 
	 * @param single if <code>true</code>, only a single service is returned, 
	 * 			if <code>false</code>, multiple services are returned
	 * 
	 * @return the service finder
	 */
	ServiceFinder single(boolean single) {
		setMultiple(!single)
		
		return this
	}
	
	/**
	 * Specify, that we are looking for a single services.
	 * 
	 * @return the service finder
	 */
	ServiceFinder single() {
		setMultiple(false)
		
		return this
	}
	
	/**
	 * Determine whether we are looking for a single or multiple services.
	 * 
	 * @return <code>true</code>, if multiple services are returned,
	 * 			<code>false</code>, if only a single service is returned 
	 * 			
	 */
	boolean isMultiple() {
		multiple
	}
	
	/**
	 * Specify, whether to look for a single or multiple services.
	 * 
	 * @param multiple if <code>true</code>, multiple services are returned 
	 * 			if <code>false</code>, only a single service is returned
	 * 
	 * @return the service finder
	 */
	ServiceFinder setMultiple(boolean multiple) {
		this.multiple = multiple
		
		return this
	}
	
	/**
	 * Specify, whether to look for a single or multiple services.
	 * 
	 * @param multiple if <code>true</code>, multiple services are returned 
	 * 			if <code>false</code>, only a single service is returned
	 * 
	 * @return the service finder
	 */
	ServiceFinder multiple(boolean multiple) {
		setMultiple(multiple)
		
		return this
	}

	/**
	 * Specify, that we are looking for multiple services.
	 * 
	 * @return the service finder
	 */
	ServiceFinder multiple() {
		setMultiple(true)
		
		return this
	}
	
	/**
	 * Get service class name.
	 * 
	 * @return service class name or <code>null</code>, if unset
	 */
	String getClassName() {
		className
	}
	
	/**
	 * Set service class name.
	 * 
	 * @param clazz service {@link Class} to look for
	 * 
	 * @return the service finder
	 */
	ServiceFinder setClassName(Class clazz) {
		setClassName(clazz.name)
		
		return this
	}
	
	/**
	 * Set service class name.
	 * 
	 * @param className name of service {@link Class} to look for
	 * 
	 * @return the service finder
	 */
	ServiceFinder setClassName(String className) {
		this.className = className
		
		return this
	}
	
	/**
	 * Set service class name.
	 * 
	 * @param clazz service {@link Class} to look for
	 * 
	 * @return the service finder
	 */
	ServiceFinder className(Class clazz) {
		setClassName(clazz.name)
		
		return this
	}
	
	/**
	 * Set service class name.
	 * 
	 * @param className name of service {@link Class} to look for
	 * 
	 * @return the service finder
	 */
	ServiceFinder className(String className) {
		setClassName(className)
		
		return this
	}
	
	/**
	 * Get service filter.
	 * 
	 * @return service filter or <code>null</code>, if unset
	 */
	String getFilter() {
		filter
	}
	
	/**
	 * Set service filter. The filter follows LDAP rules. See
	 * {@link Filter} for valid filter format.
	 * 
	 * @param filter filter string
	 * 
	 * @return the service finder
	 */
	ServiceFinder setFilter(String filter) {
		this.filter = filter
		
		return this
	}
	
	/**
	 * Set service filter using the filter DSL.
	 * 
	 * @param filter {@link Closure} with filter DSL
	 * 
	 * @return the service finder
	 */
	ServiceFinder setFilter(Closure filter) {
		// provide filter DSL
		FilterBuilder builder = createFilterBuilder()
		setFilter(builder.build(filter))
		
		return this
	}
	
	/**
	 * Create filter builder. The current implementation returns
	 * <code>null</code>, as this is not yet implemented.
	 * 
	 * @return filter builder
	 */
	protected FilterBuilder createFilterBuilder() {
		// TODO create FilterBuilder, which implements the filter DSL
		return null //new Filter4OsgiBuilder()
	} 
	
	/**
	 * Set service filter using the filter DSL.
	 *
	 * @param filter {@link Closure} with filter DSL
	 *
	 * @return the service finder
	 */
	ServiceFinder filter(Closure filter) {
		setFilter(filter)
		
		return this
	}
	
	/**
	 * Set service filter. The filter follows LDAP rules. See
	 * {@link Filter} for valid filter format.
	 *
	 * @param filter filter string
	 *
	 * @return the service finder
	 */
	ServiceFinder filter(String filter) {
		setFilter(filter)
		
		return this
	}
	
	/**
	 * Find service or services according to specified criteria.
	 * 
	 * @param closure {@link Closure} containing search DSL
	 * 
	 * @return {@link ServiceWrapper} with results
	 */
	ServiceWrapper find(Closure closure) {
		configure(closure)
		return find()
	}
	
	/**
	 * Find service or services according to configured criteria.
	 * 
	 * @return {@link ServiceWrapper} with results
	 */
	ServiceWrapper find() {
		if (isSingle()) {
			// get service matching class name with highest priority
			ServiceReference serviceReference = bundleContext.getServiceReference(className)
			return new ServiceWrapper(bundleContext, serviceReference)
		}
		else if (isAll()) {
			// get all (event incompatible) services matching class name and filter
			ServiceReference[] serviceReferences = bundleContext.getAllServiceReferences(className, filter)
			return new ServiceWrapper(bundleContext, serviceReferences)
		}
		else {
			// get all services matching class name and filter
			ServiceReference[] serviceReferences = bundleContext.getServiceReferences(className, filter)
			return new ServiceWrapper(bundleContext, serviceReferences)
		}
	}
}
