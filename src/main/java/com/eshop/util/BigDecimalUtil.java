package com.eshop.util;

import java.math.BigDecimal;

/**
 * Description: To handle the money precision in business,
 * it must use the constructor with string parameter in BigDecimal
 * Created by Jingtao Liu on 4/02/2019.
 */
public class BigDecimalUtil {

    private BigDecimalUtil(){ // cannot new BigDecimalUtil();

    }

    //Addition
    public static BigDecimal add(double m1, double m2){
        BigDecimal b1 = new BigDecimal(Double.toString(m1));
        BigDecimal b2 = new BigDecimal(Double.toString(m2));
        return b1.add(b2);
    }
    //Subtraction
    public static BigDecimal subtract(double m1, double m2){
        BigDecimal b1 = new BigDecimal(Double.toString(m1));
        BigDecimal b2 = new BigDecimal(Double.toString(m2));
        return b1.subtract(b2);
    }
    //Multiplication
    public static BigDecimal multiply(double m1, double m2){
        BigDecimal b1 = new BigDecimal(Double.toString(m1));
        BigDecimal b2 = new BigDecimal(Double.toString(m2));
        return b1.multiply(b2);
    }
    //Division
    public static BigDecimal divide(double m1, double m2){
        BigDecimal b1 = new BigDecimal(Double.toString(m1));
        BigDecimal b2 = new BigDecimal(Double.toString(m2));
        return b1.divide(b2,2,BigDecimal.ROUND_HALF_UP);
    }
}
