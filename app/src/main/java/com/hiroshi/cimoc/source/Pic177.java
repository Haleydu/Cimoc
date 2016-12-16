package com.hiroshi.cimoc.source;

/**
 * Created by Hiroshi on 2016/10/5.
 */

public class  Pic177/* extends MangaParser */{
/*
    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = StringUtils.format("http://www.177pic66.com/page/%d?s=%s", page, keyword);
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("#content > div.post_box")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.hrefWithSubString("div.tit > h2 > a", 29);
                String title = node.text("div.tit > h2 > a");
                String author = StringUtils.match("(\\[中文\\])?\\[(.*?)\\]", title, 2);
                if (author != null) {
                    title = title.replaceFirst("(\\[中文\\])?\\[(.*?)\\]\\s*", "");
                }
                String cover = node.src("div.c-con > a > img");
                String update = node.text("div.c-top > div.datetime").replace(" ", "-");
                return new Comic(SourceManager.SOURCE_177PIC, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "http://www.177pic66.com/html/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("#content > div.post > div.c-top2 > div.tit > h1");
        String author = StringUtils.match("(\\[中文\\])?\\[(.*?)\\]", title, 2);
        if (author != null) {
            title = title.replaceFirst("(\\[中文\\])?\\[(.*?)\\]\\s*", "");
        }
        String cover = body.src("#content > div.post > div.entry-content > p > img");
        String update = body.text("#content > div.post > div.c-top2 > div.datetime");
        comic.setInfo(title, cover, update, null, author, true);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        int count = body.list("#single-navi span.single-navi").size();
        for (int i = count; i > 0; --i) {
            list.add(new Chapter("Ch" + i, String.valueOf(i)));
        }
        return list;
    }

    @Override
    public Request getRecentRequest(int page) {
        String url = "http://www.177pic66.com/page/" + page;
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Comic> parseRecent(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("div.conter > div.main > div.post_box")) {
            String cid = node.hrefWithSubString("div.tit > h2 > a", 29);
            String title = node.text("div.tit > h2 > a");
            String author = StringUtils.match("(\\[中文\\])?\\[(.*?)\\]", title, 2);
            if (author != null) {
                title = title.replaceFirst("(\\[中文\\])?\\[(.*?)\\]\\s*", "");
            }
            String cover = node.src("div.c-con > a > img");
            String update = node.text("div.c-top > div.datetime").replace(" ", "-");
            list.add(new Comic(SourceManager.SOURCE_177PIC, cid, title, cover, update, author));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("http://www.177pic66.com/html/%s/%s", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        Node body = new Node(html);
        int count = 0;
        for (Node node : body.list("#content > div.post > div.entry-content > p > img")) {
            list.add(new ImageUrl(++count, node.attr("src"), false));
        }
        return list;
    }
*/
}
