package org.codehaus.groovy.osgi.runtime.equinox



import java.util.Properties;

import groovyx.osgi.runtime.OsgiRuntime
import groovyx.osgi.runtime.OsgiRuntimeFactory

/**
 * {@link OsgiRuntimeFactory} for Eclipse Equinox OSGi runtime.
 * 
 * @author Wolfgang Schell
 */
class EquinoxRuntimeFactory implements OsgiRuntimeFactory {

	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntimeFactory#createRuntime(java.util.Properties)
	 */
	public OsgiRuntime createRuntime(Properties runtimeProperties)
			throws IllegalArgumentException, Exception {
		return new EquinoxRuntime(runtimeProperties)
	}
}
