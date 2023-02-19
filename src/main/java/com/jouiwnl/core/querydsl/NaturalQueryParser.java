package com.jouiwnl.core.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.*;
import lombok.AllArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import java.time.LocalDate;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * A class to parse a filter written in natural language to be interpreted by querydsl
 * @author jouiwnl
 */
@AllArgsConstructor
public class NaturalQueryParser {

    /**
     * This method parse a natural filter to a queryDsl BooleanBuilder.
     * @param filter String filter in natural language (something = anotherThing)
     * @param clazz The class that have the expressions
     * @return A BooleanBuilder with the natural filter parsed
     */
    public static <T extends Filterable> BooleanBuilder parse(String filter, Class<T> clazz) {
        if (filter == null || filter.isEmpty()) {
            return null;
        }

        List<ComparableExpressionBase> expressions = new ArrayList<>();

        try {
            expressions = clazz.newInstance().getFilters();
        } catch (InstantiationException e) {
            throw new RuntimeException("Failed to instance class!");
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access class!");
        }

        String simpleClassName = clazz.getSimpleName().toLowerCase();
        filter = normalizeFilter(filter);


        PathBuilder<T> entityPath = new PathBuilder<>(clazz, simpleClassName);
        String[] andFilters = filter.split("(?i)\\s+and\\s+");
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        for (String parcialFilter : andFilters) {
            BooleanBuilder parcialFilterBuilder = new BooleanBuilder();

            if (parcialFilter.contains("or")) {
                String[] subFilters = parcialFilter.split("(?i)\\s+or\\s+");
                for (String subFilter : subFilters) {
                    parcialFilterBuilder.or(getPredicate(subFilter, entityPath, "OR", simpleClassName, expressions));
                }
            } else {
                parcialFilterBuilder.and(getPredicate(parcialFilter, entityPath, "AND", simpleClassName, expressions));
            }

            booleanBuilder.and(parcialFilterBuilder);
        }

        return booleanBuilder;
    }

    private static BooleanBuilder getPredicate(String filter,
                                               PathBuilder entityPath,
                                               String operador,
                                               String className,
                                               List<ComparableExpressionBase> possibleExpressions) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();

        List<String> tokens = Arrays.stream(filter.split(" ")).filter(StringUtils::isNotBlank).collect(Collectors.toList());
        String field = tokens.get(0);
        String pathName = className + "." + field;

        List<String> expressionsString = possibleExpressions
                .stream()
                .map(Objects::toString)
                .collect(Collectors.toList());

        if (!expressionsString.contains(pathName)) {
            throw new IllegalArgumentException("Only this expressions " + expressionsString + " are available.");
        }

        ComparableExpressionBase expression = possibleExpressions.stream()
                .filter(exp -> exp.toString().equals(pathName))
                .findFirst()
                .orElse(null);

        String operator = tokens.get(1);
        String value = String.join(" ", tokens.subList(2, tokens.size()));
        BooleanExpression exp = getExpressionPath(value, expression, operator);

        if ("OR".equals(operador)) {
            booleanBuilder.or(exp);
        } else if ("AND".equals(operador)) {
            booleanBuilder.and(exp);
        }

