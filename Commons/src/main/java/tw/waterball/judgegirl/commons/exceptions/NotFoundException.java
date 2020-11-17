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

package tw.waterball.judgegirl.commons.exceptions;

import tw.waterball.judgegirl.commons.utils.StringUtils;

/**
 * @author - johnny850807@gmail.com (Waterball)
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(Object id, String resourceName) {
        this(id, "id", resourceName);
    }

    public NotFoundException(Object id, String attributeName, String resourceName) {
        super(String.format("The %s given the %s %s is not found.",
                StringUtils.capitalize(resourceName), attributeName, id));
    }

    public static NotFoundExceptionBuilder resource(String resourceName) {
        return new NotFoundExceptionBuilder(resourceName);
    }

    public static class NotFoundExceptionBuilder {
        private String resourceName;

        public NotFoundExceptionBuilder(String resourceName) {
            this.resourceName = resourceName;
        }

        public NotFoundException id(String id) {
            return new NotFoundException(id, resourceName);
        }
    }
}
