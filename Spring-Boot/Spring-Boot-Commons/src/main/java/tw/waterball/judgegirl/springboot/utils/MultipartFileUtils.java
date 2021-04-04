package tw.waterball.judgegirl.springboot.utils;

import org.springframework.web.multipart.MultipartFile;
import tw.waterball.judgegirl.commons.models.files.FileResource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class MultipartFileUtils {

    public static List<FileResource> convertMultipartFilesToFileResources(MultipartFile[] multipartFiles) {
        return Arrays.stream(multipartFiles)
                .map(MultipartFileUtils::convertMultipartFileToFileResource)
                .collect(Collectors.toList());
    }

    public static FileResource convertMultipartFileToFileResource(MultipartFile multipartFile) {
        try {
            return new FileResource(multipartFile.getOriginalFilename(),
                    multipartFile.getSize(),
                    multipartFile.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException("File uploading error", e);
        }
    }
}
