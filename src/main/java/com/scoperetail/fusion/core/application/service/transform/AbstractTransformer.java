package com.scoperetail.fusion.core.application.service.transform;

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

import java.util.Map;

import com.scoperetail.fusion.core.application.service.transform.template.engine.TemplateEngine;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public abstract class AbstractTransformer implements Transformer {

  protected TemplateEngine templateEngine;

  @Override
  public String transform(
      final String event, final Map<String, Object> params, final String template)
      throws Exception {
    return templateEngine.generateTextFromTemplate(event, params, template);
  }

  @Override
  public String getTemplateDirBasePath(final String event) {
    return templateEngine.getTemplateDirBasePath(event);
  }

  @Override
  public String getTemplateFileExtension() {
    return templateEngine.getTemplateFileExtension();
  }
}
