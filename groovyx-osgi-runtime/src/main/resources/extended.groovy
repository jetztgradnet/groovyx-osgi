
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

// used bundles
allBundles = []

systemBundles = [ 
	'mvn:org.eclipse.osgi:util:3.2.100.v20100503',
	'mvn:org.eclipse.osgi:services:3.2.100.v20100503',
	'mvn:org.eclipse.equinox:common:3.6.0.v20100503',
	'mvn:org.apache.felix:org.apache.felix.configadmin:1.2.4',
	'mvn:org.apache.felix:org.apache.felix.fileinstall:3.0.0',
]
allBundles << systemBundles

loggingBundles = [
    'mvn:org.apache.log4j:com.springsource.org.apache.log4j:1.2.15',
    'mvn:org.ops4j.pax.logging:pax-logging-api:1.4',
	'mvn:org.ops4j.pax.logging:pax-logging-service:1.4',
	
	//'org.slf4j:slf4j-api:1.5.10',
	//'org.slf4j:slf4j-log4j12:1.5.10',
	//'org.slf4j:jcl-over-slf4j:1.5.10',
]
allBundles << loggingBundles

springVersion = '3.0.4.RELEASE'
springBundles = [
	"mvn:org.springframework:org.springframework.aop:$springVersion",
	"mvn:org.springframework:org.springframework.asm:$springVersion",
	"mvn:org.springframework:org.springframework.aspects:$springVersion",
	"mvn:org.springframework:org.springframework.beans:$springVersion",
	"mvn:org.springframework:org.springframework.context:$springVersion",
	"mvn:org.springframework:org.springframework.context.support:$springVersion",
	"mvn:org.springframework:org.springframework.core:$springVersion",
	"mvn:org.springframework:org.springframework.expression:$springVersion",
	"mvn:org.springframework:org.springframework.instrument:$springVersion",
	//"mvn:org.springframework:org.springframework.instrument.tomcat:$springVersion",
	"mvn:org.springframework:org.springframework.jdbc:$springVersion",
	"mvn:org.springframework:org.springframework.jms:$springVersion",
	"mvn:org.springframework:org.springframework.orm:$springVersion",
	"mvn:org.springframework:org.springframework.oxm:$springVersion",
	"mvn:org.springframework:org.springframework.transaction:$springVersion",
	"mvn:org.springframework:org.springframework.web:$springVersion",
	"mvn:org.springframework:org.springframework.web.servlet:$springVersion",
	//"mvn:org.springframework:org.springframework.web.portlet:$springVersion",
	
	//"mvn:org.springframework:org.springframework.test:$springVersion",
	
	// Spring dependencies
	'mvn:org.aopalliance:com.springsource.org.aopalliance:1.0.0',
	//'mvn:org.objectweb.asm:com.springsource.org.objectweb.asm:2.2.3',
	//'mvn:net.sourceforge.cglib:com.springsource.net.sf.cglib:2.1.3',
	'mvn:net.sourceforge.cglib:com.springsource.net.sf.cglib:2.2.0',
]
//allBundles << springBundles

webDeps = [
	'mvn:javax.annotation:com.springsource.javax.annotation:1.0.0',
	'mvn:javax.el:com.springsource.javax.el:1.0.0',
	'mvn:javax.ejb:com.springsource.javax.ejb:3.0.0',
	'mvn:javax.mail:com.springsource.javax.mail:1.4.1',
	'mvn:javax.persistence:com.springsource.javax.persistence:1.99.0',
	'mvn:javax.transaction:com.springsource.javax.transaction:1.1.0',
	'mvn:javax.servlet:com.springsource.javax.servlet:2.5.0',
	'mvn:javax.servlet:com.springsource.javax.servlet.jsp:2.1.0',
	'mvn:javax.servlet:com.springsource.javax.servlet.jsp.jstl:1.2.0',
	'mvn:javax.jms:com.springsource.javax.jms:1.1.0',
	'mvn:javax.xml.rpc:com.springsource.javax.xml.rpc:1.1.0',
	
	// contained in Equinox bundle:
//	'mvn:javax.activation:com.springsource.javax.activation:1.1.1',
//	'mvn:javax.xml.bind:com.springsource.javax.xml.bind:2.1.7',
//	'mvn:javax.xml.soap:com.springsource.javax.xml.soap:1.3.0',
//	'mvn:javax.xml.stream:com.springsource.javax.xml.stream:1.0.1',
//	'mvn:javax.xml.ws:com.springsource.javax.xml.ws:2.1.1',
]
allBundles << webDeps

