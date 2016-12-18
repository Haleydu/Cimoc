package com.hiroshi.cimoc.source;

/**
 * Created by Hiroshi on 2016/8/6.
 */
public class ExHentai/* extends MangaParser */{
/*
    @Override
    public Request getSearchRequest(String keyword, int page) {
        String url = StringUtils.format("https://exhentai.org?f_search=%s&page=%d", keyword, (page - 1));
        return new Request.Builder().url(url).header("Cookie", "ipb_member_id=2145630; ipb_pass_hash=f883b5a9dd10234c9323957b96efbd8e").build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("table.itg > tbody > tr[class^=gtr]")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.hrefWithSubString("td:eq(2) > div > div:eq(2) > a", 23, -2);
                String title = node.text("td:eq(2) > div > div:eq(2) > a");
                String cover = node.src("td:eq(2) > div > div:eq(0) > img");
                if (cover == null) {
                    String temp = node.textWithSubstring("td:eq(2) > div > div:eq(0)", 19).split("~", 2)[0];
                    cover = "https://exhentai.org/".concat(temp);
                }
                String update = node.textWithSubstring("td:eq(1)", 0, 10);
                String author = StringUtils.match("\\[(.*?)\\]", title, 1);
                title = title.replaceFirst("\\[.*?\\]\\s*", "");
                return new Comic(SourceManager.SOURCE_EXHENTAI, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = StringUtils.format("https://exhentai.org/g/%s", cid);
        return new Request.Builder().url(url).header("Cookie", "ipb_member_id=2145630; ipb_pass_hash=f883b5a9dd10234c9323957b96efbd8e;").build();
    }

    @Override
    public void parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String update = body.textWithSubstring("#gdd > table > tbody > tr:eq(0) > td:eq(1)", 0, 10);
        String title = body.text("#gn");
        String intro = body.text("#gj");
        String author = body.text("#taglist > table > tbody > tr > td:eq(1) > div > a[id^=ta_artist]");
        String cover = body.attr("#gd1 > img", "src");
        comic.setInfo(title, cover, update, intro, author, true);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        String length = body.textWithSplit("#gdd > table > tbody > tr:eq(5) > td:eq(1)", " ", 0);
        int size = Integer.parseInt(length) % 40 == 0 ? Integer.parseInt(length) / 40 : Integer.parseInt(length) / 40 + 1;
        for (int i = 0; i != size; ++i) {
            list.add(0, new Chapter("Ch" + i, String.valueOf(i)));
        }
        return list;
    }

    @Override
    public Request getRecentRequest(int page) {
        String url = StringUtils.format("https://exhentai.org/?page=%d", (page - 1));
        return new Request.Builder().url(url).header("Cookie", "ipb_member_id=2145630; ipb_pass_hash=f883b5a9dd10234c9323957b96efbd8e").build();
    }

    @Override
    public List<Comic> parseRecent(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        for (Node node : body.list("table.itg > tbody > tr[class^=gtr]")) {
            String cid = node.hrefWithSubString("td:eq(2) > div > div:eq(2) > a", 23, -2);
            String title = node.text("td:eq(2) > div > div:eq(2) > a");
            String cover = node.src("td:eq(2) > div > div:eq(0) > img");
            if (cover == null) {
                String temp = node.textWithSubstring("td:eq(2) > div > div:eq(0)", 19).split("~", 2)[0];
                cover = "https://exhentai.org/".concat(temp);
            }
            String update = node.textWithSubstring("td:eq(1)", 0, 10);
            String author = StringUtils.match("\\[(.*?)\\]", title, 1);
            title = title.replaceFirst("\\[.*?\\]\\s*", "");
            list.add(new Comic(SourceManager.SOURCE_EXHENTAI, cid, title, cover, update, author));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("https://exhentai.org/g/%s?p=%s", cid, path);
        return new Request.Builder().url(url).header("Cookie", "ipb_member_id=2145630; ipb_pass_hash=f883b5a9dd10234c9323957b96efbd8e;").build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        Node body = new Node(html);
        int count = 0;
        for (Node node : body.list("#gdt > div > div > a")) {
            list.add(new ImageUrl(++count, node.attr("href"), true));
        }
        return list;
    }

    @Override
    public Request getLazyRequest(String url) {
        return new Request.Builder().url(url).header("Cookie", "ipb_member_id=2145630; ipb_pass_hash=f883b5a9dd10234c9323957b96efbd8e").build();
    }

    @Override
    public String parseLazy(String html, String url) {
        return new Node(html).attr("#img", "src");
    }
*/
}
