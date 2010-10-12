
package org.codehaus.groovy.osgi.runtime.support


import groovy.lang.Closure
import org.codehaus.groovy.osgi.runtime.support.BundleSpec

class OsgiBuilder {
	List<BundleSpec> bundles = []
	
	def bundles(Closure closure) {
		bundles = []
		def cl = closure.clone()
		cl.delegate = this
		cl()
	}
	
	def bundle(Map args) {
		bundle(args, null)
	}
	
	/**
	 * Bundle
	 * @param specs bundle specs in format 'group:name:version'
	 */
	def bundle(String specs) {
		bundle(specs, null)
	}
	
	/**
	* Bundle
	* @param specs bundle specs in format 'group:name:version'
	*/
	def bundle(String specs, Closure closure) {
		def parts = specs.split(':')
		if (parts?.size() < 3) {
			throw new IllegalArgumentException("invalid bundle spec! Required format: 'group:name:version'")
		}
		def (group, name, version) = parts as List 
		def args = [group: group, name: name, version: version ]
		bundle(args, closure)
	}
	
	def bundle(Map args, Closure closure) {
		if (args.name) {
			BundleSpec spec = new BundleSpec()
			spec.name = args.name
			
			if (args?.group) {
				spec.group = args.group
			}
			
			if (args?.version) {
				spec.version = args.version
			}
			
			if (closure) {
				def cl = closure.clone()
				cl.delegate = this
				cl(spec)
			}
			
			bundles << spec
		}
	}
}
