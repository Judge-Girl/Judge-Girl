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

package tw.waterball.judgegirl.springboot.utils;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import tw.waterball.judgegirl.commons.models.files.FileResource;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class MongoUtils {

    public static FileResource downloadFileResourceByFileId(GridFsTemplate gridFsTemplate, String fileId) {
        GridFSFile gridFsFile = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(fileId)));
        GridFsResource resource = gridFsTemplate.getResource(requireNonNull(gridFsFile));
        try {
            return new FileResource(resource.getFilename(), resource.contentLength(), resource.getInputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Builder query(MongoTemplate mongoTemplate) {
        return new Builder(mongoTemplate);
    }


    public static class Builder {
        private MongoTemplate mongoTemplate;

        public Builder(MongoTemplate mongoTemplate) {
            this.mongoTemplate = mongoTemplate;
        }

        public <T> FromDocument<T> fromDocument(Class<T> documentType) {
            return new FromDocument<>(documentType);
        }

        public class FromDocument<T> {
            private Class<T> documentType;

            public FromDocument(Class<T> documentType) {
                this.documentType = documentType;
            }

            public SelectOneFieldExecution selectOneField(String fieldName) {
                return new SelectOneFieldExecution(mongoTemplate, fieldName);
            }

            public class SelectOneFieldExecution {
                private MongoTemplate mongoTemplate;
                private String fieldName;
                private String pkField;
                private Object pkValue;

                public SelectOneFieldExecution(MongoTemplate mongoTemplate, String fieldName) {
                    this.mongoTemplate = mongoTemplate;
                    this.fieldName = fieldName;
                }

                public SelectOneFieldExecution byId(Object id) {
                    pkField = "id";
                    pkValue = id;
                    return this;
                }

                public OfValue by(String pkField) {
                    this.pkField = pkField;
                    return this.new OfValue();
                }

                public Result execute() {
                    Query query = new Query(Criteria.where(pkField).is(pkValue));
                    query.fields().include(fieldName);
                    T document = mongoTemplate.findOne(query, documentType);
                    return new Result(document);
                }

                public class OfValue {
                    public SelectOneFieldExecution ofValue(String value) {
                        pkValue = value;
                        return SelectOneFieldExecution.this;
                    }
                }

                public class Result {
                    private T document;

                    public Result(T document) {
                        this.document = document;
                    }

                    public Optional<String> getField(Function<T, String> fieldGetter) {
                        return document == null ? Optional.empty() : Optional.of(fieldGetter.apply(document));
                    }
                }
            }
        }

    }


}
