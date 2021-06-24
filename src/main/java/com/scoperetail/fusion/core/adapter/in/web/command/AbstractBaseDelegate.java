package com.scoperetail.fusion.core.adapter.in.web.command;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import org.springframework.http.HttpStatus;
import com.scoperetail.fusion.core.application.port.in.command.DuplicateCheckUseCase;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public abstract class AbstractBaseDelegate {
  private final DuplicateCheckUseCase delegateUseCase;

  protected HttpStatus doEvent(final String event, final Object domainEntity) {
    HttpStatus result = CONFLICT;
    try {
      if (!delegateUseCase.isDuplicate(event, domainEntity)) {
        result = doProcess(event, domainEntity);
      }
    } catch (final Exception e) {
      log.error("Error in dedupe service: ", e);
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

  protected abstract HttpStatus processEvent(Object domainEntity);

}
