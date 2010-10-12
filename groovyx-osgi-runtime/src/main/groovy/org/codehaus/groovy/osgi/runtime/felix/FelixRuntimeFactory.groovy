package org.codehaus.groovy.osgi.runtime.felix



import java.util.Properties

import groovyx.osgi.runtime.OsgiRuntime
import groovyx.osgi.runtime.OsgiRuntimeFactory

/**
* {@link OsgiRuntimeFactory} for Apache Felix OSGi runtime.
*
* @author Wolfgang Schell
*/
class FelixRuntimeFactory implements OsgiRuntimeFactory {

	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntimeFactory#createRuntime(java.util.Properties)
	 */
	public OsgiRuntime createRuntime(Properties runtimeProperties)
			throws IllegalArgumentException, Exception {
		return new FelixRuntime(runtimeProperties)
	}
}
