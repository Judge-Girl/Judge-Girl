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

package tw.waterball.judgegirl.judger.filelayout;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import tw.waterball.judgegirl.commons.utils.ResourceUtils;
import tw.waterball.judgegirl.entities.problem.Testcase;
import tw.waterball.judgegirl.entities.submission.Submission;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class YAMLFileLayoutBuilder implements FileLayoutBuilder {
    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private static final String FIELD_PATH = "path";
    private String yamlResourcePath;


    public YAMLFileLayoutBuilder(String yamlResourcePath) throws IOException {
        this.yamlResourcePath = yamlResourcePath;
    }

    @Override
    public FileLayout build() {
        JsonNode tree;
        try {
            tree = mapper.readTree(ResourceUtils.getResourceAsStream(yamlResourcePath));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        JsonNode rootTree = tree.get("root");
        Path rootPath = Paths.get(rootTree.get(FIELD_PATH).asText());
        if (!rootPath.isAbsolute()) {
            throw new IllegalStateException("The root.path must be absolute.");
        }
        Directory rootDir = new Directory("root", rootPath.getFileName().toString());
        parseTree(rootDir, rootTree);
        return new FileLayout(rootDir);
    }

    private void parseTree(Directory directory, JsonNode tree) {
        Iterator<Map.Entry<String, JsonNode>> d = tree.fields();
        while (d.hasNext()) {
            Map.Entry<String, JsonNode> entry = d.next();
            if (!entry.getKey().equals(FIELD_PATH)) {
                JsonNode val = entry.getValue();
                if (val.isTextual()) {
                    directory.add(new OrdinaryFile(entry.getKey(), entry.getValue().asText()));
                } else {
                    Directory childDir = new Directory(entry.getKey(), val.get(FIELD_PATH).asText());
                    directory.add(childDir);
                    parseTree(childDir, val);
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        FileLayout layout = new YAMLFileLayoutBuilder("/judger-layout.yaml").build();
        Directory directory = layout.getRoot();
        InterpolationItemVisitor visitor =
                new InterpolationItemVisitor(
                        new Testcase("aaa", 1, 1, 1, 1, 1, 1),
                        new Submission("1", 1, 1, ""));
        directory.acceptVisitor(visitor);

        TreePrintVisitor printer = new TreePrintVisitor();
        directory.acceptVisitor(printer);
        System.out.println(printer.getOutput());
    }


}
