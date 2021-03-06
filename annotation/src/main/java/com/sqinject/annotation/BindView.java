package com.sqinject.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//作用在成员变量上
@Target(ElementType.FIELD)
//编译时注解
@Retention(RetentionPolicy.CLASS)
public @interface BindView {

    //传递参数，此处为控件id的名称
    String value();

}
