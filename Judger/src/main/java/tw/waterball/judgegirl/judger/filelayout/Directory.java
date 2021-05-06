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

import java.util.LinkedList;
import java.util.List;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class Directory extends AbstractItem {
    private final List<Item> children = new LinkedList<>();

    public Directory(String key, String name) {
        super(key, name);
    }

    public void add(Item item) {
        children.add(item);
        item.setParent(this);
    }

    public List<Item> getChildren() {
        return children;
    }

    public Item getByKey(String key) {
        return children.stream()
                .filter(item -> item.getKey().equals(key)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Child " + key + " not found."));
    }

    public Item getChild(String name) {
        return children.stream()
                .filter(item -> item.getName().equals(name)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Child " + name + " not found."));
    }

    @Override
    public void acceptVisitor(ItemVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        YAMLPrintVisitor printer = new YAMLPrintVisitor();
        acceptVisitor(printer);
        return printer.getOutput();
    }
}
