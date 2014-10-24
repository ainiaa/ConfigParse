/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package configparse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author Administrator
 */
public class Test {

    private static final char SEPARATOR = '_';

    public static String toUnderlineName(String s) {
        if (s == null) {
            return null;
        }

        StringBuilder sb = new StringBuilder();
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            boolean nextUpperCase = true;

            if (i < (s.length() - 1)) {
                nextUpperCase = Character.isUpperCase(s.charAt(i + 1));
            }

            if ((i >= 0) && Character.isUpperCase(c)) {
                if (!upperCase || !nextUpperCase) {
                    if (i > 0) {
                        sb.append(SEPARATOR);
                    }
                }
                upperCase = true;
            } else {
                upperCase = false;
            }

            sb.append(Character.toLowerCase(c));
        }

        return sb.toString();
    }

    public static String toCamelCase(String s) {
        if (s == null) {
            return null;
        }

        s = s.toLowerCase();

        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == SEPARATOR) {
                upperCase = true;
            } else if (upperCase) {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            } else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    public static String getInitials(String s) {
        return getInitials(s, true);
    }

    public static String getInitials(String s, boolean withFirstLetter) {
        if (s == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder(s.length());
        boolean upperCase = false;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == SEPARATOR) {
                upperCase = true;
            } else if (upperCase) {
                sb.append(Character.toUpperCase(c));
                upperCase = false;
            } else {
                sb.append(c);
            }
        }
        s = sb.toString();
        sb = new StringBuilder();

        if (withFirstLetter) {
//            System.out.println("s.charAt(0):" + s.charAt(0));
            sb.append(s.charAt(0));
        }

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    public static String toCapitalizeCamelCase(String s) {
        if (s == null) {
            return null;
        }
        s = toCamelCase(s);
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }

//    public static void main(String[] args) {
//        String iso_certified_staff = toCamelCase("iso_certified_staff");
//        String certified_staff = toCamelCase("certified_staff");
//        String user_id = toCamelCase("user_id");
//        System.out.println(toUnderlineName("ISOCertifiedStaff"));
//        System.out.println(toUnderlineName("CertifiedStaff"));
//        System.out.println(toUnderlineName("UserID"));
//        System.out.println(user_id + " === " + toCamelCase("user_id"));
//        System.out.println(iso_certified_staff);
//        System.out.println(certified_staff);
//        System.out.println(user_id);
//        System.out.println(getInitials(iso_certified_staff));
//        System.out.println(getInitials(certified_staff));
//        System.out.println(getInitials(user_id));
//        String timeStr = getTimeString("1393267734000", "yyyy-MM-dd");
//        System.out.println(timeStr);
//
//        String beginDate = "1328007600000";
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//
//        String sd = sdf.format(new Date(Long.parseLong(beginDate)));
//        System.out.println("sd:" + sd);
//        long getTimeStrings = timeStringTotimeStamp("2013-12-30 23:59:55", "yyyy-MM-dd HH:mm:ss");
//        String formatGetTimeStrings = getTimeString(String.valueOf(getTimeStrings), "yyyy/MM/dd HH:mm:ss");
//        System.out.println("formatGetTimeStrings : " + formatGetTimeStrings);
//    }
    public static String getTimeString(String nowTimeStamp, String format) {
        String currentTimeString;
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);//可以方便地修改日期格式 
        Date now = new Date(Long.parseLong(nowTimeStamp));
        currentTimeString = dateFormat.format(now);
        return currentTimeString;
    }

    /**
     * 将时间字符串转换为时间戳
     *
     * @param timeString
     * @return
     */
    public static long timeStringTotimeStamp(String timeString) {
        return timeStringTotimeStamp(timeString, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 将时间字符串转换为时间戳
     *
     * @param timeString
     * @param format
     * @return
     */
    public static long timeStringTotimeStamp(String timeString, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        Date date;
        long timeStamp = 0;
        try {
            date = simpleDateFormat.parse(timeString);
            timeStamp = date.getTime();
        } catch (ParseException ex) {
        }

        return timeStamp;
    }

    public static void main(String[] args) {
        try {
            Test.test();
        } catch (Exception e) {
            System.out.println("e ..." + e.toString());
        }
    }

    public static String test() throws Exception {
        throw new Exception("test");
    }
}
