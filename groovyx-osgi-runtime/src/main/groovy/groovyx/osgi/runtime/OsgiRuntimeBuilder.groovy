
package groovyx.osgi.runtime

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory;

import org.codehaus.groovy.osgi.runtime.equinox.EquinoxRuntimeFactory;
import org.codehaus.groovy.osgi.runtime.felix.FelixRuntimeFactory;

import groovy.lang.Closure


class OsgiRuntimeBuilder implements GroovyObject {
	final static Log log = LogFactory.getLog(OsgiRuntimeBuilder.class)

	Map args = [:]
	Map<String, Object> runtimeTypes = [:]
	def framework = 'equinox'
	List<String> bundles = []
	Properties runtimeProperties
	OsgiRuntime runtime
	
	public OsgiRuntimeBuilder() {
		runtimeProperties = new Properties()
		
		// default runtime type
		args.framework = 'equinox'
		
		initRuntimeTypes(runtimeTypes)
	}
	
	protected void initRuntimeTypes(Map runtimeTypes) {
		runtimeTypes['equinox'] = EquinoxRuntimeFactory.class
		runtimeTypes['felix'] = FelixRuntimeFactory.class
	}
	
	/**
	 * Delegate to runtime properties.
	 * 
	 * @param name property name
	 * @param value property value
	 */
	def propertyMissing(String name) {
		runtimeProperties[name]
	}
	
	/**
	 * Delegate to runtime properties.
	 * 
	 * @param name property name
	 * 
	 * @return property value
	 */
	def propertyMissing(String name, def value) { 
		runtimeProperties[name] = value 
	}
	
	/**
	* Get {@link OsgiRuntimeFactory} from framework type.
	*
	* @param framework OsgiRuntimeFactory instance, class, class name or class name alias
	*
	* @return {@link OsgiRuntimeFactory} or <code>null</code>, if not available
	*/
   protected OsgiRuntimeFactory getOsgiRuntimeFactory() throws Exception {
	   getOsgiRuntimeFactory(this.framework)
   }
	
	/**
	 * Get {@link OsgiRuntimeFactory} from framework type.
	 * 
	 * @param framework OsgiRuntimeFactory instance, class, class name or class name alias
	 * 
	 * @return {@link OsgiRuntimeFactory} or <code>null</code>, if not available
	 */
	protected OsgiRuntimeFactory getOsgiRuntimeFactory(def framework) throws Exception {
		Class factoryClass = null
		OsgiRuntimeFactory factory = null
		
		if (framework instanceof OsgiRuntimeFactory ) {
			return framework
		}
		else if (framework instanceof Class) {
			factoryClass = framework
		}
		else {
			// interpret as name of a OsgiRuntimeFactory class
			
			// lookup alias
			def aliasedFramework = runtimeTypes.get(framework.toString().toLowerCase())
			if (!aliasedFramework) {
				aliasedFramework = framework
			}
			if (aliasedFramework instanceof Class) {
				factoryClass = aliasedFramework
			}
			else if (aliasedFramework) { 
				String className = aliasedFramework?.toString()
			
				try {
					factoryClass = Class.forName(className)
				}
				catch (Exception e) {
					throw new IllegalArgumentException("invalid runtime type: $framework" as String, e)
				}
			}
		}
		
		factory = (OsgiRuntimeFactory) factoryClass?.newInstance();
		
		factory
	}
	
	/**
	 * Create configured {@link OsgiRuntime}
	 * 
	 * @param framework OsgiRuntimeFactory instance, class, class name or class name alias
	 * @param runtimeProperties runtime properties
	 * 
	 * @return configured, but not yet started {@link OsgiRuntime} 
	 */
	protected OsgiRuntime createRuntime(def framework, Properties runtimeProperties) throws IllegalArgumentException {
		OsgiRuntime runtime = null
		OsgiRuntimeFactory factory = getOsgiRuntimeFactory(framework);
	
		runtime = factory?.createRuntime(runtimeProperties);
		
		runtime
	}
	
