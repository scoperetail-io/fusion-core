package com.scoperetail.fusion.core.application.service.transform.impl;

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

import com.scoperetail.fusion.core.application.service.transform.AbstractTransformer;
import com.scoperetail.fusion.core.application.service.transform.template.engine.TemplateEngine;
import com.scoperetail.fusion.core.common.HashUtil;

import java.util.Map;

public abstract class AbstractHashKeyTemplateTransformer extends AbstractTransformer {

  public AbstractHashKeyTemplateTransformer(TemplateEngine templateEngine) {
    super(templateEngine);
  }

  @Override
  public String transform(final String event, final Map<String, Object> params, final String templateName) {
    final String keyJson = templateEngine.generateTextFromTemplate(event, params, templateName);
    return HashUtil.getHash(keyJson, HashUtil.SHA3_512);
  }
}
