package tw.waterball.judgegirl.springboot.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tw.waterball.judgegirl.plugins.Plugins;
import tw.waterball.judgegirl.plugins.api.JudgeGirlPluginLocator;
import tw.waterball.judgegirl.plugins.api.PresetJudgeGirlPluginLocator;


/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Configuration
public class JudgeGirlPluginConfig {

    @Bean
    public JudgeGirlPluginLocator judgeGirlPluginLocator() {
        return new PresetJudgeGirlPluginLocator(Plugins.getDefaultPlugins());
    }
}
