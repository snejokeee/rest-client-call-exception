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

package dev.alubenets.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.Nullable;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Optional;

@RestControllerAdvice
public class RestClientCallExceptionAdvice {

    private static final Logger log = LoggerFactory.getLogger(RestClientCallExceptionAdvice.class);

    @ExceptionHandler(RestClientCallException.class)
    public ErrorResponse handleRestClientCallException(RestClientCallException exception, @Nullable HttpServletRequest request) {
        var causeExceptionCanonicalName = exception.getCause().getClass().getCanonicalName();
        log.debug("Caught RestClientCallException with {} cause exception", causeExceptionCanonicalName);

        var executedRequest = exception.getRequest();

        String detail = exception.getCause().getClass().getSimpleName() +
            " occurs while requesting " +
            executedRequest.getMethod() +
            " " +
            executedRequest.getURI();

        var problemDetail = ProblemDetail.forStatusAndDetail(exception.getStatusCode(), detail);

        problemDetail.setType(URI.create(causeExceptionCanonicalName));

        Optional.ofNullable(request)
            .map(HttpServletRequest::getRequestURI)
            .map(URI::create)
            .ifPresent(problemDetail::setInstance);

        return ErrorResponse.builder(exception, problemDetail).build();
    }
}