jettyBundles = [
	'mvn:org.mortbay.jetty:com.springsource.org.mortbay.jetty.server:6.1.9',
	'mvn:org.mortbay.jetty:com.springsource.org.mortbay.util:6.1.9',
	// jetty starter and default configuration
	'mvn:org.springframework.osgi:jetty.start.osgi:1.0.0',
	'mvn:org.springframework.osgi:jetty.web.extender.fragment.osgi:1.0.1',
	// these are necessary to get a standard OSGi HTTP service
//	'mvn:org.eclipse.equinox:http.servlet:1.0.200.v20090520-1800',
//	'mvn:org.eclipse.equinox:http.jetty:2.0.0.v20090520-1800',
	'mvn:org.apache.felix:org.apache.felix.http.jetty:2.0.4',
]
allBundles << jettyBundles
                        
tomcatVersion = "6.0.29.S2-r1559" // "6.0.20.S2-r5956"
tomcatBundles = [
	// Tomcat starter and default configuration
	'mvn:org.springframework.osgi:catalina.start.osgi:1.0.0',
	// version 6.0.29/6.0.20
	"mvn:org.apache.catalina.springsource:com.springsource.org.apache.catalina.springsource:$tomcatVersion",
	"mvn:org.apache.jasper.springsource:com.springsource.org.apache.jasper.springsource:$tomcatVersion",
	"mvn:org.apache.jasper:com.springsource.org.apache.jasper.org.eclipse.jdt:6.0.16",
	"mvn:org.apache.coyote.springsource:com.springsource.org.apache.coyote.springsource:$tomcatVersion",
	"mvn:org.apache.juli.springsource:com.springsource.org.apache.juli.extras.springsource:$tomcatVersion",
	"mvn:org.apache.el.springsource:com.springsource.org.apache.el.springsource:$tomcatVersion",
]
//allBundles << tomcatBundles

springDMVersion = '2.0.0.M1'

springDMBundles = [
	//"mvn:org.springframework.osgi:spring-osgi-annotation:$springDMVersion",
	//"mvn:org.springframework.osgi:spring-osgi-annotation.app:$springDMVersion",
	"mvn:org.springframework.osgi:spring-osgi-core:$springDMVersion",
	"mvn:org.springframework.osgi:spring-osgi-extender:$springDMVersion",
	"mvn:org.springframework.osgi:spring-osgi-io:$springDMVersion",
	//"mvn:org.springframework.osgi:spring-osgi-mock:$springDMVersion",
	"mvn:org.springframework.osgi:spring-osgi-test:$springDMVersion",
	//"mvn:org.springframework.osgi:spring-osgi-test-support:$springDMVersion",
	"mvn:org.springframework.osgi:spring-osgi-web:$springDMVersion",
	"mvn:org.springframework.osgi:spring-osgi-web-extender:$springDMVersion",
]
//allBundles << springDMBundles

