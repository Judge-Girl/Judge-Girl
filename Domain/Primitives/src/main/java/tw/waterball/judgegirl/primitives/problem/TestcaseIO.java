package tw.waterball.judgegirl.primitives.problem;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import tw.waterball.judgegirl.commons.models.files.FileResource;
import tw.waterball.judgegirl.commons.models.files.StreamingResource;
import tw.waterball.judgegirl.commons.utils.validations.ValidationUtils;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;
import static tw.waterball.judgegirl.commons.utils.ArrayUtils.contains;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToList;
import static tw.waterball.judgegirl.commons.utils.StreamUtils.mapToSet;

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
        validate();
    }

    public void validate() {
        ValidationUtils.validate(this);
        if (inputFiles.stream().anyMatch(f -> f.equals(stdIn))) {
            throw new IllegalStateException("The stdIn's file name must not duplicate to any of the input file's name.");
        }
        if (outputFiles.stream().anyMatch(f -> f.equals(stdOut))) {
            throw new IllegalStateException("The stdOut's file name must not duplicate to any of the output file's name.");
        }
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

    public void validate(IoPatching patching) {
        assert apply(patching) != null;
    }

    public TestcaseIO apply(IoPatching patching) {
        // replace with the patched standard file's name if exists, otherwise keep the original name
        Optional<String> stdInName = patching.getStdIn()
                .map(StreamingResource::getFileName).or(this::mayHaveStdIn);
        Optional<String> stdOutName = patching.getStdOut()
                .map(StreamingResource::getFileName).or(this::mayHaveStdOut);

        // filter off the deleted IO Files
        Set<String> inputFileNames = getInputFiles().stream()
                .filter(inputFile -> !contains(patching.getDeletedIns(), inputFile)).collect(toSet());
        inputFileNames.addAll(mapToList(patching.getInputFiles(), StreamingResource::getFileName));
        Set<String> outputFileNames = getOutputFiles().stream()
                .filter(outputFile -> !contains(patching.getDeletedOuts(), outputFile)).collect(toSet());
        outputFileNames.addAll(mapToList(patching.getOutputFiles(), StreamingResource::getFileName));

        return new TestcaseIO(getId(), getTestcaseId(),
                stdInName.orElse(null), stdOutName.orElse(null),
                inputFileNames, outputFileNames);
    }

    @Value
    public static class IoPatching {
        String testcaseId;
        String[] deletedIns;
        String[] deletedOuts;
        FileResource stdIn;
        FileResource stdOut;
        Set<FileResource> inputFiles;
        Set<FileResource> outputFiles;

        public IoPatching(String testcaseId, String[] deletedIns, String[] deletedOuts,
                          FileResource stdIn, FileResource stdOut,
                          Set<FileResource> inputFiles, Set<FileResource> outputFiles) {
            this.testcaseId = testcaseId;
            this.deletedIns = deletedIns;
            this.deletedOuts = deletedOuts;
            this.stdIn = stdIn;
            this.stdOut = stdOut;
            this.inputFiles = inputFiles;
            this.outputFiles = outputFiles;
        }

        public Optional<FileResource> getStdIn() {
            return ofNullable(stdIn);
        }

        public Optional<FileResource> getStdOut() {
            return ofNullable(stdOut);
        }

        public TestcaseIO toTestcaseIo() {
            return new TestcaseIO(null, testcaseId,
                    getStdIn().map(StreamingResource::getFileName).orElse(null),
                    getStdOut().map(StreamingResource::getFileName).orElse(null),
                    mapToSet(getInputFiles(), StreamingResource::getFileName),
                    mapToSet(getOutputFiles(), StreamingResource::getFileName));
        }
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