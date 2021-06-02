package com.ykuee.loveyou.mail;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @program: loveyou
 * @description: 发送邮件
 * @author: Ykuee
 * @create: 2021-05-31 10:26
 **/

@Component("myZwtMailSender")
public class MailSender {

    private final static Logger logger = LoggerFactory.getLogger(MailSender.class);

    @Autowired
    private MailConfig mailConfig;

    @Value("${loveyou.rainbowfart-url}")
    private String rainbowfartUrl;

    @Value("${loveyou.weather-url}")
    private String weatherUrl;

    @Value("${loveyou.weather-city}")
    private String weatherCity;

    @Value("${loveyou.one-url}")
    private String oneUrl;

    @Value("${loveyou.template-name}")
    private String templateName;

    @Value("${loveyou.start-date}")
    private String startDate;

    @Value("${loveyou.real-note-file}")
    private String realNoteFile;

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendMimeMessage(Mail mailDTO) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper;
        try {
            messageHelper = new MimeMessageHelper(mimeMessage, true);
            messageHelper.setFrom(mailDTO.getFrom());
            if (StrUtil.isBlank(mailDTO.getFrom())) {
                messageHelper.setFrom(mailConfig.getFrom());
            }
            messageHelper.setTo(mailDTO.getTo());
            messageHelper.setSubject(mailDTO.getSubject());

            mimeMessage = messageHelper.getMimeMessage();
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(mailDTO.getText(), "text/html;charset=UTF-8");

            // 描述数据关系
            MimeMultipart mm = new MimeMultipart();
            mm.setSubType("related");
            mm.addBodyPart(mimeBodyPart);

            // 添加邮件附件
            if (mailDTO.getFilenames() != null) {
                for (String filename : mailDTO.getFilenames()) {
                    MimeBodyPart attachPart = new MimeBodyPart();
                    try {
                        attachPart.attachFile(filename);
                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.info("邮件中文件附件读取文件异常");
                    }
                    mm.addBodyPart(attachPart);
                }
            }
            mimeMessage.setContent(mm);
            mimeMessage.saveChanges();
        } catch (MessagingException e) {
            logger.info("邮件发送出现异常");
            e.printStackTrace();
        }

