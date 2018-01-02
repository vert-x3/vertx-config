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

package io.vertx.config.vault.utils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArchUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class VaultDownloader {

  public static File download() {
    File out = new File("target/vault/vault");

    if (SystemUtils.IS_OS_WINDOWS) {
      out = new File("target/vault/vault.exe");
    }

    if (out.isFile()) {
      return out;
    }

    File zip = new File("target/vault.zip");

    try {
      FileUtils.copyURLToFile(getURL(VaultProcess.VAULT_VERSION), zip);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    assert zip.isFile();

    System.out.println(zip.getAbsolutePath() + " has been downloaded, unzipping");
    try {
      unzip(zip, out.getParentFile());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    System.out.println("Vault: " + out.getAbsolutePath());
    assert out.isFile();
    out.setExecutable(true);
    assert out.canExecute();
    return out;
  }


  private static URL getURL(String version) {
    StringBuilder url = new StringBuilder();
    url.append("https://releases.hashicorp.com/vault/").append(version).append("/vault_").append(version).append("_");

    if (SystemUtils.IS_OS_MAC) {
      url.append("darwin_");
    } else if (SystemUtils.IS_OS_LINUX) {
      url.append("linux_");
    }  else if (SystemUtils.IS_OS_WINDOWS) {
      url.append("windows_");
    } else {
      throw new IllegalStateException("Unsupported operating system");
    }

    if (ArchUtils.getProcessor().is64Bit()) {
      url.append("amd64.zip");
    } else {
      url.append("386.zip");
    }

    System.out.println("Downloading " + url.toString());
    try {
      return new URL(url.toString());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }


  private static void unzip(File zipFilePath, File destDir) throws IOException {
    if (!destDir.exists()) {
      destDir.mkdir();
    }
    ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFilePath));
    ZipEntry entry = zipIn.getNextEntry();
    // iterates over entries in the zip file
    while (entry != null) {
      String filePath = destDir.getAbsolutePath() + File.separator + entry.getName();
      if (!entry.isDirectory()) {
        // if the entry is a file, extracts it
        extractFile(zipIn, filePath);
      } else {
        // if the entry is a directory, make the directory
        File dir = new File(filePath);
        dir.mkdir();
      }
      zipIn.closeEntry();
      entry = zipIn.getNextEntry();
    }
    zipIn.close();
  }

  private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
    byte[] bytesIn = new byte[4096];
    int read = 0;
    while ((read = zipIn.read(bytesIn)) != -1) {
      bos.write(bytesIn, 0, read);
    }
    bos.close();
  }

}
