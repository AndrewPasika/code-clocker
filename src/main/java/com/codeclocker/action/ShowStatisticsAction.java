package com.codeclocker.action;

import com.codeclocker.MyBundle;
import com.codeclocker.services.ActivityTracker;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import java.time.Duration;
import org.jetbrains.annotations.NotNull;

public class ShowStatisticsAction extends AnAction {

  @Override
  public void actionPerformed(@NotNull AnActionEvent e) {
    Project project = e.getProject();
    if (project == null) {
      return;
    }

    ActivityTracker activityTracker = ApplicationManager.getApplication().getService(
        ActivityTracker.class);
    StringBuilder stats = new StringBuilder();

    stats.append(MyBundle.message("timeSpentPerProject")).append("\n");
    activityTracker.getTimeSpentPerProject().forEach((name, time) -> {
      stats.append(name).append(": ")
          .append(Duration.ofNanos(time.timeSpent().getNanoTime()).getSeconds())
          .append(" seconds; ").append(time.additions().get()).append(" added lines; ")
          .append(time.removals().get()).append(" removed lines").append("\n");
    });

    stats.append("\n").append(MyBundle.message("timeSpentPerFile")).append("\n");
    activityTracker.getTimeSpentPerFile(project).forEach((name, time) -> {
      stats.append(name).append(": ")
          .append(Duration.ofNanos(time.timeSpent().getNanoTime()).getSeconds())
          .append(" seconds; ").append(time.additions().get()).append(" added lines; ")
          .append(time.removals().get()).append(" removed lines").append("\n");
    });

    stats.append("\n").append(MyBundle.message("timeSpentPerModule")).append("\n");
    activityTracker.getTimeSpentPerModule(project).forEach((name, time) -> {
      stats.append(name).append(": ")
          .append(Duration.ofNanos(time.timeSpent().getNanoTime()).getSeconds())
          .append(" seconds; ").append(time.additions().get()).append(" added lines; ")
          .append(time.removals().get()).append(" removed lines").append("\n");
    });

    // Show statistics in a dialog
    Messages.showMessageDialog(project, stats.toString(), MyBundle.message("statisticPopupTitle"),
        Messages.getInformationIcon());
  }
}
