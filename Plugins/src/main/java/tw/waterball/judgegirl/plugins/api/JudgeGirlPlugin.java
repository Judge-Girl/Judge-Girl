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

package tw.waterball.judgegirl.plugins.api;

import tw.waterball.judgegirl.entities.problem.JudgePluginTag;

import java.util.Map;
import java.util.Set;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface JudgeGirlPlugin {
    String JUDGE_GIRL_GROUP = "Judge Girl";

    default String getGroup() {
        return getTag().getGroup();
    }

    default String getName() {
        return getTag().getName();
    }

    default String getVersion() {
        return getTag().getVersion();
    }

    Set<ParameterMeta> getParameterMetas();

    Map<String, String> getParameters();

    String getDescription();

    JudgePluginTag getTag();
}
