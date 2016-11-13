package com.nitin.processing;

import com.nitin.model.GuessNumberModel;

public interface GuessNumberStorage {
	
	public GuessNumberModel getGuessNumberModel(final String token);
	public void addGuessNumerModel(GuessNumberModel model);

}
