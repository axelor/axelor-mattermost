package com.axelor.apps.mattermost.mattermost.dto;

import com.axelor.utils.api.RequestStructure;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserPostRequest extends RequestStructure {

  @NotNull private String name;
  @NotNull private String firstName;
  @NotNull private String mail;
  @NotNull private String password;
}
