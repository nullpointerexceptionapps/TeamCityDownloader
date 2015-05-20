package com.raidzero.teamcitydownloader.global;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by raidzero on 8/15/14.
 */
public class DateUtility {

    public static String friendlyDate(String timestamp) throws ParseException {
        String oldTimeFormat = "yyyyMMdd'T'HHmmssZ";
        String newTimeFormat = "yyyy-MM-dd hh:mm a z";

        SimpleDateFormat sdf = new SimpleDateFormat(oldTimeFormat);

        // get Date objects from server data
        Date date = sdf.parse(timestamp);

        sdf.applyPattern(newTimeFormat);
        return sdf.format(date);
    }
}
