/*
 * Copyright (c) 2018 Author vinayavasthi
 *
 * This software is a property of Tesco PLC
 */

package com.tesco.utilities.scheduler.scheduler;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Created by vinay on 1/8/16.
 */
public class SchedulerJettyConfigurer implements JettyServerCustomizer {

  @Override
  public void customize(Server server) {
    WebAppContext webAppContext = (WebAppContext) server.getHandler();
    try {
      // Load configuration from resource file (standard Jetty xml configuration) and configure the context.
      createConfiguration("/etc/jetty.xml").configure(webAppContext);
      createConfiguration("/etc/jetty-rewrite.xml").configure(server);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private XmlConfiguration createConfiguration(String xml) throws IOException, SAXException {
    return new XmlConfiguration(SchedulerLauncher.class.getResourceAsStream(xml));
  }
}
