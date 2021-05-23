package tw.waterball.judgegirl.springboot.problem.controllers;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tw.waterball.judgegirl.plugins.api.JudgeGirlPluginLocator;
import tw.waterball.judgegirl.primitives.problem.JudgePluginTag;
import tw.waterball.judgegirl.problemapi.views.JudgePluginTagView;

import java.util.List;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@CrossOrigin
@RestController
@RequestMapping(value = "/api/plugins")
@AllArgsConstructor
public class PluginController {
    private final JudgeGirlPluginLocator pluginLocator;

    @GetMapping
    public List<JudgePluginTagView> getJudgePluginTags(@RequestParam(required = false) String type) {
        Predicate<JudgePluginTag> predicate = type == null ? tag -> true :
                tag -> tag.getType().toString().equalsIgnoreCase(type);
        return pluginLocator.getAll().stream()
                .filter(predicate)
                .map(JudgePluginTagView::toViewModel)
                .collect(toList());
    }
}
