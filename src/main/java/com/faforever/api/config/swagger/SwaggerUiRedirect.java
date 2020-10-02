package com.faforever.api.config.swagger;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/")
public class SwaggerUiRedirect {

  @RequestMapping(method = RequestMethod.GET)
  public RedirectView redirectToSwaggerUi(RedirectAttributes attributes) {
    return new RedirectView("swagger-ui/index.html");
  }
}
