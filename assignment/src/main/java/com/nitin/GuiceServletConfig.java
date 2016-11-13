package com.nitin;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.nitin.guice.module.AppModule;

public class GuiceServletConfig extends GuiceServletContextListener {
	 
	@Override
	protected Injector getInjector() {
	return Guice.createInjector(new AppModule());
	}
}
