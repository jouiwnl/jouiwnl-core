package com.jouiwnl.core.querydsl;

import com.querydsl.core.types.dsl.ComparableExpressionBase;

import java.util.List;

/**
 * This interface must be implemented in every class that will have some filter.
 */
public interface Filterable {

    List<ComparableExpressionBase> getFilters();
}
