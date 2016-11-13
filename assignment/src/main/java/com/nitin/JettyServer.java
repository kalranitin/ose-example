package com.nitin;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.servlet.GuiceFilter;
import com.nitin.config.AppConfig;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.EventListener;

import javax.servlet.DispatcherType;

@Singleton
public class JettyServer
{
    private static final Logger log = LoggerFactory.getLogger(JettyServer.class);

    private final AppConfig config;
    private final Injector injector;
    private boolean initialized = false;
    private Server server;

    @Inject
    public JettyServer(final AppConfig config, final Injector injector)
    {
        this.config = config;
        this.injector = injector;
    }

    public void start() throws Exception
    {
        final long startTime = System.currentTimeMillis();

        server = new Server();

        final Connector connector = new SelectChannelConnector();
//        connector.setHost(config.getLocalIp());
        connector.setPort(config.getServerPort());
        connector.setLowResourceMaxIdleTime((int) config.getJettyLowResourcesMaxIdleTime().getMillis());
        connector.setMaxIdleTime((int) config.getJettyMaxIdleTime().getMillis());
        server.addConnector(connector);
        

        server.setStopAtShutdown(true);

        final QueuedThreadPool threadPool = new QueuedThreadPool();
        threadPool.setName("jetty-threadPool");
        threadPool.setMinThreads(config.getJettyMinThreads());
        threadPool.setMaxThreads(config.getJettyMaxThreads());
        threadPool.setMaxIdleTimeMs((int) config.getJettyMaxIdleTime().getMillis());
        server.setThreadPool(threadPool);
        
        final ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        
        context.addEventListener(new JettyListener(injector));

        // Jersey insists on using java.util.logging (JUL)
        final EventListener listener = new SetupJULBridge();
        context.addEventListener(listener);

        // Make sure Guice filter all requests
        final FilterHolder filterHolder = new FilterHolder(GuiceFilter.class);
        context.addFilter(filterHolder, "/*", EnumSet.of(DispatcherType.REQUEST, DispatcherType.ASYNC));

        // Backend servlet for Guice - never used
        final ServletHolder sh = new ServletHolder(DefaultServlet.class);
        context.addServlet(sh, "/*");
        
        /*final String webappDirLocation = "src/main/webapp/";
        
        WebAppContext root = new WebAppContext();

        root.setContextPath("/");
        root.setDescriptor(webappDirLocation+"/WEB-INF/web.xml");
        root.setResourceBase(webappDirLocation);
        
        //Parent loader priority is a class loader setting that Jetty accepts.
        //By default Jetty will behave like most web containers in that it will
        //allow your application to replace non-server libraries that are part of the
        //container. Setting parent loader priority to true changes this behavior.
        //Read more here: http://wiki.eclipse.org/Jetty/Reference/Jetty_Classloading
        root.setParentLoaderPriority(true);
        
        server.setHandler(root);*/

        server.start();

        final long secondsToStart = (System.currentTimeMillis() - startTime) / 1000;
        log.info(String.format("Jetty server started in %d:%02d", secondsToStart / 60, secondsToStart % 60));

        initialized = true;
    }

    public void stop()
    {
        if (!initialized) {
            return;
        }

        try {
            server.stop();
        }
        catch (Exception e) {
            log.warn("Got exception trying to stop Jetty", e);
        }
    }

    /**
     * Has Jetty finished its startup sequence?
     *
     * @return true iff Jetty has been setup
     */
    public boolean isInitialized()
    {
        return initialized;
    }
}
