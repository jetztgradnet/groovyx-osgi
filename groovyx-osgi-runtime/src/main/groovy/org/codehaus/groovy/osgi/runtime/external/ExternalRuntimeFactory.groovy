package org.codehaus.groovy.osgi.runtime.external

import java.util.Properties;

import groovyx.osgi.runtime.OsgiRuntime;
import groovyx.osgi.runtime.OsgiRuntimeFactory;

class ExternalRuntimeFactory implements OsgiRuntimeFactory {

	public OsgiRuntime createRuntime(Properties runtimeProperties)
			throws IllegalArgumentException, Exception {
		return new ExternalRuntime(runtimeProperties);
	}

}
