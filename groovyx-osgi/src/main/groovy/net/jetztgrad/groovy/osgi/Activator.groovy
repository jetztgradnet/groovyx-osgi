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

package net.jetztgrad.groovy.osgi

import groovy.lang.MissingPropertyException;
import groovyx.osgi.OsgiCategory;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

class Activator implements BundleActivator {
	public void start(BundleContext context) throws Exception {
		// install methods on Bundle, BundleContext, ServiceReference, and ServiceReference[]
		installCategoryHandler([ Bundle, BundleContext, ServiceReference, ServiceReference[] ] as Class[])
	}
	
	public void stop(BundleContext context) throws Exception {
		// uninstall methods from Bundle, BundleContext, ServiceReference
		uninstallCategoryHandler([ Bundle, BundleContext, ServiceReference, ServiceReference[] ] as Class[])
	}
	
	@SuppressWarnings("rawtypes") 
	static void installCategoryHandler(Class[] targetClasses) {
		targetClasses.each { installCategoryHandler(it) }
	}
	
	static void installCategoryHandler(final Class targetClass) {
		// NOTE: this might lead to unexpected results, if invokeMissing or
		//		 propertyMissing was or will be set by some other code as well...
		targetClass.metaClass.methodMissing = { String name, args ->
			Object[] params = args.inject([delegate]) { list, arg -> list << arg } as Object[]
			OsgiCategory.metaClass.invokeStaticMethod(OsgiCategory, name, params)
		}
		targetClass.metaClass.propertyMissing = { String name ->
			// hard-coded property access, as we would otherwise have to translate the
			// property into its corresponding getter name, which might be getXxxx or isXxxx.
			Object[] params = [delegate] as Object[]
			if ("stateAsText".equals(name)) {
				return OsgiCategory.getStateAsText(delegate)
			}
			throw MissingPropertyException(name, delegate.getClass())
		}
	}
	
	@SuppressWarnings("rawtypes") 
	static void uninstallCategoryHandler(Class[] targetClasses) {
		targetClasses.each { uninstallCategoryHandler(it) }
	}
	
	static void uninstallCategoryHandler(Class targetClass) {
		// NOTE: this might lead to unexpected results, if invokeMissing
		//		 or propertyMissing was set by some other code as well...
		targetClass.metaClass.methodMissing = null
		targetClass.metaClass.propertyMissing = null
	}
}
