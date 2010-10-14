package org.codehaus.groovy.osgi.runtime


import java.io.File
import java.util.List
import java.util.Map

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import org.osgi.util.tracker.ServiceTracker

import groovyx.osgi.runtime.OsgiRuntime

abstract class AbstractOsgiRuntime implements OsgiRuntime {
	final static Log log = LogFactory.getLog(AbstractOsgiRuntime.class)
	
	Map argsMap
	String osgiRuntimePath
	BundleContext bundleContext
	List systemPackages
	File dropinsDir
	def config
	def consoleEnabled = false
	def consolePort = 0
	
	protected configure() {
		// create runtime directory
		def dir = new File(osgiRuntimePath)
		if (!dir.exists()) {
			dir.mkdirs()
		}
		
		dropinsDir = new File(dir, 'dropins')
		if (!dropinsDir.exists()) {
			dropinsDir.mkdirs()
		}
		
		println "OSGi directory: ${osgiRuntimePath}"
		
		// configure (remote) console
		consoleEnabled = config?.osgi?.console?.enabled ?: false
		def defaultConsolePort = config?.osgi?.console?.port ?: 8023
		
		if (argsMap?.consolePort) {
			if (argsMap.consolePort instanceof Boolean) {
				consolePort = defaultConsolePort
			}
			else {
				consolePort = argsMap.consolePort
			}
			consoleEnabled = true
		}
		else if (argsMap?.remoteConsole
			&& !consolePort) {
			consolePort = defaultConsolePort
			consoleEnabled = true
		}
	}
	
	abstract BundleContext doStart();
	abstract void doStop();
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntime#isRunning()
	 */
	public boolean isRunning() {
		return (bundleContext != null);
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntime#getBundleContext()
	 */
	public BundleContext getBundleContext() {
		return bundleContext;
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.OsgiRuntime#start()
	 */
	BundleContext start() {
		if (this.bundleContext) {
			return this.bundleContext
		}
		
		configure()
		
		this.bundleContext = doStart()
		
		return this.bundleContext
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntime#stop()
	 */
	public void stop() {
		if (!this.bundleContext) {
			return
		}
		
		doStop()
		
		this.bundleContext = null 
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.OsgiRuntime#install(java.io.File, boolean)
	 */
	Bundle install(File bundleFile, boolean autoStart) {
		List bundles = install([bundleFile], autoStart)
		return bundles[0]
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntime#install(java.lang.String, boolean)
	 */
	Bundle install(String bundleFile, boolean autoStart) {
		List bundles = install([bundleFile], autoStart)
		return bundles[0]
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntime#install(java.net.URL, boolean)
	 */
	Bundle install(URL bundleFile, boolean autoStart) {
		List bundles = install([bundleFile], autoStart)
		return bundles[0]
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.OsgiRuntime#install(java.util.List, boolean)
	 */
	List<Bundle> install(List<Object> bundleFiles, boolean autoStart) {
		def bundles = []
		// install each file
		bundleFiles.each { file ->
			//println "installing bundle ${file.name}"//" (${file.absolutePath})"
			try {
				def bundle = null
				if (file instanceof File) {
					bundle = this.bundleContext.installBundle("file://${file.absolutePath}");
				}
				else if (file instanceof URL) {
					bundle = this.bundleContext.installBundle((URL) file);
				}
				else if (file instanceof InputStream) {
					bundle = this.bundleContext.installBundle(file.toString(), (InputStream) file);
				}
				else {
					bundle = this.bundleContext.installBundle(file as String);
				}
				if (bundle) {
					bundles << bundle
				}
			}
			catch (e) {
				println "failed to install bundle ${file.name}: ${e.message}"
			}
		}
		
		if (autoStart) {
			// start each bundle
			startBundles(bundles)
		}
		
		return bundles
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntime#startBundles(java.util.List)
	 */
	void startBundles(List bundles) {
		// start each bundle
		bundles.each { bundle ->
			startBundle(bundle)	
		}
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntime#startBundle(org.osgi.framework.Bundle)
	 */
	public void startBundle(Bundle bundle) {
		try {
			if (!bundle) {
				return
			}
			// skip start for fragments
			if (!isFragment(bundle)) {
				bundle.start();
			}
		}
		catch (e) {
			println "failed to start bundle ${bundle}: ${e.message}"
		}
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntime#startBundle(int)
	 */
	public void startBundle(int bundleId) {
		// get bundle by id
		def bundle = this.bundleContext.getBundle(bundleId)
		startBundle(bundle)
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntime#startBundle(java.lang.String)
	 */
	public void startBundle(String symbolicName) {
		def bundle = this.bundleContext.bundles.find { it.symbolicName == symbolicName }
		startBundle(bundle)
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntime#stopBundle(java.util.List)
	 */
	public void stopBundles(List bundles) {
		// start each bundle
		bundles.each { bundle ->
			stopBundle(bundle)
		}
	}
   
	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntime#startBundle(org.osgi.framework.Bundle)
	 */
	public void stopBundle(Bundle bundle) {
		try {
			if (!bundle) {
				return
			}
			// skip stop for fragments
			if (!isFragment(bundle)) {
				bundle.stop();
			}
		}
		catch (e) {
			println "failed to stop bundle ${bundle}: ${e.message}"
		}
	}
   
	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntime#stopBundle(int)
	 */
	public void stopBundle(int bundleId) {
		// get bundle by id
		def bundle = this.bundleContext.getBundle(bundleId)
		stopBundle(bundle)
	}
   
	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntime#stopBundle(java.lang.String)
	 */
	public void stopBundle(String symbolicName) {
		def bundle = this.bundleContext.bundles.find { it.symbolicName == symbolicName }
		stopBundle(bundle)
	}

	
	/* (non-Javadoc)
	 * @see groovyx.osgi.OsgiRuntime#isFragment(org.osgi.framework.Bundle)
	 */
	boolean isFragment(Bundle bundle) {
		// if a bundle is a fragment, its bundle context is null
		String fragmentHost = bundle.headers.get(org.osgi.framework.Constants.FRAGMENT_HOST)
		return (fragmentHost != null)
	}
}
