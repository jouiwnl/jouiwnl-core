package com.jouiwnl.core.utils;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

public class CoreUtils {

    public static <T> BigDecimal converToBigDecimal(T number) {
        return (BigDecimal) number;
    }

}
