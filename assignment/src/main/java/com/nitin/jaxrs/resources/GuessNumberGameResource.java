package com.nitin.jaxrs.resources;

import com.google.inject.Inject;
import com.nitin.model.GuessResultModel;
import com.nitin.processing.GuessNumberProcessor;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/rest/playgame")
public class GuessNumberGameResource {
	
	private final GuessNumberProcessor guessNumberProcessor;
	
	@Inject
	public GuessNumberGameResource(final GuessNumberProcessor guessNumberProcessor)
	{
		this.guessNumberProcessor = guessNumberProcessor;
	}
	
	@GET
	@Path("/token")
	@Produces(MediaType.APPLICATION_JSON)
	public GuessResultModel generateToken(@Context UriInfo ui)
	{
		return guessNumberProcessor.generateToken();
	}
	
	@GET
	@Path("/guess")
	@Produces(MediaType.APPLICATION_JSON)
	public Response validateGuessedNumber(@DefaultValue("") @QueryParam("token") final String token, @DefaultValue("0") @QueryParam("guessedNumber") final Integer guessedNumber)
	{
		return Response.ok(guessNumberProcessor.isGuessedNumberValid(token, guessedNumber)).build();
	}

}
