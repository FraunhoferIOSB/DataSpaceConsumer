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
package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient;

public final class EdcEndpointTemplates {

    private EdcEndpointTemplates() {}

    public static String availableEDRsEndpoint(String baseURL) {
        return String.format("%s/v3/edrs/request", baseURL);
    }

    public static String catalogEndpoint(String baseURL) {
        return String.format("%s/v3/catalog/request", baseURL);
    }

    public static String contractNegotiationEndpoint(String baseURL) {
        return String.format("%s/v3/edrs", baseURL);
    }

    public static String tokenEndpoint(String baseURL, String transferProcessId) {
        return String.format(
                "%s/v3/edrs/%s/dataaddress?auto_refresh=true", baseURL, transferProcessId);
    }

    public static String negotiationStateEndpoint(String baseURL, String negotiationId) {
        return String.format("%s/v3/contractnegotiations/%s/state", baseURL, negotiationId);
    }
}
