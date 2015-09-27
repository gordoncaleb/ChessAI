package com.gordoncaleb.chess.dropwizard.resource;

import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.*;

@Path("/game")
public class ChessResource {

    @Path("/{gameId}")
    @POST
    @Timed
    public void makeMove(@PathParam("gameId") String gameId, String move){

    }
}
