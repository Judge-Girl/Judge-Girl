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

package tw.waterball.judgegirl.commons.helpers;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class DownloadZipFileFromUrlHelper {

    /**
     * @return ZipInputStream that supports unzip-streaming from a url, note:
     * use <code>while ((entry = zipInputStream.getNextEntry()) != null) {} </code>
     * to ensure that the stream would finally be closed.
     */
    public ZipInputStream download(String urlString, Charset charset) throws IOException {
        URL url = new URL(urlString);
        URLConnection urlConnection = url.openConnection();
        return new ZipInputStream(urlConnection.getInputStream(), charset);
    }
    
    public void downloadAndPrint(String urlString, Charset charset) throws  IOException {
        ZipInputStream zipInputStream = download(urlString, charset);
        ZipEntry zipEntry;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            String fileName = zipEntry.getName();
            System.out.println(fileName);
            System.out.println("=============================");
            IOUtils.copy(zipInputStream, System.out);
        }
    }
}
