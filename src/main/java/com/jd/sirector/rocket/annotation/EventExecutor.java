package com.jd.sirector.rocket.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * <p></p>
 *
 * @author : zhusong
 * @date : 2020-05-27 11:15
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface EventExecutor {
    boolean extendsAllResults() default false;
}
