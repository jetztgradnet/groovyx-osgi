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

package groovyx.osgi.test;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;

import org.eclipse.gemini.blueprint.test.provisioning.ArtifactLocator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

class LocalFileSystemIvyRepository implements ArtifactLocator {
	public final static String DEFAULT_TYPE = "jar";
	
	private final static String SLASH_CHAR = "/";
	private final static String USER_HOME_PROPERTY = "user.home";
	
	private final File repositoryDir;
	
	public LocalFileSystemIvyRepository() {
		this(".ivy2/cache");
	}
	
	public LocalFileSystemIvyRepository(String baseDir) {
		this(getFileFromPath(baseDir));
	}
	
	public LocalFileSystemIvyRepository(File baseDir) {
		this.repositoryDir = baseDir;
	}
	
	public static File getFileFromPath(String path) {
		// use privileged action to get user home
		String userHome = AccessController.doPrivileged(new PrivilegedAction<String>() {

			public String run() {
				return System.getProperty(USER_HOME_PROPERTY);
			}
		});
		
		File home = new File(userHome);
		return new File(home, path);
	}

	public Resource locateArtifact(String group, String id, String version, String type) {
			
		type = type != null ? type : DEFAULT_TYPE;
		
		// path is <baseDir>/<group>/<id>/<type>s/<id>-<version>.jar
		StringBuilder location = new StringBuilder();
		if (group != null) {
			// group
			location.append(group);
			location.append(SLASH_CHAR);
		}
		
		// id
		location.append(id);
		
		location.append(SLASH_CHAR);
		// type is always in plural form
		location.append(type);
		location.append("s");
		
		// id and version
		location.append(SLASH_CHAR);
		location.append(id);
		location.append("-");
		location.append(version);
		location.append(".jar");
		
		return new FileSystemResource(new File(repositoryDir, location.toString()));
	}

	public Resource locateArtifact(String group, String id, String version) {
		return locateArtifact(group, id, version, DEFAULT_TYPE);
	}

}
