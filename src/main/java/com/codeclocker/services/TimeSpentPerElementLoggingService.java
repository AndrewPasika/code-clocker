package com.codeclocker.services;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public class TimeSpentPerElementLoggingService {

  private final Map<String, TimeSpentSample> timingByElement = new ConcurrentHashMap<>();
  private final AtomicReference<String> currentElement = new AtomicReference<>();

  public void log(String elementName) {
    log(elementName, Map.of());
  }

  public void log(String currentElement, Map<String, String> metadata) {
    String prevElement = this.currentElement.getAndSet(currentElement);

    if (prevElement != null && !Objects.equals(prevElement, currentElement)) {
      pauseWatchForPrevElement(prevElement);
    }

    timingByElement.compute(currentElement, (name, sample) -> {
      if (sample == null) {
        return TimeSpentSample.create(metadata);
      }
      return sample.resumeSpendingTime();
    });
  }

  private void pauseWatchForPrevElement(String prevElement) {
    timingByElement.compute(prevElement, (name, sample) -> {
      if (sample == null) {
        return null;
      }

      sample.pauseSpendingTime();
      return sample;
    });
  }

  public void pauseDueToInactivity() {
    currentElement.updateAndGet(current -> {
      if (current == null) {
        return null;
      }

      timingByElement.compute(current, (name, sample) -> {
        if (sample != null) {
          sample.pauseSpendingTime();
        }
        return sample;
      });

      return current;
    });
  }

  public Map<String, TimeSpentSample> getTimingByElement() {
    return timingByElement;
  }
}
