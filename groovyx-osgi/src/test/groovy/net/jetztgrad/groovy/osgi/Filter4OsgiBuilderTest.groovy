package net.jetztgrad.groovy.osgi

import junit.framework.TestCase

import groovyx.osgi.FilterBuilder

import net.jetztgrad.groovy.osgi.Filter4OsgiBuilder

/**
 * Test filter DSL.
 * 
 * @author Wolfgang Schell
 */
class Filter4OsgiBuilderTest extends TestCase {
	protected FilterBuilder builder
	
	protected void setUp() throws Exception {
		builder = new Filter4OsgiBuilder()
	}
	
	public void testFilterDSLUsingAnd() throws Exception {
		String filter = builder.build {
			and {
                eq('mailbox', 'email')
                eq('lang', 'EN_US')
			}
		}
		assertEquals('Wrong filter', '(&(mailbox=email) (lang=EN_US))', filter)
	}
	
	public void testFilterDSLUsingImplicitAnd() throws Exception {
		String filter = builder.build {
            eq('mailbox', 'email')
            eq('lang', 'EN_US')
		}
		assertEquals('Wrong filter', '(&(mailbox=email) (lang=EN_US))', filter)
	}
	
	public void testFilterDSLUsingOr() throws Exception {
		String filter = builder.build {
			or {
				eq('mailboxName', 'welcome')
				and {
					eq('lang', 'de')
					eq('mailboxID', '5')
				}
				and {
					eq('lang', 'en_CA')
					eq('mailboxID', '9')
				}
			}
		}
		assertEquals('Wrong filter', '(|(mailboxName=welcome) (&(lang=de) (mailboxID=5)) (&(lang=en_CA) (mailboxID=9)))', filter)
	}
}
