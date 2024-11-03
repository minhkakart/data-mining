package com.minhkakart.tlu.datamining.utils;

public class HtmlText {
    public static String centerText(String text) {
        return "<html><div style='text-align: center;'>" + text + "</div></html>";
    }
    
    public static String trailingText(String text) {
        return "<html><div style='text-align: right;'>" + text + "</div></html>";
    }
}
