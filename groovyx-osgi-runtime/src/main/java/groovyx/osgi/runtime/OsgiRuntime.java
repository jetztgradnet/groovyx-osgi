package groovyx.osgi.runtime
;

import java.io.File;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

interface OsgiRuntime {
	/**
	 * Start OSGi framework.
	 * 
	 * @return root bundle context
	 */
	BundleContext start();

	/**
	 * Shutdown OSGi framework.
	 */
	void stop();
	
	/**
	 * Determine whether the framework is running
	 * @return
	 */
	boolean isRunning();
	
	/**
	 * Get bundle context of system bundle.
	 * 
	 * @return bundle context or <code>null</code>, if
	 * 			the framework is not started
	 */
	BundleContext getBundleContext();
	
	/**
	 * Configure logging
	 */
	void configureLogging();
	
	/**
	 * Install bundle.
	 * 
	 * @param bundleFile file containing the bundle
	 * @param autoStart true, to start bundle after installation, false otherwise
	 *  
	 * @return installed bundle
	 */
	Bundle install(File bundleFile, boolean autoStart);

	/**
	 * Install bundled. Bundles are started after all have been installed.
	 * 
	 * @param bundleFiles list of files containing the bundles
	 * @param autoStart true, to start bundles after installation, false otherwise
	 *  
	 * @return installed bundle
	 */
	List<Bundle> install(List<File> bundleFiles, boolean autoStart);

	/**
	 * Start bundles
	 * 
	 * @param bundles list of bundle ids or bundle symbolic names
	 */
	@SuppressWarnings("rawtypes")
	void start(List bundles);

	/**
	 * Start bundle.
	 * 
	 * @param bundle bundle to start
	 */
	void start(Bundle bundle);
	
	/**
	 * Start bundle.
	 * 
	 * @param bundleId bundle id
	 */
	void start(int bundleId);
	
	/**
	 * Start bundle.
	 * 
	 * @param symbolicName bundle symbolic name
	 */
	void start(String symbolicName);

	/**
	 * Determine whether a bundle is a fragment.
	 *
	 * @param bundle bundle to check
	 * 
	 * @return true, if the bundle is a fragment, false otherwise
	 */
	boolean isFragment(Bundle bundle);
}
