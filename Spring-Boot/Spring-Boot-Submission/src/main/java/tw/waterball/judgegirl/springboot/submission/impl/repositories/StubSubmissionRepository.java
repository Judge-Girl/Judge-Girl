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

package tw.waterball.judgegirl.springboot.submission.impl.repositories;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import tw.waterball.judgegirl.entities.submission.Judge;
import tw.waterball.judgegirl.entities.problem.JudgeStatus;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.commons.models.files.StreamingResource;
import tw.waterball.judgegirl.commons.profiles.Dev;
import tw.waterball.judgegirl.commons.utils.ZipUtils;
import tw.waterball.judgegirl.entities.submission.Submission;
import tw.waterball.judgegirl.submissionservice.domain.repositories.SubmissionRepository;
import tw.waterball.judgegirl.entities.submission.SubmissionThrottling;
import tw.waterball.judgegirl.submissionservice.domain.usecases.dto.SubmissionQueryParams;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Dev
@Component
@PropertySource("judge-girl.properties")
public class StubSubmissionRepository implements SubmissionRepository {
    private Set<Submission> submissions = new HashSet<>();
    private Map<String, FileResource> zippedSubmittedCodeMap = new HashMap<>(); // <fileId, file>
    private Set<SubmissionThrottling> submissionThrottlings = new HashSet<>();

    public StubSubmissionRepository(@Value("${test-student-id}") int testStudentId) {
        Submission submission = new Submission("0", testStudentId, 1, "0");

        // the judges corresponds to the stub test cases
        // @see StubTestCaseRepository
        List<Judge> judges = new LinkedList<>();
        judges.add(new Judge("1", JudgeStatus.AC, 4, 4, "", 20));
        judges.add(new Judge("2", JudgeStatus.AC, 4, 4, "", 30));
        judges.add(new Judge("3", JudgeStatus.AC, 2, 3, "", 50));
        submission.setJudges(judges);
        submission.setJudgeTime(new Date());
        Calendar stubSubmissionTime = Calendar.getInstance();
        stubSubmissionTime.set(1996, Calendar.AUGUST, 7);
        submission.setSubmissionTime(stubSubmissionTime.getTime());

        submissions.add(submission);


        // the file names correspond to the stub submittedCodeSpecs
        // @see StubProblemRepository
        byte[] stubZipBytes = ZipUtils.zip(new StreamingResource("main.c",
                        new ByteArrayInputStream(("/* C demo code */\n" +
                                "\n" +
                                "#include <zmq.h>\n" +
                                "#include <pthread.h>\n" +
                                "#include <semaphore.h>\n" +
                                "#include <time.h>\n" +
                                "#include <stdio.h>\n" +
                                "#include <fcntl.h>\n" +
                                "#include <malloc.h>\n" +
                                "\n" +
                                "typedef struct {\n" +
                                "  void* arg_socket;\n" +
                                "  zmq_msg_t* arg_msg;\n" +
                                "  char* arg_string;\n" +
                                "  unsigned long arg_len;\n" +
                                "  int arg_int, arg_command;\n" +
                                "\n" +
                                "  int signal_fd;\n" +
                                "  int pad;\n" +
                                "  void* context;\n" +
                                "  sem_t sem;\n" +
                                "} acl_zmq_context;\n" +
                                "\n" +
                                "#define p(X) (context->arg_##X)\n" +
                                "\n" +
                                "void* zmq_thread(void* context_pointer) {\n" +
                                "  acl_zmq_context* context = (acl_zmq_context*)context_pointer;\n" +
                                "  char ok = 'K', err = 'X';\n" +
                                "  int res;\n" +
                                "\n" +
                                "  while (1) {\n" +
                                "    while ((res = sem_wait(&context->sem)) == EINTR);\n" +
                                "    if (res) {write(context->signal_fd, &err, 1); goto cleanup;}\n" +
                                "    switch(p(command)) {\n" +
                                "    case 0: goto cleanup;\n" +
                                "    case 1: p(socket) = zmq_socket(context->context, p(int)); break;\n" +
                                "    case 2: p(int) = zmq_close(p(socket)); break;\n" +
                                "    case 3: p(int) = zmq_bind(p(socket), p(string)); break;\n" +
                                "    case 4: p(int) = zmq_connect(p(socket), p(string)); break;\n" +
                                "    case 5: p(int) = zmq_getsockopt(p(socket), p(int), (void*)p(string), &p(len)); break;\n" +
                                "    case 6: p(int) = zmq_setsockopt(p(socket), p(int), (void*)p(string), p(len)); break;\n" +
                                "    case 7: p(int) = zmq_send(p(socket), p(msg), p(int)); break;\n" +
                                "    case 8: p(int) = zmq_recv(p(socket), p(msg), p(int)); break;\n" +
                                "    case 9: p(int) = zmq_poll(p(socket), p(int), p(len)); break;\n" +
                                "    }\n" +
                                "    p(command) = errno;\n" +
                                "    write(context->signal_fd, &ok, 1);\n" +
                                "  }\n" +
                                " cleanup:\n" +
                                "  close(context->signal_fd);\n" +
                                "  free(context_pointer);\n" +
                                "  return 0;\n" +
                                "}\n" +
                                "\n" +
                                "void* zmq_thread_init(void* zmq_context, int signal_fd) {\n" +
                                "  acl_zmq_context* context = malloc(sizeof(acl_zmq_context));\n" +
                                "  pthread_t thread;\n" +
                                "\n" +
                                "  context->context = zmq_context;\n" +
                                "  context->signal_fd = signal_fd;\n" +
                                "  sem_init(&context->sem, 1, 0);\n" +
                                "  pthread_create(&thread, 0, &zmq_thread, context);\n" +
                                "  pthread_detach(thread);\n" +
                                "  return context;\n" +
                                "}").getBytes())),
                new StreamingResource("function.c",
                        new ByteArrayInputStream(("#include <stdio.h>\n" +
                                "\n" +
                                "int factorial(int n) {\n" +
                                "   //base case\n" +
                                "   if(n == 0) {\n" +
                                "      return 1;\n" +
                                "   } else {\n" +
                                "      return n * factorial(n-1);\n" +
                                "   }\n" +
                                "}").getBytes())));

        zippedSubmittedCodeMap.put("0", new FileResource("main.c", stubZipBytes.length,
                new ByteArrayInputStream(stubZipBytes)));
    }

