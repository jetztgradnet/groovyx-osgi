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
