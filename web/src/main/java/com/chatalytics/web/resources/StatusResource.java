package com.chatalytics.web.resources;

import com.chatalytics.web.constant.WebConstants;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 * USed to query the status of the web server
 *
 * @author giannis
 */
@Path(StatusResource.STATUS_ENDPOINT)
public class StatusResource {

    public static final String STATUS_ENDPOINT = WebConstants.API_PATH + "status";

    @GET
    @Path("health")
    public String health() {
        return "OK";
    }
}
