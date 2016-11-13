package com.nitin;

import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;


public class JettyListener extends GuiceServletContextListener
{
    private final Injector injector;

    public JettyListener(final Injector injector)
    {
        this.injector = injector;
    }

    @Override
    protected Injector getInjector()
    {
        return injector;
    }
}
