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

package tw.waterball.judgegirl.plugins.impl.match;

import org.apache.commons.io.FileUtils;
import tw.waterball.judgegirl.entities.problem.JudgePluginTag;
import tw.waterball.judgegirl.plugins.api.ParameterMeta;
import tw.waterball.judgegirl.plugins.api.match.JudgeGirlMatchPolicyPlugin;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Use the regex expression (stated in the expected output) to match the actual output.
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
public class RegexMatchPolicyPlugin extends AbstractJudgeGirlMatchPolicyPlugin {
    public final static String GROUP = JUDGE_GIRL_GROUP;
    public final static String NAME = "Regex";
    public final static String DESCRIPTION = "Use the regex expression (stated in the expected output) to match the actual output.";
    public final static String VERSION = "1.0";
    public final static JudgePluginTag TAG = new JudgePluginTag(JudgeGirlMatchPolicyPlugin.TYPE, GROUP, NAME, VERSION);

    public RegexMatchPolicyPlugin() {
        super(Collections.emptyMap());
    }

    @Override
    public Set<ParameterMeta> getParameterMetas() {
        return Collections.emptySet();
    }

    public String getDescription() {
        return DESCRIPTION;
    }

    @Override
    public JudgePluginTag getTag() {
        return TAG;
    }


    @Override
    protected boolean onDetermineTwoFileContentMatches(Path actualFilePath, Path expectFilePath) throws Exception {
        Pattern pattern = Pattern.compile(FileUtils.readFileToString(expectFilePath.toFile(), StandardCharsets.UTF_8));
        String content = FileUtils.readFileToString(actualFilePath.toFile(), StandardCharsets.UTF_8);
        Matcher matcher = pattern.matcher(content);
        return matcher.matches();
    }
}
