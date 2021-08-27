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

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import org.springframework.http.HttpStatus;
import com.scoperetail.fusion.core.application.port.in.command.AuditUseCase;
import com.scoperetail.fusion.core.application.port.in.command.DuplicateCheckUseCase;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public abstract class AbstractBaseDelegate {
  private final DuplicateCheckUseCase duplicateCheckUseCase;
  private final AuditUseCase auditUseCase;

  protected HttpStatus doEvent(final String event, final Object domainEntity) {
    HttpStatus result = CONFLICT;
    try {
      auditUseCase.createAudit(event, domainEntity);
      if (!duplicateCheckUseCase.isDuplicate(event, domainEntity)) {
        result = doProcess(event, domainEntity);
      }
    } catch (final Exception e) {
      log.error("Exception occured: {}", e);
    }
    return result;
  }

  private HttpStatus doProcess(final String event, final Object domainEntity) {
    HttpStatus result;
    try {
      result = processEvent(domainEntity);
    } catch (final Exception e) {
      result = INTERNAL_SERVER_ERROR;
      log.error("Exception occurred during processing event {} exception is: {}", event, e);
    }
    return result;
  }

  protected abstract HttpStatus processEvent(Object domainEntity) throws Exception;

}
