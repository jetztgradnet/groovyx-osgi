package org.codehaus.groovy.osgi.runtime.support

import org.osgi.framework.Version

class PackageSpec {
	String name
	Version minVersion
	Version maxVersion
	boolean optional
}
