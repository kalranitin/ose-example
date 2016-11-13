package com.nitin.processing;

import com.nitin.model.GuessResultModel;

public interface GuessNumberProcessor {
	
	public static final String RESULT_CODE_GUESS_WRONG = "0";
	public static final String RESULT_CODE_GUESS_CORRECT = "1";
	public static final String RESULT_CODE_ATTEMPT_EXHAUST = "2";
	
	public GuessResultModel generateToken();
	public GuessResultModel isGuessedNumberValid(final String token, final Integer guessedNumber);

}
