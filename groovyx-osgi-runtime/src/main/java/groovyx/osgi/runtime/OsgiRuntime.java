package groovyx.osgi.runtime;

import java.io.File;
import java.net.URL;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public interface OsgiRuntime {
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
	 * Install bundle.
	 * 
	 * @param uri URI/URL leading to the bundle location
	 * @param autoStart true, to start bundle after installation, false otherwise
	 *  
	 * @return installed bundle
	 */
	Bundle install(String uri, boolean autoStart);
	
	/**
	 * Install bundle.
	 * 
	 * @param url URL leading to the bundle location
	 * @param autoStart true, to start bundle after installation, false otherwise
	 *  
	 * @return installed bundle
	 */
	Bundle install(URL url, boolean autoStart);

	/**
	 * Install bundled. Bundles are started after all have been installed.
	 * 
	 * @param bundleFiles list of bundle files or URIs/URLs
	 * @param autoStart true, to start bundles after installation, false otherwise
	 *  
	 * @return installed bundle
	 */
	List<Bundle> install(List<Object> bundleFiles, boolean autoStart);
	
	/**
	 * Start bundles
	 * 
	 * @param bundles list of bundle ids or bundle symbolic names
	 */
	@SuppressWarnings("rawtypes")
	void startBundle(List bundles);

	/**
	 * Start bundle.
	 * 
	 * @param bundle bundle to start
	 */
	void startBundle(Bundle bundle);
	
	/**
	 * Start bundle.
	 * 
	 * @param bundleId bundle id
	 */
	void startBundle(int bundleId);
	
	/**
	 * Start bundle.
	 * 
	 * @param symbolicName bundle symbolic name
	 */
	void startBundle(String symbolicName);
	
	/**
	 * Stop bundles
	 * 
	 * @param bundles list of bundle ids or bundle symbolic names
	 */
	@SuppressWarnings("rawtypes")
	void stopBundle(List bundles);

	/**
	 * Stop bundle.
	 * 
	 * @param bundle bundle to start
	 */
	void stopBundle(Bundle bundle);
	
	/**
	 * Stop bundle.
	 * 
	 * @param bundleId bundle id
	 */
	void stopBundle(int bundleId);
	
	/**
	 * Stop bundle.
	 * 
	 * @param symbolicName bundle symbolic name
	 */
	void stopBundle(String symbolicName);

	/**
	 * Determine whether a bundle is a fragment.
	 *
	 * @param bundle bundle to check
	 * 
	 * @return true, if the bundle is a fragment, false otherwise
	 */
	boolean isFragment(Bundle bundle);
}
