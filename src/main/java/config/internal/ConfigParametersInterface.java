package config.internal;

import java.util.Map;
import java.util.function.Predicate;

import org.immutables.value.Value;
import org.immutables.value.Value.Style.ImplementationVisibility;

@Value.Immutable
@Value.Style(visibility=ImplementationVisibility.PUBLIC, typeImmutable="*",typeAbstract="*Interface")
public interface ConfigParametersInterface {
	@Value.Parameter
	String getPath();

	Map<String, Predicate<String>> getValidators();
	
	Map<String, String> getConfigurationKeys();
}
