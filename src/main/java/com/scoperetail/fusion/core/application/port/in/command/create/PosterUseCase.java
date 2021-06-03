package com.scoperetail.fusion.core.application.port.in.command.create;

public interface PosterUseCase {

	void post(String event, Object domainEntity, boolean isValid) throws Exception;
}
