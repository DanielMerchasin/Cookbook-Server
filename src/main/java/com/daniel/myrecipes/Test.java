package com.daniel.myrecipes;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/")
public class Test {

    @GET
    public Response getTestMessage() {
        return Response.ok("GOT IT!").build();
    }

}