commonBundles = [
	'mvn:org.apache.commons:com.springsource.org.apache.commons.beanutils:1.8.0',
	'mvn:org.apache.commons:com.springsource.org.apache.commons.collections:3.2.1',
	'mvn:org.apache.commons:com.springsource.org.apache.commons.codec:1.3.0',
	'mvn:org.apache.commons:com.springsource.org.apache.commons.dbcp:1.2.2.osgi',
	'mvn:org.apache.commons:com.springsource.org.apache.commons.el:1.0.0',
	'mvn:org.apache.commons:com.springsource.org.apache.commons.digester:1.8.1',
	'mvn:org.apache.commons:com.springsource.org.apache.commons.fileupload:1.2.1',
	'mvn:org.apache.commons:com.springsource.org.apache.commons.httpclient:3.1.0',
	'mvn:org.apache.commons:com.springsource.org.apache.commons.io:1.4.0',
	'mvn:org.apache.commons:com.springsource.org.apache.commons.lang:2.4.0',
	'mvn:org.apache.commons:com.springsource.org.apache.commons.pool:1.5.3',
	'mvn:org.apache.commons:com.springsource.org.apache.commons.validator:1.3.1',
	
	'mvn:org.apache.oro:com.springsource.org.apache.oro:2.0.8',
	
	'mvn:org.apache.ant:com.springsource.org.apache.ivy:2.1.0',	//2.0.0',
	'mvn:org.apache.ant:com.springsource.org.apache.tools.ant:1.7.1',
	
	'mvn:org.antlr:com.springsource.antlr:2.7.7',
	
	'mvn:org.dom4j:com.springsource.org.dom4j:1.6.1',

	'mvn:org.aspectj:com.springsource.org.aspectj.runtime:1.6.8.RELEASE',
	'mvn:org.aspectj:com.springsource.org.aspectj.weaver:1.6.8.RELEASE',
	
	'mvn:com.opensymphony.sitemesh:com.springsource.com.opensymphony.sitemesh:2.4.1',
	
	'mvn:javax.persistence:com.springsource.javax.persistence:1.99.0',
	'mvn:org.jboss.javassist:com.springsource.javassist:3.9.0.GA',
	'mvn:org.objectweb.asm:com.springsource.org.objectweb.asm:1.5.3',

	'mvn:org.jboss.cache:com.springsource.org.jboss.cache:3.2.0.GA',
	'mvn:org.jboss.util:com.springsource.org.jboss.util:2.2.13.GA',
	'mvn:org.jboss.logging:com.springsource.org.jboss.logging:2.0.5.GA',
	'mvn:org.jgroups:com.springsource.org.jgroups:2.5.1',
	
	//'mvn:net.sf.ehcache:ehcache-core:1.7.1',
	'mvn:net.sourceforge.ehcache:com.springsource.net.sf.ehcache:1.6.2',
	
	'mvn:org.hibernate:com.springsource.org.hibernate.annotations.common:3.3.0.ga',
	'mvn:org.hibernate:com.springsource.org.hibernate.annotations:3.4.0.GA',
	//'mvn:org.hibernate:com.springsource.org.hibernate.ejb:3.4.0.GA',
	//'mvn:org.hibernate:com.springsource.org.hibernate.cache:3.3.2.GA',
	'mvn:org.hibernate:com.springsource.org.hibernate:3.3.2.GA',

	'mvn:org.xmlpull:com.springsource.org.xmlpull:1.1.4.c',
	'mvn:org.apache.xerces:com.springsource.org.apache.xerces:2.9.1',
	'mvn:org.apache.xalan:com.springsource.org.apache.xalan:2.7.1',
	'mvn:org.apache.xalan:com.springsource.org.apache.xml.serializer:2.7.1',
	'mvn:org.apache.xml:com.springsource.org.apache.xml.resolver:1.2.0',
	'mvn:org.apache.xmlcommons:com.springsource.org.apache.xmlcommons:1.3.4',
	'mvn:org.apache.xml:com.springsource.org.apache.xml.security:1.4.2',
]
allBundles << commonBundles

allBundles << springBundles
allBundles << springDMBundles

auxBundles = [
	'mvn:org.apache.felix:org.apache.felix.configadmin:1.2.4',
	'mvn:org.apache.felix:org.apache.felix.metatype:1.0.4',
	'mvn:org.apache.felix:org.apache.felix.webconsole:3.1.0',
	'mvn:org.apache.felix:org.apache.felix.webconsole.plugins.memoryusage:1.0.2',
]
allBundles << auxBundles

appBundles = []
allBundles << appBundles

allBundles = allBundles.flatten().collect { it.toString() }

OsgiRuntimeBuilder.run {
	// test alias
	framework 'equinox'

	runtimeDir 'system'

	args {
		resolverLogLevel = "warn"
	}
	
	// install all bundles in list
	allBundles.each { bdl ->
		bundle bdl
	}
}