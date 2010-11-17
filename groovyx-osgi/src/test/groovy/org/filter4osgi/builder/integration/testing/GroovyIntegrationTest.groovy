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
}
