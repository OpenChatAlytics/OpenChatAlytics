package com.chatalytics.compute.chat.dao;

import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import java.util.List;
import java.util.Random;

import javax.ws.rs.core.MediaType;

/**
 * Base class for JSON implementations of {@link IChatApiDAO} classes
 *
 * @author giannis
 *
 */
public abstract class AbstractJSONChatApiDAO implements IChatApiDAO {

    private final List<String> authTokens;
    private final String authTokenParam;
    private final Random rand;

    public AbstractJSONChatApiDAO(List<String> authTokens, String authTokenParam) {
        this.authTokens = authTokens;
        this.authTokenParam = authTokenParam;
        this.rand = new Random(System.currentTimeMillis());
    }

    /**
     * Helper method for doing GETs with <code>retries</code> number of retries in case of 403
     * errors.
     *
     * @param resource
     *            The resource to GET data from
     * @param retries
     *            The number of retries if a 403 is encountered.
     * @return The JSON result string.
     */
    protected String getJsonResultWithRetries(WebResource resource, int retries) {
        resource = addTokenQueryParam(resource);
        while (retries >= 0) {
            try {
                String jsonStr = resource.accept(MediaType.APPLICATION_JSON).get(String.class);
                return jsonStr;
            } catch (UniformInterfaceException e) {
                if (e.getResponse().getStatus() == Status.FORBIDDEN.getStatusCode()) {
                    retries--;
                }
            }
        }
        return "{}";
    }

    /**
     * Helper method for adding the token query parameter.
     *
     * @param resource
     *            The resource to add the token parameter to.
     * @return Returns a new resource with the token query parameter added.
     */
    private WebResource addTokenQueryParam(WebResource resource) {
        int tokenSize = authTokens.size();
        String randomAuthToken = authTokens.get(rand.nextInt(tokenSize));
        return resource.queryParam(authTokenParam, randomAuthToken);
    }

}
