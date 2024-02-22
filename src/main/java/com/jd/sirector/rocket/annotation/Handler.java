package com.jd.sirector.rocket.annotation;

import java.lang.annotation.*;

/**
 * <p>
 *     方法注解，注解为Handler，配合Pipeline使用
 * </p>
 *
 * @author : zhusong
 * @date : 2020-05-22 21:53
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Inherited
public @interface Handler {
    /**
     * 是否是最后一个handler
     */
    boolean end() default false;

    /**
     * 是否是第一个handler
     */
    boolean begin() default false;
    /**
     * @return handler的别名
     */
    String alias() default "";

}