	/**
	 * Set framework runtime type or name of {@link OsgiRuntimeFactory} class
	 * 
	 * @param framework runtime type or name of {@link OsgiRuntimeFactory} class
	 */
	void setFramework(String framework) {
		this.framework = framework
	}
	
	/**
	 * Set framework {@link OsgiRuntimeFactory} class.
	 * 
	 * @param frameworkFactoryClass {@link OsgiRuntimeFactory} class
	 */
	void setFramework(Class frameworkFactoryClass) {
		this.framework = frameworkFactoryClass
	}
	
	/**
	 * Set framework {@link OsgiRuntimeFactory} factory.
	 *
	 * @param frameworkFactory {@link OsgiRuntimeFactory}
	 */
	void setFramework(OsgiRuntimeFactory frameworkFactory) {
		this.framework = frameworkFactory
	}
	
	/**
	 * Get framework to use.
	 * 
	 * @return framework runtime type, framework runtime factory or name of {@link OsgiRuntimeFactory} class
	 */
	def getFramework() {
		framework
	}
	
	/**
	 * Set framework runtime type or name of {@link OsgiRuntimeFactory} class
	 *
	 * @param framework runtime type or name of {@link OsgiRuntimeFactory} class
	 */
	void framework(String framework) {
		this.framework = framework
	}
   
	/**
	 * Set framework {@link OsgiRuntimeFactory} class.
	 *
	 * @param frameworkFactoryClass {@link OsgiRuntimeFactory} class
	 */
	void framework(Class frameworkFactoryClass) {
		this.framework = frameworkFactoryClass
	}

	/**
	 * Set framework {@link OsgiRuntimeFactory} factory.
	 *
	 * @param frameworkFactory {@link OsgiRuntimeFactory}
	 */
	void framework(OsgiRuntimeFactory frameworkFactory) {
		this.framework = frameworkFactory
	}
   
	/**
	 * Perform cleanup of caches (sets property osgi.clean)
	 */
	void clean() {
		runtimeProperties.setProperty("osgi.clean", "true")
	}
	
	/**
	 * Open OSGi console.
	 */
	void console() {
		runtimeProperties.setProperty("osgi.console", "true")
	}
	
	/**
	 * Open OSGi console on specified port.
	 * 
	 * @param port port on which to run console
	 */
	void console(def port) {
		runtimeProperties.setProperty("osgi.console", "true")
		// TODO set console port
	}
	
	/**
	 * Configure runtime. This includes resolving and installing
	 * bundles
	 *  
	 * @param runtime runtime to configure
	 *  
	 * @return runtime
	 */
	protected def configure(OsgiRuntime runtime) {
		if (bundles?.size()) {
			// start runtime if it not yet running, so we
			// can install additional bundles
			if (!runtime.isRunning()) {
				runtime.start()
			}
			
			// resolve and install bundles
			bundles?.each { bundle ->
				log.info "installing bundle $bundle"
				boolean autoStart = true
				
				if ((bundle instanceof Map)
					&& bundle?.url) {
					bundle = bundle.url
				}
				
				if ((bundle instanceof Map)
					|| (bundle.toString().startsWith('mvn:'))) {
					// TODO resolve bundle
					log.error("resolving of bundles via Ivy is not yet implemented: " + bundle);
				}
				
				try {
					runtime.install(bundle, autoStart)
				}
				catch (Exception e) {
					log.error("failed to install bundle $bundle: " + e.getMessage())
					log.debug("details: ", e)
				}
			}
		}
		
		runtime
	}
	
	/**
	 * Get {@link OsgiRuntime}.
	 * 
	 * @return runtime or <code>null</code>, if not yet configured and built
	 */
	OsgiRuntime getRuntime() {
		this.runtime
	}
	
