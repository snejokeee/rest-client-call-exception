/*
 * The MIT License (MIT)
 *
 * Copyright © 2024. Aleksey Lubenets <alubenets.dev>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package dev.alubenets;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * reference {@link org.springframework.web.client.RestClientUtils}
 */
abstract class ExceptionResponseUtils {

    private ExceptionResponseUtils() {
    }

    /**
     * @return {@code byte[]} copy of full message body
     */
    public static byte[] getBody(HttpInputMessage message) {
        try {
            return FileCopyUtils.copyToByteArray(message.getBody());
        } catch (IOException ignored) {
            // silence any exceptions to return empty byte array as fallback
        }
        return new byte[0];
    }

    /**
     * @return charset from {@code Content-Type} header or {@code UTF_8} as fallback
     */
    @NonNull
    public static Charset getCharset(@Nullable HttpHeaders headers) {
        return Optional.ofNullable(headers)
            .map(HttpHeaders::getContentType)
            .map(MediaType::getCharset)
            .orElse(StandardCharsets.UTF_8);
    }
}