        return booleanBuilder;
    }

    private static BooleanExpression getExpressionPath(String value, ComparableExpressionBase expression, String operator) {
        if (Temporal.class.isAssignableFrom(expression.getType())) {
            return getExpression(
                    (DatePath) expression,
                    operator,
                    Arrays.stream(value.split(",")).map(String::trim).map(LocalDate::parse).collect(Collectors.toList())
            );
        }

        if (Number.class.isAssignableFrom(expression.getType())) {
            return getExpression(
                    (NumberPath) expression,
                    operator,
                    Arrays.stream(value.split(",")).map(String::trim).map(NumberUtils::createNumber).collect(Collectors.toList())
            );
        }

        if (expression.getType().isEnum()) {
            return getExpression(
                    (EnumPath) expression,
                    operator,
                    Arrays.stream(value.split(",")).map(String::trim).map(v -> Enum.valueOf(expression.getType(), v)).collect(Collectors.toList())
            );
        }

        if (Boolean.class.isAssignableFrom(expression.getType())) {
            return getExpression(
                    (BooleanPath) expression,
                    operator,
                    Boolean.valueOf(value)
            );
        }

        return getExpression((StringPath) expression, operator, value);
    }

    private static BooleanExpression getExpression(StringPath path, String operator, String value) {
        switch (operator) {
            case "=":
                return wrapUnaccent(path).containsIgnoreCase(wrapUnaccent(value));
            case "!=":
                return wrapUnaccent(path).ne(wrapUnaccent(value));
            case ">":
                return wrapUnaccent(path).gt(wrapUnaccent(value));
            case ">=":
                return wrapUnaccent(path).goe(wrapUnaccent(value));
            case "<":
                return wrapUnaccent(path).lt(wrapUnaccent(value));
            case "<=":
                return wrapUnaccent(path).loe(wrapUnaccent(value));
            case "in":
                String[] values = value.split(",");
                return wrapUnaccent(path).in(values);
            case "not in":
                String[] notInValues = value.split(",");
                return wrapUnaccent(path).notIn(notInValues);
            default:
                throw new IllegalArgumentException("Invalid operator: " + operator);
        }
    }

    private static BooleanExpression getExpression(NumberPath path, String operator, List<Number> numberValues) {
        Number firstValue = numberValues.get(0);
        switch (operator) {
            case "=":
                return path.eq(firstValue);
            case "!=":
                return path.ne(firstValue);
            case ">":
                return path.gt(firstValue);
            case ">=":
                return path.goe(firstValue);
            case "<":
                return path.lt(firstValue);
            case "<=":
                return path.loe(firstValue);
            case "in":
                return path.in(numberValues);
            case "not in":
                return path.notIn(numberValues);
            default:
                throw new IllegalArgumentException("Invalid operator: " + operator);
        }
    }

    private static BooleanExpression getExpression(DatePath path, String operator, List<LocalDate> dateValues) {
        LocalDate firstValue = dateValues.get(0);
        switch (operator) {
            case "=":
                return path.eq(firstValue);
            case "!=":
                return path.ne(firstValue);
            case ">":
                return path.after(firstValue);
            case ">=":
                return path.goe(firstValue);
            case "<":
                return path.before(firstValue);
            case "<=":
                return path.loe(firstValue);
            case "in":
                return path.in(dateValues);
            case "not in":
                return path.notIn(dateValues);
            default:
                throw new IllegalArgumentException("Invalid operator: " + operator);
        }
    }

    private static BooleanExpression getExpression(EnumPath path, String operator, List<Enum> enumValues) {
        Enum firstValue = enumValues.get(0);
        switch (operator) {
            case "=":
                return path.eq(firstValue);
            case "!=":
                return path.ne(firstValue);
            case ">":
                return path.gt(firstValue);
            case ">=":
                return path.goe(firstValue);
            case "<":
                return path.lt(firstValue);
            case "<=":
                return path.loe(firstValue);
            case "in":
                return path.in(enumValues);
            case "not in":
                return path.notIn(enumValues);
            default:
                throw new IllegalArgumentException("Invalid operator: " + operator);
        }
    }

    private static BooleanExpression getExpression(BooleanPath path, String operator, Boolean value) {
        switch (operator) {
            case "=":
                return path.eq(value);
            case "!=":
                return path.ne(value);
            default:
                throw new IllegalArgumentException("Invalid operator: " + operator);
        }
    }

    private static String normalizeFilter(String filter) {
        filter = filter.replaceAll("OR", "or");
        filter = filter.replaceAll("AND", "and");
        filter = filter.replaceAll("\\(", "").replaceAll("\\)", "");

        return filter;
    }

    private static StringExpression wrapUnaccent(Object value) {
        return Expressions.stringTemplate("unaccent({0})", value);
    }

}
