import java.util.jar.JarFile

/*
This script scans all jars in a Eclipse .classpath file for 
patterns of files, e.g. java packages, etc.
*/

if (args.size() < 2) {
	println "usage: finddeps.groovy xmlfile filepattern"
}
                  
def home = System.getProperty('user.home')
def jinspect = "${home}/bin/jinspect"
def input = new File(args[0])
def pattern = args[1]
def entries = new XmlSlurper().parse(input).classpathentry

entries.@path.each { path ->
	def jar = new File(path.text()) 
	//println jar.name
	if (!jar.name.endsWith('.jar')) {
		return
	}
	def jarFile = new JarFile(jar)
	def osgiEntries = jarFile.entries().findAll { entry -> entry.name.startsWith(pattern) }
	println "$jinspect ${jar.path}"
	osgiEntries.each { entry -> println "\t$entry"}
}
