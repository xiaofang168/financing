package com.jeff.financing.entity;

import java.lang.annotation.*;

/**
 * @author fangjie
 * @date Created in 8:23 下午 2020/11/11.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Persistence {
    String collName();
}
