package com.axelor.apps.mattermost.helper;

import java.util.Optional;
import org.apache.commons.lang3.StringUtils;

public class MattermostHelper {

  public static String computeName(String value) {
    return Optional.ofNullable(value)
        .map(s -> normalize(s).replaceAll("[^a-zA-Z0-9\\-]", ""))
        .orElseGet(String::new);
  }

  public static String normalize(String value) {
    return Optional.ofNullable(value)
        .map(s -> StringUtils.stripAccents(s.toLowerCase().replace(" ", "-")))
        .orElseGet(String::new);
  }
}
