package groovyx.osgi

import org.osgi.framework.Bundle
import org.osgi.framework.BundleContext

class OsgiCategory {
	static String getStateAsText(BundleContext bundleContext) {
		return getStateAsText(bundleContext.bundle)
	}
	
	static String getStateAsText(Bundle bundle) {
		def state = "UNKNOWN"
		switch (bundle.state) {
		case BundleContext.UNINSTALLED: return "UNINSTALLED"
		case BundleContext.INSTALLED:   return "INSTALLED"  
		case BundleContext.RESOLVED:    return "RESOLVED"   
		case BundleContext.STARTING:    return "STARTING"   
		case BundleContext.STOPPING:    return "STOPPING"   
		case BundleContext.ACTIVE:      return "ACTIVE"     
		}
	} 
}
