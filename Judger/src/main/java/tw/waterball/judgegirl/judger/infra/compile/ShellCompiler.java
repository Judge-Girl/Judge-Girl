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

package tw.waterball.judgegirl.judger.infra.compile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tw.waterball.judgegirl.commons.helpers.process.AbstractProcessRunner;
import tw.waterball.judgegirl.primitives.problem.Compilation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * @author - Haribo, johnny850807@gmail.com (Waterball)
 */
public class ShellCompiler extends AbstractProcessRunner implements Compiler {
    private final static Logger logger = LogManager.getLogger(ShellCompiler.class);
    private Path sourceRootPath;

    public ShellCompiler(Path sourceRootPath) {
        this.sourceRootPath = sourceRootPath;
    }

    @Override
    public CompileResult compile(Compilation compilation) {
        try {
            String[] commands = compilation.getScript().split("\\s");
            logger.info("<{}>, Compiling the program by the command: '{}'.",
                    sourceRootPath, Arrays.toString(commands));
            runProcess(sourceRootPath, commands);
            awaitTermination();
        } catch (Exception e) {
            logger.error(e);
        }

        logger.info(String.format("Compile <%s> %s.", isSuccessful() ? "Successful" : "Failed", getStderr()));
        if (isSuccessful()) {
            if (!Files.exists(sourceRootPath.resolve("a.out"))) {
                throw new IllegalStateException("Couldn't find 'a.out' executable after the compiling completed.");
            }
            return CompileResult.success();
        } else {
            return CompileResult.error(getStderr());
        }
    }
}
