package com.ykuee.loveyou.mail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @program: loveyou
 * @description: 邮件定时任务
 * @author: Ykuee
 * @create: 2021-05-31 13:23
 **/

@Component
public class MailScheduled {

    @Autowired
    private MailSender mailSender;


    /*定时执行任务方法 每天7点00执行该任务*/
    @Scheduled(cron ="${loveyou.cron}")
    public void dsrw() throws Exception {
        mailSender.sendChpMsg();
    }

}
