package com.xingray.config.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigType {

    /**
     * default
     */
    int PROPERTIES = 0;
    int JSON = 1;

    int value();
}
