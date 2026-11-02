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
package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edcclient.template;

public final class EdcEndpointTemplates {

    private EdcEndpointTemplates() {}

    private static final String MGMT_API_VERSION = "v3";

    public static String initiateTransferProcess(String baseURL) {
        return String.format("%s/%s/transferprocesses", MGMT_API_VERSION, baseURL);
    }

    public static String getTransferProcessStatus(String baseURL, String id) {
        return String.format("%s/%s/transferprocesses/%s", baseURL, MGMT_API_VERSION, id);
    }

    public static String requestCatalog(String baseURL) {
        return String.format("%s/%s/catalog/request", MGMT_API_VERSION, baseURL);
    }

    public static String initiateContractNegotiation(String baseURL) {
        return String.format("%s/%s/contractnegotiations", baseURL, MGMT_API_VERSION);
    }

    public static String requestAccessToken(String baseURL, String transferProcessId) {
        return String.format(
                "%s/%s/edrs/%s/dataaddress?auto_refresh=true",
                baseURL, MGMT_API_VERSION, transferProcessId);
    }

    public static String getNegotiationStatus(String baseURL, String negotiationId) {
        return String.format(
                "%s/%s/contractnegotiations/%s", baseURL, MGMT_API_VERSION, negotiationId);
    }
}
