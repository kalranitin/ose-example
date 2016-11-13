package com.nitin;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;

import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.servlets.ProxyServlet;

public class ProxyTransparent extends ProxyServlet{
    String _prefix;
    String _proxyTo;

    public ProxyTransparent()
    {
    }

    public ProxyTransparent(String prefix, String host, int port)
    {
        this(prefix,"http",host,port,null);
    }

    public ProxyTransparent(String prefix, String schema, String host, int port, String path)
    {
        try
        {
            if (prefix != null)
            {
                _prefix = new URI(prefix).normalize().toString();
            }
            _proxyTo = new URI(schema,null,host,port,path,null,null).normalize().toString();
        }
        catch (URISyntaxException ex)
        {
            _log.debug("Invalid URI syntax",ex);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);

        String prefix = config.getInitParameter("Prefix");
        _prefix = prefix == null?_prefix:prefix;

        // Adjust prefix value to account for context path
        String contextPath = _context.getContextPath();
        _prefix = _prefix == null?contextPath:(contextPath + _prefix);

        String proxyTo = config.getInitParameter("ProxyTo");
        String proxyHost = System.getenv("APP_SERVER_HOST");
        String proxyPort = System.getenv("APP_SERVER_PORT");
        String proxyPath = "/rest/";
        
        if(proxyHost !=null && proxyPort != null)
        {
        	_proxyTo = "http://" + proxyHost + ":" + proxyPort + proxyPath;
        }
        else
        {
        	_proxyTo = proxyTo == null?_proxyTo:proxyTo;
        }
        
        if (_proxyTo == null)
            throw new UnavailableException("ProxyTo parameter is requred.");

        if (!_prefix.startsWith("/"))
            throw new UnavailableException("Prefix parameter must start with a '/'.");

        _log.info(config.getServletName() + " @ " + _prefix + " to " + _proxyTo);
    }

    @Override
    protected HttpURI proxyHttpURI(final String scheme, final String serverName, int serverPort, final String uri) throws MalformedURLException
    {
        try
        {
            if (!uri.startsWith(_prefix))
                return null;

            URI dstUri = new URI(_proxyTo + uri.substring(_prefix.length())).normalize();

            if (!validateDestination(dstUri.getHost(),dstUri.getPath()))
                return null;

            return new HttpURI(dstUri.toString());
        }
        catch (URISyntaxException ex)
        {
            throw new MalformedURLException(ex.getMessage());
        }
    }
}
