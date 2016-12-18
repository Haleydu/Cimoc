package com.hiroshi.cimoc.source;

/**
 * Created by Hiroshi on 2016/8/14.
 */
public class NHentai/* extends MangaParser */{
/*
    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = StringUtils.format("https://nhentai.net/search/?q=%s&page=%d", keyword, page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("#content > div.index-container > div > a")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.hrefWithSplit(1);
                String title = node.text("div.caption");
                String author = StringUtils.match("\\[(.*?)\\]", title, 1);
                title = title.replaceFirst("\\[.*?\\]\\s*", "");
                String cover = "https:".concat(node.src("img"));
                return new Comic(SourceManager.SOURCE_NHENTAI, cid, title, cover, null, author);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = StringUtils.format("https://nhentai.net/g/%s", cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String title = body.text("#info > h1");
        String intro = body.text("#info > h2");
        String author = body.text("#tags > div > span > a[href^=/artist/]");
        String cover = "https:".concat(body.src("#cover > a > img"));
        comic.setInfo(title, cover, null, intro, author, true);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        list.add(new Chapter("全一话", ""));
        return list;
    }

    @Override
    public Request getRecentRequest(int page) {
        String url = StringUtils.format("https://nhentai.net/?page=%d", page);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<Comic> parseRecent(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("#content > div.index-container > div > a")) {
            String cid = node.hrefWithSplit(1);
            String title = node.text("div.caption");
            String author = StringUtils.match("\\[(.*?)\\]", title, 1);
            title = title.replaceFirst("\\[.*?\\]\\s*", "");
            String cover = "https:".concat(node.src("img"));
            list.add(new Comic(SourceManager.SOURCE_NHENTAI, cid, title, cover, null, author));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        return getInfoRequest(cid);
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        Node body = new Node(html);
        List<ImageUrl> list = new LinkedList<>();
        int count = 0;
        for (Node node : body.list("#thumbnail-container > div > a > img")) {
            String url = "https:".concat(node.attr("data-src"));
            list.add(new ImageUrl(++count, url.replace("t.jpg", ".jpg"), false));
        }
        return list;
    }
*/
}
