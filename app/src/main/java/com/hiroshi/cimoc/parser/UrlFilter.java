package com.hiroshi.cimoc.parser;

public class UrlFilter {
    public String Filter;
    public String Regex;
    public int Group;

    public UrlFilter(String filter,String regex,int group){
        Filter = filter;
        Regex = regex;
        Group = group;
    }
}