        javaMailSender.send(mimeMessage);
    }

    public void sendChpMsg() throws Exception {
        Mail mailDTO = new Mail();
        mailDTO.setFrom(mailConfig.getFrom());
        mailDTO.setTo(mailConfig.getTo());
        mailDTO.setSubject("又是充满活力的一天！早上好呀！· " + DateUtil.today());
        mailDTO.setText(getThymeleafHtml());
        logger.info("今日发送数据{}", mailDTO);
        sendMimeMessage(mailDTO);
    }

    /**
     * 远程获取要发送的信息
     */
    public String getRainbow(String rainbowfartUrlStr) {
        try {
            //创建客户端对象
            HttpClient client = HttpClients.createDefault();
            /*创建地址 https://du.shadiao.app/api.php*/
            HttpGet get = new HttpGet(rainbowfartUrlStr);
            //发起请求，接收响应对象
            HttpResponse response = client.execute(get);
            //获取响应体，响应数据是一种基于HTTP协议标准字符串的对象
            //响应体和响应头，都是封装HTTP协议数据。直接使用可能出现乱码或解析错误
            HttpEntity entity = response.getEntity();
            //通过HTTP实体工具类，转换响应体数据
            return EntityUtils.toString(entity, "utf-8");
        } catch (IOException e) {
            logger.info("获取彩虹屁句子失败,url: {}",rainbowfartUrlStr);
            return "";
        }
    }

    public One getOneData(String oneUrlStr) {
        Document doc = null;
        One one = new One();
        try {
            doc = Jsoup.connect(oneUrlStr).timeout(5000).get();
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("爬取ONE网站失败,url: {}",oneUrlStr);
            return one;
        }
        Elements elements = doc.select("#carousel-one .carousel-inner .item");
        if (elements.size() == 0) {
            return one;
        }
        Element element = elements.get(0);
        Elements img = element.select(".fp-one-imagen");
        if (img != null) {
            //输出图片链接
            one.setImgUrl(img.attr("src"));
        }
        Elements type = element.select(".fp-one-imagen-footer");
        if (type != null) {
            one.setType(type.text().replace("/(^\\s*)|(\\s*$)/g", ""));
        }
        Elements text = element.select(".fp-one-cita");
        if (text != null) {
            one.setText(text.text().replace("/(^\\s*)|(\\s*$)/g", ""));
        }
        return one;
    }

    public Document getWeatherDoc(String weatherUrl){
        try {
            return Jsoup.connect(weatherUrl).timeout(5000).get();
        } catch (IOException e) {
            e.printStackTrace();
            logger.info("获取天气doc失败,url: {}",weatherUrl);
            return null;
        }
    }

    public String getWeatherTips(Document doc) {
        if(doc==null){
            return "";
        }
        Elements elements = doc.getElementsByClass("wea_tips");
        if (elements.size() == 0) {
            return "";
        }
        Element element = elements.get(0);
        Elements tips = element.getElementsByTag("em");
        if (tips != null) {
            //输出图片链接
            return tips.text().replace("/(^\\s*)|(\\s*$)/g", "");
        }
        return "";
    }

    public List<DayWeather> getWeatherData(Document doc) {
        List<DayWeather> resList = new ArrayList<>();
        if(doc==null){
            return resList;
        }
        Elements elements = doc.select(".forecast .days");
        if (elements.size() == 0) {
            return resList;
        }
        for (Element element : elements) {
            DayWeather dayWeather = new DayWeather();
            Elements dayData = element.select("li");
            //今天 明天 后天
            dayWeather.setDay(dayData.get(0).text().replace("/(^\\s*)|(\\s*$)/g", ""));
            //天气图片src
            dayWeather.setWeatherImgUrl(dayData.get(1).select("img").attr("src"));
            //天气文字
            System.out.println();
            dayWeather.setWeatherText(dayData.get(1).text().replace("/(^\\s*)|(\\s*$)/g", ""));
            //温度文字
            dayWeather.setTemperature(dayData.get(2).text().replace("/(^\\s*)|(\\s*$)/g", ""));
            //温度文字
            dayWeather.setWindDirection(dayData.get(3).select("em").text().replace("/(^\\s*)|(\\s*$)/g", ""));
            //温度文字
            dayWeather.setWindLevel(dayData.get(3).select("b").text().replace("/(^\\s*)|(\\s*$)/g", ""));
            //温度文字
            dayWeather.setPollution(dayData.get(4).text().replace("/(^\\s*)|(\\s*$)/g", ""));
            //温度文字
            dayWeather.setPollutionLevel(dayData.get(4).select("b").attr("class"));
            resList.add(dayWeather);
        }
        return resList;
    }

    public String getRealNote(String filePath){
        File file = new File(filePath);
        if(!file.exists()){
            return "";
        }
        InputStreamReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new InputStreamReader(new FileInputStream(file));
            BufferedReader br = new BufferedReader(reader);
            String lineTxt;
            while (StrUtil.isNotBlank(lineTxt = br.readLine())) {
                sb.append(lineTxt);
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("获取真实笔记失败");
            return "";
        }
        return sb.toString();
    }

    public String getThymeleafHtml(){
        Context context = new Context();
        One one = getOneData(oneUrl);
        Document doc = getWeatherDoc(weatherUrl);
        String weatherTip = getWeatherTips(doc);
        List<DayWeather> dayWeatherList = getWeatherData(doc);
        String rainbow = getRainbow(rainbowfartUrl);
        long meetDays = DateUtil.between(new Date(), DateUtil.parse(startDate), DateUnit.DAY);
        String weatherCityStr = StrUtil.isNotBlank(weatherCity)?weatherCity:"";
        String realNote = getRealNote(realNoteFile);
        context.setVariable("meetDays", meetDays);
        context.setVariable("realNote", realNote);
        context.setVariable("weatherTip", weatherTip);
        context.setVariable("weatherCity", weatherCityStr);
        context.setVariable("dayWeatherList", dayWeatherList);
        context.setVariable("today", DateUtil.today());
        context.setVariable("one", one);
        context.setVariable("rainbow", rainbow);
        return templateEngine.process(templateName, context);
    }
}
