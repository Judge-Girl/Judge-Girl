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

package tw.waterball.judgegirl.problemapi.views;

import org.junit.jupiter.api.Test;
import tw.waterball.judgegirl.primitives.problem.*;

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static tw.waterball.judgegirl.primitives.problem.Language.C;
import static tw.waterball.judgegirl.problemapi.views.LanguageEnvView.toViewModel;

class LanguageEnvViewTest {
    @Test
    void languageEnvMappingTest() {
        LanguageEnv languageEnv = LanguageEnv.builder()
                .language(C)
                .compilation(new Compilation("Compilation Script"))
                .resourceSpec(new ResourceSpec(0.5f, 0))
                .submittedCodeSpec(new SubmittedCodeSpec(C, "main.c"))
                .providedCodes(new ProvidedCodes("providedCodesFileId", singletonList("providedCodesFileName")))
                .build();

        testMapping(languageEnv);
    }

    @Test
    void languageEnvWithoutProvidedCodesMappingTest() {
        LanguageEnv languageEnvWithoutProvideCodes = LanguageEnv.builder()
                .language(C)
                .compilation(new Compilation("Compilation Script"))
                .resourceSpec(new ResourceSpec(0.5f, 0))
                .submittedCodeSpec(new SubmittedCodeSpec(C, "main.c"))
                .build();

        testMapping(languageEnvWithoutProvideCodes);
    }

    private void testMapping(LanguageEnv languageEnv) {
        LanguageEnvView view = toViewModel(languageEnv);
        LanguageEnv mappedLanguageEnv = view.toValue();
        assertEquals(view, toViewModel(mappedLanguageEnv));
        assertLanguageEnvEquals(languageEnv, mappedLanguageEnv);
    }

    private void assertLanguageEnvEquals(LanguageEnv expected, LanguageEnv actual) {
        assertEquals(expected.getCompilation().getScript(), actual.getCompilation().getScript());
        assertEquals(expected.getResourceSpec().getCpu(), actual.getResourceSpec().getCpu());
        assertEquals(expected.getResourceSpec().getGpu(), actual.getResourceSpec().getGpu());
        assertEquals(expected.getSubmittedCodeSpecs(), actual.getSubmittedCodeSpecs());
        assertEquals(expected.getProvidedCodes().isPresent(), actual.getProvidedCodes().isPresent());
        if (expected.getProvidedCodes().isPresent()) {
            assertEquals(expected.getProvidedCodes().get().getProvidedCodesFileId(), actual.getProvidedCodes().get().getProvidedCodesFileId());
            assertEquals(expected.getProvidedCodes().get().getProvidedCodesFileName(), actual.getProvidedCodes().get().getProvidedCodesFileName());
        }
    }
}