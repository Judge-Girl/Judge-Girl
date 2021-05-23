package tw.waterball.judgegirl.springboot.problem.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import tw.waterball.judgegirl.plugins.api.*;
import tw.waterball.judgegirl.plugins.impl.match.AllMatchPolicyPlugin;
import tw.waterball.judgegirl.plugins.impl.match.RegexMatchPolicyPlugin;
import tw.waterball.judgegirl.primitives.problem.JudgePluginTag;
import tw.waterball.judgegirl.primitives.submission.verdict.VerdictIssuer;
import tw.waterball.judgegirl.problemapi.views.JudgePluginTagView;
import tw.waterball.judgegirl.springboot.problem.SpringBootProblemApplication;
import tw.waterball.judgegirl.springboot.profiles.Profiles;
import tw.waterball.judgegirl.testkit.AbstractSpringBootTest;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;
import static tw.waterball.judgegirl.problemapi.views.JudgePluginTagView.toViewModel;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@ActiveProfiles({Profiles.JWT, Profiles.EMBEDDED_MONGO})
@ContextConfiguration(classes = {SpringBootProblemApplication.class, PluginControllerTest.TestConfig.class})
public class PluginControllerTest extends AbstractSpringBootTest {
    public static final AllMatchPolicyPlugin MATCH_PLUGIN_1 = new AllMatchPolicyPlugin();
    public static final RegexMatchPolicyPlugin MATCH_PLUGIN_2 = new RegexMatchPolicyPlugin();
    public static final FilterPlugin FILTER_PLUGIN = new FilterPlugin();
    @Autowired
    JudgeGirlPluginLocator pluginLocator;

    @Configuration
    public static class TestConfig {
        @Bean
        @Primary
        JudgeGirlPluginLocator locator() {
            return new PresetJudgeGirlPluginLocator(MATCH_PLUGIN_1,
                    MATCH_PLUGIN_2, FILTER_PLUGIN);
        }
    }

    @Test
    void testGetPlugins() throws Exception {
        tagsShouldContain(getAllJudgePluginTags(),
                MATCH_PLUGIN_1, MATCH_PLUGIN_2, FILTER_PLUGIN);

        tagsShouldContain(getJudgePluginTags(JudgePluginTag.Type.OUTPUT_MATCH_POLICY),
                MATCH_PLUGIN_1, MATCH_PLUGIN_2);

        tagsShouldContain(getJudgePluginTags(JudgePluginTag.Type.FILTER), FILTER_PLUGIN);
    }

    private void tagsShouldContain(List<JudgePluginTagView> actualTags, JudgeGirlPlugin... expectedPlugins) {
        assertEqualsIgnoreOrder(actualTags,
                mapToList(expectedPlugins, plugin -> toViewModel(plugin.getTag())));
    }

    private List<JudgePluginTagView> getJudgePluginTags(JudgePluginTag.Type type) throws Exception {
        return getBody(mockMvc.perform(get("/api/plugins").queryParam("type", type.toString()))
                .andExpect(status().isOk()), new TypeReference<>() {
        });
    }

    private List<JudgePluginTagView> getAllJudgePluginTags() throws Exception {
        return getBody(mockMvc.perform(get("/api/plugins"))
                .andExpect(status().isOk()), new TypeReference<>() {
        });
    }
}

class FilterPlugin extends AbstractJudgeGirlPlugin implements JudgeGirlVerdictFilterPlugin {

    @Override
    public String getDescription() {
        return "For testing";
    }

    @Override
    public JudgePluginTag getTag() {
        return new JudgePluginTag(TYPE, "test", "filter", "1.0.0");
    }

    @Override
    public void filter(VerdictIssuer verdictIssuer) {
    }
}
