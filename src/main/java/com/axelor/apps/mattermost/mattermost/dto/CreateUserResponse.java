package com.axelor.apps.mattermost.mattermost.dto;

import com.axelor.utils.api.ResponseStructure;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;

@Getter
@JsonInclude(Include.NON_NULL)
public class CreateUserResponse extends ResponseStructure {

  protected String userId;

  public CreateUserResponse(Integer version, String userId) {
    super(version);
    this.userId = userId;
  }
}
