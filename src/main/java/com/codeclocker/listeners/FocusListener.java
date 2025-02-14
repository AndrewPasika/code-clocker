package com.codeclocker.listeners;

import static java.awt.AWTEvent.FOCUS_EVENT_MASK;

import com.codeclocker.services.ActivityTracker;
import com.intellij.ide.DataManager;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.FocusEvent;

public class FocusListener implements AWTEventListener, Disposable {

  private static final Logger LOG = Logger.getInstance(FocusListener.class);
  private final ActivityTracker activityTracker;
  private final DataManager dataManager;

  public FocusListener() {
    this.dataManager = DataManager.getInstance();
    this.activityTracker = ApplicationManager.getApplication().getService(ActivityTracker.class);
  }

  @Override
  public void eventDispatched(AWTEvent event) {
    if (!(event instanceof FocusEvent)) {
      return;
    }

    DataContext dataContext = dataManager.getDataContext(((FocusEvent) event).getComponent());
    Project project = dataContext.getData(CommonDataKeys.PROJECT);
    if (project == null) {
      LOG.warn("Project is null. Doing nothing");
      return;
    }

    activityTracker.logTime(project);

    VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
    if (file == null) {
      return;
    }

    activityTracker.logTime(project, file.getName(), file.getFileType().getName());

    // todo?
    ApplicationManager.getApplication().executeOnPooledThread(() -> {
      ApplicationManager.getApplication().runReadAction(() -> {
        Module module = ProjectFileIndex.getInstance(project).getModuleForFile(file);
        if (module != null) {
          activityTracker.logTime(project, module.getName());
        }
      });
    });
  }

  @Override
  public void dispose() {
    Toolkit.getDefaultToolkit().removeAWTEventListener(this);
  }
}
