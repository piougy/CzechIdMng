/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2009 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://opensource.org/licenses/cddl1.php
 * See the License for the specific language governing permissions and limitations
 * under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://opensource.org/licenses/cddl1.php.
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 * Portions Copyrighted 2014 ForgeRock AS.
 */
package eu.bcvsolutions.idm.ic.filter.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.filter.api.IcFilter;

/**
 * IcFilterBuilder creates a {@linkplain IcFilter filter} that will
 * {@linkplain IcFilter#accept match} any {@code ConnectorObject} that satisfies
 * all of the selection criteria that were specified using this builder.
 *
 * @author Will Droste
 * @since 1.0
 */
public final class IcFilterBuilder {

    /**
     * Select only an input <code>ConnectorObject</code> with a value for the
     * specified <code>Attribute</code> that
     * <em>contains as a final substring</em> the value of the specified
     * <code>Attribute</code>.
     * <p>
     * For example, if the specified <code>Attribute</code> were
     * <code>{"hairColor": "d"}</code>, <br>
     * this would match any <code>ConnectorObject</code> with a value such as <br>
     * &nbsp;&nbsp;&nbsp;&nbsp; <code>{"hairColor": "red"}</code> or <br>
     * &nbsp;&nbsp;&nbsp;&nbsp; <code>{"hairColor": "blond"}</code> <br>
     * but would <em>not</em> match any <code>ConnectorObject</code> that
     * contains only values such as <br>
     * &nbsp;&nbsp;&nbsp;&nbsp; <code>{"hairColor": "blonde"}</code> or <br>
     * &nbsp;&nbsp;&nbsp;&nbsp; <code>{"hairColor": "auburn"}</code>. <br>
     * This also would <em>not</em> match any <code>ConnectorObject</code> that
     * contains only <code>{"hairColor": null}</code> <br>
     * or that lacks the attribute <code>"hairColor"</code>.
     *
     * @param attr
     *            <code>Attribute</code> <em>containing exactly one value</em>
     *            to test against each value of the corresponding
     *            <code>ConnectorObject</code> attribute.
     * @return an instance of <code>Filter</code> whose <code>accept()</code>
     *         method will return <code>true</code> if at least one value of the
     *         corresponding attribute of the <code>ConnectorObject</code>
     *         <em>contains as its last part</em> the value of the specified
     *         <code>Attribute</code>; otherwise <code>false</code>.
     */
    public static IcFilter endsWith(final IcAttribute attr) {
        return new IcEndsWithFilter(attr);
    }

    /**
     * Select only an input <code>ConnectorObject</code> with a value for the
     * specified <code>Attribute</code> that contains as an
     * <em>initial substring</em> the value of the specified
     * <code>Attribute</code>.
     * <p>
     * For example, if the specified <code>Attribute</code> were
     * <code>{"hairColor": "b"}</code>, <br>
     * this would match any <code>ConnectorObject</code> with a value such as <br>
     * &nbsp;&nbsp;&nbsp;&nbsp; <code>{"hairColor": "brown"}</code> or <br>
     * &nbsp;&nbsp;&nbsp;&nbsp; <code>{"hairColor": "blond"}</code> <br>
     * but would <em>not</em> match any <code>ConnectorObject</code> that
     * contains only values such as <br>
     * &nbsp;&nbsp;&nbsp;&nbsp; <code>{"hairColor": "red"}</code> or <br>
     * &nbsp;&nbsp;&nbsp;&nbsp; <code>{"hairColor": "auburn"}</code>. <br>
     * This also would <em>not</em> match any <code>ConnectorObject</code> that
     * contains only <code>{"hairColor": null}</code> <br>
     * or that lacks the attribute <code>"hairColor"</code>.
     *
     * @param attr
     *            <code>Attribute</code> <em>containing exactly one value</em>
     *            to test against each value of the corresponding
     *            <code>ConnectorObject</code> attribute.
     * @return an instance of <code>Filter</code> whose <code>accept()</code>
     *         method will return <code>true</code> if at least one value of the
     *         corresponding attribute of the <code>ConnectorObject</code>
     *         <em>contains as its first part</em> the value of the specified
     *         <code>Attribute</code>; otherwise <code>false</code>.
     */
    public static IcFilter startsWith(final IcAttribute attr) {
        return new IcStartsWithFilter(attr);
    }

    /**
     * Select only an input <code>ConnectorObject</code> with a value for the
     * specified <code>Attribute</code> that <em>contains as any substring</em>
     * the value of the specified <code>Attribute</code>.
     * <p>
     * For example, if the specified <code>Attribute</code> were
     * <code>{"hairColor": "a"}</code>, <br>
     * this would match any <code>ConnectorObject</code> with a value such as <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "auburn"}</code> or <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "gray"}</code> <br>
     * but would <em>not</em> match any <code>ConnectorObject</code> that
     * contains only <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "red"}</code> or <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "grey"}</code>. <br>
     * This also would <em>not</em> match any <code>ConnectorObject</code> that
     * contains only <code>{"hairColor": null}</code> <br>
     * or that lacks the attribute <code>"hairColor"</code>.
     *
     * @param attr
     *            <code>Attribute</code> <em>containing exactly one</em> value
     *            to test against each value of the corresponding
     *            <code>ConnectorObject</code> attribute.
     * @return an instance of <code>Filter</code> whose <code>accept()</code>
     *         method will return <code>true</code> if at least one value of the
     *         corresponding attribute of the <code>ConnectorObject</code>
     *         <em>contains anywhere within it</em> the value of the specified
     *         <code>Attribute</code>; otherwise <code>false</code>.
     */
    public static IcFilter contains(final IcAttribute attr) {
        return new IcContainsFilter(attr);
    }

