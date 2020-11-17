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

package tw.waterball.judgegirl.judger.filelayout;

import tw.waterball.judgegirl.entities.problem.Testcase;
import tw.waterball.judgegirl.entities.submission.Submission;

import java.lang.reflect.Field;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class InterpolationItemVisitor implements ItemVisitor {
    private Submission submission;
    private Testcase testcase;

    public InterpolationItemVisitor(Testcase testcase, Submission submission) {
        this.submission = submission;
        this.testcase = testcase;
    }

    @Override
    public void visit(OrdinaryFile ordinaryFile) {
        interpolate(ordinaryFile);
    }

    @Override
    public void visit(Directory directory) {
        interpolate(directory);
        directory.getChildren()
                .forEach(item -> item.acceptVisitor(this));
    }

    private void interpolate(Item item) {
        item.setName(interpolate(item.getName()));
    }

    private String interpolate(String fileName) {
        fileName = fileName.trim();
        if (fileName.length() >= 2 &&
                fileName.substring(0, 2).equals("${") &&
                fileName.charAt(fileName.length() - 1) == '}') {
            fileName = fileName.replaceAll("[\\s${}]", "");
            String[] split = fileName.split("\\.");
            String entityName = split[0].toLowerCase();
            String fieldName = split[1];
            Object entity;
            switch (entityName) {
                case "testcase":
                    entity = testcase;
                    break;
                case "submission":
                    entity = submission;
                    break;
                default:
                    throw new IllegalArgumentException("Interpolation failed: unknown token: " + entityName + ".");
            }
            try {
                Field field = entity.getClass().getDeclaredField(fieldName);
                boolean accessible = field.isAccessible();
                if (!accessible) {
                    field.setAccessible(true);
                }
                String value = String.valueOf(field.get(entity));
                field.setAccessible(accessible);
                return value;
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        } else {
            return fileName;
        }
    }

}