	/**
	 * Configure and build OSGi runtime.
	 * 
	 * @return configured and started OSGi runtime
	 */
	OsgiRuntime build() {
		if (this.runtime) {
			// runtime already exists
			return this.runtime
		}
		
		// TODO make runtime path configurable
		File cwd = new File(System.getProperty('user.dir'))
		File osgiRuntime = new File(cwd, 'osgi')
		File dropinsDir = new File(osgiRuntime, 'dropins')
		def osgiRuntimePath = osgiRuntime.absolutePath
		
		// prepare runtime properties
		//runtimeProperties.setProperty("osgi.clean", "true")
		//runtimeProperties.setProperty("osgi.console", "true")
		runtimeProperties.setProperty("osgi.noShutdown", "true")
		runtimeProperties.setProperty("osgi.install.area", osgiRuntimePath as String)
		runtimeProperties.setProperty("osgi.configuration.area", "$osgiRuntimePath/configuration" as String)
		//runtimeProperties.setProperty("org.osgi.framework.bootdelegation", "*")
		runtimeProperties.setProperty("osgi.compatibility.bootdelegation", "true")
		runtimeProperties.setProperty("osgi.frameworkParentClassloader", "boot")
		runtimeProperties.setProperty("osgi.contextClassLoaderParent", "boot")
		
		if (runtimeProperties.systemPackages) {
			runtimeProperties.setProperty("org.osgi.framework.system.packages.extra", runtimeProperties.systemPackages.join(','))
		}
		
		//frameworkProperties.setProperty("log4j.configuration", logConfig.absolutePath)
		
		System.setProperty("bundles.configuration.location", dropinsDir.canonicalPath) // PAX ConfMan
		System.setProperty("felix.fileinstall.dir", dropinsDir.canonicalPath)			// Felix FileInstall
		System.setProperty("felix.fileinstall.debug", "1")
		// for OSGi HttpService
		System.setProperty("org.osgi.service.http.port", "8081")
		
		this.runtime = createRuntime(framework, runtimeProperties)
		
		this.runtime.osgiRuntimePath = osgiRuntimePath
		
		// configure runtime
		configure(this.runtime)
		
		this.runtime
	}
	
	/**
	 * Configure and build OSGi runtime.
	 * 
	 * @param closure configuration closure
	 * 
	 * @return configured and started OSGi runtime
	 */
	OsgiRuntime build(Closure closure) {
		if (this.runtime) {
			// runtime already exists
			return this.runtime
		}
		
		configure(closure)
		
		this.runtime = build()
		
		this.runtime
	}
	
	/**
	 * Configure OSGi runtime. The runtime does not yet exist,
	 * it can be created using {@link #build()}.
	 * 
	 * @param closure configuration closure
	 * 
	 * @return this builder instance
	 */
	def configure(Closure closure) {
		def cl = closure.clone()
		cl.delegate = this
		cl()
		
		this
	}
	
	/**
	 * Add bundles to be installed.
	 *  
	 * @param closure bundle configuration closure
	 * 
	 * @return this builder instance
	 */
	def bundles(Closure closure) {
		def cl = closure.clone()
		cl.delegate = this
		cl()
		
		this
	}
	
	/**
	 * Add bundle to be installed.
	 *  
	 * @param args bundle details
	 * 
	 * @return this builder instance
	 */
	def bundle(Map args) {
		bundle(args, null)
		
		this
	}
	
	/**
	 * Add bundle to be installed.
	 *  
	 * @param specs bundle specs in format 'group:name:version'
	 * 
	 * @return this builder instance
	 */
	def bundle(String specs) {
		bundle(specs, null)
		
		this
	}
	
	/**
	* Add bundle to be installed.
	* 
	* @param specs bundle specs in format 'group:name:version'
	* @param closure bundle configuration closure
	*/
	def bundle(String specs, Closure closure) {
		def args = [:]
		
		bundle(specs, args, closure)
		
		this
	}
	
	/**
	 * Add bundle to be installed.
	 *  
	 * @param specs bundle specs in format 'group:name:version'
	 * @param args bundle details
	 * @param closure bundle configuration closure
	 * 
	 * @return this builder instance
	 */
	def bundle(String specs, Map args, Closure closure) {
		if (closure) {
			def cl = closure.clone()
			cl.delegate = this
			cl(specs)
		}
		
		bundles << specs
		
		this
	}
}
