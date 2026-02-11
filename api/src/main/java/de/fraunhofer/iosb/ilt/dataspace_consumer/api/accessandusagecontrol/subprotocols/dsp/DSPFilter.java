package de.fraunhofer.iosb.ilt.dataspace_consumer.api.accessandusagecontrol.subprotocols.dsp;

import java.util.Optional;

/**
 * Simple filter used for DSP requests.
 *
 * @param key the field/key to filter on
 * @param operant optional operator (e.g., '=', '!=', 'contains')
 * @param value the value to compare against
 */
public record DSPFilter(String key, Optional<String> operant, String value) {}
