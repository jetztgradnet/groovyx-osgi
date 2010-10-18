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
