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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;

@ExtendWith(SpringExtension.class)
@Import({
    HttpMessageConvertersAutoConfiguration.class,
    RestClientAutoConfiguration.class,
    ErrorHandlerAutoConfiguration.class
})
@ContextConfiguration(classes = ErrorHandlerAutoConfiguration.class)
@RestClientTest
class IntegrationTests {

    @Autowired
    private RestClient.Builder restClientBuilder;

    @Autowired
    private RestClientCallExceptionRestClientCustomizer customizer;

    @Test
    void isCustomizerUp() {
        Assertions.assertNotNull(customizer);
    }

    @Test
    void isBuilderCustomized() {
        Assertions.assertNotNull(restClientBuilder);
        var statusHandlers = ReflectionTestUtils.getField(restClientBuilder, "statusHandlers");
        Assertions.assertNotNull(statusHandlers);
        Assertions.assertInstanceOf(List.class, statusHandlers);
        Assertions.assertFalse(((List<?>) statusHandlers).isEmpty());
    }

    @Test
    void catchServerException() {
        var mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        mockServer
            .expect(MockRestRequestMatchers.requestTo("server"))
            .andRespond(
                MockRestResponseCreators
                    .withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"test\"}")
            );

        var underTest = restClientBuilder.build();
        @SuppressWarnings({"java:S5778"})
        var thrownException = Assertions.assertThrows(
            RestClientCallException.class,
            () -> underTest.get().uri("server").retrieve().toBodilessEntity()
        );
        Assertions.assertInstanceOf(HttpServerErrorException.InternalServerError.class, thrownException.getCause());
    }

    @Test
    void catchClientException() {
        var mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        mockServer
            .expect(MockRestRequestMatchers.requestTo("client"))
            .andRespond(
                MockRestResponseCreators
                    .withStatus(HttpStatus.BAD_REQUEST)
                    .body("{\"error\":\"test\"}")
            );

        var underTest = restClientBuilder.build();
        @SuppressWarnings({"java:S5778"})
        var thrownException = Assertions.assertThrows(
            RestClientCallException.class,
            () -> underTest.get().uri("client").retrieve().toBodilessEntity()
        );
        Assertions.assertInstanceOf(HttpClientErrorException.BadRequest.class, thrownException.getCause());
    }
}
