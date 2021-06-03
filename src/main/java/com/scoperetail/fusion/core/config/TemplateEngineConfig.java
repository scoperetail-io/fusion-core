package com.scoperetail.fusion.core.config;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

@Configuration
public class TemplateEngineConfig {

  @Bean
  public VelocityEngine getVelocityEngine() throws VelocityException {
    final VelocityEngine velocityEngine = new VelocityEngine();
    velocityEngine.setProperty("resource.loader", "class");
    velocityEngine.setProperty("class.resource.loader.class",
        "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
    velocityEngine.init();
    return velocityEngine;
  }

  @Bean
  public FreeMarkerConfigurer freeMarkerConfigurer() {
    FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer();
    freeMarkerConfigurer.setTemplateLoaderPath("classpath:/usecases");
    freeMarkerConfigurer.setDefaultEncoding("UTF-8");
    return freeMarkerConfigurer;
  }


}
