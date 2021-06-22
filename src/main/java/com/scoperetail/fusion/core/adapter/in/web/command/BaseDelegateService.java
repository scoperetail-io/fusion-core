package com.scoperetail.fusion.core.adapter.in.web.command;

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

import com.scoperetail.fusion.core.adapter.in.web.BaseDelegate;
import com.scoperetail.fusion.core.adapter.out.persistence.jpa.DedupeJpaAdapter;
import com.scoperetail.fusion.core.application.service.transform.Transformer;
import com.scoperetail.fusion.core.application.service.transform.impl.DuplicateFtlTransformer;
import com.scoperetail.fusion.core.application.service.transform.impl.DuplicateVelocityTransformer;
import com.scoperetail.fusion.messaging.config.FusionConfig;
import com.scoperetail.fusion.messaging.config.UseCaseConfig;
import com.scoperetail.fusion.shared.kernel.common.annotation.UseCase;

import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@UseCase
@AllArgsConstructor
public class BaseDelegateService implements BaseDelegate {
	private DuplicateFtlTransformer duplicateFtlTransformer;
	private DuplicateVelocityTransformer duplicateVelocityTransformer;
	private DedupeJpaAdapter dedupeJpaAdapter;
	private FusionConfig fusionConfig;

	 @Override
	 public boolean isNotDuplicate(final String eventName, final String templateName, final Object domainEntity) {
		 Map<String, Object> paramsMap = new HashMap<>();
		 paramsMap.put(Transformer.DOMAIN_ENTITY, domainEntity);
		 String hashKey = null;
		 Optional<UseCaseConfig> useCase = fusionConfig.getUsecases().stream().filter(u -> u.getName().equals(eventName))
				 .findFirst();

		 if (useCase.isPresent() && useCase.get().getDedupeCheck()) {
		 	 if(useCase.get().getHashKeyTemplate() == UseCaseConfig.HashKeyTemplate.DOMAIN_EVENT_FTL_TRANSFORMER) {
		 	 	 hashKey = duplicateFtlTransformer.transform(eventName, paramsMap, templateName);
			 } else if (useCase.get().getHashKeyTemplate() == UseCaseConfig.HashKeyTemplate.DOMAIN_EVENT_VELOCITY_TRANSFORMER) {
		 	 	 hashKey = duplicateVelocityTransformer.transform(eventName, paramsMap, templateName);
			 }
		 }
	   return dedupeJpaAdapter.isNotDuplicate(hashKey);
	 }
}
