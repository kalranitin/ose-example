package com.nitin;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Takes java.util.logging and redirects it into log4j.
 */
public class SetupJULBridge implements ServletContextListener
{
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(SetupJULBridge.class);

    @Override
    public void contextInitialized(final ServletContextEvent event)
    {
        // we first remove the default handler(s)
        final Logger rootLogger = LogManager.getLogManager().getLogger("");
        final Handler[] handlers = rootLogger.getHandlers();

        if (!ArrayUtils.isEmpty(handlers)) {
            for (final Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }
        }
        // and then we let jul-to-sfl4j do its magic so that jersey messages go to sfl4j (and thus log4j)
        SLF4JBridgeHandler.install();

        log.info("Assimilated java.util Logging");
    }

    @Override
    public void contextDestroyed(final ServletContextEvent event)
    {
        SLF4JBridgeHandler.uninstall();
    }
}
