package com.codeclocker.listeners;

import com.codeclocker.services.ActivityTracker;
import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.wm.IdeFrame;
import org.jetbrains.annotations.NotNull;

public class AppFrameFocusLostListener implements ApplicationActivationListener {

  private static final Logger LOG = Logger.getInstance(AppFrameFocusLostListener.class);

  private final ActivityTracker activityTracker;

  public AppFrameFocusLostListener() {
    this.activityTracker = ApplicationManager.getApplication().getService(ActivityTracker.class);
  }

  @Override
  public void applicationDeactivated(@NotNull IdeFrame ideFrame) {
    LOG.warn("Application frame lost focus. Pausing all activity tracking");
    activityTracker.pause();
  }
}