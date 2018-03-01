package com.madongfang.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.madongfang.api.ReturnApi;

@ResponseStatus(value=HttpStatus.UNAUTHORIZED)
public class HttpUnauthorizedException extends HttpException {

	public HttpUnauthorizedException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public HttpUnauthorizedException(ReturnApi returnApi) {
		super(returnApi);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 1L;
}
