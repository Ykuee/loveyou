package com.ykuee.loveyou.mail;

import lombok.Data;

/**
 * @program: loveyou
 * @description: 一天的天气状况
 * @author: Ykuee
 * @create: 2021-05-31 17:45
 **/
@Data
public class DayWeather {

    private String day;

    private String weatherImgUrl;

    private String weatherText;

    private String temperature;

    private String windDirection;

    private String windLevel;

    private String pollution;

    private String pollutionLevel;

}
