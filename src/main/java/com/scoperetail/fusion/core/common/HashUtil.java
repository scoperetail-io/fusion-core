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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class HashUtil {

	public static final String SHA3_512 = "SHA3-512";
	public static final String SHA_256 = "SHA-256";

	private HashUtil() {

	}

	public static byte[] digest(final byte[] input, final String algorithm) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance(algorithm);
		} catch (final NoSuchAlgorithmException e) {
			throw new IllegalArgumentException(e);
		}
		return md.digest(input);
	}

	public static String bytesToHex(final byte[] bytes) {
		final StringBuilder sb = new StringBuilder();
		for (final byte b : bytes) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}

	public static String getHash(final String input, final String algorithm) {
		final byte[] shaInBytes = digest(input.getBytes(UTF_8), algorithm);
		final String hash = bytesToHex(shaInBytes);
		log.trace("Created hash for input: {} hash: {}", input, hash);
		return hash;
	}
}
