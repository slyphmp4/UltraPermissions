package me.TechsCode.UltraPermissions.base.item.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jetbrains.annotations.ApiStatus;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.SOURCE)
@Documented
@ApiStatus.Internal
public @interface XInfo {
   String since();

   String removedSince() default "";
}
