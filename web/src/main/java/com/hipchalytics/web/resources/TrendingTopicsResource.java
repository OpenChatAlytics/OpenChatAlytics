package com.hipchalytics.web.resources;

import com.google.common.base.Optional;
import com.hipchalytics.compute.config.HipChalyticsConfig;
import com.hipchalytics.compute.db.dao.HipChalyticsDaoFactory;
import com.hipchalytics.compute.db.dao.IHipChalyticsDao;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/trending")
public class TrendingTopicsResource {

    private static final Logger LOG = LoggerFactory.getLogger(TrendingTopicsResource.class);

    private final IHipChalyticsDao dbDao;

    public TrendingTopicsResource(HipChalyticsConfig hconfig) {
        dbDao = HipChalyticsDaoFactory.getHipchalyticsDao(hconfig);
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTrendingTopics(@FormParam("starttime") String startTime,
                                      @FormParam("endtime") String endTime,
                                      @FormParam("user") String user,
                                      @FormParam("room") String room) {
        LOG.debug("Got query for starttime={}, endtime={}, user={}, room={}", startTime, endTime,
                  user, room);

        Optional<String> username =null;
        if (user == null || user.isEmpty()) {
            username = Optional.of(user);
        } else {
            username = Optional.absent();
        }

        Optional<String> roomName =null;
        if (room == null || room.isEmpty()) {
            roomName = Optional.of(room);
        } else {
            roomName = Optional.absent();
        }

        Interval interval = new Interval(DateTime.now(), DateTime.now());
        Map<String, Long> topEntities = dbDao.getTopEntities(interval, roomName, username, 10);
        return Response.ok(topEntities).build();
    }
}
