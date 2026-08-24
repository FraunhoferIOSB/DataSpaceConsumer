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
package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient.jsonld;

import com.apicatalog.jsonld.JsonLd;
import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdErrorCode;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import com.apicatalog.jsonld.loader.DocumentLoader;
import com.apicatalog.jsonld.loader.DocumentLoaderOptions;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.json.Json;
import jakarta.json.JsonArray;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;


public class TitaniumJsonLd {
    private final ObjectMapper mapper = new ObjectMapper();


    public JsonNode expand(String node) {
        try (InputStream is = new ByteArrayInputStream(node.getBytes())) {

            JsonDocument document = JsonDocument.of(is);
            JsonLdOptions options = new JsonLdOptions();
            options.setDocumentLoader(new HttpDocumentLoader());

            JsonArray expandedArray = JsonLd.expand(document).options(options).get();

            StringWriter out = new StringWriter();
            try (var jsonWriter = Json.createWriter(out)) {
                jsonWriter.writeArray(expandedArray);
            }

            return mapper.readTree(out.toString());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private record HttpDocumentLoader(HttpClient httpClient) implements DocumentLoader {
        HttpDocumentLoader() {
            this(HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .followRedirects(HttpClient.Redirect.ALWAYS)
                    .build());
        }


        @Override
        public Document loadDocument(URI url, DocumentLoaderOptions options) throws JsonLdError {
            try {
                HttpRequest request =
                        HttpRequest.newBuilder()
                                .uri(url)
                                .header(
                                        "Accept",
                                        "application/ld+json, application/json;q=0.9, */*;q=0.1")
                                .timeout(Duration.ofSeconds(15))
                                .GET()
                                .build();

                HttpResponse<String> response =
                        httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() >= 400) {
                    throw new JsonLdError(
                            JsonLdErrorCode.LOADING_DOCUMENT_FAILED,
                            "HTTP " + response.statusCode() + " for " + url);
                }

                return JsonDocument.of(new ByteArrayInputStream(response.body().getBytes()));
            }
            catch (JsonLdError e) {
                throw e;
            }
            catch (Exception e) {
                throw new JsonLdError(JsonLdErrorCode.LOADING_DOCUMENT_FAILED, e);
            }
        }
    }
}
