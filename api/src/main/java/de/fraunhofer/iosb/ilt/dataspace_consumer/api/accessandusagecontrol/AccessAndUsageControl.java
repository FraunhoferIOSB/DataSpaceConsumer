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
package de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol;

import java.util.List;

import de.fraunhofer.iosb.ilt.dataspace_consumer.api.exception.DSCExecuteException;
import org.pf4j.ExtensionPoint;

/**
 * Extension point for access and usage control implementations.
 *
 * <p>Provides methods to negotiate and retrieve access tokens or credentials for requested data.
 */
public interface AccessAndUsageControl extends ExtensionPoint {
    /**
     * Gets the list of supported sub-protocol types.
     *
     * @return A list of supported SubProtocolType.
     */
    List<SubProtocolType> getSupportedSubProtocolTypes();

    /**
     * Retrieves access information based on the provided access request.
     *
     * @param accessRequest The request containing details about the access being requested.
     * @return The response containing the access information.
     * @throws DSCExecuteException If an error occurs during the retrieval process.
     */
    AccessResponse retrieveAccessInformation(AccessRequest accessRequest)
            throws DSCExecuteException;
}