    @Override
    public Optional<Submission> findById(String submissionId) {
        return submissions.stream().filter(s -> s.getId().equals(submissionId))
                .findFirst();
    }

    @Override
    public List<Submission> findByJudgement(JudgeStatus judgeStatus) {
        if (judgeStatus == null || judgeStatus == JudgeStatus.NONE) {
            return submissions.stream()
                    .filter(s -> !s.isJudged()).collect(Collectors.toList());
        }
        return submissions.stream()
                .filter(s -> s.getSummaryStatus() == judgeStatus)
                .collect(Collectors.toList());
    }

    @Override
    public Submission save(Submission submission) {
        submissions.add(submission);
        submission.setId(String.valueOf(submissions.size()));
        return submission;
    }


    @Override
    public String saveZippedSubmittedCodesAndGetFileId(StreamingResource streamingResource) throws IOException {
        String fileId = String.valueOf(zippedSubmittedCodeMap.size());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(streamingResource.getInputStream(), baos);
        zippedSubmittedCodeMap.put(fileId,
                new FileResource(streamingResource.getFileName(),
                        baos.toByteArray().length,
                        new ByteArrayInputStream(baos.toByteArray())));
        return fileId;
    }

    @Override
    public Optional<FileResource> findZippedSubmittedCodes(String submissionId) {
        return submissions.stream()
                .filter(s -> s.getId().equals(submissionId))
                .map(s -> zippedSubmittedCodeMap.get(s.getZippedSubmittedCodeFilesId()))
                .map(s -> {
                    try {
                        s.getInputStream().reset();  // reset to duplicate the stream
                        return s;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .findFirst();
    }

    @Override
    public List<Submission> find(SubmissionQueryParams params) {
        return submissions.stream()
                .filter(s -> s.getStudentId() == params.getStudentId() && s.getProblemId() == params.getProblemId())
                .collect(Collectors.toList());
    }

    @Override
    public Optional<SubmissionThrottling> findSubmissionThrottling(int problemId, int studentId) {
        return submissionThrottlings.stream()
                .filter(st -> st.getProblemId() == problemId && st.getStudentId() == studentId)
                .findFirst();
    }

    @Override
    public void saveSubmissionThrottling(SubmissionThrottling submissionThrottling) {
        if (submissionThrottling.getId() == null) {
            submissionThrottling.setId(String.valueOf(submissionThrottlings.size() + 1));
        } else {
            submissionThrottlings.removeIf(st -> st.getId().equals(submissionThrottling.getId()));
        }
        submissionThrottlings.add(submissionThrottling);
    }

}
