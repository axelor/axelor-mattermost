package com.axelor.apps.mattermost.mattermost.rest.service;

import com.axelor.apps.mattermost.mattermost.dto.CreateUserPostRequest;
import com.axelor.apps.mattermost.mattermost.dto.CreateUserResponse;
import com.axelor.apps.mattermost.mattermost.service.MattermostService;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.utils.api.HttpExceptionHandler;
import com.axelor.utils.api.RequestValidator;
import com.axelor.utils.api.ResponseConstructor;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/user")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MattermostRestService {

  @Path("/createUser")
  @POST
  @HttpExceptionHandler
  public Response createUser(CreateUserPostRequest request) {
    RequestValidator.validateBody(request);

    String userId =
        Beans.get(MattermostService.class)
            .createUser(
                request.getName(),
                request.getFirstName(),
                request.getMail(),
                request.getPassword());
    return ResponseConstructor.build(
        Response.Status.OK, I18n.get("User created."), new CreateUserResponse(0, userId));
  }
}
