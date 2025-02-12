package com.codeclocker.listeners;

import static java.awt.AWTEvent.FOCUS_EVENT_MASK;

import com.codeclocker.listeners.level.SingleFocusListener;
import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.wm.IdeFrame;
import java.awt.Toolkit;
import org.jetbrains.annotations.NotNull;

public class AppLevelRegistrator implements ApplicationActivationListener {

  private static final Logger LOG = Logger.getInstance(AppLevelRegistrator.class);

  @Override
  public void applicationActivated(@NotNull IdeFrame ideFrame) {
    LOG.warn(
        "Don't forget to remove all non-needed sample code files with their corresponding registration entries in `plugin.xml`.");

    focusedProjectListener();
  }

  private static void focusedProjectListener() {
    Toolkit.getDefaultToolkit().addAWTEventListener(new SingleFocusListener(), FOCUS_EVENT_MASK);
  }
}