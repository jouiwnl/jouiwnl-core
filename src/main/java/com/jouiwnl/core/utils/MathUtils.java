package com.jouiwnl.core.utils;

import org.apache.commons.lang.math.NumberUtils;

public class MathUtils {

    public static Number createNumberOrNull(String value) {
        try {
            return NumberUtils.createNumber(value);
        } catch (Exception e) {
            return null;
        }
    }

}
