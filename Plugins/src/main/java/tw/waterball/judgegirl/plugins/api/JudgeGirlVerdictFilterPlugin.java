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
import tw.waterball.judgegirl.primitives.submission.verdict.VerdictIssuer;

/**
 * The filter that add new runtime behaviors and modify some parts of the verdict.
 * The filter method will be invoked after the testcases execution and output matching has been completed.
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
public interface JudgeGirlVerdictFilterPlugin extends JudgeGirlPlugin {
    JudgePluginTag.Type TYPE = JudgePluginTag.Type.FILTER;

    void filter(VerdictIssuer verdictIssuer);
}
