package com.qingshu.hmapisdk.model.params;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Author qingshu
 * @Date 2023/10/5
 */
@Data
@Accessors(chain = true)
public class HoroscopeParams implements Serializable {
    private static final long serialVersionUID = -6594953716764602371L;
    /**
     * 十二星座对应英文小写，aries, taurus, gemini, cancer, leo, virgo, libra, scorpio, sagittarius, capricorn, aquarius, pisces
     */
    private String type;
    /**
     * 十二星座对应英文小写，aries, taurus, gemini, cancer, leo, virgo, libra, scorpio, sagittarius, capricorn, aquarius, pisces
     */
    private String time;
}
