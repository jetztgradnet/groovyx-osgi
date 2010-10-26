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

package net.jetztgrad.groovy.osgi.runtime.felix

import java.util.Properties

import groovyx.osgi.runtime.OsgiRuntime
import groovyx.osgi.runtime.OsgiRuntimeFactory

/**
* {@link OsgiRuntimeFactory} for Apache Felix OSGi runtime.
*
* @author Wolfgang Schell
*/
class FelixRuntimeFactory implements OsgiRuntimeFactory {

	/* (non-Javadoc)
	 * @see groovyx.osgi.runtime.OsgiRuntimeFactory#createRuntime(java.util.Properties)
	 */
	public OsgiRuntime createRuntime(Properties runtimeProperties)
			throws IllegalArgumentException, Exception {
		return new FelixRuntime(runtimeProperties)
	}
}
