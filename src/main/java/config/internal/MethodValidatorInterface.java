package config.internal;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Predicate;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;


@Value.Immutable
@Value.Style(visibility=ImplementationVisibility.PUBLIC, typeImmutable="*",typeAbstract="*Interface")
public interface MethodValidatorInterface {
	@Value.Parameter
	Method getMethod();
	
	@Value.Parameter
	String getKey();
	
	@Value.Parameter
	Optional<Predicate<String>> getValidator();
}
