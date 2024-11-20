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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    RestClient restClient(RestClient.Builder builder) {
        return builder
            .baseUrl("http://httpstat.us")
            .build();
    }

    @RestControllerAdvice
    static class RestClientExceptionHandler {

        @ExceptionHandler(RestClientCallException.class)
        String handleRestClientCallException(RestClientCallException exception) {
            return "Caught RestClientCallException: " + exception.getClass().getCanonicalName() + ": " + exception.getMessage();
        }

        @ExceptionHandler(RestClientException.class)
        String catchRestClientExceptions(RestClientException exception) {
            return "Caught " + exception.getClass().getCanonicalName() + ": " + exception.getMessage();
        }
    }

    @RestController
    static class WebController {
        private final RestClient restClient;

        WebController(RestClient restClient) {
            this.restClient = restClient;
        }

        @GetMapping("/server")
        public String server() {
            return restClient.get().uri("502")
                .retrieve()
                .body(String.class);
        }

        @GetMapping("/client")
        public String client() {
            return restClient.get().uri("400")
                .retrieve()
                .body(String.class);
        }
    }

}
