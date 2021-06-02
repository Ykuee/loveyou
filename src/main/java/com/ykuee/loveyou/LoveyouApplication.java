package com.ykuee.loveyou;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetAddress;

@EnableScheduling
@SpringBootApplication
public class LoveyouApplication extends SpringBootServletInitializer {

    private static Logger logger = LoggerFactory.getLogger(LoveyouApplication.class);

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext application = SpringApplication.run(LoveyouApplication.class, args);
        Environment env = application.getEnvironment();
        logger.info("\n----------------------------------------------------------\n\t" +
                        "项目运行成功! 访问连接:\t" +
                        "http://{}:{}{}\n" +
                        "----------------------------------------------------------",
                InetAddress.getLocalHost().getHostAddress(),
                env.getProperty("server.port"),
                env.getProperty("server.servlet.context-path"));
    }

}
