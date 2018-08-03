package com.hiroshi.cimoc.network;

public class DNSHelper {

    static public String getIpByHost(String hostname){
        switch (hostname){
            case "m.manhuagui.com":
            case "tw.manhuagui.com":
            case "www.manhuagui.com":
                return "47.89.23.88";

            default:
                return null;
        }
    }
}
