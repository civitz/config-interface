package config.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.Predicate;

@Retention(RUNTIME)
@Target({METHOD})
public @interface ValidateWith {
	Class<? extends Predicate<String>> value();
}
