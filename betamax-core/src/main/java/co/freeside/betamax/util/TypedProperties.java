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

package co.freeside.betamax.util;

import java.util.*;

public class TypedProperties extends Properties {

    public static boolean getBoolean(Properties properties, String key, boolean defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Boolean.valueOf(value) : defaultValue;
    }

    public static boolean getBoolean(Properties properties, String key) {
        return getBoolean(properties, key, false);
    }

    public static int getInteger(Properties properties, String key, int defaultValue) {
        String value = properties.getProperty(key);
        return value != null ? Integer.parseInt(value) : defaultValue;
    }

    public static int getInteger(Properties properties, String key) {
        return getInteger(properties, key, 0);
    }

    public static <T extends Enum<T>> T getEnum(Properties properties, String key, T defaultValue) {
        String value = properties.getProperty(key);
        T anEnum = Enum.valueOf((Class<T>) defaultValue.getClass(), value);
        return value != null ? anEnum : defaultValue;
    }

}
