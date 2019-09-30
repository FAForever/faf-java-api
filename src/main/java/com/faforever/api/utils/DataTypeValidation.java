package com.faforever.api.utils;

public class DataTypeValidation {

  public static boolean isNumeric(String strNum) {
    return strNum.matches("-?\\d+(\\.\\d+)?");
  }
}
