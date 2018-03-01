package com.madongfang.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.madongfang.api.ReturnApi;

@ControllerAdvice
@RestController
public class GlobalExceptionHandler {
	
	@ExceptionHandler(HttpBadRequestException.class)
	@ResponseStatus(value=HttpStatus.BAD_REQUEST)
	public ReturnApi httpBadRequestHandler(HttpException e) {
		return e.getReturnApi();
	}
	
	@ExceptionHandler(HttpNotFoundException.class)
	@ResponseStatus(value=HttpStatus.NOT_FOUND)
	public ReturnApi httpNotFoundHandler(HttpException e) {
		return e.getReturnApi();
	}
	
	@ExceptionHandler(HttpUnauthorizedException.class)
	@ResponseStatus(value=HttpStatus.UNAUTHORIZED)
	public ReturnApi httpUnauthorizedHandler(HttpException e) {
		return e.getReturnApi();
	}
	
	@ExceptionHandler(HttpNotAcceptableException.class)
	@ResponseStatus(value=HttpStatus.NOT_ACCEPTABLE)
	public ReturnApi httpNotAcceptableHandler(HttpException e) {
		return e.getReturnApi();
	}
	
	@ExceptionHandler(HttpInternalServerErrorException.class)
	@ResponseStatus(value=HttpStatus.INTERNAL_SERVER_ERROR)
	public ReturnApi httpInternalServerErrorHandler(HttpException e) {
		return e.getReturnApi();
	}
}
