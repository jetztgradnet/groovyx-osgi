package org.codehaus.groovy.osgi.runtime.external

import org.codehaus.groovy.osgi.runtime.AbstractOsgiRuntime

import org.osgi.framework.BundleContext;

/**
 * The external runtime does not provide a {@link BundleContext} and can
 * not be started or stopped. Installing bundles works via the dropins folder.
 * 
 * @author Wolfgang Schell
 */
class ExternalRuntime extends AbstractOsgiRuntime {

	/* (non-Javadoc)
	 * @see org.codehaus.groovy.osgi.runtime.AbstractOsgiRuntime#doStart()
	 */
	@Override
	public BundleContext doStart() {
		throw new RuntimeException("the runtime can not be started")
	}
	
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.osgi.runtime.AbstractOsgiRuntime#canStart()
	 */
	@Override
	public boolean canStart() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.codehaus.groovy.osgi.runtime.AbstractOsgiRuntime#doStop()
	 */
	@Override
	public void doStop() {
		throw new RuntimeException("the runtime can not be stopped")
	}
	
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.osgi.runtime.AbstractOsgiRuntime#canStop()
	 */
	@Override
	public boolean canStop() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.osgi.runtime.AbstractOsgiRuntime#isRunning()
	 */
	@Override
	public boolean isRunning() {
		// assume the runtime is always running
		return true;
	}
}
