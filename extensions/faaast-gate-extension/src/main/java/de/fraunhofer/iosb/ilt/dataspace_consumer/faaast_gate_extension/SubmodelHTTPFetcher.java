package de.fraunhofer.iosb.ilt.dataspace_consumer.faaast_gate_extension;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.fraunhofer.iosb.ilt.faaast.client.exception.BadRequestException;
import de.fraunhofer.iosb.ilt.faaast.client.exception.ConflictException;
import de.fraunhofer.iosb.ilt.faaast.client.exception.ConnectivityException;
import de.fraunhofer.iosb.ilt.faaast.client.exception.ForbiddenException;
import de.fraunhofer.iosb.ilt.faaast.client.exception.InternalServerErrorException;
import de.fraunhofer.iosb.ilt.faaast.client.exception.InvalidPayloadException;
import de.fraunhofer.iosb.ilt.faaast.client.exception.MethodNotAllowedException;
import de.fraunhofer.iosb.ilt.faaast.client.exception.NotFoundException;
import de.fraunhofer.iosb.ilt.faaast.client.exception.StatusCodeException;
import de.fraunhofer.iosb.ilt.faaast.client.exception.UnauthorizedException;
import de.fraunhofer.iosb.ilt.faaast.client.exception.UnsupportedStatusCodeException;
import de.fraunhofer.iosb.ilt.faaast.client.http.HttpMethod;
import de.fraunhofer.iosb.ilt.faaast.client.http.HttpStatus;
import de.fraunhofer.iosb.ilt.faaast.client.util.HttpHelper;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.DeserializationException;
import de.fraunhofer.iosb.ilt.faaast.service.dataformat.json.JsonApiDeserializer;
import de.fraunhofer.iosb.ilt.faaast.service.model.api.paging.Page;
import org.eclipse.digitaltwin.aas4j.v3.model.Submodel;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Helper class to fetch and deserialize Submodels over HTTP from an AAS server. This class
 * encapsulates:
 *
 * <ul>
 *   <li>sending HTTP GET requests to an AAS endpoint,
 *   <li>validation of HTTP response codes, and
 *   <li>conversion of the JSON response into Java models ({@link Submodel}).
 * </ul>
 *
 * <p>The class uses data structures from the faaast client and service to interpret paginated
 * responses and to report error conditions as {@link
 * de.fraunhofer.iosb.ilt.faaast.client.exception.StatusCodeException}.
 */
public class SubmodelHTTPFetcher {

    private SubmodelHTTPFetcher() {}

    private static final List<HttpStatus> SUPPORTED_DEFAULT_HTTP_STATUS =
            List.of(
                    HttpStatus.BAD_REQUEST,
                    HttpStatus.UNAUTHORIZED,
                    HttpStatus.FORBIDDEN,
                    HttpStatus.NOT_FOUND,
                    HttpStatus.INTERNAL_SERVER_ERROR);

    /**
     * Validates the HTTP status code of a response against an expected status. If the status does
     * not match, specialized exceptions from the faaast client package are thrown to represent
     * concrete error cases (e.g. NotFound, BadRequest).
     *
     * @param method the HTTP method used (e.g. GET, POST)
     * @param response the received HTTP response object (must not be {@code null})
     * @param expected the expected {@link HttpStatus}
     * @throws StatusCodeException if the status code does not match the expected one
     */
    private static void validateStatusCode(
            HttpMethod method, HttpResponse<?> response, HttpStatus expected)
            throws StatusCodeException {
        if (Objects.isNull(response)) {
            throw new IllegalArgumentException("response must be non-null");
        }
        if (Objects.equals(expected.getCode(), response.statusCode())) {
            return;
        }
        List<HttpStatus> supported = new ArrayList<>(SUPPORTED_DEFAULT_HTTP_STATUS);
        if (Objects.equals(method, HttpMethod.POST)) {
            supported.add(HttpStatus.METHOD_NOT_ALLOWED);
            supported.add(HttpStatus.CONFLICT);
        }

        try {
            HttpStatus status = HttpStatus.from(response.statusCode());
            @SuppressWarnings("unchecked")
            HttpResponse<String> stringResponse = (HttpResponse<String>) response;
            if (!supported.contains(status)) {
                throw new UnsupportedStatusCodeException(stringResponse);
            }
            Exception exception =
                    switch (status) {
                        case BAD_REQUEST -> new BadRequestException(stringResponse);
                        case UNAUTHORIZED -> new UnauthorizedException(stringResponse);
                        case FORBIDDEN -> new ForbiddenException(stringResponse);
                        case NOT_FOUND -> new NotFoundException(stringResponse);
                        case METHOD_NOT_ALLOWED -> new MethodNotAllowedException(stringResponse);
                        case CONFLICT -> new ConflictException(stringResponse);
                        case INTERNAL_SERVER_ERROR ->
                                new InternalServerErrorException(stringResponse);
                        default -> new UnsupportedStatusCodeException(stringResponse);
                    };
            throw (StatusCodeException) exception;
        } catch (IllegalArgumentException e) {
            @SuppressWarnings("unchecked")
            HttpResponse<String> stringResponse = (HttpResponse<String>) response;
            throw new UnsupportedStatusCodeException(stringResponse);
        }
    }

    /**
     * Executes a GET request to the given URL and deserializes the returned submodels. The method
     * expects a paginated JSON response containing "result" (array) and "paging_metadata".
     *
     * @param url the target URI of the AAS endpoint
     * @param client the HTTP client used to send the request
     * @return a list of deserialized {@link Submodel} objects
     * @throws ConnectivityException on network or connectivity failures
     * @throws StatusCodeException if the HTTP status code is not the expected one
     */
    public static List<Submodel> getAllSubmodels(URI url, HttpClient client)
            throws ConnectivityException, StatusCodeException {
        HttpRequest request =
                HttpRequest.newBuilder().uri(url).timeout(Duration.ofSeconds(10)).GET().build();
        HttpResponse<String> response = HttpHelper.send(client, request);
        validateStatusCode(HttpMethod.GET, response, HttpStatus.OK);
        try {
            return deserializeResult(response.body());
        } catch (DeserializationException | JSONException e) {
            throw new InvalidPayloadException(e);
        }
    }

    private static Page<Submodel> getSubmodelFromJson(JSONObject json)
            throws DeserializationException, JSONException {

        if (json.has("modelType") && json.getString("modelType").equals("Submodel")) {
            return new Page.Builder<Submodel>()
                    .result(new JsonApiDeserializer().read(json.toString(), Submodel.class))
                    .build();
        } else {
            return new Page<>();
        }
    }

    private static List<Submodel> deserializeResult(String responseBody)
            throws DeserializationException, JSONException {
        JSONObject root = new JSONObject(responseBody);

        List<Submodel> submodels = new ArrayList<>();

        if (root.has("result")) {

            JSONArray result = root.getJSONArray("result");
            for (int i = 0; i < result.length(); i++) {
                JSONObject obj = result.getJSONObject(i);
                submodels.addAll(getSubmodelFromJson(obj).getContent());
            }
            return submodels;
        } else {
            return getSubmodelFromJson(root).getContent();
        }
    }
}
