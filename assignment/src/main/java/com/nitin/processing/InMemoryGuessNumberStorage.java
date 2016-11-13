package com.nitin.processing;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.inject.Inject;
import com.nitin.config.AppConfig;
import com.nitin.model.GuessNumberModel;

public class InMemoryGuessNumberStorage implements GuessNumberStorage{
	
	final Cache<String, GuessNumberModel> guessNumberModelCache;
	final AppConfig config;
	
	@Inject
	public InMemoryGuessNumberStorage(final AppConfig config)
	{
		this.config = config;
		this.guessNumberModelCache = CacheBuilder.newBuilder().expireAfterWrite(config.getCacheTimeout().getPeriod(), config.getCacheTimeout().getUnit()).build();
	}
	
	public GuessNumberModel getGuessNumberModel(final String token)
	{
		return this.guessNumberModelCache.getIfPresent(token);
	}
	
	public void addGuessNumerModel(GuessNumberModel model)
	{
		this.guessNumberModelCache.put(model.getToken(), model);
	}

}
