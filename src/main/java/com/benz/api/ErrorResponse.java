package com.benz.api;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
	private int status;
	private String message;
	private String stackTrace;

	public String getStackTrace() {
		return stackTrace;
	}

	public void setStackTrace(String stackTrace) {
		this.stackTrace = stackTrace;
	}

	public int getStatus() {
		return status;
	}

	public String getMessage() {
		return message;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public ErrorResponse() {

	}

	public ErrorResponse(int status, String message, String stackTrace) {
		super();
		this.status = status;
		this.message = message;
		this.stackTrace = stackTrace;
	}
}