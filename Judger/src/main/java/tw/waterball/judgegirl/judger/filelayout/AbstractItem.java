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

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public abstract class AbstractItem implements Item {
    protected Directory parent;
    protected String key;
    protected String name;

    public AbstractItem(String key, String name) {
        this.key = key;
        this.name = name;
    }

    @Override
    public void setParent(Directory parent) {
        this.parent = parent;
    }

    @Override
    public Directory getParent() {
        return parent;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public Path getAbsolutePath() {
        if (parent != null) {
            String name = getName();
            return parent.getAbsolutePath().resolve(name);
        }
        return Paths.get("/").resolve(getName());
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

}
