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

  /**
   * Return the string value with the first letter capitalized and lower all others
   *
   * @param value String
   * @return
   */
  public static String capitalizeFirstLetter(String value) {
    if (value == null) {
      return null;
    }
    if (value.length() == 0) {
      return value;
    }
    StringBuilder result = new StringBuilder(value.toLowerCase());
    result.replace(0, 1, result.substring(0, 1).toUpperCase());
    return result.toString();
  }
}
