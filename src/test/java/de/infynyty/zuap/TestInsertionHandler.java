package de.infynyty.zuap;


import de.infynyty.zuap.insertion.Insertion;
import de.infynyty.zuap.insertionHandler.*;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TestInsertionHandler {

    public static InsertionAnnouncer announcer = spy(InsertionAnnouncer.class);
    public static MockHTTPClient httpClient = spy(MockHTTPClient.class);
    private static final String testingName = "TestingName";

    @Spy
    public static HttpResponse<String> response;


    @ParameterizedTest
    @MethodSource("provideHandlers")
    public void testHandlerName(InsertionHandler<?> insertionHandler) {
        Assertions.assertEquals(testingName, insertionHandler.getHandlerName());
    }

    @ParameterizedTest
    @MethodSource("provideHandlers")
    public void testDeniedRequest(InsertionHandler<?> insertionHandler) throws IOException, InterruptedException {
        when(response.statusCode()).thenReturn(400);
        when(httpClient.send(ArgumentMatchers.any(), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(response);

        try {
            insertionHandler.updateCurrentInsertions();
        } catch (Exception ex) {
            Assertions.fail("Handler did not handle HTTP error response correctly: " + ex.getMessage());
        }
        verify(announcer, times(0)).announce(any());
    }

    @ParameterizedTest
    @MethodSource("provideHandlers")
    public void testIncorrectData(InsertionHandler<?> insertionHandler) throws IOException, InterruptedException {
        when(response.statusCode()).thenReturn(200);
        when(httpClient.send(ArgumentMatchers.any(), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any())).thenReturn(response);

        when(response.body()).thenReturn(null);
        try {
            insertionHandler.updateCurrentInsertions();
        } catch (Exception ex) {
            Assertions.fail("Handler did not handle null HTTP response correctly: " + ex.getMessage());
        }
        verify(announcer, times(0)).announce(any());

        when(response.body()).thenReturn("");
        try {
            insertionHandler.updateCurrentInsertions();
        } catch (Exception ex) {
            Assertions.fail("Handler did not handle empty HTTP response correctly: " + ex.getMessage());
        }
        verify(announcer, times(0)).announce(any());

        when(response.body()).thenReturn("{}");
        try {
            insertionHandler.updateCurrentInsertions();
        } catch (Exception ex) {
            Assertions.fail("Handler did not handle empty JSON response correctly: " + ex.getMessage());
        }
        verify(announcer, times(0)).announce(any());
    }

    @ParameterizedTest
    @MethodSource("provideHandlersWithRealHTTPClient")
    public <T extends Insertion> void  testInitialInsertionsAreNotAnnounced(InsertionHandler<T> insertionHandler) {
        insertionHandler.updateCurrentInsertions();
        verify(announcer, never()).announce(any());
    }

    public static Stream<Arguments> provideHandlers() {
        return Stream.of(
                arguments(new MeinWGZimmerHandler(testingName, announcer, httpClient)),
                arguments(new WGZimmerHandler(testingName, announcer, httpClient)),
                arguments(new WOKOInsertionHandler(testingName, announcer, httpClient))
        );
    }

    public static Stream<Arguments> provideHandlersWithRealHTTPClient() {
        final HttpClient client = HttpClient.newHttpClient();
        return Stream.of(
                arguments(new MeinWGZimmerHandler(testingName, announcer, client)),
                arguments(new WGZimmerHandler(testingName, announcer, client)),
                arguments(new WOKOInsertionHandler(testingName, announcer, client))
        );
    }

    private static class MockHTTPClient extends HttpClient {

        @Override
        public Optional<CookieHandler> cookieHandler() {
            return Optional.empty();
        }

        @Override
        public Optional<Duration> connectTimeout() {
            return Optional.empty();
        }

        @Override
        public Redirect followRedirects() {
            return null;
        }

        @Override
        public Optional<ProxySelector> proxy() {
            return Optional.empty();
        }

        @Override
        public SSLContext sslContext() {
            return null;
        }

        @Override
        public SSLParameters sslParameters() {
            return null;
        }

        @Override
        public Optional<Authenticator> authenticator() {
            return Optional.empty();
        }

        @Override
        public Version version() {
            return null;
        }

        @Override
        public Optional<Executor> executor() {
            return Optional.empty();
        }

        @Override
        public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) throws IOException, InterruptedException {
            return new HttpResponse<T>() {
                @Override
                public int statusCode() {
                    return 400;
                }

                @Override
                public HttpRequest request() {
                    return null;
                }

                @Override
                public Optional<HttpResponse<T>> previousResponse() {
                    return Optional.empty();
                }

                @Override
                public HttpHeaders headers() {
                    return null;
                }

                @Override
                public T body() {
                    return null;
                }

                @Override
                public Optional<SSLSession> sslSession() {
                    return Optional.empty();
                }

                @Override
                public URI uri() {
                    return null;
                }

                @Override
                public Version version() {
                    return null;
                }
            };
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
            return null;
        }

        @Override
        public <T> CompletableFuture<HttpResponse<T>> sendAsync(HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler, HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
            return null;
        }
    }
}
