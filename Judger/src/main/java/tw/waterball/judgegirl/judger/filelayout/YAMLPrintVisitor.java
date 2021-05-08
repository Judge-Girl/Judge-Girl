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

/**
 * Construct a yaml presentation while visiting a composite structure.
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
public class YAMLPrintVisitor implements ItemVisitor {
    private final StringBuilder output = new StringBuilder();
    private int indentationLevel;

    @Override
    public void visit(OrdinaryFile ordinaryFile) {
        writeIndentation();
        output.append(ordinaryFile.getKey()).append(": \"")
                .append(ordinaryFile.getName()).append("\"\n");
    }

    @Override
    public void visit(Directory directory) {
        writeIndentation();
        output.append(directory.getKey()).append(": \n");
        indentationLevel++;
        writeIndentation();
        output.append("path: \"").append(directory.getName()).append("\"\n");
        directory.getChildren().forEach(item -> item.acceptVisitor(this));
        indentationLevel--;
    }

    private void writeIndentation() {
        for (int i = 0; i < indentationLevel; i++) {
            output.append("  ");
        }
    }

    public String getOutput() {
        return output.toString();
    }

    @Override
    public String toString() {
        return getOutput();
    }
}
