/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.openapi.vfs.impl.local;

import com.google.common.collect.Lists;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;
import java.util.List;

public final class CanonicalFileMap extends CanonicalFileMapper {
  private static final Logger LOG = Logger.getInstance(CanonicalFileMap.class);

  private static final class OriginalWatchPath {
    @NotNull public final String path;
    @NotNull public final MappingType mappingType;

    public OriginalWatchPath(@NotNull String path, @NotNull MappingType mappingType) {
      this.path = path;
      this.mappingType = mappingType;
    }
  }

  private final MultiMap<String, OriginalWatchPath> canonicalPathToOriginalPath = MultiMap.createSet();

  /**
   * @return the canonical path for {@param file}
   */
  @Override
  @Nullable
  public String addMapping(@NotNull File file, @NotNull MappingType mappingType) {
    String canonicalFilePath = symLinkToRealPath(file);
    if (canonicalFilePath == null) {
      LOG.warn("Could not find canonical path for " + file.getPath());
      return null;
    }
    Collection<OriginalWatchPath> possibleOriginalPaths = canonicalPathToOriginalPath.getModifiable(canonicalFilePath);
    possibleOriginalPaths.add(new OriginalWatchPath(file.getPath(), mappingType));
    return canonicalFilePath;
  }

  @Override
  @NotNull
  public List<String> getMapping(@NotNull String canonicalPath) {
    List<String> pathComponents = FileUtil.splitPath(canonicalPath);
    List<String> originalPathsToFile = Lists.newArrayList();

    // The first position will always be the root
    File runningPath = new File(pathComponents.get(0) + File.separator);
    for (int i = 1; i < pathComponents.size(); ++i) {
      runningPath = new File(runningPath, pathComponents.get(i));
      Collection<OriginalWatchPath> possibleOriginalPaths = canonicalPathToOriginalPath.get(runningPath.getPath());
      for (OriginalWatchPath possibleOriginalPath : possibleOriginalPaths) {
        // For a flat root, i + 1 would be the last item in the list
        if (possibleOriginalPath.mappingType == MappingType.RECURSIVE ||
            (i + 1) == (pathComponents.size() - 1)) {
          originalPathsToFile.add(combine(possibleOriginalPath.path, pathComponents, i + 1));
        }
      }
    }
    return originalPathsToFile;
  }
}
