package com.cqupt.hmi.core.ioc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CCIoCView {
	int id();

	String onClick() default "";

	String itemClick() default "";

	String longClick() default "";

	String itemLongClick() default "";
}
