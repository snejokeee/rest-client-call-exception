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

import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.core.ResolvableType;
import org.springframework.core.log.LogFormatUtils;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.function.Function;

/**
 * reference {@link org.springframework.web.client.StatusHandler#defaultHandler(List)}
 */
class RestClientCallExceptionHandler implements RestClient.ResponseSpec.ErrorHandler {

    private final HttpMessageConverters messageConverters;

    RestClientCallExceptionHandler(HttpMessageConverters messageConverters) {
        this.messageConverters = messageConverters;
    }

    private static Function<ResolvableType, ?> initBodyConvertFunction(ClientHttpResponse response, List<HttpMessageConverter<?>> messageConverters) {
        Assert.state(!CollectionUtils.isEmpty(messageConverters), "Expected message converters");
        return resolvableType -> {
            try {
                var extractor = new HttpMessageConverterExtractor<>(
                    resolvableType.getType(), messageConverters
                );
                return extractor.extractData(new CachedBodyResponse(response));
            } catch (IOException ex) {
                throw new RestClientException("Error while extracting response for type [" + resolvableType + "]", ex);
            }
        };
    }

    private static String getErrorMessage(
        int rawStatusCode,
        String statusText,
        @Nullable byte[] responseBody,
        @NonNull Charset charset
    ) {
        String preface = rawStatusCode + " " + statusText + ": ";
        if (ObjectUtils.isEmpty(responseBody)) {
            return preface + "[no body]";
        }
        String bodyText = new String(responseBody, charset);
        bodyText = LogFormatUtils.formatValue(
            bodyText, -1, true
        );
        return preface + bodyText;
    }

    /**
     * @throws RestClientCallException wrapper around {@link RestClientResponseException}
     */
    @Override
    public void handle(HttpRequest request, ClientHttpResponse response) throws RestClientException, IOException {
        var cachedBodyResponse = new CachedBodyResponse(response);
        var statusCode = response.getStatusCode();
        var statusText = response.getStatusText();
        var headers = response.getHeaders();
        var body = ExceptionResponseUtils.getBody(cachedBodyResponse);
        var charset = ExceptionResponseUtils.getCharset(headers);
        var message = getErrorMessage(statusCode.value(), statusText, body, charset);

        RestClientResponseException ex;

        if (statusCode.is4xxClientError()) {
            ex = HttpClientErrorException.create(message, statusCode, statusText, headers, body, charset);
        } else if (statusCode.is5xxServerError()) {
            ex = HttpServerErrorException.create(message, statusCode, statusText, headers, body, charset);
        } else {
            ex = new UnknownHttpStatusCodeException(message, statusCode.value(), statusText, headers, body, charset);
        }
        var wrapper = new RestClientCallException(request, ex);
        if (!CollectionUtils.isEmpty(messageConverters.getConverters())) {
            var bodyConvertFunction = initBodyConvertFunction(response, messageConverters.getConverters());
            wrapper.setBodyConvertFunction(bodyConvertFunction);
        }
        throw wrapper;
    }
}
