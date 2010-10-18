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

package groovyx.osgi.runtime;

import java.util.Properties;

/**
 * Factory class for an {@link OsgiRuntime}.
 * 
 * @author Wolfgang Schell
 */
public interface OsgiRuntimeFactory {
	/**
	 * Create an {@link OsgiRuntime}. The runtime is ready to run 
	 * but not yet started.
	 * 
	 * @param runtimeProperties runtime properties. May contain both 
	 * 			standard (org.osgi.*) and implementation-specific 
	 * 			properties  (e.g. felix.* or eclipse.*)
	 * 
	 * @return ready to run {@link OsgiRuntime}
	 * 
	 * @throws IllegalArgumentException in case of illegal arguments
	 * @throws Exception in case of other errors
	 */
	OsgiRuntime createRuntime(Properties runtimeProperties) throws IllegalArgumentException, Exception;
}
