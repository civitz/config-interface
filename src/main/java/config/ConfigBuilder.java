package config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Stream;

import config.annotations.ConfigurationKey;
import config.annotations.ValidateWith;
import config.internal.ConfigParameters;
import config.internal.ConfigParameters.Builder;
import config.internal.MethodValidator;

public class ConfigBuilder implements InvocationHandler {

	private static final Predicate<String> DEFAULT_VALIDATOR = x->true;
	private Properties props = new Properties();
	private ConfigParameters parameters;

	public ConfigBuilder(ConfigParameters parameters) {
		this.parameters = parameters;
		try {
			props.load(new FileInputStream(new File(parameters.getPath())));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T create(Class<T> configInterface, String path) {

		ConfigParameters parameters = validateInterface(configInterface, path);

		return (T) Proxy.newProxyInstance(configInterface.getClassLoader(), new Class<?>[] { configInterface },
				new ConfigBuilder(parameters));
	}

	@SuppressWarnings("unchecked")
	private static <T> ConfigParameters validateInterface(Class<T> configInterface, String path) {
		if(! configInterface.isInterface()){
			throw new IllegalArgumentException(configInterface.getName() + "is not an interface");
		}
		Builder builder = ConfigParameters.builder();
		builder.path(path);
		Stream.of(configInterface.getDeclaredMethods())
			.map(x -> {
				String key = getAnnotationOrDie(x, ConfigurationKey.class, "every method must have the configuration key").value();
				Optional<Predicate<String>> maybeValidator = getAnnotation(x, ValidateWith.class)
						.map(ValidateWith::value)
						.map(ConfigBuilder::safeNewInstance);
				return MethodValidator.of(x,key,  maybeValidator);
			})
			.forEach(mv->{
				builder.putConfigurationKeys(mv.getMethod().getName(), mv.getKey());
				mv.getValidator().ifPresent(v-> builder.putValidators(mv.getMethod().getName(), v));
			});
		
		return builder.build();
	}

	private static <T> T safeNewInstance(Class<? extends T> validatorClass) {
		try {
			return (T) validatorClass.newInstance();
		} catch (IllegalAccessException|InstantiationException e) {
			throw new RuntimeException(e);
		}
	}

	private static <A extends Annotation> Optional<A> getAnnotation(Method m, Class<A> annotation) {
		return Optional.ofNullable(m.getAnnotation(annotation));
	}
	
	private static <A extends Annotation> A getAnnotationOrDie(Method m, Class<A> annotation, final String message) {
		return getAnnotation(m, annotation)
				.orElseThrow(() -> new IllegalArgumentException(annotation.getName() + " not present, " + message));
	}

	private static <T, A extends Annotation> Optional<A> getAnnotation(Class<T> configInterface,
			final Class<A> configPathAnnotation) {
		final A annotation = configInterface.getAnnotation(configPathAnnotation);
		return Optional.ofNullable(annotation);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// If the method is a method from Object then defer to normal
		// invocation.
		if (method.getDeclaringClass() == Object.class) {
			return method.invoke(this, args);
		}

		final String methodName = method.getName();
		Predicate<String> validator = parameters.getValidators().getOrDefault(methodName, DEFAULT_VALIDATOR);
		String confKey = Optional.ofNullable(parameters.getConfigurationKeys().get(methodName))
				.orElseThrow(() -> new IllegalArgumentException("No conf key for method " + methodName));
		
		String property = Optional.ofNullable(props.getProperty(confKey))
				.orElseThrow(()->new IllegalArgumentException("No value for property "+confKey));
		
		if(validator.test(property)){
			return property;
		}else{
			throw new IllegalArgumentException("Property "+ confKey + "has invalid value" );
		}
	}
}
