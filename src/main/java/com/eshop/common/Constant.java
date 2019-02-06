package com.eshop.common;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * Description: Constant Interface means a group of constants
 * Created by Jingtao Liu on 2018/6/28.
 */
public class Constant {
    public static final String CURRENT_USER = "currentUser";

    public static final String EMAIL = "email";
    public static final String USERNAME = "username";

    public interface Role{
        int ROLE_CUSTOMER = 0;
        int ROLE_ADMINISTRTOR =1;
    }

    public enum ProductStatusEnum {
        ON_SALE(1,"on sale"),
        OFF_SALE(0,"off sale");
        private int code;
        private String value;
        ProductStatusEnum(int code,String value){
            this.code = code;
            this.value = value;
        }
        public int getCode() {
            return code;
        }
        public String getValue() {
            return value;
        }
    }

    public interface ProductListOrderBy{
        Set<String> PRICE_ORDER_BY = Sets.newHashSet("price_desc","price_asc");
    }

    public interface Cart{
        int TICK = 1;// a product is ticked in cart
        int UNTICK = 0;//a product is unticked in cart

        String LIMIT_NUM_FAIL = "LIMIT_NUM_FAIL";
        String LIMIT_NUM_SUCCESS = "LIMIT_NUM_SUCCESS";
    }
}
