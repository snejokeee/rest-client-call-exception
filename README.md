# RestClientCallException

## What is the problem ?

In some cases, you may want an instance of `HttpRequest` sent along with your `RestClient`.
But this is not possible because the default status handler `org.springframework.web.client.StatusHandler#defaultHandler(List)` ignores the request parameter when creating `RestClientException` and its successors.

More information at:
* Question on StackOverflow: [How to get HttpRequest in @ExceptionHandler with RestClientException?](https://stackoverflow.com/questions/79135141).
* Issue in spring project: [HttpRequest awareness for RestClientException](https://github.com/spring-projects/spring-framework/issues/33814)

## Solution

This dependency introduces a new `RestClientException` descendant class for the exception - `RestClientCallException` and injects a pre-configured `RestClient.ResponseSpec.ErrorHandler` into `RestClient.Builder` via `RestClientCustomizer`.

## Quick Start

Actual versions you can find on [mavenCentral](https://mvnrepository.com/artifact/dev.alubenets/rest-client-call-exception)

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

## How does it work ?