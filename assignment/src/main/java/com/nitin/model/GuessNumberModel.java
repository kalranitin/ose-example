package com.nitin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.base.Strings;

import java.util.UUID;

public class GuessNumberModel {
	private final String token;
	private final Integer numberToGuess;
	private Integer attemptCount;
	
	@JsonCreator
	public GuessNumberModel(@JsonProperty("token")final String token, 
			@JsonProperty("numberToGuess") final Integer numberToGuess, 
			@JsonProperty("attemptCount") final Integer attemptCount)
	{
		if(Strings.isNullOrEmpty(token))
		{
			this.token = UUID.randomUUID().toString();
		}
		else
		{
			this.token = token;
		}

		this.numberToGuess = numberToGuess;
		
		if(Objects.equal(null, attemptCount))
		{
			this.attemptCount = 0;
		}
		else
		{
			this.attemptCount = attemptCount;
		}
	}

	@JsonProperty("numberToGuess")
	public Integer getNumberToGuess() {
		return numberToGuess;
	}

	@JsonProperty("attemptCount")
	public Integer getAttemptCount() {
		return attemptCount;
	}
	
	@JsonProperty("token")
	public String getToken() {
		return token;
	}

	@JsonIgnore
	public void IncrementAttemptCount()
	{
		this.attemptCount++;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((token == null) ? 0 : token.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GuessNumberModel other = (GuessNumberModel) obj;
		if (token == null) {
			if (other.token != null)
				return false;
		} else if (!token.equals(other.token))
			return false;
		return true;
	}
}
