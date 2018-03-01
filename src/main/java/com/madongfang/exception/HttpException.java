package com.madongfang.exception;

import com.madongfang.api.ReturnApi;

public class HttpException extends RuntimeException {

	public HttpException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public HttpException(ReturnApi returnApi) {
		super();
		this.returnApi = returnApi;
	}

	public ReturnApi getReturnApi() {
		return returnApi;
	}

	public void setReturnApi(ReturnApi returnApi) {
		this.returnApi = returnApi;
	}

	private ReturnApi returnApi;
	
	private static final long serialVersionUID = 1L;
}
