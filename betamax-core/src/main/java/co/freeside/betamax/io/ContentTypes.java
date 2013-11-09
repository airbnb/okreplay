/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package co.freeside.betamax.io;

import java.util.regex.Pattern;
import com.google.common.net.MediaType;
import static com.google.common.net.MediaType.*;

public class ContentTypes {

    private static final Pattern TEXT_CONTENT_TYPE_PATTERN = Pattern.compile("(json|javascript|(\\w+\\+)?xml)");

    public static boolean isTextContentType(String contentType) {
        if (contentType == null) {
            return false;
        }
        MediaType mediaType = MediaType.parse(contentType);
        if (mediaType.is(ANY_TEXT_TYPE) || mediaType.is(FORM_DATA)) {
            return true;
        }
        if (mediaType.is(ANY_APPLICATION_TYPE) && TEXT_CONTENT_TYPE_PATTERN.matcher(mediaType.subtype()).matches()) {
            return true;
        }
        return false;
    }

    private ContentTypes() {
    }
}
