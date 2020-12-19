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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class DirectoryUtils {

    /**
     * TODO should be improved, the current algorithm is brutal
     * Compare the equality entirely of contents file-by-file under the given
     * two directories' paths.
     * <p>
     * Note: this is just comparing actual files,
     * If you have empty folders you want to compare too,
     * you may need to implement another util method for this.
     */
    public static boolean contentEquals(Path p1, Path p2) {
        return contentEqualsOneSideComparison(p1, p2) &&
                contentEqualsOneSideComparison(p2, p1);
    }

    private static boolean contentEqualsOneSideComparison(Path p1, Path p2) {
        final boolean[] equal = {true};
        try {
            Files.walkFileTree(p1, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file,
                                                 BasicFileAttributes attrs)
                        throws IOException {
                    FileVisitResult result = super.visitFile(file, attrs);

                    Path relativize = p1.relativize(file);
                    Path fileInOther = p2.resolve(relativize);

                    byte[] otherBytes = Files.readAllBytes(fileInOther);
                    byte[] theseBytes = Files.readAllBytes(file);
                    if (!Arrays.equals(otherBytes, theseBytes)) {
                        equal[0] = false;
                    }
                    return result;
                }
            });
        } catch (IOException e) {
            return false;
        }
        return equal[0];
    }

    public static File[] removeHiddenDirectories(File[] testcaseDirs) {
        return Arrays.stream(testcaseDirs)
                .filter(d -> !d.isHidden())
                .toArray(File[]::new);
    }
}
