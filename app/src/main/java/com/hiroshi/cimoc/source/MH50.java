package com.hiroshi.cimoc.source;

import android.util.Pair;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Lists;
import com.hiroshi.cimoc.App;
import com.hiroshi.cimoc.manager.PreferenceManager;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.parser.MangaCategory;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.NodeIterator;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.parser.UrlFilter;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.DecryptionUtils;
import com.hiroshi.cimoc.utils.HttpUtils;
import com.hiroshi.cimoc.utils.StringUtils;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import javax.annotation.Nullable;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by WinterWhisper on 2019/2/25.
 */
public class MH50 extends MangaParser {

    public static final int TYPE = 80;
    public static final String DEFAULT_TITLE = "漫画堆";
    public static final String  baseUrl = "https://m.manhuadai.com";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public MH50(Source source) {
        init(source, new Category());
    }

    @Override
    public Request getSearchRequest(String keyword, int page) {
        if (page == 1) {
            String url = StringUtils.format(baseUrl+"/search/?keywords=%s&page=%d", keyword, page);
            return HttpUtils.getSimpleMobileRequest(url);
        }
        return null;
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list(".UpdateList > .itemBox")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.hrefWithSplit(".itemTxt > a", 1);
                String title = node.text(".itemTxt > a");
                String cover = node.src(".itemImg > a > img");
                if (cover.startsWith("//")) cover = "https:" + cover;
                String update = node.text(".itemTxt > p.txtItme:eq(3)");
                String author = node.text(".itemTxt > p.txtItme:eq(1)");
                return new Comic(TYPE, cid, title, cover, update, author);
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return StringUtils.format(baseUrl + "/manhua/%s/", cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter(baseUrl));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = StringUtils.format(baseUrl + "/manhua/%s/", cid);
        return HttpUtils.getSimpleMobileRequest(url);
    }

