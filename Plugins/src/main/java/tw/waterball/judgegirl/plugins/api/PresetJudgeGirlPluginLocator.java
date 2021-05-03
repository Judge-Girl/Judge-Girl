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


import tw.waterball.judgegirl.primitives.problem.JudgePluginTag;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableSet;

/**
 * The locator the preset with a set of plugins.
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
public class PresetJudgeGirlPluginLocator implements JudgeGirlPluginLocator {
    private final Map<JudgePluginTag, JudgeGirlPlugin> pluginMap = new HashMap<>();

    public PresetJudgeGirlPluginLocator(JudgeGirlPlugin... plugins) {
        for (JudgeGirlPlugin plugin : plugins) {
            pluginMap.put(plugin.getTag(), plugin);
        }
    }

    public PresetJudgeGirlPluginLocator(List<JudgeGirlPlugin> plugins) {
        for (JudgeGirlPlugin plugin : plugins) {
            pluginMap.put(plugin.getTag(), plugin);
        }
    }

    @Override
    public JudgeGirlPlugin locate(JudgePluginTag judgePluginTag) {
        if (!pluginMap.containsKey(judgePluginTag)) {
            throw new JudgeGirlPluginNotFoundException(judgePluginTag);
        }
        return pluginMap.get(judgePluginTag);
    }

    @Override
    public Collection<JudgePluginTag> getAll() {
        return unmodifiableSet(pluginMap.keySet());
    }

}
