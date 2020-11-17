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

package tw.waterball.judgegirl.problemservice.domain.repositories;

import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.commons.utils.ZipUtils;
import tw.waterball.judgegirl.entities.problem.*;
import tw.waterball.judgegirl.plugins.impl.match.AllMatchPolicyPlugin;

import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */

@Named
public class StubProblemRepository implements ProblemRepository {
    private final static int STUB_ID = 1;

    private final static Problem PROBLEM_STUB = Problem.builder()
            .id(STUB_ID)
            .title("8 Queens")
            .markdownDescription("<!-- Highest Discount Rate -->\n" +
                    "\n" +
                    "## Task Description ##\n" +
                    "There is a sales coming. Every payment $p$ (a **positive integer**) will have a discount $d$ according to the following table.\n" +
                    "\n" +
                    "| | range of $p$ | discount $d$ |\n" +
                    "|---|---|---|\n" +
                    "| 1 | $0 < p < a$ | 0 |\n" +
                    "| 2 | $a \\leq p < b$ | $v, \\text{ if }p\\text{ is odd}$<br><hr>$w, \\text{ if }p\\text{ is even}$ |\n" +
                    "| 3 | $b \\leq p$ | $x, \\text{ if } p \\equiv 0\\ (mod\\ 3)$<br><hr>$y, \\text{ if }p \\equiv 1\\ (mod\\ 3)$<br><hr>$z, \\text{ if }p \\equiv 2\\ (mod\\ 3)$ |\n" +
                    "\n" +
                    "We define **discount rate** as $d/p$. Write a program to find the $p$ that has the highest discount rate. If there are multiple payments that give the highest discount rate, output the smallest one.\n" +
                    "\n" +
                    "## Input Format ##\n" +
                    "There will be 7 lines for integers $a, b, v, w, x, y, z$.\n" +
                    "\n" +
                    "* $v, w, x, y, z \\geq 0$\n" +
                    "* $a > 1$\n" +
                    "* $b > a + 2$\n" +
                    "\n" +
                    "## Output Format ##\n" +
                    "The smallest payment $p$ that has the highest $d/p$ discount rate.\n" +
                    "\n" +
                    "## Sample Input ##\n" +
                    "```\n" +
                    "10\n" +
                    "30\n" +
                    "5\n" +
                    "6\n" +
                    "18\n" +
                    "8\n" +
                    "9\n" +
                    "```\n" +
                    "## Sample Output ##\n" +
                    "```\n" +
                    "10\n" +
                    "```\n" +
                    "## Hint ##\n" +
                    "Since we have not yet discuss the floating point numbers, so we compare two fractional" +
                    " numbers $a/b$ and $c/d$, by comparing $a \\* d$ and $b \\* c$ instead, when both $b$ and $d$ are positive integers.\n" +
                    "![Test Image](https://i.imgur.com/E9hmeDw.png)")
            .judgeSpec(new JudgeSpec(Language.C, JudgeEnv.NORMAL, 0.5f, 0))
            .outputMatchPolicyPluginTag(AllMatchPolicyPlugin.TAG)
            .tag("tag1").tag("tag2")
            .submittedCodeSpec(new SubmittedCodeSpec(Language.C, "main.c"))
            .submittedCodeSpec(new SubmittedCodeSpec(Language.C, "function.c"))
            .providedCodesFileId("providedCodesFileId")
            .testcaseIOsFileId("testcaseIOsFileId")
            .compilation(new Compilation("chmod +x nvprof.sh\n" +
                    "nvcc -Xcompiler \"-O2 -fopenmp\" main.cu -o main\n" +
                    "./nvprof.sh ./main\n" +
                    "cat nvvp.log"))
            .build();


    @Override
    public Optional<Problem> findProblemById(int problemId) {
        if (problemId == 1) {
            return Optional.of(PROBLEM_STUB);
        }
        return Optional.empty();
    }

    @Override
    public Optional<FileResource> downloadZippedProvidedCodes(int problemId) {
        if (problemId == 1) {
            ByteArrayInputStream in = ZipUtils.zipClassPathResourcesToStream("/stubs/file1.c", "/stubs/file2.c");
            return Optional.of(
                    new FileResource(PROBLEM_STUB.getProvidedCodesFileName(),
                            in.available(), in));
        }
        return Optional.empty();
    }

    @Override
    public List<Problem> find(ProblemQueryParams params) {
        for (String tag : params.getTags()) {
            if (PROBLEM_STUB.getTags().contains(tag)) {
                return Collections.singletonList(PROBLEM_STUB);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public int getPageSize() {
        return 10;
    }

    @Override
    public List<Problem> findAll() {
        return Collections.singletonList(PROBLEM_STUB);
    }


    @Override
    public List<String> getTags() {
        return PROBLEM_STUB.getTags();
    }

}
