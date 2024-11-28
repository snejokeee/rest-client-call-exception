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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.webservices.client.AutoConfigureMockWebServiceServer;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Import({
    HttpMessageConvertersAutoConfiguration.class,
    RestClientAutoConfiguration.class,
    RestClientCallExceptionAutoConfiguration.class,
    DefaultAdviceTestConfiguration.class
})
@ContextConfiguration(classes = RestClientCallExceptionAutoConfiguration.class)
@WebMvcTest(controllers = DefaultAdviceTestConfiguration.class)
@AutoConfigureMockWebServiceServer
class DefaultAdviceTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestClient.Builder restClientBuilder;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private DefaultAdviceTestConfiguration.ReferenceRestController referenceRestController;

    @Test
    void adviceTest() throws Exception {
        MockRestServiceServer.bindTo(restClientBuilder)
            .build()
            .expect(MockRestRequestMatchers.requestTo("/exception/path"))
            .andRespond(
                MockRestResponseCreators
                    .withStatus(HttpStatus.BAD_GATEWAY)
                    .body("{\"error\":\"test\"}")
            );

        ReflectionTestUtils.setField(referenceRestController, "restClientBuilder", restClientBuilder);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/test/path")
            )
            .andExpect(status().isBadGateway())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(
                jsonPath("$.status").value(HttpStatus.BAD_GATEWAY.value())
            )
            .andExpect(
                jsonPath("$.type")
                    .value("org.springframework.web.client.HttpServerErrorException.BadGateway")
            )
            .andExpect(
                jsonPath("$.instance").value("/test/path")
            )
            .andExpect(
                jsonPath("$.detail")
                    .value("BadGateway occurs while requesting GET /exception/path")
            );
    }
}
