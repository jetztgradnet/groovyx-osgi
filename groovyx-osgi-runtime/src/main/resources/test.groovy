// these are necessary for current version (3.6.1) of Equinox
@GrabResolver(name='ebrRelease', root='http://repository.springsource.com/maven/bundles/release')
@GrabResolver(name='ebrExternal', root='http://repository.springsource.com/maven/bundles/external')
@Grapes([
	@GrabConfig(systemClassLoader=true),
	//@Grab(group='groovyx.osgi', module='groovyx.osgi.runtime'),
	@Grab(group='org.eclipse.osgi', module='org.eclipse.osgi', version='3.6.1.R36x_v20100806'),
	@Grab(group='org.apache.commons', module='com.springsource.org.apache.commons.logging', version='1.1.1')
])
import groovyx.osgi.runtime.OsgiRuntimeBuilder

OsgiRuntimeBuilder.run {
		framework 'equinox'

		bundle 'mvn:org.apache.felix:org.apache.felix.fileinstall:3.0.2'

		beforeStart = {
			println "starting OSGi runtime"
		}
		
		afterInstallBundles = { runtime ->
			def bundleContext = runtime.bundleContext
			if (bundleContext) {
				println "installed bundles:"
				bundleContext?.bundles?.each { bundle ->
					// TODO add bundle state
					println "[${bundle.bundleId}] ${bundle.symbolicName} ${bundle.version}"
				}
			}
			else {
				println "failed to list bundles"
			}
		}

		doRun = {
			// wait one minute
			println "stopping OSGi runtime after one minute"
			Thread.sleep 60000
		}

		afterStop = {
			println "stopped OSGi runtime"
		}
}