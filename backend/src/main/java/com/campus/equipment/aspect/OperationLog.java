package com.campus.equipment.aspect;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OperationLog {
    String module() default "";
    String operation() default "";
    boolean saveParams() default true;
    boolean saveResult() default false;
}
