/* Copyright 2004-2005 the original author or authors.
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

package org.filter4osgi.builder.integration.testing
import junit.framework.TestCase;

import org.osgi.framework.BundleContext

import groovyx.osgi.test.AbstractGroovyxOsgiTests

import static org.filter4osgi.builder.FilterBuilder.*

/**
 * Integration test.
 * @author Hamlet D'Arcy
 */
class GroovyIntegrationTest extends TestCase {
    public void test_BundleIsUsable() {
        def result = and(
                eq('mailbox', 'email'),
                eq('lang', 'EN_US')
        ).toString()

        assertEquals('Wrong filter', '(&(mailbox=email) (lang=EN_US))', result)
    }
	
	public void test_EscapeValues() {
        def result = eq('filepattern', 'c:\\path\\file.(*)').toString()

        assertEquals('Filter should contain escaped value', '(filepattern=c:\\\\path\\\\file.\\(\\*\\))', result)
    }
}
