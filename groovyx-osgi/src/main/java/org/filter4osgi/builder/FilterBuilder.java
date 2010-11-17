package org.filter4osgi.builder;

/**
 * The FilterBuilder class provides a useful API for working with LDAP style
 * constraints. The methods are meant to be chained together to create a more
 * readable declaration of the filter than a simple static String provides. <br/><br/>
 * Here are some Groovy examples of how to use this simple library to construct LDAP style filters:
 * <code><pre>
 * import static org.filter4osgi.builder.FilterBuilder.*
 * <p/>
 * def result = and(
 *   eq('mailboxName', 'welcome'),
 *   eq('lang', 'en')
 * )
 * <p/>
 * assert '(&(mailboxName=welcome) (lang=en))' == result.toString()
 * </pre></code>
 * or a more complex example:
 * <code><pre>
 * <p/>
 * def result = or(
 *   eq('mailboxName', 'welcome'),
 *   and(
 *     eq('lang', 'de'),
 *     eq('mailboxID', '5')
 *   ),
 *   and(
 *     eq('lang', 'en_CA'),
 *     eq('mailboxID', '9')
 *   )
 * )
 * <p/>
 * assert '(|(mailboxName=welcome) (&(lang=de) (mailboxID=5)) (&(lang=en_CA) (mailboxID=9)))' == result.toString()
 * </pre></code>
 *
 * @author Hamlet D'Arcy
 */
public class FilterBuilder {

    private final String base;

    /**
     * Not meant for public consumption.
     *
     * @param base base filter that will be wrapped.
     */
    private FilterBuilder(String base) {
        this.base = base;
    }

    /**
     * Creates an and (&...) filter
     *
     * @param filters previous filters
     * @return an and filter
     */
    public static FilterBuilder and(FilterBuilder... filters) {
        if (filters == null) throw new RuntimeException("Null: filters");
        if (filters.length == 0) throw new RuntimeException("Empty: filters");
        return new FilterBuilder("(&" + joinArray(filters, ' ') + ")");
    }

    /**
     * Creates an or (|...) filter
     *
     * @param filters previous filters
     * @return an or filter
     */
    public static FilterBuilder or(FilterBuilder... filters) {
        if (filters == null) throw new RuntimeException("Null: filters");
        if (filters.length == 0) throw new RuntimeException("Empty: filters");
        return new FilterBuilder("(|" + joinArray(filters, ' ') + ")");
    }

    /**
     * Creates a not (!...) filter
     *
     * @param target previous filter
     * @return a not filter
     */
    public static FilterBuilder not(FilterBuilder target) {
        if (target == null) throw new RuntimeException("Null: target");
        return new FilterBuilder("(!" + target + ")");
    }

    /**
     * Creates an equals (... = ...) filter
     *
     * @param attribute attribute
     * @param value     value
     * @return an equals filter
     */
    public static FilterBuilder eq(String attribute, Object value) {
        if (attribute == null) throw new RuntimeException("Null: attribute");
        if (value == null) throw new RuntimeException("Null: value");
        return new FilterBuilder("(" + attribute + "=" + value + ")");
    }

    /**
     * Creates an approximate (... ~= ...) filter
     *
     * @param attribute attribute
     * @param value     value
     * @return an approximate filter
     */
    public static FilterBuilder approx(String attribute, Object value) {
        if (attribute == null) throw new RuntimeException("Null: attribute");
        if (value == null) throw new RuntimeException("Null: value");
        return new FilterBuilder("(" + attribute + "~=" + value + ")");
    }

    /**
     * Creates a greater than or equal (... >= ...) filter
     *
     * @param attribute attribute
     * @param value     value
     * @return an greater than or equal filter
     */
    public static FilterBuilder gte(String attribute, Object value) {
        if (attribute == null) throw new RuntimeException("Null: attribute");
        if (value == null) throw new RuntimeException("Null: value");
        return new FilterBuilder("(" + attribute + ">=" + value + ")");
    }

    /**
     * Creates a less than or equal (... <= ...) filter
     *
     * @param attribute attribute
     * @param value     value
     * @return an greater than or equal filter
     */
    public static FilterBuilder lte(String attribute, Object value) {
        if (attribute == null) throw new RuntimeException("Null: attribute");
        if (value == null) throw new RuntimeException("Null: value");
        return new FilterBuilder("(" + attribute + "<=" + value + ")");
    }

    /**
     * Creates an exists (... =*) filter
     *
     * @param attribute attribute
     * @return an exists filter
     */
    public static FilterBuilder exists(String attribute) {
        if (attribute == null) throw new RuntimeException("Null: attribute");
        return new FilterBuilder("(" + attribute + "=*)");
    }

    /**
     * Creates an isType (objectClass= ...) filter
     *
     * @param type expected type
     * @return an isType filter
     */
    public static FilterBuilder isType(Class type) {
        if (type == null) throw new RuntimeException("Null: type");
        return new FilterBuilder("(objectClass=" + type.getName() + ")");
    }

    /**
     * Creates an ends with (... =*...) filter
     *
     * @param attribute attribute
     * @param substring substring that target should end with
     * @return an endsWith filter
     */
    public static FilterBuilder endsWith(String attribute, String substring) {
        if (attribute == null) throw new RuntimeException("Null: attribute");
        if (substring == null) throw new RuntimeException("Null: substring");
        return new FilterBuilder("(" + attribute + "=*" + substring + ")");
    }

    /**
     * Creates a starts with (... =...*) filter
     *
     * @param attribute attribute
     * @param substring substring that target should start with
     * @return an startsWith filter
     */
    public static FilterBuilder startsWith(String attribute, String substring) {
        if (attribute == null) throw new RuntimeException("Null: attribute");
        if (substring == null) throw new RuntimeException("Null: substring");
        return new FilterBuilder("(" + attribute + "=" + substring + "*)");
    }

    /**
     * Creates a contains with (... =*...*) filter
     *
     * @param attribute attribute
     * @param substring substring that target should contain
     * @return an contains filter
     */
    public static FilterBuilder contains(String attribute, String substring) {
        if (attribute == null) throw new RuntimeException("Null: attribute");
        if (substring == null) throw new RuntimeException("Null: substring");
        return new FilterBuilder("(" + attribute + "=*" + substring + "*)");
    }

    /**
     * Helper method to join an array.
     *
     * @param array array to join
     * @param join  character to delimit entries
     * @return String form
     */
    private static String joinArray(Object[] array, char join) {
        final StringBuilder result = new StringBuilder();
        for (int x = 0; x < array.length; x++) {
            result.append(array[x]);
            if (x < (array.length - 1)) {
                result.append(join);
            }
        }
        return result.toString();
    }

    /**
     * Returns the filter in LDAP filter representation.
     *
     * @return filter in LDAP filter representation
     */
    @Override
    public String toString() {
        return base; 
    }
}