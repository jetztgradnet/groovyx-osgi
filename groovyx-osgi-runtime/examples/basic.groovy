configure {
	// test alias
	framework 'equinox'

	runtimeDir 'system'

	args {
		resolverLogLevel = "warn"
	}

	bundle 'mvn:org.apache.felix:org.apache.felix.fileinstall:3.0.2'
	bundle group: 'org.apache.felix', name:'org.apache.felix.configadmin', version:'1.2.4'
}