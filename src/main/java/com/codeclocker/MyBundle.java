package com.codeclocker;

import com.intellij.DynamicBundle;
import java.util.function.Supplier;
import org.jetbrains.annotations.PropertyKey;

public class MyBundle extends DynamicBundle {

  private static final String BUNDLE = "messages.MyBundle";
  private static final MyBundle INSTANCE = new MyBundle();

  private MyBundle() {
    super(BUNDLE);
  }

  public static String message(@PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
    return INSTANCE.getMessage(key, params);
  }

  @SuppressWarnings("unused")
  public static Supplier<String> messagePointer(@PropertyKey(resourceBundle = BUNDLE) String key,
      Object... params) {
    return INSTANCE.getLazyMessage(key, params);
  }
}
