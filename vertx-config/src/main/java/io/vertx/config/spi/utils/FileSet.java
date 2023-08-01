/*
 * Copyright (c) 2014 Red Hat, Inc. and others
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */

package io.vertx.config.spi.utils;

import io.vertx.config.spi.ConfigProcessor;
import io.vertx.core.*;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;

/**
 * Utility class to manage file set selected using a pattern.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class FileSet {

  private final static Logger LOGGER = LoggerFactory.getLogger(FileSet.class);

  private final String pattern;
  private final ConfigProcessor processor;
  private final File root;
  private final Vertx vertx;
  private final Boolean rawData;
  private final Boolean hierarchical;

  /**
   * Creates a new {@link FileSet} from a json object.
   *
   * @param vertx the Vert.x instance
   * @param root  the root of the fileset (directory)
   * @param set   the configuration
   */
  public FileSet(Vertx vertx, File root, JsonObject set) {
    this.vertx = vertx;
    this.root = root;
    this.pattern = set.getString("pattern");
    if (this.pattern == null) {
      throw new IllegalArgumentException("Each file set needs to contain a `pattern`");
    }
    this.rawData = set.getBoolean("raw-data", false);
    this.hierarchical = set.getBoolean("hierarchical", false);
    String format = set.getString("format", "json");
    this.processor = Processors.get(format);
    if (this.processor == null) {
      throw new IllegalArgumentException("Unknown configuration format `" + format + "`, supported types are " +
        Processors.getSupportedFormats());
    }
  }

  private boolean matches(String path) {
    return match(pattern, path, false);
  }

  /**
   * Tests whether or not a string matches against a pattern.
   * The pattern may contain two special characters:<br>
   * '*' means zero or more characters<br>
   * '?' means one and only one character
   *
   * @param pattern         The pattern to match against.
   *                        Must not be{@code null}.
   * @param str             The string which must be matched against the pattern.
   *                        Must not be{@code null}.
   * @param isCaseSensitive Whether or not matching should be performed
   *                        case sensitively.
   * @return {@code true} if the string matches against the pattern,
   * or {@code false} otherwise.
   */
  public static boolean match(String pattern, String str, boolean isCaseSensitive) {
    char[] patArr = pattern.toCharArray();
    char[] strArr = str.toCharArray();
    return match(patArr, strArr, isCaseSensitive);
  }

  private static boolean match(char[] patArr, char[] strArr, boolean isCaseSensitive) {
    int patIdxStart = 0;
    int patIdxEnd = patArr.length - 1;
    int strIdxStart = 0;
    int strIdxEnd = strArr.length - 1;
    char ch;

    boolean containsStar = false;
    for (char aPatArr : patArr) {
      if (aPatArr == '*') {
        containsStar = true;
        break;
      }
    }

    if (!containsStar) {
      // No '*'s, so we make a shortcut
      if (patIdxEnd != strIdxEnd) {
        return false; // Pattern and string do not have the same size
      }
      for (int i = 0; i <= patIdxEnd; i++) {
        ch = patArr[i];
        if (ch != '?' && !equals(ch, strArr[i], isCaseSensitive)) {
          return false; // Character mismatch
        }
      }
      return true; // String matches against pattern
    }

    if (patIdxEnd == 0) {
      return true; // Pattern contains only '*', which matches anything
    }

    // Process characters before first star
    while ((ch = patArr[patIdxStart]) != '*' && strIdxStart <= strIdxEnd) {
      if (ch != '?' && !equals(ch, strArr[strIdxStart], isCaseSensitive)) {
        return false; // Character mismatch
      }
      patIdxStart++;
      strIdxStart++;
    }
    if (strIdxStart > strIdxEnd) {
      return checkOnlyStartsLeft(patArr, patIdxStart, patIdxEnd);
    }

    // Process characters after last star
    while ((ch = patArr[patIdxEnd]) != '*' && strIdxStart <= strIdxEnd) {
      if (ch != '?' && !equals(ch, strArr[strIdxEnd], isCaseSensitive)) {
        return false; // Character mismatch
      }
      patIdxEnd--;
      strIdxEnd--;
    }
    if (strIdxStart > strIdxEnd) {
      // All characters in the string are used. Check if only '*'s are
      // left in the pattern. If so, we succeeded. Otherwise failure.
      return checkOnlyStartsLeft(patArr, patIdxStart, patIdxEnd);
    }

    // process pattern between stars. padIdxStart and patIdxEnd point
    // always to a '*'.
    while (patIdxStart != patIdxEnd && strIdxStart <= strIdxEnd) {
      int patIdxTmp = -1;
      for (int i = patIdxStart + 1; i <= patIdxEnd; i++) {
        if (patArr[i] == '*') {
          patIdxTmp = i;
          break;
        }
      }
      if (patIdxTmp == patIdxStart + 1) {
        // Two stars next to each other, skip the first one.
        patIdxStart++;
        continue;
      }
      // Find the pattern between padIdxStart & padIdxTmp in str between
      // strIdxStart & strIdxEnd
      int patLength = (patIdxTmp - patIdxStart - 1);
      int strLength = (strIdxEnd - strIdxStart + 1);
      int foundIdx = -1;
      strLoop:
      for (int i = 0; i <= strLength - patLength; i++) {
        for (int j = 0; j < patLength; j++) {
          ch = patArr[patIdxStart + j + 1];
          if (ch != '?' && !equals(ch, strArr[strIdxStart + i + j], isCaseSensitive)) {
            continue strLoop;
          }
        }

        foundIdx = strIdxStart + i;
        break;
      }

      if (foundIdx == -1) {
        return false;
      }

      patIdxStart = patIdxTmp;
      strIdxStart = foundIdx + patLength;
    }

    // All characters in the string are used. Check if only '*'s are left
    // in the pattern. If so, we succeeded. Otherwise failure.
    return checkOnlyStartsLeft(patArr, patIdxStart, patIdxEnd);
  }

  private static boolean checkOnlyStartsLeft(char[] patArr, int patIdxStart, int patIdxEnd) {
    // All characters in the string are used. Check if only '*'s are
    // left in the pattern. If so, we succeeded. Otherwise failure.
    for (int i = patIdxStart; i <= patIdxEnd; i++) {
      if (patArr[i] != '*') {
        return false;
      }
    }
    return true;
  }

  /**
   * Tests whether two characters are equal.
   */
  private static boolean equals(char c1, char c2, boolean isCaseSensitive) {
    if (c1 == c2) {
      return true;
    }
    if (!isCaseSensitive) {
      // NOTE: Try both upper case and lower case as done by String.equalsIgnoreCase()
      if (Character.toUpperCase(c1) == Character.toUpperCase(c2)
        || Character.toLowerCase(c1) == Character.toLowerCase(c2)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Iterates over the given set of files, and for each matching file, computes the resulting configuration. The
   * given handler is called with the merged configuration (containing the configuration obtained by merging the
   * configuration from all matching files).
   *
   * @param files   the list of files
   * @param handler the handler called with the computed configuration
   */
  public void buildConfiguration(List<File> files, Handler<AsyncResult<JsonObject>> handler) {
    List<Future<JsonObject>> futures = new ArrayList<>();

    files.stream()
      .map(file -> {
        String relative = null;
        if (file.getAbsolutePath().startsWith(root.getAbsolutePath())) {
          relative = file.getAbsolutePath().substring(root.getAbsolutePath().length() + 1);
        }
        if (relative == null) {
          LOGGER.warn("The file `" + file.getAbsolutePath() + "` is not in '" + root
            .getAbsolutePath() + "'");
        }
        return relative;
      })
      .filter(Objects::nonNull)
      .filter(this::matches)
      .map(s -> new File(root, s))
      .forEach(file -> {
        Promise<JsonObject> promise = Promise.promise();
        futures.add(promise.future());
        try {
          vertx.fileSystem().readFile(file.getAbsolutePath()).onComplete(buffer -> {
              if (buffer.failed()) {
                promise.fail(buffer.cause());
              } else {
                processor.process(vertx, new JsonObject().put("raw-data", rawData)
                                                         .put("hierarchical", hierarchical), buffer.result())
                         .onComplete(promise);
              }
            });
        } catch (RejectedExecutionException e) {
          // May happen because ot the internal thread pool used in the async file system.
          promise.fail(e);
        }
      });

    Future.all(futures).onComplete(ar -> {
      if (ar.failed()) {
        handler.handle(Future.failedFuture(ar.cause()));
      } else {
        // Merge
        JsonObject result = new JsonObject();
        futures.stream()
          .map(future -> (JsonObject) future.result())
          .forEach(config -> result.mergeIn(config, true));
        handler.handle(Future.succeededFuture(result));
      }
    });
  }

  /**
   * List all the files from a directory (recursive)
   *
   * @param root the root
   * @return the list of files
   */
  public static List<File> traverse(File root) {
    List<File> files = new ArrayList<>();
    if (!root.isDirectory()) {
      return files;
    } else {
      File[] children = root.listFiles();
      if (children == null) {
        return files;
      } else {
        for (File file : children) {
          if (file.isDirectory()) {
            files.addAll(traverse(file));
          } else {
            files.add(file);
          }
        }
      }
      return files;
    }
  }
}
