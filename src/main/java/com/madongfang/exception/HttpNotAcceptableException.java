package com.madongfang.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.madongfang.api.ReturnApi;

@ResponseStatus(value=HttpStatus.NOT_ACCEPTABLE)
public class HttpNotAcceptableException extends HttpException {

	public HttpNotAcceptableException() {
		// TODO Auto-generated constructor stub
	}

	public HttpNotAcceptableException(ReturnApi returnApi) {
		super(returnApi);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 1L;
}
