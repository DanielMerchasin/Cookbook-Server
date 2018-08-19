package com.daniel.myrecipes.networking;

import org.json.JSONObject;

import javax.ws.rs.core.Response;

public class ResponseException extends Exception {

    private Response response;

    public ResponseException(Response.Status status) {
        this(status, null);
    }

    public ResponseException(Response.Status status, String message) {
        super(String.format("[%d] %s: %s", status.getStatusCode(), status.getReasonPhrase(), message));
        response = createResponse(status, message);
    }

    public Response getResponse() {
        return response;
    }

    public static Response createResponse(Response.Status status, Exception e) {
        return createResponse(status, e.getMessage());
    }

    public static Response createResponse(Response.Status status, String message) {
        JSONObject entity = new JSONObject()
                .put("status", status.getStatusCode())
                .put("reason", status.getReasonPhrase());

        if (message != null)
            entity.put("message", message);

        return Response.status(status).entity(entity.toString()).build();
    }

}
