package org.codehaus.groovy.osgi.runtime.external

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.codehaus.groovy.osgi.runtime.AbstractOsgiRuntime

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The external runtime does not provide a {@link BundleContext} and can
 * not be started or stopped. Installing bundles works via the dropins folder.
 * 
 * @author Wolfgang Schell
 */
class ExternalRuntime extends AbstractOsgiRuntime {
	ExternalRuntime(Map runtimeProperties) {
		super(runtimeProperties)
	}

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
	
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.osgi.runtime.AbstractOsgiRuntime#install(java.io.File, boolean)
	 */
	@Override
	public Bundle install(File bundleFile, boolean autoStart) {
		if (!bundleFile.exists()
			|| !bundleFile.isFile()) {
			throw new IllegalArgumentException("invalid bundle file: " + bundleFile)
		}
		
		File dropinsDir = getDropinsDir()
		if ((dropinsDir == null)
			|| !dropinsDir.exists()
			|| !dropinsDir.isDirectory()) {
			throw new IllegalArgumentException("invalid dropins directory: " + dropinsDir)
		}
		// copy file into dropins folder
		File targetFile = new File(dropinsDir, bundleFile.name)
		targetFile << bundleFile.newInputStream()
	}
	
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.osgi.runtime.AbstractOsgiRuntime#install(java.io.InputStream, boolean)
	 */
	@Override
	public Bundle install(InputStream stream, boolean autoStart) {
		// TODO not yet supported
		if (stream) {
			// close quietly
			try {
				stream.close()
			}
			catch (Throwable t) {
				// ignore
			}
		}
		return null
	}
	
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.osgi.runtime.AbstractOsgiRuntime#install(java.lang.String, boolean)
	 */
	@Override
	public Bundle install(String bundleFile, boolean autoStart) {
		File file = new File(bundleFile)
		return install(file, autoStart);
	}
	
	/* (non-Javadoc)
	 * @see org.codehaus.groovy.osgi.runtime.AbstractOsgiRuntime#install(java.net.URL, boolean)
	 */
	@Override
	public Bundle install(URL url, boolean autoStart) {
		if (url.getProtocol() == "file") {
			File file = new File(url.getFile())
			return install(file, autoStart)
		}
		else {
			return install(url.openStream(), autoStart)
		}
	}
}

