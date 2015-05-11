package com.chatalytics.compute.chat.dao;

import com.google.common.collect.Lists;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.type.CollectionType;
import org.codehaus.jackson.map.type.MapType;
import org.codehaus.jackson.map.type.TypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.core.MediaType;

/**
 * Base class for JSON implementations of {@link IChatApiDAO} classes
 *
 * @author giannis
 *
 */
public abstract class AbstractJSONChatApiDAO implements IChatApiDAO {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractJSONChatApiDAO.class);

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

    /**
     * Helper method for deserializing a hipchat JSON response to a collection of objects.
     *
     * @param jsonStr
     *            The hipchat JSON response.
     * @param mapElement
     *            Hipchat JSON responses are actually maps with a single element. This argument is
     *            the value of the element to pull out from the map.
     * @param colClassElements
     *            The types of objects that the collection object will contain.
     * @param objMapper
     *            The JSON object mapper used to deserialize the JSON string.
     * @return A collection of elements of type <code>colClassElements</code>.
     */
    protected <T> Collection<T> deserializeJsonStr(String jsonStr, String mapElement,
                                                   Class<T> colClassElements,
                                                   ObjectMapper objMapper) {
        Map<String, Collection<T>> re;
        try {
            TypeFactory typeFactory = objMapper.getTypeFactory();
            CollectionType type = typeFactory.constructCollectionType(List.class, colClassElements);
            MapType thetype = typeFactory.constructMapType(HashMap.class,
                                                           typeFactory.constructType(String.class),
                                                           type);
            re = objMapper.readValue(jsonStr, thetype);
        } catch (IOException e) {
            LOG.error("Got exception when trying to deserialize list of {}", colClassElements, e);
            return Lists.newArrayListWithExpectedSize(0);
        }
        return re.get(mapElement);
    }

}
