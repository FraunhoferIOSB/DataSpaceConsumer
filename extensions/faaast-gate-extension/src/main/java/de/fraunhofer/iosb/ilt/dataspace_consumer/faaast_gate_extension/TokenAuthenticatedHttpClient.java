/*
 * Copyright (c) 2026 Fraunhofer IOSB, eine rechtlich nicht selbstaendige
 * Einrichtung der Fraunhofer-Gesellschaft zur Foerderung der angewandten
 * Forschung e.V.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.iosb.ilt.dataspace_consumer.faaast_gate_extension;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

/**
 * HttpClient wrapper that automatically adds an Authorization header containing a token to outgoing
 * requests and delegates execution to an internal {@link HttpClient}.
 *
 * <p>This class builds a new {@link java.net.http.HttpRequest} based on the original request,
 * copies existing headers, and injects an "Authorization" header containing the provided token. All
 * actual network operations are delegated to the internal client instance.
 *
 * <p>Usage: Use this client when you need to authenticate requests with a static token value. For
 * dynamic token refresh or more advanced authentication flows, prefer a specialized client.
 */
public class TokenAuthenticatedHttpClient extends HttpClient {
    private final HttpClient delegate;
    private final String token;

    /**
     * Creates a new TokenAuthenticatedHttpClient.
     *
     * @param token the token value to be inserted into the "Authorization" header for each request
     */
    public TokenAuthenticatedHttpClient(String token) {
        this.delegate = HttpClient.newHttpClient();
        this.token = token;
    }

    /**
     * Returns a new {@link HttpRequest} based on the original one with the "Authorization" header
     * set to the configured token. Existing headers from the original request are copied.
     *
     * @param original the original request
     * @return a new {@link HttpRequest} containing the Authorization header
     */
    private HttpRequest withAuth(HttpRequest original) {
        HttpRequest.Builder builder =
                HttpRequest.newBuilder(original.uri())
                        .method(
                                original.method(),
                                original.bodyPublisher()
                                        .orElse(HttpRequest.BodyPublishers.noBody()));

        original.headers()
                .map()
                .forEach(
                        (name, values) -> {
                            for (String value : values) {
                                builder.header(name, value);
                            }
                        });

        builder.header("Authorization", token);

        return builder.build();
    }

    @Override
    public <T> HttpResponse<T> send(
            HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler)
            throws IOException, InterruptedException {
        return delegate.send(withAuth(request), responseBodyHandler);
    }

    /**
     * Sends an asynchronous request. The Authorization header is automatically added before
     * sending.
     *
     * @param request the request to send
     * @param responseBodyHandler handler to process the response body
     * @param <T> response body type
     * @return a {@link CompletableFuture} containing the {@link HttpResponse}
     */
    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(
            HttpRequest request, HttpResponse.BodyHandler<T> responseBodyHandler) {
        return delegate.sendAsync(withAuth(request), responseBodyHandler);
    }

    @Override
    public <T> CompletableFuture<HttpResponse<T>> sendAsync(
            HttpRequest request,
            HttpResponse.BodyHandler<T> responseBodyHandler,
            HttpResponse.PushPromiseHandler<T> pushPromiseHandler) {
        return delegate.sendAsync(withAuth(request), responseBodyHandler, pushPromiseHandler);
    }

    // Delegate all other HttpClient methods
    @Override
    public Optional<CookieHandler> cookieHandler() {
        return delegate.cookieHandler();
    }

    @Override
    public Optional<Duration> connectTimeout() {
        return delegate.connectTimeout();
    }

    @Override
    public Redirect followRedirects() {
        return delegate.followRedirects();
    }

    @Override
    public Optional<ProxySelector> proxy() {
        return delegate.proxy();
    }

    @Override
    public SSLContext sslContext() {
        return delegate.sslContext();
    }

    @Override
    public SSLParameters sslParameters() {
        return delegate.sslParameters();
    }

    @Override
    public Optional<Executor> executor() {
        return delegate.executor();
    }

    @Override
    public Version version() {
        return delegate.version();
    }

    @Override
    public Optional<Authenticator> authenticator() {
        return delegate.authenticator();
    }
}
