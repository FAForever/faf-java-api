package com.faforever.api.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class GogService {

  private static final Pattern GOG_USERNAME_PATTERN = Pattern.compile("[\w]+");


}
