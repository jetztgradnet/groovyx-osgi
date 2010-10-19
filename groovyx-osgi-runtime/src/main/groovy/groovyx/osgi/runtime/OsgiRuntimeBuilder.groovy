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

package groovyx.osgi.runtime

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.lang.Script;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory;

import org.codehaus.groovy.osgi.runtime.equinox.EquinoxRuntimeFactory;
import org.codehaus.groovy.osgi.runtime.felix.FelixRuntimeFactory;
import org.codehaus.groovy.osgi.runtime.external.ExternalRuntimeFactory

import org.codehaus.groovy.osgi.runtime.resolve.IvyDependencyManager


class OsgiRuntimeBuilder implements GroovyObject {
	final static Log log = LogFactory.getLog(OsgiRuntimeBuilder.class)
	
	Map args = [:]
	Map<String, Object> runtimeTypes = [:]
	def framework = 'equinox'
	def dropinsDir = 'dropins'
	boolean purge = false
	List<String> bundles = []
	Closure repositoriesConfig = null
	Properties runtimeProperties
	OsgiRuntime runtime
	
	// callbacks
	Closure beforeStart
	Closure afterStart
	Closure beforeInstallBundles
	Closure afterInstallBundles
	Closure doRun
	Closure beforeStop
	Closure afterStop
	
	public OsgiRuntimeBuilder() {
		runtimeProperties = new Properties()
		
		// default runtime type
		args.framework = 'equinox'
		// set default equinox version drop
		args.equinoxDrop = 'R-3.6-201006080911' // Helios release
		// set default Eclipse mirror
		args.equinoxMirror = "http://ftp-stud.fht-esslingen.de/pub/Mirrors/eclipse/equinox/drops/"
		
		initRuntimeTypes(runtimeTypes)
	}
	
	/**
	 * Setup aliases for OSGi runtimes. May be overridden
	 * to add additional aliases.
	 * 
	 * @param runtimeTypes map to add types to
	 */
	protected void initRuntimeTypes(Map runtimeTypes) {
		runtimeTypes['equinox'] = EquinoxRuntimeFactory.class
		runtimeTypes['felix'] = FelixRuntimeFactory.class
		runtimeTypes['external'] = ExternalRuntimeFactory.class
	}
	
	/**
	 * Setup binding for configuration scripts. May 
	 * be overridden to add more bindings 
	 * 
	 * @param binding binding to set up
	 */
	protected void setupBinding(Binding binding) {
		binding.builder = this
		binding.configure = { 
			configure it
		}
	}
	
	/**
	* Set runtime property.
	*
	* @param name property name
	* @param value property value
	*/
	def setRuntimeProperty(String name, def value) { 
		runtimeProperties[name] = value 
	}
	
	/**
	* Get runtime property.
	*
	* @param name property name
	*
	* @return property value
	*/
	def getRuntimeProperty(String name) {
		runtimeProperties[name]
	}
	
	/**
	* Get runtime property.
	*
	* @param name property name
	* @param defValue default value
	*
	* @return property value or default value, if there 
	* 			is no property of this name
	*/
	def getRuntimeProperty(String name, def defValue) {
		if (runtimeProperties.containsKey(name)) {
			return runtimeProperties[name]
		}
		
		return defValue
	}
	
	/**
	 * Delegate to runtime properties.
	 * 
	 * @param name property name
	 * 
	 * @return property value
	 */
	def propertyMissing(String name) {
		if (runtimeProperties.containsKey(name)) {
			return runtimeProperties[name]
		}
	}
	
