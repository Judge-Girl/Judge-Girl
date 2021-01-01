/*
 *  Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package tw.waterball.judgegirl.commons.utils;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import tw.waterball.judgegirl.commons.models.files.InputStreamResource;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.util.Objects.requireNonNull;

/**
 * TODO Test Coverage and make it cleaner
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
public class ZipUtils {
    public static ByteArrayInputStream zipClassPathResourcesToStream(String... resourcePaths) {
        return new ByteArrayInputStream(zipRegularFilesFromResources(resourcePaths));
    }

    public static byte[] zipDirectoryFromResources(String directoryResourcePath) throws IOException {
        var baos = new ByteArrayOutputStream();
        var zos = new ZipOutputStream(baos);
        ZipFile zipFile = new ZipFile(ResourceUtils.getFile(directoryResourcePath));
        zipFile.stream().forEach(zn -> writeZipEntry(zn, zos));
        zos.finish();
        return baos.toByteArray();
    }

    @SneakyThrows
    public static void writeZipEntry(ZipEntry zipEntry, ZipOutputStream zos) {
        zos.putNextEntry(zipEntry);
    }

    public static byte[] zipRegularFilesFromResources(String... resourcePaths) {
        return zip(
                Arrays.stream(resourcePaths)
                        .map(path -> new InputStreamResource(PathUtils.getFileName(path),
                                ResourceUtils.getResourceAsStream(path)))
                        .collect(Collectors.toList()));
    }

    public static <T extends InputStreamResource> byte[] zip(List<T> streamingResources) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zipos = new ZipOutputStream(baos)) {
            for (InputStreamResource inputStreamResource : streamingResources) {
                writeFileAsZipEntry(inputStreamResource.getFileName(), zipos,
                        inputStreamResource.getInputStream());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }


    public static byte[] zip(InputStreamResource... inputStreamResources) {
        return zip(Arrays.asList(inputStreamResources));
    }

    public static ByteArrayInputStream zipToStream(InputStreamResource... multipartFiles) {
        return new ByteArrayInputStream(zip(multipartFiles));
    }

    public static <T extends InputStreamResource> ByteArrayInputStream zipToStream(List<T> multipartFiles) {
        return new ByteArrayInputStream(zip(multipartFiles));
    }

    public static byte[] zip(String fileName, InputStream in) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zipos = new ZipOutputStream(baos)) {
            writeFileAsZipEntry(fileName, zipos, in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    public static byte[] zip(String fileName, String str) {
        return zip(fileName, new ByteArrayInputStream(str.getBytes()));
    }

    public static ByteArrayInputStream zipToStream(String fileName, String str) {
        return new ByteArrayInputStream(zip(fileName, str));
    }

    @SuppressWarnings("RedundantIfStatement")
    public static void unzipToDestination(InputStream in,
                                          Path destinationPath) throws IOException {
        try (ZipInputStream zin = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                Path path = destinationPath.resolve(entry.getName());
                FileUtils.forceMkdir(path.getParent().toFile());
                if (entry.isDirectory()) {
                    Files.createDirectory(path);
                } else {
                    try (FileOutputStream out = new FileOutputStream(path.toFile())) {
                        IOUtils.copy(zin, out);
                    }
                }
                zin.closeEntry();
            }
        }
    }


    /**
     * @return the raw data of the first file in the zip
     */
    public static byte[] unzipFirst(InputStream in) throws IOException {
        return unzip(in).get(0);
    }

    public static List<byte[]> unzip(InputStream in) throws IOException {
        try (ZipInputStream zin = new ZipInputStream(in)) {
            List<byte[]> result = new ArrayList<>();
            while (zin.getNextEntry() != null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                IOUtils.copy(zin, baos);
                result.add(baos.toByteArray());
            }
            return result;
        }
    }

    /**
     * Zip the given file recursively (i.e. including all sub-directories)
     * and write to the FileOutputStream, except those whose file names are contained in
     * ignoredFileNames.
     */
    @SneakyThrows
    public static void zip(File file, FileOutputStream fos, String... ignoredFileNames) {
        zip(file.toURI().toURL(), fos, ignoredFileNames);
    }

    /**
     * Zip the given files recursively (i.e. including all sub-directories)
     * and write to the FileOutputStream, except those whose file names are contained in
     * ignoredFileNames.
     */
    @SneakyThrows
    public static void zip(File[] files, FileOutputStream fos, String... ignoredFileNames) {
        URL[] urls = new URL[files.length];
        for (int i = 0; i < files.length; i++) {
            urls[i] = files[i].toURI().toURL();
        }
        zip(urls, fos, ignoredFileNames);
    }

    /**
     * Zip the given file (from the sourceURL) recursively (i.e. including all sub-directories)
     * and write to the FileOutputStream, except those whose file names are contained in
     * ignoredFileNames.
     */
    public static void zip(URL sourceURL, FileOutputStream fos, String... ignoredFileNames) {
        zip(new URL[]{sourceURL}, fos, ignoredFileNames);
    }

    /**
     * Zip the given files (from sourceURLs) recursively (i.e. including all sub-directories)
     * and write to the FileOutputStream, except those whose file names are contained in
     * ignoredFileNames.
     */
    public static void zip(URL[] sourceURLs, FileOutputStream fos, String... ignoredFileNames) {
        try (ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (URL url : sourceURLs) {
                String path = url.getPath();
                // remove the trailing separator '/' (in order to extract its file name)
                path = removeTrailingSeparator(path);
                if (!ArrayUtils.contains(ignoredFileNames, FilenameUtils.getName(path))) {
                    writeZipEntry(url, zos, ignoredFileNames);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeZipEntry(URL sourceURL, ZipOutputStream zipos,
                                      String... ignoredFileNames) throws IOException {
        writeZipEntry("", sourceURL, zipos, ignoredFileNames);
    }

    private static void writeZipEntry(String path, URL sourceURL,
                                      ZipOutputStream zipos, String... ignoredFileNames) throws IOException {
        String sourcePath = sourceURL.getPath();
        // remove the trailing separator '/' (in order to extract its file name)
        sourcePath = removeTrailingSeparator(sourcePath);
        if (!ArrayUtils.contains(ignoredFileNames, FilenameUtils.getName(sourcePath))) {
            File sourceFile = Paths.get(sourcePath).toFile();
            if (sourceFile.isDirectory()) {
                for (File file : requireNonNull(sourceFile.listFiles())) {
                    writeZipEntry(path + file.getName() + "/",
                            file.toURI().toURL(), zipos, ignoredFileNames);
                }
            } else {
                writeFileAsZipEntry(removeTrailingSeparator(path), zipos,
                        new FileInputStream(sourceFile));
            }
        }
    }

    private static void writeFileAsZipEntry(String fileName,
                                            ZipOutputStream zipos, InputStream in) throws IOException {
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipos.putNextEntry(zipEntry);
        IOUtils.copy(in, zipos);
        in.close();
        zipos.closeEntry();
    }

    private static String removeTrailingSeparator(String path) {
        if (path.charAt(path.length() - 1) == File.separatorChar) {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

}
