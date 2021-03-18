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
 * Construct a tree presentation while visiting a composite structure.
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
public class TreePrintVisitor implements ItemVisitor {
    private int indentationLevel = 0;
    private boolean finalChild = false;
    private StringBuilder output = new StringBuilder();

    @Override
    public void visit(OrdinaryFile ordinaryFile) {
        writeIndentation();
        output.append(finalChild ? "└──" : "├── ")
                .append(ordinaryFile.getName()).append("\n");
    }

    @Override
    public void visit(Directory directory) {
        writeIndentation();
        String name = directory.getName();
        output.append(finalChild ? "└──" : "├── ").append(name).append("\n");
        indentationLevel++;
        for (int i = 0; i < directory.getChildren().size(); i++) {
            Item child = directory.getChildren().get(i);
            finalChild = i == directory.getChildren().size() - 1;
            child.acceptVisitor(this);
        }
        indentationLevel--;
    }

    private void writeIndentation() {
        for (int i = 0; i < indentationLevel; i++) {
            output.append("   ");
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
