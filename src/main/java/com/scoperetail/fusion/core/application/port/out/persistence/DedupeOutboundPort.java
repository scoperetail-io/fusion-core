package com.scoperetail.fusion.core.application.port.out.persistence;

public interface DedupeOutboundPort {
  Boolean isNotDuplicate(String logKey);
}
