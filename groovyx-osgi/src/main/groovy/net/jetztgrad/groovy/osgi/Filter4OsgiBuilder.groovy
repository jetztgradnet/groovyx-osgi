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

package net.jetztgrad.groovy.osgi

import groovy.lang.Closure;
import groovyx.osgi.FilterBuilder;

import org.filter4osgi.builder.FilterBuilder
import org.filter4osgi.builder.FilterBuilder.*

/**
 * This {@link FilterBuilder} uses an existing filter builder from 
 * <a href="http://code.google.com/p/filter4osgi/">filter4osgi</a>
 * (Apache License 2.0).
 * 
 * @author Wolfgang Schell
 * @author Hamlet D'Arcy
 */
class Filter4OsgiBuilder implements groovyx.osgi.FilterBuilder {
	
	/**
	 * This list acts as a stack of lists, each of which contains 
	 * zero or more filters from current DSL evaluation level.
	 * 
	 * Example:
	 * 
	 * <pre>
	 * {
	 * 	and {
	 * 		eq('name', 'value')
	 * 		not(eq('name', 'value')
	 * 	}
	 * }
	 * </pre>
	 */
	private List filterStack = []

	public String build(Closure closure) {
		FilterBuilder builder = and(closure)
		return builder ? builder.toString() : null
	}
	
	/**
	 * Add filter to top list on filter stack.
	 * 
	 * @param f
	 * @return
	 */
	protected FilterBuilder addToStack(FilterBuilder filter) {
		if (!filter) {
			return null
		}
		if (!filterStack) {
			return filter
		}
		List filters = filterStack.last()
		filters.push(filter)
		
		filter
	}
	
	protected FilterBuilder[] combine(Closure closure) {
		filterStack << []
		
		Closure cl = closure.clone();
		cl.delegate = this
		cl()
		
		def filters = []
		if (filterStack) {
			filters = filterStack.pop() 
		}
		
		filters as FilterBuilder[]
	}
	
	/**
     * Creates an and (&...) filter
     *
     * @param filters previous filters
     * @return an and filter
     */
	FilterBuilder and(Closure closure) {
		// execute closure
		FilterBuilder[] filters = combine(closure)
		if (filters) {
			FilterBuilder f
			if (filters.length > 1) {
				// combine filters using AND
				f = FilterBuilder.and(filters)
			}
			else {
				f = filters[0]
			}
			addToStack(f)
			return f
		}
		null
	}
	
	/**
     * Creates an or (|...) filter
     *
     * @param filters previous filters
     * @return an or filter
     */
	FilterBuilder or(Closure closure) {
		// execute closure
		FilterBuilder[] filters = combine(closure)
		if (filters) {
			FilterBuilder f
			if (filters.length > 1) {
				// combine filters using OR
				f = FilterBuilder.or(filters)
			}
			else {
				f = filters[0]
			}
			addToStack(f)
			return f
		}
		null
	}
	
	/**
     * Creates a not (!...) filter
     *
     * @param target previous filter
     * @return a not filter
     */
    FilterBuilder not(Closure closure) {
		// execute closure
		FilterBuilder[] filters = combine(closure)
		if (filters) {
			if (filters.length > 1) {
				throw new RuntimeException("Not: more than filter expression to negate");
			}
			// negate filter
			return not(filters[0])
		}
		null
    }
	
	/**
     * Creates a not (!...) filter
     *
     * @param target previous filter
     * @return a not filter
     */
    FilterBuilder not(FilterBuilder filter) {
		FilterBuilder f = FilterBuilder.not(filter)
		addToStack(f)
		return f
	}
	
    /**
     * Creates an equals (... = ...) filter
     *
     * @param attribute attribute
     * @param value     value
     * @return an equals filter
     */
    FilterBuilder eq(String attribute, Object value) {
		FilterBuilder f = FilterBuilder.eq(attribute, value)
		addToStack(f)
        return f
    }

    /**
     * Creates an approximate (... ~= ...) filter
     *
     * @param attribute attribute
     * @param value     value
     * @return an approximate filter
     */
    FilterBuilder approx(String attribute, Object value) {
		FilterBuilder f = FilterBuilder.approx(attribute, value)
		addToStack(f)
        return f
    }

    /**
     * Creates a greater than or equal (... >= ...) filter
     *
     * @param attribute attribute
     * @param value     value
     * @return an greater than or equal filter
     */
    FilterBuilder gte(String attribute, Object value) {
		FilterBuilder f = FilterBuilder.gte(attribute, value)
		addToStack(f)
        return f
    }

    /**
     * Creates a less than or equal (... <= ...) filter
     *
     * @param attribute attribute
     * @param value     value
     * @return an greater than or equal filter
     */
    FilterBuilder lte(String attribute, Object value) {
		FilterBuilder f = FilterBuilder.lte(attribute, value)
		addToStack(f)
        return f
    }

    /**
     * Creates an exists (... =*) filter
     *
     * @param attribute attribute
     * @return an exists filter
     */
    FilterBuilder exists(String attribute) {
		FilterBuilder f = FilterBuilder.exists(attribute)
		addToStack(f)
        return f
    }

    /**
     * Creates an isType (objectClass= ...) filter
     *
     * @param type expected type
     * @return an isType filter
     */
    FilterBuilder isType(Class type) {
		FilterBuilder f = FilterBuilder.isType(type)
		addToStack(f)
        return f
    }

    /**
     * Creates an ends with (... =*...) filter
     *
     * @param attribute attribute
     * @param substring substring that target should end with
     * @return an endsWith filter
     */
    FilterBuilder endsWith(String attribute, String substring) {
		FilterBuilder f = FilterBuilder.endsWith(attribute, substring)
		addToStack(f)
        return f
    }

    /**
     * Creates a starts with (... =...*) filter
     *
     * @param attribute attribute
     * @param substring substring that target should start with
     * @return an startsWith filter
     */
    FilterBuilder startsWith(String attribute, String substring) {
		FilterBuilder f = FilterBuilder.startsWith(attribute, substring)
		addToStack(f)
        return f
    }

    /**
     * Creates a contains with (... =*...*) filter
     *
     * @param attribute attribute
     * @param substring substring that target should contain
     * @return an contains filter
     */
    FilterBuilder contains(String attribute, String substring) {
		FilterBuilder f = FilterBuilder.contains(attribute, substring)
		addToStack(f)
        return f
    }
}
