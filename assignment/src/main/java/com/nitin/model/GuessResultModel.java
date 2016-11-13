package com.nitin.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GuessResultModel {
	
	private final String token;
	private final String resultCode;
	private final String resultMsg;
	
	@JsonCreator
	public GuessResultModel(@JsonProperty final String token, @JsonProperty final String resultCode, @JsonProperty final String resultMsg){
		this.token = token;
		this.resultCode = resultCode;
		this.resultMsg = resultMsg;
	}

	public String getToken() {
		return token;
	}

	public String getResultCode() {
		return resultCode;
	}

	public String getResultMsg() {
		return resultMsg;
	}
}
