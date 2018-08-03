package com.hiroshi.cimoc.network;

import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import okhttp3.Dns;

public class HttpDns implements Dns {
    private static final Dns SYSTEM = Dns.SYSTEM;

    @Override
    public List<InetAddress> lookup(String hostname) throws UnknownHostException {
        Log.e("HttpDns", "lookup:" + hostname);
        String ip = DNSHelper.getIpByHost(hostname);
        if (ip != null && !ip.equals("")) {
            List<InetAddress> inetAddresses = Arrays.asList(InetAddress.getAllByName(ip));
            Log.e("HttpDns", "inetAddresses:" + inetAddresses);
            return inetAddresses; //返回自己解析的地址列表
        }
        return SYSTEM.lookup(hostname); //走系统的dns列表
    }
}