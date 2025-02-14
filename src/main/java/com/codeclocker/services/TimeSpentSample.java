package com.codeclocker.services;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.commons.lang3.time.StopWatch;

public record TimeSpentSample(
    long samplingStartedAt, StopWatch timeSpent, AtomicLong additions, AtomicLong removals,
    Map<String, String> metadata) {

  public static TimeSpentSample create(Map<String, String> metadata) {
    return new TimeSpentSample(System.currentTimeMillis(), StopWatch.createStarted(),
        new AtomicLong(), new AtomicLong(), metadata);
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

  public void incrementAdditions(long lines) {
    this.additions.getAndUpdate(prev -> prev + lines);
  }

  public void incrementRemovals(long lines) {
    this.removals.getAndUpdate(prev -> prev + lines);
  }
}
