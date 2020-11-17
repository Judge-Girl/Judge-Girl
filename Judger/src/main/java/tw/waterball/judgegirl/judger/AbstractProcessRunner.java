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

package tw.waterball.judgegirl.judger;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public abstract class AbstractProcessRunner implements ProcessRunner {
    private Process process;
    private int exitCode = -1;
    private Thread stdoutWorker;
    private Thread stderrWorker;
    private String stderr;
    private String stdout;

    protected Process runProcess(String... commands) throws IOException {
        return runProcess(null, commands);
    }

    protected Process runProcess(@Nullable Path directory, String... commands) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        if (directory != null) {
            processBuilder.directory(directory.toFile());
        }
        process = processBuilder.start();
        readFromStreamsAsync();
        return process;
    }

    private void readFromStreamsAsync() {
        this.stdoutWorker = startStreamWorker(process.getInputStream(), o -> stdout = o);
        this.stderrWorker = startStreamWorker(process.getErrorStream(), e -> stderr = e);
    }

    private Thread startStreamWorker(InputStream in, Consumer<String> outputConsumer) {
        Thread worker = new Thread(() -> {
            try {
                String output = IOUtils.toString(in, StandardCharsets.UTF_8);
                outputConsumer.accept(output);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        worker.start();
        return worker;
    }

    public String getStderr() {
        return stderr;
    }

    public String getStdout() {
        return stdout;
    }

    @Override
    public void awaitTermination() {
        try {
            this.exitCode = process.waitFor();
            this.stdoutWorker.join();
            this.stderrWorker.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        process.destroy();
    }

    @Override
    public boolean isTerminated() {
        return !process.isAlive();
    }

    @Override
    public boolean isSuccessful() {
        return this.exitCode == 0;
    }

    @Override
    public int getExitCode() {
        return this.exitCode;
    }


}
