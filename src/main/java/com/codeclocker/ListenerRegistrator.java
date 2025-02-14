package com.codeclocker;

import static java.awt.AWTEvent.FOCUS_EVENT_MASK;

import com.codeclocker.listeners.FocusListener;
import com.intellij.ide.util.RunOnceUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import java.awt.Toolkit;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ListenerRegistrator implements ProjectActivity {

  @Nullable
  @Override
  public Object execute(@NotNull Project project,
      @NotNull Continuation<? super Unit> continuation) {
    registerFocusListener();

    return null;
  }

  private static void registerFocusListener() {
    RunOnceUtil.runOnceForApp(FocusListener.class.getName(), () -> Toolkit.getDefaultToolkit()
        .addAWTEventListener(new FocusListener(), FOCUS_EVENT_MASK));
  }
}
