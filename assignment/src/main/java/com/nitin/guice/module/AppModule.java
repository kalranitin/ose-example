package com.nitin.guice.module;

import com.nitin.config.AppConfig;
import com.nitin.processing.GuessNumberProcessor;
import com.nitin.processing.GuessNumberProcessorImpl;
import com.nitin.processing.GuessNumberStorage;
import com.nitin.processing.InMemoryGuessNumberStorage;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.guice.JerseyServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import org.eclipse.jetty.servlet.DefaultServlet;
import org.skife.config.ConfigurationObjectFactory;

import java.util.HashMap;

public class AppModule extends JerseyServletModule{

	@Override
	protected void configureServlets() {
		final ConfigurationObjectFactory configFactory = new ConfigurationObjectFactory(System.getProperties());
		final AppConfig appConfig = configFactory.build(AppConfig.class);
		bind(ConfigurationObjectFactory.class).toInstance(configFactory);
		bind(AppConfig.class).toInstance(appConfig);
		
		bind(GuessNumberStorage.class).to(InMemoryGuessNumberStorage.class).asEagerSingleton();
		bind(GuessNumberProcessor.class).to(GuessNumberProcessorImpl.class).asEagerSingleton();
		
		bind(DefaultServlet.class).asEagerSingleton();
		
		
		serveRegex("/rest(.*)").with(GuiceContainer.class, new HashMap<String, String>()
        		{
		        	{
		        		put(PackagesResourceConfig.PROPERTY_PACKAGES, "com.nitin.jaxrs.resources");
		        	}
        		});
		
        
		serve("/*").with(DefaultServlet.class);
        
		
	}

}
