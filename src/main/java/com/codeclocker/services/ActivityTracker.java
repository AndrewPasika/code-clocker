package com.codeclocker.services;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

public class ActivityTracker {

  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
  private final Map<String, TimeSpentPerElementLoggingService> timeSpentPerModuleByProject = new ConcurrentHashMap<>();
  private final Map<String, TimeSpentPerElementLoggingService> timeSpentPerFileByProject = new ConcurrentHashMap<>();
  private final TimeSpentPerElementLoggingService timeSpentPerProject = new TimeSpentPerElementLoggingService();
  private final long pauseActivityAfterInactivityMillis = Duration.ofSeconds(30).toMillis();
  private final AtomicReference<ScheduledFuture<?>> scheduledTask;
  private long lastRescheduledAt;
  private final AtomicReference<String> currentProject = new AtomicReference<>();

  public ActivityTracker() {
    this.scheduledTask = new AtomicReference<>(schedule());
  }

  public void logAdditions(Project project, Module module, VirtualFile file, long lines) {
    timeSpentPerProject.compute(project.getName(), sample -> sample.incrementAdditions(lines));
    timeSpentPerModuleByProject.get(project.getName())
        .compute(module.getName(), sample -> sample.incrementAdditions(lines));
    timeSpentPerFileByProject.get(project.getName())
        .compute(file.getName(), sample -> sample.incrementAdditions(lines));
  }

  public void logRemovals(Project project, Module module, VirtualFile file, long lines) {
    timeSpentPerProject.compute(project.getName(), sample -> sample.incrementRemovals(lines));
    timeSpentPerModuleByProject.get(project.getName())
        .compute(module.getName(), sample -> sample.incrementRemovals(lines));
    timeSpentPerFileByProject.get(project.getName())
        .compute(file.getName(), sample -> sample.incrementRemovals(lines));
  }

  public Map<String, TimeSpentSample> getTimeSpentPerProject() {
    return this.timeSpentPerProject.getTimingByElement();
  }

  public Map<String, TimeSpentSample> getTimeSpentPerModule(Project project) {
    return this.timeSpentPerModuleByProject.get(project.getName()).getTimingByElement();
  }

  public Map<String, TimeSpentSample> getTimeSpentPerFile(Project project) {
    return this.timeSpentPerFileByProject.get(project.getName()).getTimingByElement();
  }

  public void logTime(Project project) {
    pauseOtherProjects(project);
    rescheduleInactivityTask();
    timeSpentPerProject.log(project.getName());
  }

  public void logTime(Project project, String module) {
    pauseOtherProjects(project);
    rescheduleInactivityTask();
    log(timeSpentPerModuleByProject, project, module, Map.of());
  }

  public void logTime(Project project, String file, String fileType) {
    pauseOtherProjects(project);
    rescheduleInactivityTask();
    log(timeSpentPerFileByProject, project, file, Map.of("fileType", fileType));
  }

  public void rescheduleInactivityTask() {
    long now = System.currentTimeMillis();
    if (now - lastRescheduledAt < 1000) {
      return;
    }
    lastRescheduledAt = now;

    scheduledTask.updateAndGet(currentTask -> {
      currentTask.cancel(false);
      return schedule();
    });
  }

  private void pauseOtherProjects(Project project) {
    currentProject.updateAndGet(current -> {
      if (current == null || Objects.equals(current, project.getName())) {
        return project.getName();
      }

      timeSpentPerModuleByProject.get(current).pauseDueToInactivity();
      timeSpentPerFileByProject.get(current).pauseDueToInactivity();

      return project.getName();
    });
  }

  public void pause() {
    timeSpentPerProject.pauseDueToInactivity();
    currentProject.updateAndGet(active -> {
      if (active == null) {
        return null;
      }

      timeSpentPerModuleByProject.get(active).pauseDueToInactivity();
      timeSpentPerFileByProject.get(active).pauseDueToInactivity();

      return active;
    });
  }

  private ScheduledFuture<?> schedule() {
    return this.executor.schedule(this::pause, pauseActivityAfterInactivityMillis, MILLISECONDS);
  }

  private void log(Map<String, TimeSpentPerElementLoggingService> map, Project project, String file,
      Map<String, String> metadata) {
    map.compute(project.getName(), (name, service) -> {
      if (service == null) {
        service = new TimeSpentPerElementLoggingService();
      }

      service.log(file, metadata);
      return service;
    });
  }
}
