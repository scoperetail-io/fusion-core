package com.scoperetail.fusion.core.common;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Scanner;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

/*-
 * *****
 * fusion-core
 * -----
 * Copyright (C) 2018 - 2021 Scope Retail Systems Inc.
 * -----
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * =====
 */

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CustomResponseErrorHandler implements ResponseErrorHandler {

	@Override
	public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
		HttpStatus status = clientHttpResponse.getStatusCode();
		return status.is4xxClientError() || status.is5xxServerError();
	}

	@Override
	public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
		String responseAsString = toString(clientHttpResponse.getBody());
		log.error("ResponseBody: {}", responseAsString);

		throw new CustomException(responseAsString);
	}

	@Override
	public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
		String responseAsString = toString(response.getBody());
		log.error("URL: {}, HttpMethod: {}, ResponseBody: {}", url, method, responseAsString);

		throw new CustomException(responseAsString);
	}

	String toString(InputStream inputStream) {
		Scanner s = new Scanner(inputStream).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	static class CustomException extends IOException {
		private static final long serialVersionUID = 1L;

		public CustomException(String message) {
			super(message);
		}
	}
}
