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
package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edc;

public final class EdcRequestTemplates {

    private EdcRequestTemplates() {}

    public static String catalogRequest(
            String counterPartyAddress,
            String counterPartyId,
            String operandLeft,
            String operandRight) {

        return """
        {
          "@context": {
            "@vocab": "https://w3id.org/edc/v0.0.1/ns/",
            "odrl": "http://www.w3.org/ns/odrl/2/"
          },
          "@type": "CatalogRequest",
          "counterPartyAddress": "%s",
          "counterPartyId": "%s",
          "protocol": "dataspace-protocol-http:2025-1",
          "querySpec": {
            "@type": "QuerySpec",
            "filterExpression": [
              {
                "operandLeft": "%s",
                "operator": "=",
                "operandRight": "%s"
              }
            ]
          }
        }
        """
                .formatted(counterPartyAddress, counterPartyId, operandLeft, operandRight);
    }

    public static String contractNegotiation(String counterPartyAddress, String policyJson) {
        return """
        {
          "@context": [
            "https://w3id.org/tractusx/auth/v1.0.0",
            "https://w3id.org/catenax/2025/9/policy/context.jsonld",
            "https://w3id.org/catenax/2025/9/policy/odrl.jsonld",
            "https://w3id.org/dspace/2025/1/context.jsonld",
            "https://w3id.org/edc/dspace/v0.0.1",
            { "fx-policy": "https://w3id.org/factoryx/policy/v1.0/" }
          ],
          "@type": "https://w3id.org/edc/v0.0.1/ns/ContractRequest",
          "https://w3id.org/edc/v0.0.1/ns/counterPartyAddress": "%s",
          "https://w3id.org/edc/v0.0.1/ns/protocol": "dataspace-protocol-http:2025-1",
          "https://w3id.org/edc/v0.0.1/ns/policy": %s
        }
        """
                .formatted(counterPartyAddress, policyJson);
    }

    public static String availableEdrQuery(String assetId) {
        return """
        {
          "@context": {
            "@vocab": "https://w3id.org/edc/v0.0.1/ns/"
          },
          "@type": "QuerySpec",
          "sortField": "createdAt",
          "sortOrder": "DESC",
          "filterExpression": [
            {
              "operandLeft": "assetId",
              "operator": "=",
              "operandRight": "%s"
            }
          ]
        }
        """
                .formatted(assetId);
    }
}
