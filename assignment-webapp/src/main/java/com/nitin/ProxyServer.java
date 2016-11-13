package com.nitin;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ConnectHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.nio.SelectChannelConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.ProxyServlet;

public class ProxyServer {
	
	public static void main(String[] args) throws Exception {
		Server server = new Server();
		
		SelectChannelConnector connector = new SelectChannelConnector();
		connector.setPort(8888);
		server.addConnector(connector);
		
		HandlerCollection handlers = new HandlerCollection();
        server.setHandler(handlers);
		
		// Setup proxy Servlet
		ServletContextHandler context = new ServletContextHandler(handlers, "/", ServletContextHandler.SESSIONS);
		ServletHolder proxyServlet = new ServletHolder(ProxyServlet.class);
        proxyServlet.setInitParameter("whiteList", "google.com, www.eclipse.org, localhost");
        proxyServlet.setInitParameter("blackList", "google.com/calendar/*, www.eclipse.org/committers/");
        context.addServlet(proxyServlet, "/*");

        // Setup proxy handler to handle CONNECT methods
        ConnectHandler proxy = new ConnectHandler();
        proxy.setWhite(new String[]{"mail.google.com"});
        proxy.addWhite("www.google.com");
        handlers.addHandler(proxy);

        server.start();
        
	}

}
