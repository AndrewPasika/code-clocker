package com.codeclocker.listeners.level;

import com.codeclocker.services.ActivityTracker;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectFileIndex;
import com.intellij.openapi.vfs.VirtualFile;
import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.FocusEvent;

public class SingleFocusListener implements AWTEventListener {

  private static final Logger LOG = Logger.getInstance(SingleFocusListener.class);
  private final ActivityTracker activityTracker;

  public SingleFocusListener() {
    this.activityTracker = ApplicationManager.getApplication().getService(ActivityTracker.class);
  }

  @Override
  public void eventDispatched(AWTEvent event) {
    if (!(event instanceof FocusEvent)) {
      return;
    }

    DataContext dataContext = DataManager.getInstance()
        .getDataContext(((FocusEvent) event).getComponent());
    Project project = dataContext.getData(CommonDataKeys.PROJECT);
    if (project == null) {
      LOG.warn("Project is null. Doing nothing");
      return;
    }

    activityTracker.logTimePerProject(project);

    VirtualFile file = CommonDataKeys.VIRTUAL_FILE.getData(dataContext);
    if (file == null) {
      return;
    }

    activityTracker.logTimeSpentPerFile(project, file.getName(), file.getFileType().getName());

    Module module = ProjectFileIndex.getInstance(project).getModuleForFile(file);
    if (module != null) {
      activityTracker.logTimePerModule(project, module.getName());
    }
  }
}
