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

		onStart = {
			println "started OSGi runtime"
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