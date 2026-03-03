package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension;


public class LoggingUtil{

    private LoggingUtil(){
        
    }

    /**
     * Masks the central part of a token string leaving the specified number of characters visible
     * at both ends.
     *
     * @param token the token string to mask
     * @param visible number of characters to keep visible at each end
     * @return the masked token string
     */
    public static String maskToken(String token, int visible) {
        if (token == null || token.isEmpty()) {
            return token;
        }

        if (token.length() <= visible * 2) {
            return "*".repeat(token.length());
        }

        String start = token.substring(0, visible);
        String end = token.substring(token.length() - visible);
        String masked = "*".repeat(token.length() - (visible * 2));

        return start + masked + end;
    }
}
