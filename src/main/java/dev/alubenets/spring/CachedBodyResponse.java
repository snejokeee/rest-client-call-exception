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

package dev.alubenets.spring;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * reference {@link org.springframework.web.client.ClientHttpResponseDecorator}, {@link org.springframework.http.client.BufferingClientHttpResponseWrapper}
 */
final class CachedBodyResponse implements ClientHttpResponse {

    private final ClientHttpResponse delegate;

    @Nullable
    private byte[] body;

    public CachedBodyResponse(ClientHttpResponse delegate) {
        this.delegate = delegate;
    }

    @Override
    public HttpStatusCode getStatusCode() throws IOException {
        return this.delegate.getStatusCode();
    }

    @Override
    public String getStatusText() throws IOException {
        return this.delegate.getStatusText();
    }

    @Override
    public void close() {
        this.delegate.close();
    }

    @Override
    public InputStream getBody() throws IOException {
        if (this.body == null) {
            this.body = StreamUtils.copyToByteArray(this.delegate.getBody());
        }
        return new ByteArrayInputStream(this.body);
    }

    @Override
    public HttpHeaders getHeaders() {
        return this.delegate.getHeaders();
    }
}
