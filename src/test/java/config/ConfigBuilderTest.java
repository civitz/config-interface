package config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.function.Predicate;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import config.ConfigBuilder;
import config.annotations.ConfigurationKey;
import config.annotations.ValidateWith;

public class ConfigBuilderTest {
	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	interface Yo {
		@ConfigurationKey("test")
		String getSomeValue();
		
		@ValidateWith(LengthValidator.class)
		@ConfigurationKey("test2")
		String getSomeValidatedValue();
	}
	
	@Test
	public void testSomeValue() throws Exception {
		final File propFile = tempFolder.newFile("something.properties");
		final String someValue = "somevalue";
		new Properties(){{
			setProperty("test", someValue);
		}}.store(new FileOutputStream(propFile), "");;
		
		Yo yo = ConfigBuilder.create(Yo.class, propFile.getPath());
		assertThat(yo.getSomeValue()).isEqualTo(someValue);
	}
	
	
	@Test(expected=IllegalArgumentException.class)
	public void testSomeInvalidValue() throws Exception {
		final File propFile = tempFolder.newFile("something.properties");
		final String someValue = "invalid";
		new Properties(){{
			setProperty("test2", someValue);
		}}.store(new FileOutputStream(propFile), "");;
		
		Yo yo = ConfigBuilder.create(Yo.class, propFile.getPath());
		yo.getSomeValidatedValue();
		failBecauseExceptionWasNotThrown(IllegalArgumentException.class);
		
	}
	
	public static class LengthValidator implements Predicate<String>{
		public LengthValidator() {
		}
		
		@Override
		public boolean test(String t) {
			return t.length()>10;
		}
		
	}
}
