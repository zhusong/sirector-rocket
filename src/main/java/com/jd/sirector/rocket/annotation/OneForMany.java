package com.jd.sirector.rocket.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * <p>
 *     声明类为Pipeline类型的事件编排，配合Handler注解使用, 返回结果为空
 * </p>
 *
 * @author : zhusong
 * @date : 2020-05-24 20:09
 **/
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface OneForMany {

}
