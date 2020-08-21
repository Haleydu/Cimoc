package com.hiroshi.cimoc.source;

import android.annotation.SuppressLint;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

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
import com.hiroshi.cimoc.ui.activity.ResultActivity;
import com.hiroshi.cimoc.utils.DecryptionUtils;
import com.hiroshi.cimoc.utils.LogUtil;
import com.hiroshi.cimoc.utils.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Headers;
import okhttp3.Request;

import static com.hiroshi.cimoc.utils.DecryptionUtils.desDecrypt;

/**
 * Created by Haleyd on 2020/8/7.
 */

public class Ohmanhua extends MangaParser {

    public static final int TYPE = 71;
    public static final String DEFAULT_TITLE = "oh漫画";
    public static final String baseUrl = "https://www.ohmanhua.com";
    private static final String serverUrl = "https://img.ohmanhua.com/comic/";

    public Ohmanhua(Source source) {
        init(source, null);
    }

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws UnsupportedEncodingException {
        String url = "";
        if (page == 1) {
            url = StringUtils.format(baseUrl+"/search?searchString=%s", keyword);
        }
        return new Request.Builder().url(url).build();
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) {
        Node body = new Node(html);
        return new NodeIterator(body.list("dl.fed-deta-info")) {
            @Override
            protected Comic parse(Node node) {
                String cid = node.href("dd > h1 > a");
                String title = node.text("dd > h1 > a");
                String cover = node.attr("dt > a", "data-original");
                String author = node.text("dd > ul > li:eq(3)");
                String update = node.text("dd > ul > li:eq(4)");

                if (!author.contains("作者")){
                    author = update;
                }
                if (!update.contains("更新")){
                    update = node.text("dd > ul > li:eq(5)");
                }
                //Log.d("getSearchIterator",cid + ","+title+ "," + cover + ","+author +","+update);
                return new Comic(TYPE, cid, title, cover,
                        update.replace("更新",""),
                        author.replace("作者",""));
            }
        };
    }

