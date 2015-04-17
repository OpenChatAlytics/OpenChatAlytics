package com.hipchalytics.web.resources;

import org.mortbay.jetty.Response;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/trending")
public class TrendingTopicsResource {

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTrendingTopics(@FormParam("starttime") String startTime,
                                      @FormParam("endtime") String endTime,
                                      @FormParam("user") String user,
                                      @FormParam("room") String room) {
        return null;
    }
}
