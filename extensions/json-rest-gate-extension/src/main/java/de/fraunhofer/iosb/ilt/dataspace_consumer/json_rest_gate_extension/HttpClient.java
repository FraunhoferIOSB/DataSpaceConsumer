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
package de.fraunhofer.iosb.ilt.dataspace_consumer.json_rest_gate_extension;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HttpClient extends OkHttpClient {

    private static final String AUTHORIZATION_KWD = "Authorization";

    byte[] executeRequest(String url, String token) throws IOException {
        Request request = buildRequest(url, token);
        Call httpCall = newCall(request);
        try (Response response = httpCall.execute()) {
            return response.body().bytes();
        }
    }

    private Request buildRequest(String url, String token) {
        return new Request.Builder().header(AUTHORIZATION_KWD, token).url(url).build();
    }
}
