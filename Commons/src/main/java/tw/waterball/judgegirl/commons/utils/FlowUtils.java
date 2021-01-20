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

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class FlowUtils {
    public static void repeat(Runnable runnable, int repeat) throws IllegalStateException {
        int count = 0;
        boolean success;
        do {
            success = true;
            try {
                if (count++ >= repeat) {
                    success = false;
                    break;
                }
                runnable.run();
            } catch (Exception err) {
                success = false;
            }
        } while (!success);
        if (!success) {
            throw new IllegalStateException("Fail over " + repeat + "times, please re-run.");
        }
    }
}
