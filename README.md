# RestClientCallException
![Maven Central Version](https://img.shields.io/maven-central/v/dev.alubenets/rest-client-call-exception)

A simple wrapper around RestClientResponseException that can contain an instance of HttpRequest inside it.

## What is the problem ?

In some cases, you may want an instance of `HttpRequest` sent by `RestClient`.
But this is not possible because the default status handler `org.springframework.web.client.StatusHandler#defaultHandler(List)` ignores the request parameter when creating `RestClientException` and its successors.

More information at:
* Question on StackOverflow: [How to get HttpRequest in @ExceptionHandler with RestClientException?](https://stackoverflow.com/questions/79135141).
* Issue in spring project: [HttpRequest awareness for RestClientException](https://github.com/spring-projects/spring-framework/issues/33814)

## Solution

This dependency introduces a new `RestClientCallException` (successor of `RestClientResponseException`) class and injects a pre-configured `RestClient.ResponseSpec.ErrorHandler` into `RestClient.Builder` via `RestClientCustomizer`.

## Quick Start

Actual version you can find on [mavenCentral](https://central.sonatype.com/artifact/dev.alubenets/rest-client-call-exception/overview)

### Gradle

```groovy
implementation 'dev.alubenets:rest-client-call-exception:<version>'
```
### Maven

```xml
<dependency>
    <groupId>dev.alubenets</groupId>
    <artifactId>rest-client-call-exception</artifactId>
    <version>${version}</version>
</dependency>
```

## How to use

Just for example, a default `@RestControllerAdvice` is introduced with one method `@ExceptionHandler`, which intercepts the `RestClientCallException` and formats the response compatible with [RFC-9457](https://datatracker.ietf.org/doc/html/rfc9457 ). You can disable this advice by setting the `dev.alubenets.exceptions.enable-default-advice` property to `false`. You can find implementation in `dev.alubenets.exceptions.RestClientCallExceptionAdvice` class.

## How does it work ?

`<TBD>`