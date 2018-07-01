
package com.avasthi.microservices.scheduler;


import com.avasthi.microservices.exceptions.LoggingException;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.jetty.JettyServerCustomizer;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

/**
 * Created by vinay on 2/3/16.
 */
@Configuration
public class SchedulerEmbeddedJettyCustomizer {

  /**
   * Container customizer embedded servlet container customizer.
   *
   * @param keystoreFile    the keystore file
   * @param keystorePass    the keystore pass
   * @param port            the port
   * @param acceptQueueSize the accept queue size
   * @param maxThreads      the max threads
   * @param minThreads      the min threads
   * @param idleTimeout     the idle timeout
   * @return the embedded servlet container customizer
   * @throws Exception the exception
   */
  @Bean
  WebServerFactoryCustomizer<JettyServletWebServerFactory> containerCustomizer(
//          @Value("${keystore.file}") String keystoreFile,
//          @Value("${keystore.pass}") final String keystorePass,
          @Value("${server.port:8080}") final String port,
          @Value("${jetty.acceptqueuesize:5000}") final String acceptQueueSize,
          @Value("${jetty.threadPool.maxThreads:2000}") final String maxThreads,
          @Value("${jetty.threadPool.minThreads:8}") final String minThreads,
          @Value("${jetty.threadPool.idleTimeout:60000}") final String idleTimeout,
          @Value("${logging.path:/tmp}") final String loggingPath)
      throws Exception {


    return container -> {
      container.addServerCustomizers((JettyServerCustomizer) server -> {
        // Enable logs
        File logFile = new File(loggingPath + "/scheduler-yyyy_mm_dd.access.log");
        String logFilename = logFile.getAbsolutePath();
        if (!logFile.getParentFile().exists() && !logFile.getParentFile().mkdirs()) {
          throw new LoggingException(String.format("%s could not be created.", logFile.getParentFile().getAbsolutePath()));
        }
        NCSARequestLog requestLog = new NCSARequestLog(logFilename);
        requestLog.setAppend(true);
        requestLog.setExtended(false);
        requestLog.setLogTimeZone("UTC");
        RequestLogHandler rlh = new RequestLogHandler();
        rlh.setRequestLog(requestLog);
        Handler[] handlers = server.getHandlers();
        if (handlers == null || handlers.length == 0) {
          server.setHandler(rlh);
        }
        else {
          HandlerCollection handlerCollection = new HandlerCollection();
          for (int i = 0; i < handlers.length; ++i) {
            handlerCollection.addHandler(handlers[i]);
          }
          handlerCollection.addHandler(rlh);
          server.setHandler(handlerCollection);
        }
        // Tweak the connection pool used by Jetty to handle incoming HTTP connections
        final QueuedThreadPool threadPool = server.getBean(QueuedThreadPool.class);
        threadPool.setMaxThreads(Integer.valueOf(maxThreads));
        threadPool.setMinThreads(Integer.valueOf(minThreads));
        threadPool.setIdleTimeout(Integer.valueOf(idleTimeout));
        threadPool.setName("Jetty-Threadpool");

        for (Connector connector : server.getConnectors()) {
          if (connector instanceof ServerConnector) {
            ServerConnector serverConnector = (ServerConnector)connector;
            serverConnector.setAcceptQueueSize(Integer.valueOf(acceptQueueSize));
            serverConnector.setIdleTimeout(Integer.valueOf(idleTimeout));
            HttpConnectionFactory connectionFactory = connector.getConnectionFactory(HttpConnectionFactory.class);
            connectionFactory.getHttpConfiguration().setSendServerVersion(false);
          }
        }
      });
    };
  }
}