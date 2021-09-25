package tw.waterball.judgegirl.primitives.problem;

import lombok.Builder;
import lombok.Getter;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.commons.models.files.StreamingResource;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToSet;
import static tw.waterball.judgegirl.commons.utils.validations.ValidationUtils.validate;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
@Getter
@Builder
public class TestcaseIO {
    public static final String DEFAULT_STD_IN = "std.in";
    public static final String DEFAULT_STD_OUT = "std.out";
    String id;
    @NotNull
    String testcaseId;
    String stdIn;
    String stdOut;
    @Builder.Default
    Set<@Size(min = 1, max = 300) String> inputFiles = new HashSet<>();
    @Builder.Default
    Set<@Size(min = 1, max = 300) String> outputFiles = new HashSet<>();

    public TestcaseIO(String testcaseId, String stdIn, String stdOut, Set<String> inputFiles, Set<String> outputFiles) {
        this(null, testcaseId, stdIn, stdOut, inputFiles, outputFiles);
    }

    public TestcaseIO(String id, String testcaseId, String stdIn, String stdOut, Set<String> inputFiles, Set<String> outputFiles) {
        this.id = id;
        this.testcaseId = testcaseId;
        this.stdIn = stdIn;
        this.stdOut = stdOut;
        this.inputFiles = inputFiles;
        this.outputFiles = outputFiles;
        validate(this);
    }

    public Optional<String> mayHaveStdIn() {
        return ofNullable(stdIn);
    }

    public Optional<String> mayHaveStdOut() {
        return ofNullable(stdOut);
    }

    public Optional<String> mayHaveId() {
        return ofNullable(id);
    }

    public void setId(String id) {
        this.id = id;
    }

    public static class Files extends TestcaseIO {
        public String testcaseId;
        public FileResource stdIn;
        public FileResource stdOut;
        public Set<FileResource> inputFiles;
        public Set<FileResource> outputFiles;

        public Files(String testcaseId, FileResource stdIn, FileResource stdOut,
                     Set<FileResource> inputFiles, Set<FileResource> outputFiles) {
            super(testcaseId, stdIn.getFileName(), stdOut.getFileName(),
                    mapToSet(inputFiles, StreamingResource::getFileName),
                    mapToSet(outputFiles, StreamingResource::getFileName));
            this.testcaseId = testcaseId;
            this.stdIn = stdIn;
            this.stdOut = stdOut;
            this.inputFiles = inputFiles;
            this.outputFiles = outputFiles;
        }

        public Set<FileResource> all() {
            Set<FileResource> all = new HashSet<>();
            all.add(stdIn);
            all.add(stdOut);
            all.addAll(inputFiles);
            all.addAll(outputFiles);
            return all;
        }

    }
}