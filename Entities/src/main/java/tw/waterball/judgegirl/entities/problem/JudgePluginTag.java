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

package tw.waterball.judgegirl.entities.problem;

import lombok.*;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
public class JudgePluginTag {
    private Type type;
    private String group;
    private String name;
    private String version;

    @Override
    public String toString() {
        return String.format("[%s] %s:%s:%s", type, group, name, version);
    }

    public enum Type {
        OUTPUT_MATCH_POLICY,
        CODE_INSPECTION
    }
}
