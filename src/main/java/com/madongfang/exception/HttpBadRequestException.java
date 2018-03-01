package com.madongfang.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.madongfang.api.ReturnApi;

@ResponseStatus(value=HttpStatus.BAD_REQUEST)
public class HttpBadRequestException extends HttpException {

	public HttpBadRequestException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public HttpBadRequestException(ReturnApi returnApi) {
		super(returnApi);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 1L;
	
}
