package org.xsj.android.spring.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Configuration {
//	public String[] basePackage() default {};
//	public boolean allowInjectFault() default true;
	public boolean debug() default false;
	public boolean lazyLoad() default false;
}