    /**
     * Select only an input <code>ConnectorObject</code> with a value for the
     * specified <code>Attribute</code> that is <em>lexically equal to</em> the
     * value of the specified <code>Attribute</code>.
     * <p>
     * <b>NOTE: Is comparison case-sensitive?</b>
     * <p>
     * For example, if the specified <code>Attribute</code> were
     * <code>{"hairColor": "brown"}</code>, <br>
     * this would match any <code>ConnectorObject</code> with a value such as <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "brown"}</code> or <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "BROWN"}</code> <br>
     * but would <em>not</em> match any <code>ConnectorObject</code> that
     * contains only <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "brownish-gray"}</code> or <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "auburn"}</code>. <br>
     * This also would <em>not</em> match any <code>ConnectorObject</code> that
     * contains only <code>{"hairColor": null}</code> <br>
     * or that lacks the attribute <code>"hairColor"</code>.
     * <p>
     * <b>NOTE:</b> <i>Lexical</i> comparison of two string values compares the
     * characters of each value, even if the string values could be interpreted
     * as numeric. The values <code>"01"</code> and <code>"1"</code> are unequal
     * lexically, although they would be equivalent arithmetically.
     * <p>
     * Two attributes with binary syntax are equal if and only if their
     * constituent bytes match.
     *
     * @param attr
     *            <code>Attribute</code> <em>containing exactly one value</em>
     *            to test against each value of the corresponding
     *            <code>ConnectorObject</code> attribute.
     * @return an instance of <code>Filter</code> whose <code>accept()</code>
     *         method will return <code>true</code> if at least one value of the
     *         corresponding attribute of the <code>ConnectorObject</code>
     *         <em>matches lexically</em> the value of the specified
     *         <code>Attribute</code>; otherwise <code>false</code>.
     */
    public static IcFilter equalTo(final IcAttribute attr) {
        return new IcEqualsFilter(attr);
    }
    
    /**
     * Select only an input <code>ConnectorObject</code> with a value for the
     * specified <code>Attribute</code> that is <em>lexically less than</em> the
     * value of the specified <code>Attribute</code>.
     * <p>
     * <b>NOTE: Is comparison case-sensitive?</b>
     * <p>
     * For example, if the specified <code>Attribute</code> were
     * <code>{"hairColor": "brown"}</code>, <br>
     * this would match any <code>ConnectorObject</code> with a value such as <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "black"}</code> or <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "blond"}</code> or <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "auburn"}</code> <br>
     * but would <em>not</em> match any <code>ConnectorObject</code> that
     * contains only <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "brown"}</code> or <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "brownish-gray"}</code> or <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "red"}</code> <br>
     * This also would <em>not</em> match any <code>ConnectorObject</code> that
     * contains only <code>{"hairColor": null}</code> <br>
     * or that lacks the attribute <code>"hairColor"</code>.
     * <p>
     * <b>NOTE:</b> <i>Lexical</i> comparison of two string values compares the
     * characters of each value, even if the string values could be interpreted
     * as numeric. <br>
     * When compared lexically, <code>"99"</code> is greater than
     * <code>"123"</code>. <br>
     * When compared arithmetically, <code>99</code> is less than
     * <code>123</code>.
     *
     * @param attr
     *            <code>Attribute</code> <em>containing exactly one value</em>
     *            to test against each value of the corresponding
     *            <code>ConnectorObject</code> attribute.
     * @return an instance of <code>Filter</code> whose <code>accept()</code>
     *         method will return <code>true</code> if at least one value of the
     *         corresponding attribute of the <code>ConnectorObject</code>
     *         <em>sorts alphabetically before</em> the value of the specified
     *         <code>Attribute</code>; otherwise <code>false</code>.
     */
    public static IcFilter lessThan(final IcAttribute attr) {
        return new IcLessThanFilter(attr);
    }

