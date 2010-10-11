package groovyx.osgi.runtime

import java.io.File
import java.util.List
import java.util.Map

import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext
import org.osgi.util.tracker.ServiceTracker


abstract class AbstractOsgiRuntime implements OsgiRuntime {
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
		consoleEnabled = config?.osgi.console.enabled ?: false
		def defaultConsolePort = config?.osgi.console.port ?: 8023
		
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
	 * @see groovyx.osgi.OsgiRuntime#install(java.io.File, boolean)
	 */
	Bundle install(File bundleFile, boolean autoStart) {
		List bundles = install([bundleFile], autoStart)
		return bundles[0]
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.OsgiRuntime#install(java.util.List, boolean)
	 */
	List<Bundle> install(List<File> bundleFiles, boolean autoStart) {
		def bundles = []
		// install each file
		bundleFiles.each { file ->
			//println "installing bundle ${file.name}"//" (${file.absolutePath})"
			try {
				def bundle = this.bundleContext.installBundle("file://${file.absolutePath}");
				bundles << bundle
			}
			catch (e) {
				println "failed to install bundle ${file.name}: ${e.message}"
			}
		}
		
		if (autoStart) {
			// start each bundle
			start(bundles)
		}
		
		return bundles
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.OsgiRuntime#start(java.util.List)
	 */
	void start(List bundles) {
		// start each bundle
		bundles.each { bundle ->
			start(bundle)	
		}
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntime#start(org.osgi.framework.Bundle)
	 */
	public void start(Bundle bundle) {
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
	 * @see groovyx.osgi.runtime.OsgiRuntime#start(int)
	 */
	public void start(int bundleId) {
		// get bundle by id
		def bundle = this.bundleContext.getBundle(bundleId)
		start(bundle)
	}
	
	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntime#start(java.lang.String)
	 */
	public void start(String symbolicName) {
		def bundle = this.bundleContext.bundles.find { it.symbolicName == symbolicName }
		start(bundle)
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
