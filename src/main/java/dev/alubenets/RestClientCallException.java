/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024. Aleksey Lubenets <alubenets.dev>
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

import org.springframework.core.ResolvableType;
import org.springframework.http.HttpRequest;
import org.springframework.web.client.RestClientResponseException;

import java.util.function.Function;

/**
 * @see <a href="https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/client/package-tree.html">Hierarchy For Package org.springframework.web.client</a>
 */
public class RestClientCallException extends RestClientResponseException {

    private final transient HttpRequest request;

    RestClientCallException(HttpRequest request, RestClientResponseException cause) {
        super(
            cause.getMessage(),
            cause.getStatusCode(),
            cause.getStatusText(),
            cause.getResponseHeaders(),
            cause.getResponseBodyAsByteArray(),
            ExceptionResponseUtils.getCharset(cause.getResponseHeaders())
        );
        this.request = request;
        this.initCause(cause);
    }

    public HttpRequest getRequest() {
        return request;
    }

    @Override
    public void setBodyConvertFunction(Function<ResolvableType, ?> bodyConvertFunction) {
        super.setBodyConvertFunction(bodyConvertFunction);
        ((RestClientResponseException) getCause()).setBodyConvertFunction(bodyConvertFunction);
    }
}

