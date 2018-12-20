package com.hiroshi.cimoc.network;

public class DNSHelper {

    static public String getIpByHost(String hostname) {
        switch (hostname) {
            case "m.manhuagui.com":
            case "tw.manhuagui.com":
                return "47.89.23.88";
            case "www.manhuagui.com":
                return "14.49.38.185";
            //return "106.185.40.107";
            default:
                return null;
        }
    }
}
