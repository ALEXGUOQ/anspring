package org.xsj.android.spring.core.db.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Transactional {
	String value() default "";
	boolean readOnly() default false;
	Class<? extends Throwable>[] rollbackFor() default {RuntimeException.class};
	Class<? extends Throwable>[] noRollbackFor() default {};
}
