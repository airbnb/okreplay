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

package co.freeside.betamax.proxy.netty;

import java.io.*;
import com.google.common.io.InputSupplier;
import io.netty.buffer.*;

public class ByteBufInputSupplier implements InputSupplier<InputStream> {

    private final ByteBuf buffer;

    public ByteBufInputSupplier(ByteBuf buffer) {
        this.buffer = buffer;
    }

    public ByteBufInputSupplier(ByteBufHolder buffer) {
        this.buffer = buffer.content();
    }

    @Override
    public InputStream getInput() throws IOException {
        return new ByteBufInputStream(buffer);
    }
}
