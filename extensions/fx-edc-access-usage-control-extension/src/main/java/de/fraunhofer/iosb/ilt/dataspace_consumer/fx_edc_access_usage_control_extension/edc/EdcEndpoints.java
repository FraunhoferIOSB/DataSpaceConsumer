package de.fraunhofer.iosb.ilt.dataspace_consumer.fx_edc_access_usage_control_extension.edc;

public final class EdcEndpoints {

    private EdcEndpoints() {}

    public static String catalogEndpoint(String baseURL) {

        return String.format("%s/v3/catalog/request", baseURL);
    }

    public static String negotiationEndpoint(String baseURL) {
        return String.format("%s/v3/edrs", baseURL);
    }

    public static String availableEDRsEndpoint(String baseURL) {
        return String.format("%s/v3/edrs/request", baseURL);
    }

    public static String negotiationStatusEndpoint(String baseURL, String negotiationId) {
        return String.format("%s/v3/contractnegotiations/%s/state", baseURL, negotiationId);
    }

    public static String tokenEndpoint(String baseURL, String transferProcessId) {
        return String.format(
                "%s/v3/edrs/%s/dataaddress?auto_refresh=true", baseURL, transferProcessId);
    }
}
