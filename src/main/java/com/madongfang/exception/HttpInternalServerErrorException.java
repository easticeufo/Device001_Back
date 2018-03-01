package com.madongfang.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.madongfang.api.ReturnApi;

@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
public class HttpInternalServerErrorException extends HttpException {

	public HttpInternalServerErrorException() {
		// TODO Auto-generated constructor stub
	}

	public HttpInternalServerErrorException(ReturnApi returnApi) {
		super(returnApi);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 1L;
}
