package org.xsj.android.spr1ng.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SpringConfig {
	@Deprecated
	public String[] basePackage() default {};
	public boolean allowInjectFault() default true;
	public boolean threadSafe() default true;
	public boolean debug() default true;
}