	/**
	 * Delegate to runtime properties.
	 * 
	 * @param name property name
	 * @param value property value
	 */
	def propertyMissing(String name, def value) { 
		setRuntimeProperty(name, value)
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
	 * Get runtime properties.
	 * 
	 * @return runtime properties
	 */
	public Properties getRuntimeProperties() {
		return runtimeProperties;
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
	* Set directory, into which to installed bundles
	*
	* @param dir dropins directory
	*/
	void dropinsDir(File dir) {
		dropinsDir = dir
	}
	
	/**
	 * Set directory, into which the runtime will be installed
	 *  
	 * @param dir runtime directory
	 */
	void runtimeDir(File dir) {
		args.runtimeDir = dir
	}
	
	/**
	* Set directory, into which the runtime will be installed
	*
	* @param dir runtime directory, either absolute or relative
	* 			to the current directory
	*/
	void runtimeDir(String dir) {
		args.runtimeDir = dir
	}
   
	/**
	 * Perform cleanup of caches (sets property osgi.clean)
	 */
	void clean() {
		runtimeProperties.setProperty("osgi.clean", "true")
	}
	
	/**
	* Perform cleanup of caches (sets property osgi.clean)
	*/
   void clean(boolean purge) {
	   clean()
	   runtimeProperties.setProperty("purge", purge)
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
		runtimeProperties.setProperty("osgi.console", port)
	}
	
	/**
	 * Configure runtime. This includes resolving and installing
	 * bundles.
	 *  
	 * @param runtime runtime to configure
	 *  
	 * @return runtime
	 */
	protected def configureRuntime(OsgiRuntime runtime) {
		installBundles(runtime, bundles)
	}
	
	protected void doStart() {
		if (!runtime.isRunning()) {
			if (beforeStart) {
				beforeStart(runtime)
			}
			
			runtime.start()
			
			if (afterStart) {
				afterStart(runtime)
			}
		}
	}
	
	protected void waitForFinish() {
		if (runtime.isRunning()) {
			if (doRun) {
				doRun(runtime)
			}
			else {
				long timeout = 0
				
				// wait for timeout or user to press CTRL-C
				def locker = new Object()
				synchronized(locker) {
					if (timeout) {
						// wait for timeout
						locker.wait(timeout)
					}
					else {
						locker.wait()
					}
				}
			}
		}
	}
	
	protected void doStop() {
		if (runtime.isRunning()) {
			if (beforeStop) {
				beforeStop(runtime)
			}
			
			runtime.stop()
			
			if (afterStop) {
				afterStop(runtime)
			}
		}
	}
	
	/**
	 * Install bundles in runtime.
	 * 
	 * @param runtime runtime in which to install bundles
	 * @param bundles list of bundles. Each element is either 
	 * 			an URL (file, http, ..., mvn:group:module:version) or
	 * 			a Map with the elements group, name, and version
	 * @return OSGi runtime
	 */
	protected def installBundles(OsgiRuntime runtime, List bundles) {
		if (bundles?.size()) {
			// start runtime if it not yet running, so we
			// can install additional bundles
			if (!runtime.isRunning()) {
				doStart()
			}
			
			// resolve bundles
			def bundlesToResolve = []
			def artifactsToResolve = []
			bundles?.each { bundle ->
				if (bundle instanceof Map) {
					bundlesToResolve << bundle
					
					artifactsToResolve << bundle
				}
				else if (bundle.toString().startsWith('mvn:')) {
					bundlesToResolve << bundle
					
					def spec = bundle.toString()
					// remove URL scheme
					spec -= 'mvn:'
					
					artifactsToResolve << spec
				}
			}
			if (artifactsToResolve) {
				// remove bundles to resolve
				bundles -= bundlesToResolve
				def artifactURLs = []

				// resolve bundles				
				artifactURLs = resolveBundles(artifactsToResolve)
				// add all resolved file URLs
				bundles.addAll(artifactURLs)
			}
			
			if (beforeInstallBundles) {
				beforeInstallBundles(runtime)
			}
			
			// install bundles
			bundles?.each { bundle ->
				log.info "installing bundle $bundle"
				boolean autoStart = getRuntimeProperty("autoStart", true)
				
				if ((bundle instanceof Map)
					&& bundle?.url) {
					bundle = bundle.url
				}
				
				if ((bundle instanceof Map)
					|| (bundle.toString().startsWith('mvn:'))) {
					log.error("unresolved bundle: " + bundle);
				}
				
				try {
					runtime.install(bundle, autoStart)
				}
				catch (Exception e) {
					log.error("failed to install bundle $bundle: " + e.getMessage())
					log.debug("details: ", e)
				}
			}
			
			if (afterInstallBundles) {
				afterInstallBundles(runtime)
			}
		}
		
		runtime
	}
	
	/**
	 * Resolve bundles and return list of local URLs.
	 * 
	 * @param bundles list of bundle specs.
	 * 			Each element is either 
	 * 			an URL (file, http, ..., mvn:group:module:version) or
	 * 			a Map with the elements group, name, and version
	 * 
	 * @return list of local URLs (as String)
	 * 
	 * @throws Exception in case of resolve errors
	 */
	List<String> resolveBundles(List bundles) throws Exception {
		List<URL> artifactURLs = new ArrayList<URL>()
		
		// application name and version are dummy values
		IvyDependencyManager manager = new IvyDependencyManager("groovyx.osgi", "1.0")
		
		def ivySettings = manager.ivySettings
		
		String logLevel = args.resolverLogLevel ?: "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
		
		String equinoxRepository = null
		if (args.equinoxDrop && args.equinoxMirror) {
			def equinoxDrop = args.equinoxDrop
			def equinoxMirror = args.equinoxMirror
			equinoxRepository = "$equinoxMirror/$equinoxDrop".toString()
			// make sure we have only single '/', if the eclipse mirror ended with '/'
			equinoxRepository = equinoxRepository.replace("//", "/")
		}
		
		def dependencies = {
			log logLevel
			
			// add user repositories
			if (repositoriesConfig) {
				repositories repositoriesConfig 
			}
			
			// check whether to add default repositories
			def defaultRepositories = true
			if (args.containsKey('defaultRepositories')) {
				defaultRepositories = args.defaultRepositories
			}
			if (defaultRepositories) {
				// default repos
				repositories {
					mavenLocal()
					ebr()
					mavenCentral()
					
					mavenRepo "http://maven.springframework.org/milestone"
					mavenRepo 'http://repository.ops4j.org/maven2/'
					
					mavenRepo 'http://s3.amazonaws.com/maven.springframework.org/osgi'
					mavenRepo 'http://s3.amazonaws.com/maven.springframework.org/milestone'
					
					if (equinoxRepository) {
						// configure resolver for Eclipse Equinox OSGi framework
						def equinoxResolver = new org.apache.ivy.plugins.resolver.URLResolver(name: 'Equinox' )
						equinoxResolver.addArtifactPattern("${equinoxRepository}/[organisation].[module]_[revision].[ext]")
						equinoxResolver.settings = ivySettings
						equinoxResolver.latestStrategy = new org.apache.ivy.plugins.latest.LatestTimeStrategy()
						equinoxResolver.changingPattern = ".*SNAPSHOT"
						equinoxResolver.setCheckmodified(true)
						resolver equinoxResolver
					}
				}
			}
			
			if (bundles) {
				dependencies {
					bundles.each{ dep ->
						runtime (dep) {
							transitive = false
						}
					}
				}
			}
		}
		
		// parse bundles/dependencies from above
		manager.parseDependencies(dependencies)
		
		// resolve bundles
		def report = manager.resolveDependencies()
		if(report.hasError()) {
			log.error """
There was an error resolving the dependencies.
This could be because you have passed an invalid dependency name or because the dependency was not found in one of the default repositories.
Try passing a valid Maven repository with the --repository argument."""
			report.allProblemMessages.each { problem -> log.error ": $problem" }
			throw new RuntimeException("failed to resolve some modules")
		}
		else {
			/*
			for (def artifactDownloadReport in report.getAllArtifactsReports()) {
				def artifact = artifactDownloadReport.getArtifact()
				if (artifactDownloadReport.localFile) {
					artifactURLs << artifactDownloadReport.localFile.toURL().toString()
				}
			}
			*/
			artifactURLs = report.allArtifactsReports*.localFile*.toURL()*.toString()
			//artifactURLs.each { url ->
			//	println url
			//}
		}
		
		
		artifactURLs
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
		
		File cwd = new File(System.getProperty('user.dir'))
		File runtimeDir = args?.runtimeDir ? new File(args?.runtimeDir.toString()) :  new File(cwd, 'system')
		File dropinsDir = (this.dropinsDir instanceof File ? this.dropinsDir : new File(runtimeDir, this.dropinsDir?.toString() ?: 'dropins'))
		
		if (!runtimeDir.exists()) {
			runtimeDir.mkdirs()
		}
		if (!dropinsDir.exists()) {
			dropinsDir.mkdirs()
		}
		
		
		def osgiRuntimePath = runtimeDir.absolutePath
		
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
		
		System.setProperty("bundles.configuration.location", dropinsDir.canonicalPath)	// PAX ConfMan
		System.setProperty("felix.fileinstall.dir", dropinsDir.canonicalPath)			// Felix FileInstall
		System.setProperty("felix.fileinstall.debug", "1")
		// for OSGi HttpService
		System.setProperty("org.osgi.service.http.port", "8081")
		
		this.runtime = createRuntime(framework, runtimeProperties)
		
		this.runtime.osgiRuntimePath = osgiRuntimePath
		this.runtime.dropinsDir = dropinsDir
		
		// configure runtime
		configureRuntime(this.runtime)
		
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
	 * Configure OSGi runtime. The closure has access
	 * to all methods provided by this builder.
	 * 
	 * @param closure configuration closure
	 * 
	 * @return this builder instance
	 */
	def configure(Closure closure) {
		def cl = closure.clone()
		cl.delegate = this
		cl.setResolveStrategy(Closure.DELEGATE_FIRST)
		cl()
		
		this
	}
	
	/**
	 * Configure OSGi runtime. The file is loaded as
	 * Groovy {@link Script} and can access the predefined
	 * method {@link #configure(Closure)} and the
	 * builder instance as property <pre>builder</pre>.
	 * 
	 * @param file file with configuration script
	 * 
	 * @return this builder instance
	 */
	def configure(File file) {
		// parse file as Groovy script using GroovyShell
		Binding binding = new Binding()
		setupBinding(binding)
		
		GroovyShell shell = new GroovyShell(binding)
		shell.evaluate(file)
		
		this
	}
	
	/**
	 * Configure OSGi runtime. The {@link Script} can access 
	 * the predefined method {@link #configure(Closure)} 
	 * and the builder instance as property <pre>builder</pre>.
	 * 
	 * @param script configuration script
	 * 
	 * @return this builder instance
	 */
	def configure(Script script) {
		// evaluate script
		Binding binding = new Binding()
		setupBinding(binding)
		
		script.setBinding(binding)
		script.run()
		
		this
	}
	
	/**
	 * Configure OSGi runtime. The stream is loaded as
	 * Groovy {@link Script} and can access the predefined
	 * method {@link #configure(Closure)} and the
	 * builder instance as property <pre>builder</pre>.
	 * 
	 * @param input {@link InputStream} containing the
	 * 			configuration script
	 * 
	 * @return this builder instance
	 */
	def configure(InputStream input) {
		configure(new InputStreamReader(input))
		
		this
	}
	
	/**
	 * Configure OSGi runtime. The content is loaded as
	 * Groovy {@link Script} and can access the predefined
	 * method {@link #configure(Closure)} and the
	 * builder instance as property <pre>builder</pre>.
	 * 
	 * @param input {@link Reader} containing the configuration 
	 * 				script
	 * 
	 * @return this builder instance
	 */
	def configure(Reader input) {
		// parse stream as Groovy script using GroovyShell
		Binding binding = new Binding()
		setupBinding(binding)
		
		GroovyShell shell = new GroovyShell(binding)
		shell.evaluate(input)
		
		this
	}
	
	
	/**
	 * Configure OSGi runtime. The resource is loaded as
	 * Groovy {@link Script} and can access the predefined
	 * method {@link #configure(Closure)} and the
	 * builder instance as property <pre>builder</pre>.
	 * 
	 * @param url url of the configuration script
	 * 
	 * @return this builder instance
	 */
	def configure(URL url) {
		configure(url.openStream())
		
		this
	}
	
	/**
	 * Configure OSGi runtime. The content is loaded as
	 * Groovy {@link Script} and can access the predefined
	 * method {@link #configure(Closure)} and the
	 * builder instance as property <pre>builder</pre>.
	 * 
	 * @param input either url or text containing the 
	 * 			configuration script
	 * 
	 * @return this builder instance
	 */
	def configure(CharSequence input) {
		String text = input.toString()
		
		// if first 20 chars contain a ':', we try to parse text as URL
		if (text.substring(0, 20).contains(":")) {
			try {
				// check whether String is an URL
				URL url = new URL(input)
				configure(url)
			}
			catch (MalformedURLException e) {
				// interpret as script
				configure(new StringReader(text))
			}
		}
		else {
			// interpret as script
			configure(new StringReader(text))
		}
		
		this
	}
	
	/**
	 * Configure using an array of configuration elements.
	 * 
	 * @param args array of configuration elements. See 
	 * 			{@link #configure(List)} for supported types
	 * 
	 * @return this builder instance
	 */
	def configure(Object[] args) {
		configure(args as List)
		
		this
	}
	
	/**
	 * Configure using an array of configuration elements. 
	 * 
	 * <p>
	 * Supported types:<br />
	 * <ul>
	 * <li>{@link Reader}, see {@link #configure(Reader)}</li>
	 * <li>{@link InputStream}, see {@link #configure(InputStream)}</li>
	 * <li>{@link Script}, see {@link #configure(Script)}</li>
	 * <li>{@link File}, see {@link #configure(File)}</li>
	 * <li>{@link URL}, see {@link #configure(URL)}</li>
	 * <li>{@link CharSequence}, see {@link #configure(CharSequence)}</li>
	 * </ul>
	 * </p>
	 * 
	 * @param list list of configuration elements.
	 * 
	 * @return this builder instance
	 */
	def configure(List list) {
		list.each { item ->
			switch (item) {
				case Reader: configure(item as Reader); break;
				case InputStream: configure(item as InputStream); break;
				case Script: configure(item as Script); break;
				case File: configure(item as File); break;
				case URL: configure(item as URL); break;
				case CharSequence: configure(item as CharSequence); break;
				default: throw new IllegalArgumentException("invalid configuration item");
			}
		}
		
		this
	}
	
	/**
	* Set runtime args.
	*
	* @param closure configuration closure
	*
	* @return this builder instance
	*/
   def args(Closure closure) {
	   def cl = closure.clone()
	   cl.delegate = args
	   cl.setResolveStrategy(Closure.DELEGATE_FIRST)
	   cl()
	   
	   this
   }
	
	def repositories(Closure closure) {
		repositoriesConfig = closure
	}
	
	/**
	 * Add bundles to be installed.
	 *  
	 * @param closure bundle configuration closure
	 * 
	 * @return this builder instance
	 */
	def bundles(Closure closure) {
		Closure cl = closure.clone()
		cl.delegate = this
		cl.setResolveStrategy(Closure.DELEGATE_FIRST)
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
		bundle(null, args, null)
		
		this
	}
	
	/**
	 * Add bundle to be installed.
	 *  
	 * @param specs bundle specs in format 'group:name:version'
	 * 
	 * @return this builder instance
	 */
	def bundle(CharSequence specs) {
		bundle(specs, null)
		
		this
	}
	
	/**
	* Add bundle to be installed.
	* 
	* @param specs bundle specs in format 'group:name:version'
	* @param closure bundle configuration closure
	*/
	def bundle(CharSequence specs, Closure closure) {
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
	def bundle(CharSequence specs, Map args, Closure closure) {
		if (closure) {
			def cl = closure.clone()
			cl.delegate = this
			cl.setResolveStrategy(Closure.DELEGATE_FIRST)
			cl(specs)
		}
		
		if (args) {
			bundles << args
		}
		else if (specs) {
			bundles << specs.toString()
		}
		
		this
	}
	
	public static void main(String[] args) {
		run(args)
	}
	
	/**
	 * Run OSGi environment.
	 * 
	 * <p>Example:
	 * <pre>
	 * @GrabResolver(name='ebrRelease', root='http://repository.springsource.com/maven/bundles/release')
	 * @GrabResolver(name='ebrExternal', root='http://repository.springsource.com/maven/bundles/external')
	 * @Grapes([
	 * 	@GrabConfig(systemClassLoader=true),
	 * 	@Grab(group='groovyx.osgi', module='groovyx.osgi.runtime'),
	 * 	@Grab(group='org.eclipse.osgi', module='org.eclipse.osgi', version='3.6.1.R36x_v20100806'),
	 * 	@Grab(group='org.apache.commons', module='com.springsource.org.apache.commons.logging', version='1.1.1')
	 * ])
	 * import groovyx.osgi.runtime.OsgiRuntimeBuilder
	 *  
	 * OsgiRuntimeBuilder.run {
	 * 		framework 'equinox'
	 * 
	 * 		bundle 'mvn:org.apache.felix:org.apache.felix.fileinstall:3.0.2'
	 * 
	 * 		afterStart = {
	 * 			println "started OSGi runtime"
	 * 		}
	 * 
	 * 		doRun = {
	 * 			// wait one minute
	 * 			Thread.sleep 30000
	 * 		}
	 * 
	 * 		afterStop = {
	 * 			println "started OSGi runtime"
	 * 		}
	 * }
	 * </pre>
	 * </p>
	 * 
	 * @param args configuration args. See {@link #configure(List)} for supported types
	 */
	public static void run(def args) {
		// create builder and configure using all args
		OsgiRuntimeBuilder builder = new OsgiRuntimeBuilder()
		builder.configure(args)
		
		// build and start runtime
		OsgiRuntime runtime = builder.build()
		builder.doStart()
		
		// wait for finish
		builder.waitForFinish()
		
		// stop runtime
		builder.doStop()
	}
}
