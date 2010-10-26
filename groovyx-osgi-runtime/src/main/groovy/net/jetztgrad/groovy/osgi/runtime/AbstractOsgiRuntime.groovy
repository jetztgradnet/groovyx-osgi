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

package net.jetztgrad.groovy.osgi.runtime

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

	Properties frameworkProperties = new Properties()
	Map argsMap
	String osgiRuntimePath
	BundleContext bundleContext
	List systemPackages
	File dropinsDir
	def config
	def consoleEnabled = false
	def consolePort = 0
	
	AbstractOsgiRuntime(Map runtimeProperties) {
		if (runtimeProperties) {
			frameworkProperties.putAll(runtimeProperties)
		}
	}
	
	protected configure() {
		// create runtime directory
		def dir = new File(osgiRuntimePath)
		
		if (frameworkProperties?.purge) {
			dir.deleteDir()
		}
		
		if (!dir.exists()) {
			dir.mkdirs()
		}
		
		if (!dropinsDir) {
			dropinsDir = new File(dir, 'dropins')
		}
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
	
	/**
	 * Configure logging
	 */
	protected void configureLogging() {
		// TODO configure logging via PAX Logging/SLF4J and log4j
		// TODO create logging DSL (e.g. copy from Grails) 
	}
	
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
		if (isRunning()) {
			return this.bundleContext
		}
		
		configure()

		if (canStart()) {
			this.bundleContext = doStart()
		}
		
		return this.bundleContext
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntime#stop()
	 */
	public void stop() {
		if (!isRunning()) {
			return
		}
		
		if (canStop()) {
			doStop()
		}
		
		this.bundleContext = null 
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntime#canStart()
	 */
	public boolean canStart() {
		// always return true
		return true;
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntime#canStop()
	 */
	public boolean canStop() {
		// always return true
		return true;
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.OsgiRuntime#install(java.io.File, boolean)
	 */
	Bundle install(File bundleFile, boolean autoStart) {
		def bundle = this.bundleContext.installBundle("file://${bundleFile.absolutePath}");
		if (autoStart) {
			startBundle(bundle)
		}
		return bundle
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntime#install(java.lang.String, boolean)
	 */
	Bundle install(String bundleFile, boolean autoStart) {
		def bundle = this.bundleContext.installBundle(bundleFile);
		if (autoStart) {
			startBundle(bundle)
		}
		return bundle
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntime#install(java.net.URL, boolean)
	 */
	Bundle install(URL url, boolean autoStart) {
		def bundle = this.bundleContext.installBundle(url);
		if (autoStart) {
			startBundle(bundle)
		}
		return bundle
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntime#install(java.io.InputStream, boolean)
	 */
	Bundle install(InputStream stream, boolean autoStart) {
		def bundle = this.bundleContext.installBundle(stream.toString(), stream)
		if (autoStart) {
			startBundle(bundle)
		}
		return bundle
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.OsgiRuntime#install(java.util.List, boolean)
	 */
	List<Bundle> install(List<Object> bundleFiles, boolean autoStart) {
		def bundles = []
		// install each file (without starting it)
		bundleFiles.each { file ->
			//println "installing bundle ${file.name}"//" (${file.absolutePath})"
			try {
				def bundle = null
				if (file instanceof File) {
					bundle = install(file as File, false)
				}
				else if (file instanceof URL) {
					bundle = install(file as URL, false);
				}
				else if (file instanceof InputStream) {
					bundle = install(file as InputStream, false);
				}
				else {
					bundle = install(file as String, false);
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
