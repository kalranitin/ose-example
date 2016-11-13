package com.nitin.processing;

import com.google.common.base.Objects;
import com.google.inject.Inject;
import com.nitin.config.AppConfig;
import com.nitin.model.GuessNumberModel;
import com.nitin.model.GuessResultModel;

import java.util.Random;

public class GuessNumberProcessorImpl implements GuessNumberProcessor{
	
	private final AppConfig config;
	private final GuessNumberStorage storage;
	
	@Inject
	public GuessNumberProcessorImpl(AppConfig config, final GuessNumberStorage storage)
	{
		this.config = config;
		this.storage = storage;
	}
	
	public GuessResultModel generateToken()
	{
		Random random = new Random();
		
		GuessNumberModel guessNumberModel = new GuessNumberModel(null, random.nextInt(config.getMaxRandomNumber()), null);
		storage.addGuessNumerModel(guessNumberModel);
		
		return new GuessResultModel(guessNumberModel.getToken(), null, null);
	}
	
	public GuessResultModel isGuessedNumberValid(final String token, final Integer guessedNumber){
		GuessNumberModel model = storage.getGuessNumberModel(token);
		
		if(Objects.equal(null, model))
		{
			return new GuessResultModel(model.getToken(), RESULT_CODE_GUESS_WRONG, "Invalid Request/Token");
		}
		
		System.out.println("Model value is - "+model.getNumberToGuess());
		
		if(model.getAttemptCount() >= config.getMaxGuessAttempt())
		{
			return new GuessResultModel(model.getToken(), RESULT_CODE_ATTEMPT_EXHAUST, String.format("All attempts exhausted. The number you should have guessed was %d. PLease try again later!", model.getNumberToGuess()));
		}
		
		model.IncrementAttemptCount();
		return Objects.equal(model.getNumberToGuess(), guessedNumber)?
				new GuessResultModel(model.getToken(), RESULT_CODE_GUESS_CORRECT, "Excellent... you guessed it right!")
				:new GuessResultModel(model.getToken(), RESULT_CODE_GUESS_WRONG, "Oops!! Worng guess ... Try again. You have "+(config.getMaxGuessAttempt() - model.getAttemptCount())+" attempts to go.");
	}

}
