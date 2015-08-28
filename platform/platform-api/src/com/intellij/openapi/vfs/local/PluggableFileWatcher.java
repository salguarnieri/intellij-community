/*
 * Copyright 2000-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.openapi.vfs.local;

import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.ManagingFS;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.io.IOException;
import java.util.List;

/**
 * @author dslomov
 */
public abstract class PluggableFileWatcher {
  public static final ExtensionPointName<PluggableFileWatcher> EP_NAME = ExtensionPointName.create("com.intellij.vfs.local.pluggableFileWatcher");

  public abstract void initialize(@NotNull ManagingFS managingFS, @NotNull FileWatcherNotificationSink notificationSink);

  public abstract void dispose();

  public abstract boolean isOperational();

  public abstract boolean isSettingRoots();

  /**
   *
   * @return the manual watch roots. All returned paths will be canonical.
   */
  @NotNull
  public abstract List<String> getManualWatchRoots();

  /**
   * The inputs to this method must be absolute and free of symbolic links.
   *
   * @param recursive list of canonical paths to watch recursively
   * @param flat list of canonical paths to watch non-recursively
   */
  public abstract void setWatchRoots(@NotNull List<String> recursive, @NotNull List<String> flat);

  /**
   * @param file a canonical file
   * @return true if the file is being watched by this file watcher, false otherwise
   */
  public abstract boolean isWatched(@NotNull VirtualFile file);

  public abstract void resetChangedPaths();

  @TestOnly
  public abstract void startup() throws IOException;

  @TestOnly
  public abstract void shutdown() throws InterruptedException;
}
