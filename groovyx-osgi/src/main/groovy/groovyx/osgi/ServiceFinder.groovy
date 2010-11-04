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

import org.osgi.framework.BundleContext
import org.osgi.framework.ServiceReference

import net.jetztgrad.groovy.osgi.Filter4OsgiBuilder

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
	
	ServiceFinder configure(Closure closure) {
		Closure cl = closure.clone()
		cl.delegate = this
		cl()
		
		return this
	}
	
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
	
	ServiceFinder configure(Map options, Closure closure) {
		configure(options)
		configure(closure)
		
		return this
	}
	
	boolean getAll() {
		all
	}
	
	ServiceFinder all() {
		setAll(true)
		
		return this
	}
	
	ServiceFinder all(boolean all) {
		setAll(all)
		
		return this
	}
	
	ServiceFinder setAll(boolean all) {
		// always set multiple to true
		setMultiple(true)
		
		this.all = all
		
		return this
	}
	
	boolean isSingle() {
		!multiple
	}
	
	ServiceFinder setSingle(boolean single) {
		setMultiple(!single)
		
		return this
	}
	
	ServiceFinder single(boolean single) {
		setMultiple(!single)
		
		return this
	}
	
	ServiceFinder single() {
		setMultiple(false)
		
		return this
	}
	
	boolean isMultiple() {
		multiple
	}
	
	ServiceFinder setMultiple(boolean multiple) {
		this.multiple = multiple
		
		return this
	}
	
	ServiceFinder multiple(boolean multiple) {
		setMultiple(multiple)
		
		return this
	}
	
	ServiceFinder multiple() {
		setMultiple(true)
		
		return this
	}
	
	String getClassName() {
		className
	}
	
	ServiceFinder setClassName(Class clazz) {
		setClassName(clazz.name)
		
		return this
	}
	
	ServiceFinder setClassName(String className) {
		this.className = className
		
		return this
	}
	
	ServiceFinder className(Class clazz) {
		setClassName(clazz.name)
		
		return this
	}
	
	ServiceFinder className(String className) {
		setClassName(className)
		
		return this
	}
	
	String getFilter() {
		filter
	}
	
	ServiceFinder setFilter(String filter) {
		this.filter = filter
		
		return this
	}
	
	ServiceFinder setFilter(Closure filter) {
		// provide filter DSL
		FilterBuilder builder = createFilterBuilder()
		setFilter(builder.build(filter))
		
		return this
	}
	
	protected FilterBuilder createFilterBuilder() {
		return new Filter4OsgiBuilder()
	} 
	
	ServiceFinder filter(Closure filter) {
		setFilter(filter)
		
		return this
	}
	
	ServiceFinder filter(String filter) {
		setFilter(filter)
		
		return this
	}
	
	ServiceWrapper find(Closure closure) {
		configure(closure)
		return find()
	}
	
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
