package com.scoperetail.fusion.core.common;

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