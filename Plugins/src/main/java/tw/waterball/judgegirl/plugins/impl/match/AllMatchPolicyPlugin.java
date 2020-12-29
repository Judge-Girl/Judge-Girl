/*
 *  Copyright 2020 Johnny850807 (Waterball) 潘冠辰
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package tw.waterball.judgegirl.plugins.impl.match;

import org.apache.commons.io.IOUtils;
import tw.waterball.judgegirl.entities.problem.JudgePluginTag;
import tw.waterball.judgegirl.plugins.api.ParameterMeta;
import tw.waterball.judgegirl.plugins.api.match.JudgeGirlMatchPolicyPlugin;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

/**
 * Match the actual output exactly as same as the content of expected output's,
 * even any little differences in `lines` or `spaces` will lead to WA.
 * <p>
 * So it means literally `All Match` in every byte.
 *
 * @author - johnny850807@gmail.com (Waterball)
 */
public class AllMatchPolicyPlugin extends AbstractJudgeGirlMatchPolicyPlugin {
    public final static String GROUP = JUDGE_GIRL_GROUP;
    public final static String NAME = "All Match";
    public final static String DESCRIPTION = "Assert the actual output exactly as same as the expected output, " +
            "even any differences in `lines` or `spaces` will lead to WA.";
    public final static String VERSION = "1.0";
    public final static JudgePluginTag TAG = new JudgePluginTag(JudgeGirlMatchPolicyPlugin.TYPE, GROUP, NAME, VERSION);
    private boolean strictTrailingBreakLine = false;

    public AllMatchPolicyPlugin() {
        super(Collections.emptyMap());
    }

    public void setStrictTrailingBreakLine(boolean strictTrailingBreakLine) {
        this.strictTrailingBreakLine = strictTrailingBreakLine;
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
        // TODO strictTrailingBreakLine is not implemented yet
        String actual = IOUtils.toString(new FileInputStream(actualFilePath.toFile()), StandardCharsets.US_ASCII);
        String expected = IOUtils.toString(new FileInputStream(expectFilePath.toFile()), StandardCharsets.US_ASCII);
        return actual.equals(expected);

    }
}
