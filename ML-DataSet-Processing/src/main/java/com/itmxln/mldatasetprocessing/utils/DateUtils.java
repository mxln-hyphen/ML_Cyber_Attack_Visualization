package com.itmxln.mldatasetprocessing.utils;

import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Date;

@Slf4j
public class DateUtils {

    public static Date timestampToDate(Instant timestamp) {
        try {
            return new Date(timestamp.toEpochMilli());
        } catch (ArithmeticException ex) {
            log.error("时间戳转换错误！");
            throw new IllegalArgumentException(ex);
        }

    }
}
