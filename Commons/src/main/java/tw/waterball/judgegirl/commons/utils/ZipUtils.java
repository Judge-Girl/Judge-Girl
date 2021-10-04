/*
 * Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package tw.waterball.judgegirl.commons.utils;

import org.apache.commons.io.IOUtils;
import tw.waterball.judgegirl.commons.models.files.StreamingResource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.io.FileUtils.forceMkdir;

/**
 * TODO: make it cleaner
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
public class ZipUtils {

    public static byte[] zipFilesFromResources(String... resourcePaths) {
        return zip(stream(resourcePaths)
                .map(path -> new StreamingResource(PathUtils.getFileName(path),
                        ResourceUtils.getResourceAsStream(path)))
                .collect(toList()));
    }

    public static <T extends StreamingResource> byte[] zip(Collection<T> streamingResources) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zipos = new ZipOutputStream(baos)) {
            for (StreamingResource streamingResource : streamingResources) {
                writeFileAsZipEntry(streamingResource.getFileName(), zipos,
                        streamingResource.getInputStream());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }


    public static byte[] zip(StreamingResource... streamingResources) {
        return zip(Arrays.asList(streamingResources));
    }

    public static ByteArrayInputStream zipToStream(StreamingResource... files) {
        return new ByteArrayInputStream(zip(files));
    }

    public static <T extends StreamingResource> ByteArrayInputStream zipToStream(Collection<T> files) {
        return new ByteArrayInputStream(zip(files));
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

    public static byte[] zip(String fileName, String str) throws IOException {
        return zip(fileName, new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)));
    }

    public static ByteArrayInputStream zipToStream(String fileName, String str) throws IOException {
        return new ByteArrayInputStream(zip(fileName, str));
    }

    public static void unzipToDestination(InputStream in,
                                          Path destinationPath) throws IOException {
        try (ZipInputStream zin = new ZipInputStream(in)) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                Path path = destinationPath.resolve(entry.getName());
                Path parent = path.getParent();
                if (parent != null) {
                    forceMkdir(parent.toFile());
                }
                if (entry.isDirectory()) {
                    if (!Files.exists(path)) {
                        Files.createDirectory(path);
                    }
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

    public static void zipDirectoryContents(File directory, OutputStream out, String... ignoredFileNames) throws IOException {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("The file " + directory.getAbsolutePath() + " is not a directory.");
        }
        File[] files = directory.listFiles();
        if (files != null) {
            zipFromFile(files, out, ignoredFileNames);
        }

    }

    public static void zipFromFile(File file, OutputStream out, String... ignoredFileNames) throws IOException {
        zipFromFile(new File[]{file}, out, ignoredFileNames);
    }

    /**
     * Zip the given files recursively (i.e. including all sub-directories)
     * through the given FileOutputStream, except those whose file names are contained in
     * ignoredFileNames.
     */
    public static void zipFromFile(File[] files, OutputStream out, String... ignoredFileNames) throws IOException {
        zipFromFile(files, out, name -> ArrayUtils.contains(ignoredFileNames, name));
    }

    public static void zipFromFile(File[] files, OutputStream out, Predicate<String> ignoredFilePredicate) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(out)) {
            for (File file : files) {
                if (!ignoredFilePredicate.test(file.getName())) {
                    writeZipEntry(file, zos, ignoredFilePredicate);
                }
            }
        }
    }

    private static void writeZipEntry(File file, ZipOutputStream zipos,
                                      Predicate<String> ignoredFilePredicate) throws IOException {
        writeZipEntry("", file, zipos, ignoredFilePredicate);
    }

    private static void writeZipEntry(String path, File file,
                                      ZipOutputStream zipos, Predicate<String> ignoredFilePredicate) throws IOException {
        if (!ignoredFilePredicate.test(file.getName())) {
            if (file.isDirectory()) {
                String[] files = file.list();
                if (files != null) {
                    for (String fileName : files) {
                        writeZipEntry(path + file.getName() + "/",
                                new File(file, fileName), zipos, ignoredFilePredicate);
                    }
                }

            } else {
                writeFileAsZipEntry(path + file.getName(), zipos,
                        new FileInputStream(file));
            }
        }
    }

    public static void writeFileAsZipEntry(String fileName,
                                           ZipOutputStream zipos, InputStream in) throws IOException {
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipos.putNextEntry(zipEntry);
        IOUtils.copy(in, zipos);
        in.close();
        zipos.closeEntry();
    }

}
