package com.ykuee.loveyou.mail;

import lombok.Data;

import java.util.Date;

/**
 * @program: loveyou
 * @description: 邮件
 * @author: Ykuee
 * @create: 2021-05-31 10:53
 **/
@Data
public class Mail {

    private String from;

    private String replyTo;

    private String[] to;

    private String[] cc;

    private String[] bcc;

    private Date sentDate;

    private String subject;

    private String text;

    private String[] filenames;

}
