package com.hiroshi.cimoc.parser;

import android.net.Uri;

import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.source.DM5;
//import com.hiroshi.cimoc.source.HHSSEE;
import com.hiroshi.cimoc.source.IKanman;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.List;

import okhttp3.Headers;
import okhttp3.Request;

/**
 * Created by Hiroshi on 2016/8/22.
 */
public interface Parser {

    /**
     * 搜索页的 HTTP 请求
     *
     * @param keyword 关键字
     * @param page    页码
     */
    Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException, Exception;

    /**
     * 获取搜索结果迭代器，这里不直接解析成列表是为了多图源搜索时，不同图源漫画穿插的效果
     *
     * @param html 页面源代码
     * @param page 页码，可能对于一些判断有用
     */
    SearchIterator getSearchIterator(String html, int page) throws JSONException;

    /**
     * 详情页的 HTTP 请求
     *
     * @param cid 漫画 ID
     */
    Request getInfoRequest(String cid);

    /**
     * 解析详情
     *
     * @param html  页面源代码
     * @param comic 漫画实体类，需要设置其中的字段
     */
    Comic parseInfo(String html, Comic comic) throws UnsupportedEncodingException;


    /**
     * 章节列表的 HTTP 请求，若在 {@link #parseInfo} 中可以解析出章节列表，返回 null，代表不用再次解析
     *
     * @param html 详情页面源代码，与 {@link #parseInfo} 中的第一个参数相同
     * @param cid  漫画 ID
     * @see MangaParser#getChapterRequest
     */
    Request getChapterRequest(String html, String cid);

    /**
     * 解析章节列表
     *
     * @param html 页面源代码
     */
    List<Chapter> parseChapter(String html) throws JSONException;

    /**
     * 解析章节列表，新增函数
     *
     *  使用了Lists.reverse(list)后要把TYPE加入interpretationUtils的isReverseOrder函数
     *
     * @param html 页面源代码
     * @param comic 漫画名
     */
    List<Chapter> parseChapter(String html, Comic comic, Long sourceComic) throws JSONException;

    /**
     * 图片列表的 HTTP 请求
     *
     * @param cid  漫画 ID
     * @param path 章节路径
     */
    Request getImagesRequest(String cid, String path);

    /**
     * 解析图片列表，若为惰性加载，则 {@link ImageUrl#lazy} 为 true
     * 惰性加载的情况，一次性不能拿到所有图片链接，例如网站使用了多次异步请求 {@link DM5#parseImages}，或需要跳转到不同页面
     * 才能获取 {@link HHSSEE#parseImages}，这些情况一般可以根据页码构造出相应的请求链接，到阅读时再解析
     * 支持多个链接 {@link ImageUrl#urls}，例如 {@link IKanman#parseImages}
     *
     * @param html 页面源代码
     */
    List<ImageUrl> parseImages(String html) throws Manga.NetworkErrorException, JSONException;

    /**
     * 新增函数
     * @param html 页面源代码
     * @param chapter 漫画章节
     */
    List<ImageUrl> parseImages(String html,Chapter chapter) throws Manga.NetworkErrorException, JSONException;
    /**
     * 图片惰性加载的 HTTP 请求
     *
     * @param url 请求链接
     */
    Request getLazyRequest(String url);

    /**
     * 解析图片链接
     *
     * @param html 页面源代码
     * @param url  请求链接，可能需要其中的参数
     */
    String parseLazy(String html, String url);

    /**
     * 检查更新的 HTTP 请求，一般与 {@link #getInfoRequest} 相同
     *
     * @param cid 漫画 ID
     */
    Request getCheckRequest(String cid);

    /**
     * 解析最后更新时间，用于与原来的比较，一般与 {@link #parseInfo} 获取 {@link Comic#update} 字段的方式相同
     *
     * @param html 页面源代码
     */
    String parseCheck(String html);

    /**
     * 获取漫画分类
     *
     * @see Category
     */
    Category getCategory();

    /**
     * 获取分类的 HTTP 请求
     *
     * @param format 格式化字符串，包含一个 %d 用于页码
     * @param page   页码
     */
    Request getCategoryRequest(String format, int page);

    /**
     * 解析分类
     *
     * @param html 页面源代码
     * @param page 页面，可能对于一些判断有用
     */
    List<Comic> parseCategory(String html, int page);

    /**
     * 获取图源标题，为了方便强行塞进来的
     */
    String getTitle();

    /**
     * 获取下载图片时的 HTTP 请求头，一般用来设置 Referer 和 Cookie
     */
    Headers getHeader();

    Headers getHeader(String url);

    Headers getHeader(List<ImageUrl> list);

    /*
     * 设置当前漫画url，以便分享使用。
     */
    String getUrl(String cid);

    /**
     * 检测host是否属于改漫画
     */
    boolean isHere(Uri uri);

    /**
     * 根据uri返回comic id
     */
    String getComicId(Uri uri);
}
