package com.nitin.config;

import org.skife.config.Config;
import org.skife.config.Default;
import org.skife.config.Description;
import org.skife.config.TimeSpan;

public interface AppConfig {
	
	@Config("server.ip")
    @Default("127.0.0.1")
    String getLocalIp();
	
	@Config("server.port")
    @Default("8081")
    Integer getServerPort();
	
	@Config("jetty.LowResourcesMaxIdleTime")
    @Default("3s")
    TimeSpan getJettyLowResourcesMaxIdleTime();
	
	@Config("jetty.maxIdleTime")
    @Default("15s")
    TimeSpan getJettyMaxIdleTime();
	
	@Config("jetty.minThreads")
    @Default("200")
    int getJettyMinThreads();

    @Config("jetty.maxThreads")
    @Default("2000")
    int getJettyMaxThreads();
	
	@Config("guess.number.max.random")
	@Default("20")
	Integer getMaxRandomNumber();
	
	@Config("guess.number.max.attempt")
	@Default("5")
	Integer getMaxGuessAttempt();
	
	@Description("How long the data remain in cache")
    @Config("guess.cache.timeout")
    @Default("1h")
    TimeSpan getCacheTimeout();
	
	
	

}
