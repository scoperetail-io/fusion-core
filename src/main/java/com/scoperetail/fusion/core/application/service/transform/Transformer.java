package com.scoperetail.fusion.core.application.service.transform;

import static org.apache.commons.lang.StringUtils.EMPTY;

import java.util.Map;

public interface Transformer {

	String DOMAIN_ENTITY = "DOMAIN_ENTITY";

	default String transform(final String event, final Map<String, Object> params, final String templateName)
			throws Exception {
		return EMPTY;
	}
}