    @Override
    public Comic parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String intro = body.text("#full-des");
        String title = body.text("#comicName");
        String cover = body.src("#Cover > img");
        if (cover.startsWith("//")) cover = "https:" + cover;
        String author = body.text(".Introduct_Sub > .sub_r > .txtItme:eq(0)");
        String update = body.text(".Introduct_Sub > .sub_r > .txtItme:eq(4)");
        boolean status = isFinish(body.text(".Introduct_Sub > .sub_r > .txtItme:eq(2) > a:eq(3)"));
        comic.setInfo(title, cover, update, intro, author, status);
        return comic;
    }

    @Override
    public List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) {
        List<Chapter> list = new LinkedList<>();
        Node body = new Node(html);
        int i=0;
        for (Node node : body.list(".chapter-warp > ul > li > a")) {
            String title = node.text();
            String path = StringUtils.split(node.href(), "/", 3);
            list.add(new Chapter(Long.parseLong(sourceComic + "000" + i++), sourceComic, title, path));
        }

        return Lists.reverse(list);
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format(baseUrl + "/manhua/%s/%s", cid, path);
        return HttpUtils.getSimpleMobileRequest(url);
    }

    private final String[] server = {
            "https://mhcdn.manhuazj.com",
            "https://manga8.mlxsc.com",
            "https://manga9.mlxsc.com",
            "https://img01.eshanyao.com",
            "https://imgdm.eshanyao.com/"
    };

    @Nullable
    private String decrypt(String code) {
        String key = App.getPreferenceManager().getString(PreferenceManager.PREFERENCES_MH50_KEY_MSG, "KA58ZAQ321oobbG8");
        String iv = App.getPreferenceManager().getString(PreferenceManager.PREFERENCES_MH50_IV_MSG, "A1B2C3DEF1G321o8");
        try {
            return DecryptionUtils.aesDecrypt(code, key, iv);
        } catch (Exception e) {
            return null;
        }
    }

    //根据文件名获取图片url，参考common.js中getChapterImage函数
    private String getImageUrlByKey(String key, String domain, String chapter) {
        if (key.startsWith("http://images.dmzj.com")) {
            try {
                return "https://imgdm.eshanyao.com/showImage.php?url=" + key;
            } catch (Exception e) {
                return null;
            }
        }
        if (Pattern.matches("\\^[a-z]//i", key)) {
            try {
                return domain + "/showImage.php?url=" + URLEncoder.encode("https://images.dmzj.com/" + key, "utf-8");
            } catch (Exception e) {
                return null;
            }
        }
        if (key.startsWith("http") || key.startsWith("ftp")) {
            return key;
        }
        return domain + "/" + chapter + key;
    }

    @Override
    public List<ImageUrl> parseImages(String html,Chapter chapter) {
        List<ImageUrl> list = new LinkedList<>();

        //该章节的所有图片url，aes加密
        String arrayStringCode = decrypt(StringUtils.match("var chapterImages =\\s*\"(.*?)\";", html, 1));
        if (arrayStringCode == null) return list;
        JSONArray imageList = JSONArray.parseArray(arrayStringCode);

        //章节url，用于拼接最终的图片url
        String chapterPath = StringUtils.match("var chapterPath = \"([\\s\\S]*?)\";", html, 1);

        int imageListSize = imageList.size();
        for (int i = 0; i != imageListSize; ++i) {
            String key = imageList.getString(i);
            String imageUrl = getImageUrlByKey(key, server[0], chapterPath);
            if (imageUrl != null && imageUrl.contains("images.dmzj.com")) {
                imageUrl = imageUrl.replace("%", "%25");
            }
            Long comicChapter = chapter.getId();
            Long id = Long.parseLong(comicChapter + "000" + i);
            list.add(new ImageUrl(id, comicChapter, i + 1, imageUrl, false));
        }
        return list;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        return new Node(html).text(".Introduct_Sub > .sub_r > .txtItme:eq(4)");
    }

    @Override
    public List<Comic> parseCategory(String html, int page) {
        List<Comic> list = new LinkedList<>();
        Node body = new Node(html);
        int totalPage = Integer.parseInt(body.attr("#total-page", "value"));
        if (page <= totalPage) {
            for (Node node : body.list("#comic-items > li")) {
                String cid = node.hrefWithSplit("a.ImgA", 1);
                String title = node.text("a.txtA");
                String cover = node.src("a.ImgA img");
                if (cover.startsWith("//")) cover = "https:" + cover;
                String update = node.text(".info");
                list.add(new Comic(TYPE, cid, title, cover, update, null));
            }
        }
        return list;
    }

    private static class Category extends MangaCategory {

        @Override
        public boolean isComposite() {
            return true;
        }

        @Override
        public String getFormat(String... args) {
            String path = args[CATEGORY_SUBJECT].concat(" ").concat(args[CATEGORY_AREA]).concat(" ")
                    .concat(args[CATEGORY_READER]).concat(" ").concat(args[CATEGORY_YEAR]).concat(" ")
                    .concat(args[CATEGORY_PROGRESS]).trim();
            String finalPath;
            if (path.isEmpty()) {
                finalPath = StringUtils.format(baseUrl + "/list/");
            } else {
                finalPath = StringUtils.format(baseUrl + "/list/%s/?page=%%d", path).replaceAll("\\s+", "-");
            }
            return finalPath;
        }

        @Override
        public List<Pair<String, String>> getSubject() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("热血", "rexue"));
            list.add(Pair.create("冒险", "maoxian"));
            list.add(Pair.create("玄幻", "xuanhuan"));
            list.add(Pair.create("搞笑", "gaoxiao"));
            list.add(Pair.create("恋爱", "lianai"));
            list.add(Pair.create("宠物", "chongwu"));
            list.add(Pair.create("新作", "xinzuo"));
            list.add(Pair.create("神魔", "shenmo"));
            list.add(Pair.create("竞技", "jingji"));
            list.add(Pair.create("穿越", "chuanyue"));
            list.add(Pair.create("漫改", "mangai"));
            list.add(Pair.create("霸总", "bazong"));
            list.add(Pair.create("都市", "dushi"));
            list.add(Pair.create("武侠", "wuxia"));
            list.add(Pair.create("社会", "shehui"));
            list.add(Pair.create("古风", "gufeng"));
            list.add(Pair.create("恐怖", "kongbu"));
            list.add(Pair.create("东方", "dongfang"));
            list.add(Pair.create("格斗", "gedou"));
            list.add(Pair.create("魔法", "mofa"));
            list.add(Pair.create("轻小说", "qingxiaoshuo"));
            list.add(Pair.create("魔幻", "mohuan"));
            list.add(Pair.create("生活", "shenghuo"));
            list.add(Pair.create("欢乐向", "huanlexiang"));
            list.add(Pair.create("励志", "lizhi"));
            list.add(Pair.create("音乐舞蹈", "yinyuewudao"));
            list.add(Pair.create("科幻", "kehuan"));
            list.add(Pair.create("美食", "meishi"));
            list.add(Pair.create("节操", "jiecao"));
            list.add(Pair.create("神鬼", "shengui"));
            list.add(Pair.create("爱情", "aiqing"));
            list.add(Pair.create("校园", "xiaoyuan"));
            list.add(Pair.create("治愈", "zhiyu"));
            list.add(Pair.create("奇幻", "qihuan"));
            list.add(Pair.create("仙侠", "xianxia"));
            list.add(Pair.create("运动", "yundong"));
            list.add(Pair.create("动作", "dongzuo"));
            list.add(Pair.create("日更", "rigeng"));
            list.add(Pair.create("历史", "lishi"));
            list.add(Pair.create("推理", "tuili"));
            list.add(Pair.create("悬疑", "xuanyi"));
            list.add(Pair.create("修真", "xiuzhen"));
            list.add(Pair.create("游戏", "youxi"));
            list.add(Pair.create("战争", "zhanzheng"));
            list.add(Pair.create("后宫", "hougong"));
            list.add(Pair.create("职场", "zhichang"));
            list.add(Pair.create("四格", "sige"));
            list.add(Pair.create("性转换", "xingzhuanhuan"));
            list.add(Pair.create("伪娘", "weiniang"));
            list.add(Pair.create("颜艺", "yanyi"));
            list.add(Pair.create("真人", "zhenren"));
            list.add(Pair.create("杂志", "zazhi"));
            list.add(Pair.create("侦探", "zhentan"));
            list.add(Pair.create("萌系", "mengxi"));
            list.add(Pair.create("耽美", "danmei"));
            list.add(Pair.create("百合", "baihe"));
            list.add(Pair.create("西方魔幻", "xifangmohuan"));
            list.add(Pair.create("机战", "jizhan"));
            list.add(Pair.create("宅系", "zhaixi"));
            list.add(Pair.create("忍者", "renzhe"));
            list.add(Pair.create("萝莉", "luoli"));
            list.add(Pair.create("异世界", "yishijie"));
            list.add(Pair.create("吸血", "xixie"));
            list.add(Pair.create("其他", "qita"));
            return list;
        }

        @Override
        public boolean hasArea() {
            return true;
        }

        @Override
        public List<Pair<String, String>> getArea() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("日本", "riben"));
            list.add(Pair.create("大陆", "dalu"));
            list.add(Pair.create("香港", "hongkong"));
            list.add(Pair.create("台湾", "taiwan"));
            list.add(Pair.create("欧美", "oumei"));
            list.add(Pair.create("韩国", "hanguo"));
            list.add(Pair.create("其他", "qita"));
            return list;
        }

        @Override
        public boolean hasReader() {
            return true;
        }

        @Override
        public List<Pair<String, String>> getReader() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("儿童漫画", "ertong"));
            list.add(Pair.create("少年漫画", "shaonian"));
            list.add(Pair.create("少女漫画", "shaonv"));
            list.add(Pair.create("青年漫画", "qingnian"));
            return list;
        }

        @Override
        public boolean hasProgress() {
            return true;
        }

        @Override
        public List<Pair<String, String>> getProgress() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("连载", "lianzai"));
            list.add(Pair.create("完结", "wanjie"));
            return list;
        }

        @Override
        protected boolean hasYear() {
            return true;
        }

        @Override
        protected List<Pair<String, String>> getYear() {
            List<Pair<String, String>> list = new ArrayList<>();
            list.add(Pair.create("全部", ""));
            list.add(Pair.create("2000年前", "2000nianqian"));
            list.add(Pair.create("2001年", "2001nian"));
            list.add(Pair.create("2002年", "2002nian"));
            list.add(Pair.create("2003年", "2003nian"));
            list.add(Pair.create("2004年", "2004nian"));
            list.add(Pair.create("2005年", "2005nian"));
            list.add(Pair.create("2006年", "2006nian"));
            list.add(Pair.create("2007年", "2007nian"));
            list.add(Pair.create("2008年", "2008nian"));
            list.add(Pair.create("2009年", "2009nian"));
            list.add(Pair.create("2010年", "2010nian"));
            list.add(Pair.create("2011年", "2011nian"));
            list.add(Pair.create("2012年", "2012nian"));
            list.add(Pair.create("2013年", "2013nian"));
            list.add(Pair.create("2014年", "2014nian"));
            list.add(Pair.create("2015年", "2015nian"));
            list.add(Pair.create("2016年", "2016nian"));
            list.add(Pair.create("2017年", "2017nian"));
            list.add(Pair.create("2018年", "2018nian"));
            list.add(Pair.create("2019年", "2019nian"));
            list.add(Pair.create("2020年", "2020nian"));
            return list;
        }
    }

    @Override
    public Headers getHeader() {
        return Headers.of("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36");
    }
}
