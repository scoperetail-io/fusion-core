package com.scoperetail.fusion.core.common;

/*-
 * *****
 * fusion-core
 * -----
 * Copyright (C) 2018 - 2021 Scope Retail Systems Inc.
 * -----
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =====
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Scanner;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import lombok.extern.slf4j.Slf4j;
import org.webjars.NotFoundException;

@Slf4j
public class GenericRestResponseErrorHandler implements ResponseErrorHandler {

	@Override
	public boolean hasError(ClientHttpResponse clientHttpResponse) throws IOException {
		HttpStatus status = clientHttpResponse.getStatusCode();
		return status.is4xxClientError() || status.is5xxServerError();
	}

	@Override
	public void handleError(ClientHttpResponse clientHttpResponse) throws IOException {
		String responseAsString = toString(clientHttpResponse.getBody());
		log.error("ResponseBody: {}", responseAsString);
		throw new IOException(responseAsString);
	}

	@Override
	public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {
		String responseAsString = toString(response.getBody());
		if (response.getStatusCode().series() == HttpStatus.Series.SERVER_ERROR) {
			log.error("Server Error. URL: {}, HttpMethod: {}, ResponseBody: {}", url, method, responseAsString);
		} else if(response.getStatusCode().series() == HttpStatus.Series.CLIENT_ERROR) {
			log.error("Client Error. URL: {}, HttpMethod: {}, ResponseBody: {}", url, method, responseAsString);
		} else if(response.getStatusCode() == HttpStatus.NOT_FOUND) {
			log.error("Not Found Error. URL: {}, HttpMethod: {}, ResponseBody: {}", url, method, responseAsString);
			throw new NotFoundException(responseAsString);
		} else {
			log.error("Generic Error. URL: {}, HttpMethod: {}, ResponseBody: {}", url, method, responseAsString);
			throw new IOException(responseAsString);
		}
	}

	String toString(InputStream inputStream) {
		Scanner s = new Scanner(inputStream).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}
}
