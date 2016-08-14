package com.hiroshi.cimoc.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Hiroshi on 2016/7/21.
 */
public class MachiSoup {

    public static String match(String regex, String input, int group) {
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                return matcher.group(group);
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static String[] match(String regex, String input, int... group) {
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(input);
            if (matcher.find()) {
                String[] result = new String[group.length];
                for (int i = 0; i != result.length; ++i) {
                    result[i] = matcher.group(group[i]);
                }
                return result;
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static List<String> matchAll(String regex, String input, int group) {
        LinkedList<String> list = new LinkedList<>();
        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(input);
            while (matcher.find()) {
                list.add(matcher.group(group));
            }
        } catch (Exception e) {
        }
        return list;
    }

    public static Node body(String html) {
        return new Node(Jsoup.parse(html).body());
    }

    public static class Node {

        private Element element;

        public Node(Element element) {
            this.element = element;
        }

        public Node id(String id) {
            return new Node(element.getElementById(id));
        }

        public Node select(String cssQuery) {
            return new Node(element.select(cssQuery).first());
        }

        public List<Node> list(String cssQuery) {
            Elements elements = element.select(cssQuery);
            List<Node> list = new LinkedList<>();
            for (Element e : elements) {
                list.add(new Node(e));
            }
            return list;
        }

        public boolean exist(String cssQuery) {
            return !element.select(cssQuery).isEmpty();
        }

        public String text() {
            return element.text();
        }

        public String text(String cssQuery) {
            try {
                return element.select(cssQuery).first().text();
            } catch (Exception e) {
            }
            return null;
        }

        public String text(String cssQuery, int start, int end) {
            String s = text(cssQuery);
            if (s == null) {
                return null;
            }
            if (end < 0) {
                return s.substring(start, s.length() + 1 + end);
            }
            return s.substring(start, end);
        }

        public String text(String cssQuery, int start) {
            return text(cssQuery, start, -1);
        }

        public String text(String cssQuery, String regex, int index) {
            String s = text(cssQuery);
            if (s != null) {
                return s.split(regex)[index];
            }
            return null;
        }

        public String attr(String attr) {
            try {
                return element.attr(attr);
            } catch (Exception e) {
            }
            return null;
        }

        public String attr(String attr, String regex, int index) {
            return attr(attr).split(regex)[index];
        }

        public String attr(String cssQuery, String attr) {
            try {
                return element.select(cssQuery).first().attr(attr);
            } catch (Exception e) {
            }
            return null;
        }

        public String attr(String cssQuery, String attr, String regex, int index) {
            return attr(cssQuery, attr).split(regex)[index];
        }

    }

}