    @Override
    public String getUrl(String cid) {
        return baseUrl+cid;
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("www.ohmanhua.com"));
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = baseUrl + cid;
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) throws UnsupportedEncodingException {
        Node body = new Node(html);
        String title = body.text("dl.fed-deta-info > dd > h1");
        String cover = body.attr("dl.fed-deta-info > dt > a","data-original");
        String intro = body.text("div.fed-tabs-boxs > div > p");
        String statusStr = body.text("dl.fed-deta-info > dd > ul > li:eq(0)");
        String author = body.text("dl.fed-deta-info > dd > ul > li:eq(1)");
        String update = body.text("dl.fed-deta-info > dd > ul > li:eq(2)");

        if (!statusStr.contains("状态")){
            statusStr = author;
        }
        if (!author.contains("作者")){
            author = update;
        }
        if (!update.contains("更新")){
            update = body.text("dl.fed-deta-info > dd > ul > li:eq(3) > a");
        }
        boolean status = isFinish(statusStr.replace("状态",""));

        //Log.d("parseInfo",status + ","+title+ "," + cover + ","+author +","+update + ","+intro);
        comic.setInfo(title, cover, update.replace("更新",""), intro, author.replace("作者",""), status);
    }

    @Override
    public List<Chapter> parseChapter(String html) {
        List<Chapter> list = new LinkedList<>();
        for (Node node : new Node(html).list("div:not(.fed-hidden) > div.all_data_list > ul.fed-part-rows a")) {
            String title = node.attr("title");
            String path = node.href("a");
            //Log.d("parseChapter",title+","+path);
            list.add(new Chapter(title, path));
        }
        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = baseUrl + path;
        return new Request.Builder().url(url).build();
    }

    @SuppressLint("NewApi")
    @Override
    public List<ImageUrl> parseImages(String html) {
        List<ImageUrl> list = new LinkedList<>();
        String encodedData = StringUtils.match("C_DATA=\\'(.+?)\\'", html, 1);
        if (encodedData != null) {
            try {
                //"|SEPARATER|"

                String id1="dlSxHGdzZuE8HuExLyP0Muc2NWM4ZmIzZ4giNvngHCAgHCAgHGZhciB1aFSzVFhkQmVuayziTl9BL0PwcE3MQEIhT0VJQj3SNXQZLmQOV0haUFIXVTMWQzjwR19RPlVXeDBWa4M1ZUdnQSLxbSQNQlQxTkV2bEr5WjMNbU9HY0QWLU3SWjdUL4Q3YyBvUkEyaSdNRE9ZTjVjQFPvUmMiVkVxVmkzQz3SRTIWVTh2YyBRQU0XdyMNQzZSR19jRjqxTmkkVySwTjdFUFVDNW0XbkEzU4r1MWM9dEQMejIaYjdWT0HyZSdaLGcvZW0qMU3STTMVaykVTVc0Uj0YRmkUayB3Wl02LVLwQTMQVTS9VuIyekSWZ1kiakk4TuSnWVZYcFhkbE9OZSdnc4SpQl0jblQETjdaVE0YZEkMQTSPUzVWTFPvRkBQVU9vZFrReSZFZGSQLzISTjVWclSXcyBaLSIvUl0jR4IFWmIOVykoYl3qbV9UZErVQzZFU0VRdFMoPkSkRSIwTkUwcVQEQkIMVyEyVUZaMj0XMXdWLmBaYTBFQlQHPkQiVXBUYzZaMSEyZF0hQ1hWVUdnTlM5TkZNQVZTV49nU0SrcDZjQFhyWuBteVIYPXhVQzZDVl9JQj8EVk9QaykxZScwMV9Vd1kTRSIvUVQaV0MUQmMhRE9IVF9CRE8XMWkVV0UyY0dNbSExaGhQUyjwY0ZnRkZEPjMkQ09JWVZCbkVraySULUzxY0hWMlIXLUhTV1g4UkVNb4VFQkMjQUZCYjQFQlLyVkSNLDEvTzVRWU8WRkBZakHvVl9nUVIXWuBZa09ZYlq0TSdFcGBUVU9GTTBaMVQscDZueXQCYjZaV4VVdEVNQXQpU0QFTkZVcFhuQFqwVShjLkdUTuZXVkYzTDMvdWVTdSIWbVZQTTBad0YyZEdWLz3GY4wvaz3qZGkiV4hGVSQNUknyVjkiblqvUyMpeFQpUkMMWEjiNvngHCAgHCAgHGZhciB1dFYxOV8fY4QkY1I3cGPnd1QlLRvgP1I3cGQtRkLsZW3uKjIhc4U4MC3vYXIyZRh1aFSzVFhkQmVuaxjsdF8TdGI9blcnP1I3cGQtRkLsZW3uKkVzZug9JTqJHCAgHCAgHCBkdlSqJGdzZuH9Nv";
                String wtf1="123456789cvfrtgh";
                String whatTheFuck="NjA3T1pNMDBaOUJFNE9tY2tOWXZPbWU3VGI1KzRBeWx0VkcweGhDS2lTNFdrNUxlKzZCNmJHcTV1NEZGT3dyc0pRQ3hWNHJYNEdDd0RsbVUrVytGNEI2VU8xc0REMWw3NGFEKzdJK2NyeW1qNGFPeC9mWnQ4Skw5citDMzBZbGVOR3dWZ0w0eml5NEM3Uk9UMW5RMXJyTk0yZmx1S1E3QU1iV2szQVgybjYvN1hYVXphelJOdWhsakFmdndDNGZTMXdIME1PSEVLd0JPQUJpdkRxVFdqQ3BENEVraWs0Z0RpRmdKbFZrOW9jbnlmZTdKUGFFSURtcjBQeHRqNU1qTDFRMW13UFZ6MW5wV2pZa0FFdHBTbUpTcFZ4Q3dmaGxVUGhNczNVNEVSWjhSQmp6dDhsZ0oyRXAxUGFCVjJFODVZQk9rdW15ZUwySHRpQTZWSTFsaHJITjBHOW5iUWU3cWNlQ2hxQS91cVhJVDBCeGZJYVBnUmk1T1M2cXV6bW1HSWx6RUNoeFFSdEFBbDFFc3VQN010OERYOVJPYjR0VjhQRWZ0YkZYbk5LWFppTUJGM0Z5Tnp6cytBbFZWeUtENEtkSTFNVUphcDk1TXd2WTN6WVV4L3pueStRVmVQM0ZwV3dGV3NGclpkNldybWhGTTNRZ3VIbnk0S3kxdkRSMXI";
                String C_DATA="NGFnd2pPWGI4R0t5VDkyekdxdjcrQ3ZnQWphQnNBZGxSQ25HS1pmeWZBUjgraE0zbDNSd2pMZ1N4ZmtWWjBJVStwZS9uZkJiMzFiUnVmN1JtMHo4N3FycUZGNm9EOEpKTCsyMVVHSjBTUjJERU80VEpETUliVGN4a2hIVW53VEhINmczOTQ4azg1ZlduNm1HMzNMdUJBK3lLVXM1dUNNODFWdTlwY2Y1TW1OT1BvWkNsS3c3Qyt5TlF5aUU5aVpoTm9oSE1HYldidkVzM240cEdVbDF4Z2F1anE5cXdjNWRPbllMaldHSVVDdE1CWUIvamV0cEdwQ0ZMT2YwbEY3emhTaTY4T0NoNXY5M251ZnN6SnhoMWxMeEIzSmlkU2s5QWl6VzNRVHZaZzc1N3FJZDhWZkFEbnA1djV6YlI5R1ViMG42UDcxYnlhbllYVEhZTitDVkVxeXVGOHNJTFRLR2hFaEdZblE2UkE4ZjlzTXVLZWxqVllnTVZEaUJoSnlmbldsOVl0cEdob0k4bDFVTE95MkkxYkFsOXc5SkxuNWNtT3ZxUDFJU1ZRWXZNa0JJcm1hSkdBMjM5cy9zODUvUWlCSGc1b3VRRjlkNWFvSVRmWUlYaGFVRmplaDE3VmVTaFM1QU5waHY0cTBQV1lmbnR3bjBOb1pRU1BXbEhoRGhEbEZyTFBoZlRCQVZ5N25ZR25VUGEwRjUySmJwWW9zZUtJdHpVSTV1ZFZ1SnMvTEFKZ3NnWGVpcVBjK2pFRk9qMmxRbmt5emUvaVlKVkhCOFZ0NXhyOWxDMnBGY3NXWWVla3JtRytveTBQWU5GdzJvMG8yVjZtdlRDa0tCVEcvaVZSVW9qSXVYZkZPOGNKbEIvV1NoUjRnUjRPYUxrQmZYZVdxQ0UzMkNGNFdsQlkzb2RlMVhrb1V1UURhWWIrS3REMW1INTdjSjlEYUdVRWoxcFI0UTRRNVJheXo0WDB3UUZjdTUyQnAxRDJ0c1g3eVp4bG16bHB6bkk0dVI0ZTZFa0toQmJBcFU5cGRWMklveFpSaU00dW9rNjhhQitLL3dGam42YlJTdXZmaDdGenBBbFVNN08zMTZUL1RESHc1QU9MTGFrQmIwMTErR0tHS2ZMSnVFZE1MN0RML3l5UUwxWVdYUVFubjhDWVN2SWpBVEJIc3lSZzF3dTVwL1g3VldpVFRiNGdYNGpSb0JYYUNnK1Q1Yjk5Vmg1bmFtQkdRT3JqNVh3c25aaUFuaDhEVEl5SU1NMnJRWVJWS3B1OVBPMmhrZEJ1VnFTa1l1TGI0UjJkSmwzNFNxbloyeVRjTHJuSE5yRktxenl2Qm9CT3BEUFNGUjBKcG5mMW02dnBjSFZaV3hnL21LMkY3dTlvdU1Vd040Y2o3TUdjS1hSK3pKUnNwV3dGQlhsWDlGaUo5R3AvcTNtTTdCYklSSUI1UTdQNFpRcUxrV1VPYjEyTzhmbEFLeHBJcFBTWVJOODMvTmhtL2ZXZ0JEdEk2Q3dZenM1NTNrWHh4ZGFramZZRDRRNXdlcXl6eUtpdXp1TkFuRUZoanN3cmtGcStyMW1kNXhoeWxMSHd0OTVWaGpmclFNQWZ2NllIeFNBdXUyTVFZdEdOSGNTL3E2ZVZ4cXJWb1hrSm1FS3lDUlNnZmZkcTVkT2dzNTBpK2VUSVd3eUJqUStVeUlSYWlCSU9kY1ZiRmZuSlRvTmZ6bFBzWGUwRHQ0OVRqeXdGVVZLSXlMbDN4VHZIQ1pRZjFrb1VlSUVlRG1pNUFYMTNscWdoTjlnaGVGcFFXTjZIWHRWNUtGTGtBMm1HL2lyUTlaaCtlM0NmUTJobEJJOWFVZUVPRU9VV3NzK0Y5TUVCWEx1ZGdhZFE5cmJGKzhtY1paczVhYzV5T0xrZUh1aEhvUW0wRysyNmswQ2ZnT3ZRemNqelVBNXpCbUNzRTJFQ2MwNUpXbTRIR3dQWjNHNE1UOWdJdlNqTkJGWDFUa2FVTkdLZm1qbzdrQWZzaHV4WFBSbHhYQyt3eS84c2tDOVdGbDBFSjUvQW1FcnlJd0V3UjdNa1lOY0x1YWYxKzFWb2swMitJRitJMGFBVjJnb1BrK1cvZlZZZVoycGdSa0RxNCtWOExKMllnSjRmQTB5TWlERE5xMEdFVlNxYnZUenRvWkhRYmxha3BHTGkyK0VkblNaZCtFcXAyZHNrM0M2NXh6YXhTcXM4cndmUHBpUGppd04xRm9JR0w2UlNuSmF3cU5kZHlKMDJiK2xERjIrYUxYbVA4VlRhTmw4cmp3QXdjZkxBdjF0ZkVPL2FzNkovL0RkTVRHMDZNRWtzMnQrVCtHVUtpNUZsRG05ZGp2SDVRQ3NhU0tUMG1FVGZOL3pZWnYzMW9BUTdTT2dzR003T2VkNUY4Y1hXcEkzMkErRU9jSHFzczhpb3JzN2pRSnhCWVk3TUs1QmF2cTlabmVjWWNwU3g4TGZlVllZMzYwREFINyttQjhVZ0xydGpFR0xSSlBSd0MxK0g0dk5IQitXODkyaHhFbi9NS2JYS2RkZlpNYlRqdXF2dlJnRUJqQkl1QUord1pjT3F3YmlqNldNZnVXWUVoeTBwNmNEK3R6dEtPQm8xTlZGU2lNaTVkOFU3eHdtVUg5WktGSGlCSGc1b3VRRjlkNWFvSVRmWUlYaGFVRmplaDE3VmVTaFM1QU5waHY0cTBQV1lmbnR3bjBOb1pRU1BXbEhoRGhEbEZyTFBoZlRCQVZ5N25ZR25VUGEyeGZ2Sm5HV2JPV25PY2ppNUhoN29SNkVKdEJ2dHVwTkFuNERyME0zSTgxbW50SkNtYWxLMlJJU2xzVnlOWDJubzVySDFGWlpTdXI5VGJkNnNBOG5jRVBPL2ZWdWpiK0ZoWHlnMDR1YkhvWnd2c012L0xKQXZWaFpkQkNlZndKaEs4aU1CTUVlekpHRFhDN21uOWZ0VmFKTk52aUJmaU5HZ0Zkb0tENVBsdjMxV0htZHFZRVpBNnVQbGZDeWRtSUNlSHdOTWpJZ3d6YXRCaEZVcW03MDg3YUdSMEc1V3BLUmk0dHZoSFowbVhmaEtxZG5iSk53dXVjYzJzVXFyUEs4Q2RkN1NqUGRDT05wY3QxRUZXckU5REtSYmxBSWhRUU0xMTMyUDVOZ0VwUG1Ldm1zQWJFenhzM2gyM2hHT3ZjMGxmQXB2Zy9rNG9Hdk90Vnl0WnJXWjQvaGxDb3VSWlE1dlhZN3grVUFyR2tpazlKaEUzemY4MkdiOTlhQUVPMGpvTEJqT3pubmVSZkhGMXFTTjlnUGhEbkI2ckxQSXFLN080MENjUVdHT3pDdVFXcjZ2V1ozbkdIS1VzZkMzM2xXR04rdEF3QisvcGdmRklDNjdZeEJpMFMyQ1V0QzNpZFFLVkJPenNub1hHR1g3RGUvSEpqa2JMYkFqb0VnZHBEdW1GdUJGS2NBVmxpZUV0dytJMkJIQlF4c01kSE1jM2ZTR21uMWQ2emdKVzBWUlVvakl1WGZGTzhjSmxCL1dTaFI0Z1I0T2FMa0JmWGVXcUNFMzJDRjRXbEJZM29kZTFYa29VdVFEYVliK0t0RDFtSDU3Y0o5RGFHVUVqMXBSNFE0UTVSYXl6NFgwd1FGY3U1MkJwMUQydHNYN3laeGxtemxwem5JNHVSNGU2RWVoQ2JRYjdicVRRSitBNjlETnlQTlY5dThsWlVzMmtoaHVUQktZdEYwZG54T0phNTJPR1ZBWmV6Qll2RVh6OS9QbU5ZTjF4QWExS1RkTkptZ0JoeDQ4TDdETC95eVFMMVlXWFFRbm44Q1lTdklqQVRCSHN5Umcxd3U1cC9YN1ZXaVRUYjRnWDRqUm9CWGFDZytUNWI5OVZoNW5hbUJHUU9yajVYd3NuWmlBbmg4RFRJeUlNTTJyUVlSVktwdTlQTzJoa2RCdVZxU2tZdUxiNFIyZEpsMzRTcW5aMnlUY0xybkhOckZLcXp5dkRGZjdOZVRuNVZhYTJBWlcwbEtCNHZZZjdoRXh3bTRhb3REUDhQS1cwbFFoZWRybnM5elZXNkk3amlNYzlBTk50T2ZCK3p4NkpYU1VMQWxoZjhmbTZjUDRaUXFMa1dVT2IxMk84ZmxBS3hwSXBQU1lSTjgzL05obS9mV2dCRHRJNkN3WXpzNTUza1h4eGRha2pmWUQ0UTV3ZXF5enlLaXV6dU5BbkVGaGpzd3JrRnErcjFtZDV4aHlsTEh3dDk1VmhqZnJRTUFmdjZZSHhTQXV1Mk1RWXQyd3FVSGtnT0xmYjZoVkV1OCtGK0JxSUJkenFNNGpDeU1EaWc4S0xITjJGTm9Bb1RZOVJGRlBoc3hKY09qK1p4aGovR3FCakdxRXVVVm5LbXpkYWhWMVVWS0l5TGwzeFR2SENaUWYxa29VZUlFZURtaTVBWDEzbHFnaE45Z2hlRnBRV042SFh0VjVLRkxrQTJtRy9pclE5WmgrZTNDZlEyaGxCSTlhVWVFT0VPVVdzcytGOU1FQlhMdWRnYWRROXJiRis4bWNaWnM1YWM1eU9Ma2VIdWhIb1FtMEcrMjZrMENmZ092UXpjanpVL0J4ZXdKSmlPejFPM21jbFVRWlBsTFVaUGdXbmxibFN3cnZxczRpSzZkcXliQjhpT3hTVnVXWTYwQ0paN01ZdkMrd3kvOHNrQzlXRmwwRUo1L0FtRXJ5SXdFd1I3TWtZTmNMdWFmMSsxVm9rMDIrSUYrSTBhQVYyZ29QaytXL2ZWWWVaMnBnUmtEcTQrVjhMSjJZZ0o0ZkEweU1pREROcTBHRVZTcWJ2VHp0b1pIUWJsYWtwR0xpMitFZG5TWmQrRXFwMmRzazNDNjV4emF4U3FzOHJ3a0hNYXdneDUwMHJYTHJQeStUZ29US1Y2Tkh6THFOYkZvemIyMzlpQndlL1VKbzRPZGxmaXJJZlo4OWVML0FMWVdNeklNQmxPNFJEallVeTlQQXh2VFQrR1VLaTVGbERtOWRqdkg1UUNzYVNLVDBtRVRmTi96WVp2MzFvQVE3U09nc0dNN09lZDVGOGNYV3BJMzJBK0VPY0hxc3M4aW9yczdqUUp4QllZN01LNUJhdnE5Wm5lY1ljcFN4OExmZVZZWTM2MERBSDcrbUI4VWdMcnRqRUdMUjVDcy84V29WUktJTHhPYndwV29VNEI2dHkreEo3ZEVRa3hudVZjSHZWaEg5Q09nZ2MwKzU0K2pzbWlkRTJyTkxDZzBqZytjL2Y4dnU0blNESTlJc0pWRlNpTWk1ZDhVN3h3bVVIOVpLRkhpQkhnNW91UUY5ZDVhb0lUZllJWGhhVUZqZWgxN1ZlU2hTNUFOcGh2NHEwUFdZZm50d24wTm9aUVNQV2xIaERoRGxGckxQaGZUQkFWeTduWUduVVBhMnhmdkpuR1diT1duT2NqaTVIaDdvUjZFSnRCdnR1cE5BbjREcjBNM0k4MVdkT0VSR2NaaE9tRzJ0dTZNZUxDcVA0TkNrckZBbDVqSlF5K1ZTU2M0YzhRcTRxRVZ1NDc3VWVZNUVOZzR4aTh3dnNNdi9MSkF2VmhaZEJDZWZ3SmhLOGlNQk1FZXpKR0RYQzdtbjlmdFZhSk5OdmlCZmlOR2dGZG9LRDVQbHYzMVdIbWRxWUVaQTZ1UGxmQ3lkbUlDZUh3Tk1qSWd3emF0QmhGVXFtNzA4N2FHUjBHNVdwS1JpNHR2aEhaMG1YZmhLcWRuYkpOd3V1Y2Myc1VxclBLOEo0bFVNWmxsb0g5eHZxK2VxNjNyL1MxNksxUXN0NUIwdHlsazdKQjhTVnZzT0dhTkhPU2xsYlg3MThiS1RTYWhjN3NGbTBwejZOa3VQSzVIZStmak9JL2hsQ291UlpRNXZYWTd4K1VBckdraWs5SmhFM3pmODJHYjk5YUFFTzBqb0xCak96bm5lUmZIRjFxU045Z1BoRG5CNnJMUElxSzdPNDBDY1FXR096Q3VRV3I2dldaM25HSEtVc2ZDMzNsV0dOK3RBd0IrL3BnZkZJQzY3WXhCaTJQK0hxSkRTcHdLcUQ0L3dvZXF2YllDWWtaSDJHU3Z5bjNuaDlmRkRPTlN1aXg0TDRzeEUxcHZWQ0F0V0RKc1NRcDgyaXlmRXNJYU13QTNwU2R3eG1KVlJVb2pJdVhmRk84Y0psQi9XU2hSNGdSNE9hTGtCZlhlV3FDRTMyQ0Y0V2xCWTNvZGUxWGtvVXVRRGFZYitLdEQxbUg1N2NKOURhR1VFajFwUjRRNFE1UmF5ejRYMHdRRmN1NTJCcDFEMnRzWDd5WnhsbXpscHpuSTR1UjRlNkVlaENiUWI3YnFUUUorQTY5RE55UE5ZTlF2NjdPOHlFSTllaXBZWnhNclBBRW9kZmE3eWRQUW5yaU16Tkh1RldncXVHNTAxUEtTSnBneU5lSWdST1lWY0w3REwveXlRTDFZV1hRUW5uOENZU3ZJakFUQkhzeVJnMXd1NXAvWDdWV2lUVGI0Z1g0alJvQlhhQ2crVDViOTlWaDVuYW1CR1FPcmo1WHdzblppQW5oOERUSXlJTU0yclFZUlZLcHU5UE8yaGtkQnVWcVNrWXVMYjRSMmRKbDM0U3FuWjJ5VGNMcm5ITnJGS3F6eXZBK2huaVNEaHRoV3FyWHNLVEV0eVA5UnNNRzVDMXAwM1hqUklneG15V0YvWlhqVmxsRUxpMG9tazdyVVBTM2sxTHp4TGFrUEFLWXJpcWxSNEYvZEFVSVA0WlFxTGtXVU9iMTJPOGZsQUt4cElwUFNZUk44My9OaG0vZldnQkR0STZDd1l6czU1M2tYeHhkYWtqZllENFE1d2VxeXp5S2l1enVOQW5FRmhqc3dya0ZxK3IxbWQ1eGh5bExId3Q5NVZoamZyUU1BZnY2WUh4U0F1dTJNUVl0RWRsU0J0TWZLU3lhQS8yanJUZjZQZk9GS3l3UW44TXVwSHpnOEo0b1pZblpKajd1OGpoeTdOcVdDSlJvT1dPTDBReEJnVTNpajJ2TVJ1a3FGRkNuVDFVVktJeUxsM3hUdkhDWlFmMWtvVWVJRWVEbWk1QVgxM2xxZ2hOOWdoZUZwUVdONkhYdFY1S0ZMa0EybUcvaXJROVpoK2UzQ2ZRMmhsQkk5YVVlRU9FT1VXc3MrRjlNRUJYTHVkZ2FkUTlyYkYrOG1jWlpzNWFjNXlPTGtlSHVoSG9RbTBHKzI2azBDZmdPdlF6Y2p6WGtWODZGRlpXNTFxVnNzMjF4NkU2VXV3NERGb2NwV01aZkk1eVBObkxxZ3dXL2RnTVQzRDlleWlUd0pBTzhickRDK3d5Lzhza0M5V0ZsMEVKNS9BbUVyeUl3RXdSN01rWU5jTHVhZjErMVZvazAyK0lGK0kwYUFWMmdvUGsrVy9mVlllWjJwZ1JrRHE0K1Y4TEoyWWdKNGZBMHlNaURETnEwR0VWU3FidlR6dG9aSFFibGFrcEdMaTIrRWRuU1pkK0VxcDJkc2szQzY1eHpheFNxczhyd1JpNW1FSkkyVDgzM3ZSQm5UR3BnU0VscjloM05JTXZ0eWlpUS9DdVJ1U01OUGpZNFBaeVRjRDN0UmxmbUg1NzRFckp4L3ZSTmFtTEVHeWtzZm85aExqK0dVS2k1RmxEbTlkanZINVFDc2FTS1QwbUVUZk4velladjMxb0FRN1NPZ3NHTTdPZWQ1RjhjWFdwSTMyQStFT2NIcXNzOGlvcnM3alFKeEJZWTdNSzVCYXZxOVpuZWNZY3BTeDhMZmVWWVkzNjBEQUg3K21COFVnTHJ0akVHTFUrYUxHSHVYdFA4dEM2R0RGMHl1M1gvVlFWRUZtUzA1eVlTOGYwY0w3OE80R1ZuQlNqcmRjQ3lVUlg4QkFkc3orcnZBZ083aDRkQklOeGFNSkR5eTNWVkZTaU1pNWQ4VTd4d21VSDlaS0ZIaUJIZzVvdVFGOWQ1YW9JVGZZSVhoYVVGamVoMTdWZVNoUzVBTnBodjRxMFBXWWZudHduME5vWlFTUFdsSGhEaERsRnJMUGhmVEJBVnk3bllHblVQYTJ4ZnZKbkdXYk9Xbk9jamk1SGg3b1I2RUp0QnZ0dXBOQW40RHIwTTNJODFZeWdsQzA5ZGRKODMwQ3k5NGpvM2tQZzlhckJkc2k2RUcwQngxa0JHWVIwSlNHZDRKVlFLT3dTLzhXY29jUmdid3ZzTXYvTEpBdlZoWmRCQ2Vmd0poSzhpTUJNRWV6SkdEWEM3bW45ZnRWYUpOTnZpQmZpTkdnRmRvS0Q1UGx2MzFXSG1kcVlFWkE2dVBsZkN5ZG1JQ2VId05Naklnd3phdEJoRlVxbTcwODdhR1IwRzVXcEtSaTR0dmhIWjBtWGZoS3FkbmJKTnd1dWNjMnNVcXJQSzhBQTlUTThjcU11c2s3VXBGbVJlTWErQlgyQ3F4WFdac284SjdDSCtlQ3hQSVpydXpSN3RJZWRRZHVKK2V3MzdHM3BiSXFpQlFMc21NcmdvNjIxVzNzNC9obENvdVJaUTV2WFk3eCtVQXJHa2lrOUpoRTN6ZjgyR2I5OWFBRU8wam9MQmpPem5uZVJmSEYxcVNOOWdQaERuQjZyTFBJcUs3TzQwQ2NRV0dPekN1UVdyNnZXWjNuR0hLVXNmQzMzbFdHTit0QXdCKy9wZ2ZGSUM2N1l4QmkyOGhtTlZpTmg1eVJLa0RWV01PZzZLclpiKzZha0h6dEJJS1QyUW1QaE94VHZmaVI3Qk1ueStrMEluZEVreWZVdnFtN01jeU41NjBUU3lVTG5wcjgrS1ZSVW9qSXVYZkZPOGNKbEIvV1NoUjRnUjRPYUxrQmZYZVdxQ0UzMkNGNFdsQlkzb2RlMVhrb1V1UURhWWIrS3REMW1INTdjSjlEYUdVRWoxcFI0UTRRNVJheXo0WDB3UUZjdTUyQnAxRDJ0c1g3eVp4bG16bHB6bkk0dVI0ZTZFZWhDYlFiN2JxVFFKK0E2OUROeVBOWHZPeVJwOWtub0lCWU1JYitKQmtFRDQ0clpWZXZyQ1F5K2MwV05EcUNkcTh3WUgvTlhjRkx1UVVhRmVaV3FRejhMN0RML3l5UUwxWVdYUVFubjhDWVN2SWpBVEJIc3lSZzF3dTVwL1g3VldpVFRiNGdYNGpSb0JYYUNnK1Q1Yjk5Vmg1bmFtQkdRT3JqNVh3c25aaUFuaDhEVEl5SU1NMnJRWVJWS3B1OVBPMmhrZEJ1VnFTa1l1TGI0UjJkSmwzNFNxbloyeVRjTHJuSE5yRktxenl2QmlFNHNHenNvTkZEWUVKeFo0QThvVURua0kzaUl3S3FVUkpNaW56eEFNNGdUZVBpRmJrZE1rK1VNNHRZcTJOUVlVWWFjcGRzRW4zUGVMa2xSVmladGFQNFpRcUxrV1VPYjEyTzhmbEFLeHBJcFBTWVJOODMvTmhtL2ZXZ0JEdEk2Q3dZenM1NTNrWHh4ZGFramZZRDRRNXdlcXl6eUtpdXp1TkFuRUZoanN3cmtGcStyMW1kNXhoeWxMSHd0OTVWaGpmclFNQWZ2NllIeFNBdXUyTVFZdGUyVG50SlM2MVhLMWVVSmNxUnI3T0EvdlZvSXNqTW9XSHllT29sUWl5ZEVJVUVrcCt3cGdWSEd2YmxCUU90V3lnNjBBbi8vWEEySDRnblpoVHNFR0lWVVZLSXlMbDN4VHZIQ1pRZjFrb1VlSUVlRG1pNUFYMTNscWdoTjlnaGVGcFFXTjZIWHRWNUtGTGtBMm1HL2lyUTlaaCtlM0NmUTJobEJJOWFVZUVPRU9VV3NzK0Y5TUVCWEx1ZGdhZFE5cmJGKzhtY1paczVhYzV5T0xrZUh1aEhvUW0wRysyNmswQ2ZnT3ZRemNqelhXT3pydU9Rbit3ZGNwVzhwbWZyNWJtY0hMUE1SSkI5bkJUc1RwaVk5R3M4SW9yQzlZUXhEdk5UdytFU09VOU5qQyt3eS84c2tDOVdGbDBFSjUvQW1FcnlJd0V3UjdNa1lOY0x1YWYxKzFWb2swMitJRitJMGFBVjJnb1BrK1cvZlZZZVoycGdSa0RxNCtWOExKMllnSjRmQTB5TWlERE5xMEdFVlNxYnZUenRvWkhRYmxha3BHTGkyK0VkblNaZCtFcXAyZHNrM0M2NXh6YXhTcXM4cndtZDFYTHg4MmQyNjMwWHhWNlAvUGQ5V1RjSmRadnJTTWRvbW5wQWhvS0NGWGZ3bHM5T2dJMDZqWFVtaG54TnlIMW12WHFzQ3U5b1RtT25USmRNN0FxVCtHVUtpNUZsRG05ZGp2SDVRQ3NhU0tUMG1FVGZOL3pZWnYzMW9BUTdTT2dzR003T2VkNUY4Y1hXcEkzMkErRU9jSHFzczhpb3JzN2pRSnhCWVk3TUs1QmF2cTlabmVjWWNwU3g4TGZlVllZMzYwREFINyttQjhVZ0xydGpFR0xTY2RrUjk0b1ZxS2J0TTJMb2dBSXpVbTJrcDkyL2ZNYUtKSVNsa3dmbXNLYWRlNVhad0FUTkxXK2VyRldHUGxYU1BHT1l5WmgzRHZUUlB0Z0p3VkllbFZGU2lNaTVkOFU3eHdtVUg5WktGSGlCSGc1b3VRRjlkNWFvSVRmWUlYaGFVRmplaDE3VmVTaFM1QU5waHY0cTBQV1lmbnR3bjBOb1pRU1BXbEhoRGhEbEZyTFBoZlRCQVZ5N25ZR25VUGEwRjUySmJwWW9zZUtJdHpVSTV1ZFZzNlpVcE1pV3VVTXlFQUwrOGhsU1dmYy9nVHV4WGhWRGJZSnBlWS9HQTdrOVl4TzdjZUkzV1RqVkl2VGNKdGg2NE5MOVgxdk55bWZ3ZC9VZmVvL1J6VlZSVW9qSXVYZkZPOGNKbEIvV1NoUjRnUjRPYUxrQmZYZVdxQ0UzMkNGNFdsQlkzb2RlMVhrb1V1UURhWWIrS3REMW1INTdjSjlEYUdVRWoxcFI0UTRRNVJheXo0WDB3UUZjdTUyQnAxRDJ0c1g3eVp4bG16bHB6bkk0dVI0ZTZFZWhDYlFiN2JxVFFKK0E2OUROeVBOZi92MUJCSGFmcVVabGpuRjZMRGdWazFtUmhINUppR1F5RkpQTWdVQWVoSlRmSmVjZ25jNUhYU3BuZEEyc1E5ZjhMN0RML3l5UUwxWVdYUVFubjhDWVN2SWpBVEJIc3lSZzF3dTVwL1g3VldpVFRiNGdYNGpSb0JYYUNnK1Q1Yjk5Vmg1bmFtQkdRT3JqNVh3c25aaUFuaDhEVEl5SU1NMnJRWVJWS3B1OVBPMmhrZEJ1VnFTa1l1TGI0UjJkSmwzNFNxbloyeVRjTHJuSE5yRktxenl2Q2RIbzNLaVVkSzE3OHFzNS84SE9tSVBZQVVBWDZXN3BBNmFhTWZkVXVGSnBxbW81blpWMHJBYXEwdVU3S0h2eTk2aVBZaG4zenhva1BnZG5tK2FTa3ZQNFpRcUxrV1VPYjEyTzhmbEFLeHBJcFBTWVJOODMvTmhtL2ZXZ0JEdEk2Q3dZenM1NTNrWHh4ZGFramZZRDRRNXdlcXl6eUtpdXp1TkFuRUZoanN3cmtGcStyMW1kNXhoeWxMSHd0OTVWaGpmclFNQWZ2NllIeFNBdXUyTVFZdGQxMTBmYnR5SDlLLzZwVUhtSzY1Si9hYk96UlA2UHBvVWY0MFdMUkwxL3RrWmhHNzJOQjJGZG50U3NpdEFZRVBPanFlbGUxamQ1T1ZJZEhPUlI5dTU0eXlwOFNaaGJOTnIzR3QvektYR0ErVlJNYzJYMklUeFFteUpzTlZXdXMvK0N1SmI0TFRnOEZHdkhKdytRMUt3Zz09";
                String urls__direct="aHR0cDovL2ltZy5tYW5odWFkYW8uY24vYXBpL3YxL2Jvb2tjZW50ZXIvc2NlbmV0aHVtYm5haWwvMS8zMzg0OTcxL1VfZDczOWMwYjktNGJkMy00ODA4LWJiNGUtMjFjNWYxNGY5OGEzLnBuZ3xTRVBBUkFURVJ8aHR0cDovL2ltZy5tYW5odWFkYW8uY24vYXBpL3YxL2Jvb2tjZW50ZXIvc2NlbmV0aHVtYm5haWwvMC8zMzg0OTcxL1VfMF8wX2M0OTU1ZTRkLTg1MTAtNGU5OC04YzM3LTZkODVjOWVmM2ZlZS5wbmd8U0VQQVJBVEVSfGh0dHA6Ly9pbWcubWFuaHVhZGFvLmNuL2FwaS92MS9ib29rY2VudGVyL3NjZW5ldGh1bWJuYWlsLzAvMzM4NDk3MS9VXzBfMV82Yjk1OTUwZi1jN2Y2LTQ3YTgtOTcyYS0zNjczZWY5OGViN2UucG5nfFNFUEFSQVRFUnxodHRwOi8vaW1nLm1hbmh1YWRhby5jbi9hcGkvdjEvYm9va2NlbnRlci9zY2VuZXRodW1ibmFpbC8xLzMzODQ5NzEvVV8wXzJfM2Q1YWEzMzAtY2MyNS00YmU3LTlhMDItYmMxNzYxZTIyZTdmLnBuZ3xTRVBBUkFURVJ8aHR0cDovL2ltZy5tYW5odWFkYW8uY24vYXBpL3YxL2Jvb2tjZW50ZXIvc2NlbmV0aHVtYm5haWwvMC8zMzg0OTcxL1VfMF8wXzA1MDNiOTkwLTg1NzMtNGI2NC1hZTRkLTEyYmZjM2EyNTM5Zi5wbmd8U0VQQVJBVEVSfGh0dHA6Ly9pbWcubWFuaHVhZGFvLmNuL2FwaS92MS9ib29rY2VudGVyL3NjZW5ldGh1bWJuYWlsLzAvMzM4NDk3MS9VXzBfMV83NGMzZWIxYi1lMTkxLTQ2MTEtYWUwMy01N2NkNThkYzUzMTkucG5nfFNFUEFSQVRFUnxodHRwOi8vaW1nLm1hbmh1YWRhby5jbi9hcGkvdjEvYm9va2NlbnRlci9zY2VuZXRodW1ibmFpbC8xLzMzODQ5NzEvVV8wXzJfZTFjN2I4NjItMWM5ZC00MzAxLWIwNjQtNjFmOTdhMzNiYTU1LnBuZ3xTRVBBUkFURVJ8aHR0cDovL2ltZy5tYW5odWFkYW8uY24vYXBpL3YxL2Jvb2tjZW50ZXIvc2NlbmV0aHVtYm5haWwvMC8zMzg0OTcxL1VfMF8wXzdhYzY3NTI4LTkwNWItNDI0NC1hMmU0LTJiZTY0Y2M4Y2QxZC5wbmd8U0VQQVJBVEVSfGh0dHA6Ly9pbWcubWFuaHVhZGFvLmNuL2FwaS92MS9ib29rY2VudGVyL3NjZW5ldGh1bWJuYWlsLzAvMzM4NDk3MS9VXzBfMV8wNjM4MmFjNC1lOGJhLTQwZjMtYmNiZS1iOTQ0ZTVjMzMwZGMucG5nfFNFUEFSQVRFUnxodHRwOi8vaW1nLm1hbmh1YWRhby5jbi9hcGkvdjEvYm9va2NlbnRlci9zY2VuZXRodW1ibmFpbC8xLzMzODQ5NzEvVV8wXzJfZTcxYjczNWItZGI5Yy00ZjJhLTlkODAtMTcwNTUxYmNkMTZiLnBuZ3xTRVBBUkFURVJ8aHR0cDovL2ltZy5tYW5odWFkYW8uY24vYXBpL3YxL2Jvb2tjZW50ZXIvc2NlbmV0aHVtYm5haWwvMC8zMzg0OTcxL1VfMF8wXzQ2NTQ4ZjAxLWIzNzctNDBkZi1hYzZlLWMxYjU5OGI0MzBiYS5wbmd8U0VQQVJBVEVSfGh0dHA6Ly9pbWcubWFuaHVhZGFvLmNuL2FwaS92MS9ib29rY2VudGVyL3NjZW5ldGh1bWJuYWlsLzAvMzM4NDk3MS9VXzBfMV9jYzg0YmNlOS1jYzQ5LTRkNGMtOGJmYS04YjIzMmQ5Mjk2ZTkucG5nfFNFUEFSQVRFUnxodHRwOi8vaW1nLm1hbmh1YWRhby5jbi9hcGkvdjEvYm9va2NlbnRlci9zY2VuZXRodW1ibmFpbC8xLzMzODQ5NzEvVV8wXzJfNzRlMTkxNTUtMGRlYy00NDU1LTk1ZTAtN2I2MDI2MTY2MmYzLnBuZ3xTRVBBUkFURVJ8aHR0cDovL2ltZy5tYW5odWFkYW8uY24vYXBpL3YxL2Jvb2tjZW50ZXIvc2NlbmV0aHVtYm5haWwvMC8zMzg0OTcxL1VfMF8wXzAzZTAzZTYyLTAxYzItNGI5Yy1hNTY4LWQ5MWE0ZGEyMjBhMS5wbmd8U0VQQVJBVEVSfGh0dHA6Ly9pbWcubWFuaHVhZGFvLmNuL2FwaS92MS9ib29rY2VudGVyL3NjZW5ldGh1bWJuYWlsLzAvMzM4NDk3MS9VXzBfMV85M2NmOGFjOC0zYmM0LTQ1MTAtOWYxNC0yYzMzMWMwYjZlMDMucG5nfFNFUEFSQVRFUnxodHRwOi8vaW1nLm1hbmh1YWRhby5jbi9hcGkvdjEvYm9va2NlbnRlci9zY2VuZXRodW1ibmFpbC8xLzMzODQ5NzEvVV8wXzJfM2QxNzMzYzYtZWVjNy00MTYxLWFlZTEtMGRmMmQ0NjhmNTRjLnBuZ3xTRVBBUkFURVJ8aHR0cDovL2ltZy5tYW5odWFkYW8uY24vYXBpL3YxL2Jvb2tjZW50ZXIvc2NlbmV0aHVtYm5haWwvMC8zMzg0OTcxL1VfMF8wXzg1YTMyMWIzLWQwYjktNGE3Yy05YjFhLTNlM2Q0MDc5MzA2My5wbmd8U0VQQVJBVEVSfGh0dHA6Ly9pbWcubWFuaHVhZGFvLmNuL2FwaS92MS9ib29rY2VudGVyL3NjZW5ldGh1bWJuYWlsLzAvMzM4NDk3MS9VXzBfMV80NDViZDdiMi00YjI0LTRkNTAtOGU5OS1mMDAzZmMzYmYxZDYucG5nfFNFUEFSQVRFUnxodHRwOi8vaW1nLm1hbmh1YWRhby5jbi9hcGkvdjEvYm9va2NlbnRlci9zY2VuZXRodW1ibmFpbC8xLzMzODQ5NzEvVV8wXzJfZWUwZGEzOTAtNmI0Yi00YjY3LWFiZmUtMThkNjljMzA2NzkxLnBuZ3xTRVBBUkFURVJ8aHR0cDovL2ltZy5tYW5odWFkYW8uY24vYXBpL3YxL2Jvb2tjZW50ZXIvc2NlbmV0aHVtYm5haWwvMC8zMzg0OTcxL1VfMF8wXzI5MDFmODY0LWE3NzItNDZmZS1iOTkzLWZmZDI4ZmY3ZTFlNi5wbmd8U0VQQVJBVEVSfGh0dHA6Ly9pbWcubWFuaHVhZGFvLmNuL2FwaS92MS9ib29rY2VudGVyL3NjZW5ldGh1bWJuYWlsLzAvMzM4NDk3MS9VXzBfMV9mNGNmMTAzNS1jODc1LTQwMWItOGE4NC02ZThkNmNjYjVkZjAucG5nfFNFUEFSQVRFUnxodHRwOi8vaW1nLm1hbmh1YWRhby5jbi9hcGkvdjEvYm9va2NlbnRlci9zY2VuZXRodW1ibmFpbC8xLzMzODQ5NzEvVV8wXzJfMjVkMTY4ZDktZGQ1Yy00ZTI2LThkNDItY2Q2ZTQwMzU1MDg3LnBuZ3xTRVBBUkFURVJ8aHR0cDovL2ltZy5tYW5odWFkYW8uY24vYXBpL3YxL2Jvb2tjZW50ZXIvc2NlbmV0aHVtYm5haWwvMC8zMzg0OTcxL1VfMF8wXzkzNGZmNGNiLTA0ZGYtNDA3MS05MzY3LWFhNjg0ODFmNTZkNy5wbmd8U0VQQVJBVEVSfGh0dHA6Ly9pbWcubWFuaHVhZGFvLmNuL2FwaS92MS9ib29rY2VudGVyL3NjZW5ldGh1bWJuYWlsLzAvMzM4NDk3MS9VXzBfMV9mOTRhZDUwNC1mNDhiLTQyMzQtOWVkNC1kMDNmYzQ3NmM3NzMucG5nfFNFUEFSQVRFUnxodHRwOi8vaW1nLm1hbmh1YWRhby5jbi9hcGkvdjEvYm9va2NlbnRlci9zY2VuZXRodW1ibmFpbC8xLzMzODQ5NzEvVV8wXzJfZmEzOWMwZGEtZDQwMS00MzRhLTkzMDAtZDUzYjIxYmE0YzA4LnBuZ3xTRVBBUkFURVJ8aHR0cDovL2ltZy5tYW5odWFkYW8uY24vYXBpL3YxL2Jvb2tjZW50ZXIvc2NlbmV0aHVtYm5haWwvMC8zMzg0OTcxL1VfMF8wXzY3MDYyZjA5LWEzMmQtNGJhZi1iY2I3LWU0MDY3MjdmNmVjYy5wbmd8U0VQQVJBVEVSfGh0dHA6Ly9pbWcubWFuaHVhZGFvLmNuL2FwaS92MS9ib29rY2VudGVyL3NjZW5ldGh1bWJuYWlsLzAvMzM4NDk3MS9VXzBfMV82MWUxOWRmYi01YmYyLTRkNjEtODQ2NC0yNmUxYWI2ZmI4NjMucG5nfFNFUEFSQVRFUnxodHRwOi8vaW1nLm1hbmh1YWRhby5jbi9hcGkvdjEvYm9va2NlbnRlci9zY2VuZXRodW1ibmFpbC8xLzMzODQ5NzEvVV8wXzJfMDdhMjZjZjQtMTNkNS00OTk0LWIyZjMtZDQ3MjY4YTQ4NzFmLnBuZ3xTRVBBUkFURVJ8aHR0cDovL2ltZy5tYW5odWFkYW8uY24vYXBpL3YxL2Jvb2tjZW50ZXIvc2NlbmV0aHVtYm5haWwvMC8zMzg0OTcxL1VfMF8wXzNjM2VjY2FiLWVjZTYtNDBmNC1hMzUyLTRjNWVkZTUzODY4OS5wbmd8U0VQQVJBVEVSfGh0dHA6Ly9pbWcubWFuaHVhZGFvLmNuL2FwaS92MS9ib29rY2VudGVyL3NjZW5ldGh1bWJuYWlsLzAvMzM4NDk3MS9VXzBfMV9lOGVkNzU3MS1kZTVmLTQwNDAtOWM1Ni1iYmEwOWZjZTIyZjkucG5nfFNFUEFSQVRFUnxodHRwOi8vaW1nLm1hbmh1YWRhby5jbi9hcGkvdjEvYm9va2NlbnRlci9zY2VuZXRodW1ibmFpbC8xLzMzODQ5NzEvVV8wXzJfYzNmOWI3ZWYtNjdmOC00ZTJmLTllMWYtZDRiYWVlOGU1Y2IyLnBuZ3xTRVBBUkFURVJ8aHR0cDovL2ltZy5tYW5odWFkYW8uY24vYXBpL3YxL2Jvb2tjZW50ZXIvc2NlbmV0aHVtYm5haWwvMC8zMzg0OTcxL1VfMF8wXzIzY2ViYTU4LWJkZGMtNDJjNC1hNTZmLWI5MTMwYjk5NmY0OS5wbmd8U0VQQVJBVEVSfGh0dHA6Ly9pbWcubWFuaHVhZGFvLmNuL2FwaS92MS9ib29rY2VudGVyL3NjZW5ldGh1bWJuYWlsLzAvMzM4NDk3MS9VXzBfMV9kNTI5YTlmMy0xMTk3LTRkYTAtODg4Zi1lOTgzMzQ4N2Y4ZGEucG5nfFNFUEFSQVRFUnxodHRwOi8vaW1nLm1hbmh1YWRhby5jbi9hcGkvdjEvYm9va2NlbnRlci9zY2VuZXRodW1ibmFpbC8xLzMzODQ5NzEvVV8wXzJfYjhjOTJjZmItMDc2Ni00OWJiLThmOGUtYWMwNTZmODkxMjQ0LnBuZ3xTRVBBUkFURVJ8aHR0cDovL2ltZy5tYW5odWFkYW8uY24vYXBpL3YxL2Jvb2tjZW50ZXIvc2NlbmV0aHVtYm5haWwvMS8zMzg0OTcxL1VfN2Q1ZDQ2MTUtNzJjMi00ZTNiLWIzMTEtNjg0MjkwMDgyNDFmLnBuZ3xTRVBBUkFURVJ8aHR0cDovL2ltZy5tYW5odWFkYW8uY24vYXBpL3YxL2Jvb2tjZW50ZXIvc2NlbmV0aHVtYm5haWwvMC8zMzg0OTcxL1VfMF8wXzkxOWI2NTZiLTliYWItNGI4My04OWRjLTA4ZjFiYmI1NWNiOC5wbmd8U0VQQVJBVEVSfGh0dHA6Ly9pbWcubWFuaHVhZGFvLmNuL2FwaS92MS9ib29rY2VudGVyL3NjZW5ldGh1bWJuYWlsLzAvMzM4NDk3MS9VXzBfMV85NGI2YTJkMy1kYmJjLTQ3N2UtYTQ4Zi02YzkwOTA4YTZjN2MucG5nfFNFUEFSQVRFUnxodHRwOi8vaW1nLm1hbmh1YWRhby5jbi9hcGkvdjEvYm9va2NlbnRlci9zY2VuZXRodW1ibmFpbC8xLzMzODQ5NzEvVV8wXzJfMTE3N2FlMjYtYThhZS00NjdmLTgwZmItZjBkOGFiMjZiOWI5LnBuZw";
                String READKEY = "fw12558899ertyui";


                whatTheFuck = new String(Base64.decode(whatTheFuck, Base64.DEFAULT), StandardCharsets.UTF_8);
                LogUtil.iLength("haleydu whatTheFuck1",whatTheFuck);

                whatTheFuck = DecryptionUtils.decryptAES(whatTheFuck, wtf1);
                LogUtil.iLength("haleydu whatTheFuck2",whatTheFuck);

                whatTheFuck = new String(Base64.decode(urls__direct, Base64.DEFAULT));
                LogUtil.iLength("haleydu whatTheFuck3",whatTheFuck);


                String decryptKey = "JRUIFMVJDIWE569j";
                String decodedData  = new String(Base64.decode(encodedData, Base64.DEFAULT));
                LogUtil.iLength("haleydu decodedData",decodedData);

                String decryptedData = DecryptionUtils.decryptAES(decodedData, READKEY);
                LogUtil.iLength("haleydu decryptedData",decryptedData);

                String imgRelativePath = StringUtils.match("imgpath:\"(.+?)\"",decryptedData,1);
                String startImg = StringUtils.match("startimg:([0-9]+?),",decryptedData,1);
                String totalPages = StringUtils.match("totalimg:([0-9]+?),",decryptedData,1);
                for (int i = Integer.parseInt(startImg); i <= Integer.parseInt(totalPages); ++i) {
                    String jpg = StringUtils.format("%04d.jpg", i);
                    list.add(new ImageUrl(i, serverUrl + imgRelativePath + jpg, false));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    @Override
    public Request getCheckRequest(String cid) {
        return getInfoRequest(cid);
    }

    @Override
    public String parseCheck(String html) {
        Node body = new Node(html);
        String update = body.text("dl.fed-deta-info > dd > ul > li:eq(2)");
        if (!update.contains("更新")){
            update = body.text("dl.fed-deta-info > dd > ul > li:eq(3) > a");
        }
        return update.replace("更新","");
    }

    @Override
    public Headers getHeader() {
        return Headers.of("Referer", baseUrl);
    }


}