    /**
     * Select only an input <code>ConnectorObject</code> with a value for the
     * specified <code>Attribute</code> that is <em>lexically greater than</em>
     * the value of the specified <code>Attribute</code>.
     * <p>
     * <b>NOTE: Is comparison case-sensitive?</b>
     * <p>
     * For example, if the specified <code>Attribute</code> were
     * <code>{"hairColor": "brown"}</code>, <br>
     * this would match any <code>ConnectorObject</code> with a value such as <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "brownish-gray"}</code> or <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "red"}</code> <br>
     * but would <em>not</em> match any <code>ConnectorObject</code> that
     * contains only <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "brown"}</code> or <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "black"}</code> or <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "blond"}</code> or <br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<code>{"hairColor": "auburn"}</code>. <br>
     * This also would <em>not</em> match any <code>ConnectorObject</code> that
     * contains only <code>{"hairColor": null}</code> <br>
     * or that lacks the attribute <code>"hairColor"</code>.
     * <p>
     * <b>NOTE:</b> <i>Lexical</i> comparison of two string values compares the
     * characters of each value, even if the string values could be interpreted
     * as numeric. <br>
     * When compared lexically, <code>"99"</code> is greater than
     * <code>"123"</code>. <br>
     * When compared arithmetically, <code>99</code> is less than
     * <code>123</code>.
     *
     * @param attr
     *            <code>Attribute</code> <em>containing exactly one value</em>
     *            to test against each value of the corresponding
     *            <code>ConnectorObject</code> attribute.
     * @return an instance of <code>Filter</code> whose <code>accept()</code>
     *         method will return <code>true</code> if at least one value of the
     *         corresponding attribute of the <code>ConnectorObject</code>
     *         <em>sorts alphabetically after</em> the value of the specified
     *         <code>Attribute</code>; otherwise <code>false</code>.
     */
    public static IcFilter greaterThan(final IcAttribute attr) {
        return new IcGreaterThanFilter(attr);
    }


    
    /**
     * Logically "ANDs" together the two specified instances of {@link IcFilter}.
     * The resulting <i>conjunct</i> <code>Filter</code> is true if and only if
     * both of the specified filters are true.
     *
     * @param leftHandSide
     *            left-hand-side filter.
     * @param rightHandSide
     *            right-hand-side filter.
     * @return the result of
     *         <code>(leftHandSide &amp;&amp; rightHandSide)</code>
     */
    public static IcFilter and(final IcFilter leftHandSide, final IcFilter rightHandSide) {
        return new IcAndFilter(leftHandSide, rightHandSide);
    }

    /**
     * Creates a new "AND" filter using the provided list of sub-filters.
     * <p>
     * Creating a new "AND" filter with a {@code null} or empty list of
     * sub-filters is equivalent to calling "alwaysTrue".
     *
     * @param subFilters
     *            The list of sub-filters, may be empty or {@code null}.
     * @return The newly created "AND" filter.
     */
    public static IcFilter and(final Collection<IcFilter> subFilters) {
        switch (subFilters.size()) {
            case 0:
                return null;
            case 1:
                return subFilters.iterator().next();
            default:
                return new IcAndFilter(new ArrayList<IcFilter>(subFilters));
        }
    }

    /**
     * Creates a new "AND" filter using the provided list of sub-filters.
     * <p>
     * Creating a new "AND" filter with a {@code null} or empty list of
     * sub-filters is equivalent to calling "alwaysTrue".
     *
     * @param subFilters
     *            The list of sub-filters, may be empty or {@code null}.
     * @return The newly created "AND" filter.
     */
    public static IcFilter and(final IcFilter... subFilters) {
        return and(Arrays.asList(subFilters));
    }


    /**
     * Logically "OR" together the two specified instances of {@link IcFilter}.
     * The resulting <i>disjunct</i> <code>Filter</code> is true if and only if
     * at least one of the specified filters is true.
     *
     * @param leftHandSide
     *            left-hand-side filter.
     * @param rightHandSide
     *            right-hand-side filter.
     * @return the result of <code>(leftHandSide || rightHandSide)</code>
     */
    public static IcFilter or(final IcFilter leftHandSide, final IcFilter rightHandSide) {
        return new IcOrFilter(leftHandSide, rightHandSide);
    }

    /**
     * Creates a new "OR" filter using the provided list of sub-filters.
     * <p>
     * Creating a new "OR" filter with a {@code null} or empty list of
     * sub-filters is equivalent to  "alwaysTrue".
     *
     * @param subFilters
     *            The list of sub-filters, may be empty or {@code null}.
     * @return The newly created {@code or} filter.
     */
    public static IcFilter or(final Collection<IcFilter> subFilters) {
        switch (subFilters.size()) {
            case 0:
                return null;
            case 1:
                return subFilters.iterator().next();
            default:
                return new IcOrFilter(new ArrayList<IcFilter>(subFilters));
        }
    }

    /**
     * Creates a new "OR" filter using the provided list of sub-filters.
     * <p>
     * Creating a new "OR" filter with a {@code null} or empty list of
     * sub-filters is equivalent to  "alwaysTrue".
     *
     * @param subFilters
     *            The list of sub-filters, may be empty or {@code null}.
     * @return The newly created {@code or} filter.
     */
    public static IcFilter or(final IcFilter... subFilters) {
        return or(Arrays.asList(subFilters));
    }


    /**
     * Logically negate the specified {@link IcFilter}. The resulting
     * <code>Filter</code> is true if and only if the specified filter is false.
     *
     * @param filter
     *            the <code>Filter</code> to negate.
     * @return the result of <code>(!filter)</code>.
     */
    public static IcFilter not(final IcFilter filter) {
        return new IcNotFilter(filter);
    }
}
