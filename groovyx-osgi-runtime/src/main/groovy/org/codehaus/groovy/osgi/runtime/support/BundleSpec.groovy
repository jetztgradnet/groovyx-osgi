package org.codehaus.groovy.osgi.runtime.support;

import org.osgi.framework.Version;

class BundleSpec {
	String name
	String group
	Version minVersion
	Version maxVersion
	def source
	boolean optional
	def startLevel
}
