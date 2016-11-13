package com.nitin;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.nitin.config.AppConfig;
import com.nitin.guice.module.AppModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandaloneAppServer
{
    private static final Logger log = LoggerFactory.getLogger(StandaloneAppServer.class);

    private final JettyServer jettyServer;

    @Inject
    public StandaloneAppServer(final JettyServer jettyServer)
    {
        this.jettyServer = jettyServer;
    }

    private void start()
    {
        try {
            log.info("Starting the Jetty server...");
            jettyServer.start();
            log.info("Jetty server has started");
        }
        catch (Exception ex) {
            log.error("Error when stopping servers: " + ex.getMessage(), ex);
        }
    }

    private void stop()
    {
        try {
            log.info("Shutting down the Jetty server...");
            jettyServer.stop();
            log.info("Jetty server has stopped");
        }
        catch (Exception ex) {
            log.error("Error when stopping servers: " + ex.getMessage(), ex);
        }
    }

    public static void main(final String... args) throws Exception
    {
        final long startTime = System.currentTimeMillis();

        // Stage.PRODUCTION is mandatory for jmxutils
        final Injector injector = Guice.createInjector(Stage.PRODUCTION, new AppModule());
        final StandaloneAppServer server = injector.getInstance(StandaloneAppServer.class);

        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                server.stop();
            }
        });

        server.start();

        final long secondsToStart = (System.currentTimeMillis() - startTime) / 1000;
        log.info(String.format("App Server initialized in %d:%02d", secondsToStart / 60, secondsToStart % 60));

        Thread.currentThread().join();
    }
}
