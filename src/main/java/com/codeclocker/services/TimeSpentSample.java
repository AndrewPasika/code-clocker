package com.codeclocker.services;

import java.util.Map;
import org.apache.commons.lang3.time.StopWatch;

public record TimeSpentSample(
    long samplingStartedAt, StopWatch timeSpent, Map<String, String> metadata) {

  public static TimeSpentSample create(Map<String, String> metadata) {
    return new TimeSpentSample(System.currentTimeMillis(), StopWatch.createStarted(), metadata);
  }

  public TimeSpentSample resumeSpendingTime() {
    if (timeSpent.isSuspended()) {
      timeSpent.resume();
    }
    return this;
  }

  public void pauseSpendingTime() {
    if (!timeSpent.isSuspended()) {
      timeSpent.suspend();
    }
  }
}
