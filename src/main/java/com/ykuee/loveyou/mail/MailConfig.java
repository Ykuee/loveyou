package com.ykuee.loveyou.mail;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @program: loveyou
 * @description: 邮箱配置文件
 * @author: Ykuee
 * @create: 2021-05-31 10:19
 **/
@Data
@Component
@ConfigurationProperties(prefix = "mail")
public class MailConfig {

    private String domain;

    private String from;

    private String[] to;
}
