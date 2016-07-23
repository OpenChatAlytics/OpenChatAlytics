package com.chatalytics.compute.web.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path(StatusResource.STATUS_ENDPOINT)
public class StatusResource {

    public static final String STATUS_ENDPOINT = "/compute/status";

    @GET
    @Path("health")
    public String health() {
        return "OK";
    }

}
