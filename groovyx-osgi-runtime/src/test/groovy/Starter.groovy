import org.osgi.framework.BundleContext

import groovyx.osgi.runtime.*
import org.codehaus.groovy.osgi.runtime.*
import org.codehaus.groovy.osgi.runtime.equinox.EquinoxRuntime;
import org.codehaus.groovy.osgi.runtime.resolve.*


def osgiDependencies = {
}

println "resolving OSGi dependencies..."
def bundleFiles = []
def manager = new IvyDependencyManager("myapp", "1.2.3")
manager.parseDependencies(osgiDependencies)

def report = manager.resolveDependencies()
if(report.hasError()) {
	println """
There was an error resolving the OSGi dependencies.
This could be because you have passed an invalid dependency name or because the dependency was not found in one of the default repositories.
Try passing a valid Maven repository with the --repository argument."""
	report.allProblemMessages.each { problem -> println ": $problem" }
	exit 1
}
else {
	bundleFiles = report.allArtifactsReports*.localFile
	//bundleFiles.each { file ->
	//	println file.name					
	//}
}

def osgiRuntimePath = new File(new File(warName).parentFile, 'osgi').canonicalPath
if (argsMap?.clean) {
	try {
		File dir = new File(osgiRuntimePath)
		if (dir.exists()) {
			println "cleaning up osgi runtime directory..."
			dir.deleteDir()
		}
	}
	catch (Throwable t) {
		println "failed to cleanup osgi runtime directory: " + t.message
	}
}

try {
	EquinoxRuntime runner = new EquinoxRuntime(argsMap: argsMap,
											buildSettings: grailsSettings,
											osgiRuntimePath: osgiRuntimePath, 
											systemPackages: systemPackages)
	BundleContext ctx = runner.start()

	// install and start infrastructure bundles
	def bundles = runner.install(bundleFiles, false)

	// start bundles required for logging
	runner.start([
		'org.eclipse.osgi.util',
		'org.eclipse.osgi.services',
		'org.eclipse.equinox.common',
		
		// we need these for logging configuration:
		'org.apache.felix.configadmin',
		'org.apache.felix.fileinstall',
	])

	// configure logging
	runner.configureLogging()

	// start other bundles
	runner.start(bundles)

	// install and start grails app bundle
	if (!argsMap?.noApp) {
		println "installing and starting grails app bundle: $warName"
		runner.install(new File(warName), true)
	}
	else {
		println "skipped installing grails app bundle: $grailsAppName"
	}
}
catch (Throwable t) {
	println "***ERROR***: " + t.message
	t.printStackTrace()
}