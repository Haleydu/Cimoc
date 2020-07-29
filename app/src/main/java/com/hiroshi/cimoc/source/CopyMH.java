package com.hiroshi.cimoc.source;

import android.util.Base64;
import android.util.Log;
import com.facebook.common.util.Hex;
import com.hiroshi.cimoc.core.Manga;
import com.hiroshi.cimoc.model.Chapter;
import com.hiroshi.cimoc.model.Comic;
import com.hiroshi.cimoc.model.ImageUrl;
import com.hiroshi.cimoc.model.Source;
import com.hiroshi.cimoc.parser.JsonIterator;
import com.hiroshi.cimoc.parser.MangaParser;
import com.hiroshi.cimoc.parser.SearchIterator;
import com.hiroshi.cimoc.parser.UrlFilter;
import com.hiroshi.cimoc.soup.Node;
import com.hiroshi.cimoc.utils.DecryptionUtils;
import com.hiroshi.cimoc.utils.HttpUtils;
import com.hiroshi.cimoc.utils.LogUtil;
import com.hiroshi.cimoc.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import okhttp3.Request;
import taobe.tec.jcc.JChineseConvertor;

public class CopyMH extends MangaParser {
    public static final int TYPE = 26;
    public static final String DEFAULT_TITLE = "拷贝漫画";

    public static Source getDefaultSource() {
        return new Source(null, DEFAULT_TITLE, TYPE, true);
    }

    public CopyMH(Source source) {
        init(source, null);
    }

    @Override
    public Request getSearchRequest(String keyword, int page) throws Exception {
        String url = "";
        if (page == 1) {
            url = StringUtils.format("https://copymanga.com/search?q=%s", keyword);
            return new Request.Builder().url(url).build();
        }
        return null;
    }

    @Override
    public String getUrl(String cid) {
        return "https://copymanga.com/h5/details/comic/".concat(cid);
    }

    @Override
    protected void initUrlFilterList() {
        filter.add(new UrlFilter("copymanga.com", "\\w+", 0));
    }

    @Override
    public SearchIterator getSearchIterator(String html, int page) throws JSONException {
        Node body = new Node(html);
        String kw=body.text("#searchKey");

        JSONObject jsonObject=HttpUtils.httpsRequest("https://www.copymanga.com/api/kb/web/search/count?offset=0&platform=2&limit=50&q="
                .concat(kw),"GET",null);

        JSONArray array=jsonObject.getJSONObject("results").getJSONObject("comic").getJSONArray("list");
        org.json.JSONArray array1= new org.json.JSONArray(array.toString());

        return new JsonIterator(array1) {
            @Override
            protected Comic parse(org.json.JSONObject object) throws JSONException {
                String title = null;
                try {
                    title = object.getString("name");
                    JChineseConvertor jChineseConvertor = JChineseConvertor.getInstance();
                    title = jChineseConvertor.s2t(title);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String cid = object.getString("path_word");
                String cover = object.getString("cover");

                String author = "";
                org.json.JSONArray jsonObjectList = object.getJSONArray("author");
                for (int i = 0; i < jsonObjectList.length(); ++i) {
                    author += jsonObjectList.getJSONObject(i).getString("name") + " ";
                }
                author = author.trim();

                return new Comic(TYPE, cid, title, cover, null, author);
            }
        };
    }

    @Override
    public Request getInfoRequest(String cid) {
        String url = "https://www.copymanga.com/comic/".concat(cid);
        return new Request.Builder().url(url).build();
    }

    @Override
    public void parseInfo(String html, Comic comic) {
        Node body = new Node(html);
        String cover = body.attr("div.comicParticulars-left-img.loadingIcon > img","data-src");
        String intro = body.text("p.intro");
        String title = body.text("div.col-9.comicParticulars-title-right > ul > li:nth-child(1) > h6");

        String update = body.text("div.col-9.comicParticulars-title-right > ul > li:nth-child(5) > span.comicParticulars-right-txt");
        String author = body.text("div.col-9.comicParticulars-title-right > ul > li:nth-child(3) > span.comicParticulars-right-txt > a");

        // 连载状态
        String status=body.text("div.col-9.comicParticulars-title-right > ul > li:nth-child(6) > span.comicParticulars-right-txt");
        boolean finish=isFinish(status);

        comic.setInfo(title, cover, update, intro, author, finish);
    }

    @Override
    public List<Chapter> parseChapter(String html) throws JSONException {
        List<Chapter> list = new LinkedList<>();

        Node body=new Node(html);
        String cid=body.attr("img.lazyload","data-src").replace("https://mirrorvip2.mangafunc.fun/comic/","").replaceAll("/cover.*","");

        JSONObject jsonObject=HttpUtils.httpsRequest(String.format("https://api.copymanga.com/api/v3/comic/%s/group/default/chapters?limit=500&offset=0",cid),"GET",null);
        JSONArray array=jsonObject.getJSONObject("results").getJSONArray("list");

        for (int i=0;i<array.length();++i){
            String title=array.getJSONObject(i).getString("name");
            String path=array.getJSONObject(i).getString("uuid");
            list.add(new Chapter(title,path));
        }

        return list;
    }

    @Override
    public Request getImagesRequest(String cid, String path) {
        String url = StringUtils.format("https://www.copymanga.com/comic/%s/chapter/%s", cid, path);
        return new Request.Builder().url(url).build();
    }

    @Override
    public List<ImageUrl> parseImages(String html) throws Manga.NetworkErrorException, JSONException {
        List<ImageUrl> list = new LinkedList<>();
        Node body = new Node(html);
        String data = body.attr("div.disposableData","disposable");
        //String data = "qBc7vWTRnQOp0HtSad7503098d47be257b6e0b8fafa6d97f08a0b4c9d65a7599157d1fa2fac4d22a842cfa731cff740d1696c40051a6f93b48480f4ff73d915b11261a62ae1e57a33458159b39e08b1cd38ced93df365b402c595601f2eb8c9b579e809f9a1b6c3a1fcc7fecab57a49603d5c4fc1104d5c8fb0c457215131c6d015fb18e748b7407e656aa2afd96d2ff578c42455f257e6dedab1c4c0f11cb9f316dbaa9f9008f6b9da5dc6ff6453bf15f82467f35bd1cac6366cb45a78afb6a363cbda3be2b1f25e98a60b3b848ff145be9811451601b7a2d24c27673c27ad745a918e3ff15164af7a12bdcdca5418a42a8ea4c7c24cd8358cb9abf36ae1e802114547c6c27b8f8abb330d392d442264e66e22b7d6685a667d277b691d206f3af35884b8426a077c31fbe215aa99a707e12de148a12673078eb934f15db9ad936635ead3b105c57700160459e6c4f16317f31beed9d208886678009af2560543a9ea2181ba6e3fd31b8cb5aff4619be7227887f95864578da34ff1b378071296822e66f2580db910915fc80b0a5e256cc61d73767357780d4287a4d1d7bdafc3983c72ccc0fa5f45f8c7cfba84b1c64157c86f9eba77c5fbbd1c6234f6ebca968d9c5fe7cdabff2ea48022151ead33e38adda5dccb59bbb332655f598981df60f9b4d4c3024195e55d6d75110f681d46b2e880403d3c285bec3e66c646c0966a2b94b5eeb4be2dde4af04544190b0d7cbac51cb909dd2f147579b672344f2e5e68fa7c3204964e6df4073fa93e3f05d8d4a1a2ae2e341d1663395e03989d85340ad11fd3069042bbd88c7065c2166db5971e4c298275d4374ba3ce4cb52fe2bdcc557dc7c4d0f3e4deabd00de5d74a6092abd8ee60b131c65e86bf1dd31ff57b329705d52d2c0ced9d67fb5867f194b349273950f805f9081c491e9fadfb1da2c618be97dd363f0fffd455c873ae49e4556d2d4510b72c11ff6685895f11716f7747fe1efb964755de11bc865e4ff01a5dd0cc425096e0cd55861862cdbd31f894dad0cc7bf8306daafe39166e4ec3d02fc70528f2cdeebd1125df729e71d5d1c198d8fc76cda9585963f99f5163181b05d655f4942457cb21ceb9291f4160ac1baf38294855baaa7d4690f31d176fc4d076ad10bcbacad775feebfda6fe9aa602117888792406e0cf20ce727e87682109667a2054a18a842722a977bbb612bc5034072e4a4d276aec0467f6a9c6a5f5209a0f7a4ec404508f1fff3e823057daf52a41402a0eb13d6d1194f8e5e2e333cad539c4f74be42b21c5c7340f50f8ca86a38615f5a9b215d9489ea74590b5368d9828d052bb1e6e1451fbe0219310f5cd4789f7f066bdc82a9ea751d6994e823bf6db81d894d7406f82028e6f3da91b2d17adba56eb72b2734e08f4ac8d2018a029503724bb44459f01b3ebc82aa5b469e7fe3f07b531dfe196558bf21a48e7b0d272b1175e1356a6336a636012a0223f5348275dacfdf6d5d0d66602249d6868e2bb5173c8233b5555c5284f53c9d56ba57e3d6d2a0f2d30ca94a96a0b3e7a2dd5abf730e983773a187435db689a1d959a946c777eaf41c1b0c876ae0a154cf249e11a5b121bfee11f8f56a20ce6f7d566dc600dceb99fa1f0e382fd719a1dee96cff1ef2fa933fcb38c1e90938961f87034776ab20ba181b8173c92bafa4870a2f136ac85d1c86bf60d2758c3f5a79478ef7d5ad5f3663d5a62bfebc52a2b7aee949ec39a422e471a464de327a90ee6b138f5c6733972407b013228d467ddccc00df6b9ee594611b2034eaf56d4402cddf27f8bf9bafd68c83ee592b8d870e40424d06cf6830cf879e52722ad0744a688ef750059eda145edc24b60f723a7d7bc51ad29b72ff3b8d91ab7a00a8e032fce4a241f0a1af0cd6e2ee51528bec486dde0241e6c49d5f0a818628eb27471ccc5570aeeb2869dcbd778807f2647f734819cd42e51191ab822421443318f0993a300aa9182e1b0e3cd68934d51b51989a2a4e0237ba53dbb444d59c4141a2cde7a98ba6ff78205507e89b2653645029759dba6a53dca33ce1fbdd74dd646818445c806d47f4eea36b99ce189bc8b67b724d4582688144d5cca4c1a7ea1920064f42a21e0aaa7fedafa036e836b0046d8ef6917646bc8495bcf5e0029d89de58e5d161e8a3632141d8bd21d673d6a2fac81a8dff6f30f742294d94dead99a3e7d18083eb16460142846a966bfa9e54940765b7024c20f7c10e4dba8efc158a60a88d495dbfef91d90b9e7ea4aaaa313a9f023ef392975922c0b3b72df2a6a27636ab75386e1f85bf25acffe9c3268b0e7151c230e5afd99f81d68c399a3fadabd573db9ad7eb21aecd10808366d9ef1238721b2ec0b03b2bd6e3fc717b7248b571bd14af050786c8fc67c730ff8dcc5b3d078c97938f81635d85b7168b737c6396eff235cd9ff2c033c16392f7769e2860735cffcf9bd7cdc64aa70535fd898a2affcd6d1bbf217780c2469250d363f237ec9ea9f105302399531a43c53d9f2b5fefeb5592852991b2cb3e75c91b08ea6139a5b557cb70280d92f79eb3584fff4ef5f5c82a4bba69829ddf28b4bf5cb57aafa3d12f6c41463fb0aebaa25f6d56258af42941a98f8058c733f4a30c5ff9769248b761376fb06f9f8622e148ecfa463a85c21b5381896333404ee20d377fcbbe23b39321d23b5926b587ef0d9f1b5837b51c868944ec6044998c67d710b8006ab9e7499e60da637ef479e9d9090e52ac27a460ad174ef4a19eb62a1f976fdce1ac163b03c1f8247018bca8b6e8b78323147ef4a0eeee9c8d3eb0a8e7b4e0c4f8f2e7b8e4809f1fb4c159734e63447ba2e6d6be563e71cca3ed5a3881d169836f0534c14188ba8db3c7532b8c8858402c2a66bc94b35958db684a169638f109b59f0baa2a4057d68d95f25ea1a3a497231c1cfda2f8080629f0a7a75880ea34c074dff6662f682e24629bd764c6b6e835bd913209337b2af3865d67a4b69d4ae3ce807c31f66482a0aa7accb0a36ed12a82724346c0a709a641213867525f796c405f2fd110c12e7bbf972c890601991a223a0d3c5789738c1c5f6735b99256020b59d5ec9affeb8734beb7cc632896a3df61e2acd2d914e5233f60bd74d431d263afddcf417bed2b6b12a9f04fa2bfcfeb7a4ff0d06a2a51d80825a3dd082b8ce9f1813fefea6eb91c17c978bf188e213c6d9091e251587fdfb8ac3c97bbcdbdefc36f658d284e3c4873e7932de19defdd995513e8fc25eadd7463307e360b65925595c8dad596fd81e4cf4d54d8f7246b818b353dbbb5391ec98775fe6b32ae58bbf30b9af2c0b9330c6383b4e47bc581a6bc8fba49cae3c24ffb76f6dd1de82f5af5241efeda8436b0264f1b2c56aa16f25664fd39b674531a5463c28cce169d8bc4dd66922a8a2a1db4129625736781a897169d0423580a3556e7af5397af30c35d08bdf3bb49f70badfcf37615db49feddaad75c1da60106e28c9ca0c728537f6a856f86762c87fff3cf399af955a888918c011a4cf57d6ba5c6d5fbeed2d8ddc314103640bb9ae45d691df794c2d65227c8f4268a36b4887d60d47bc0def44503a78ce81eaf74ad20027ed2a3516847a5c89205c5aed5201e86407c473f10885b3f343cf8b277792777f28f9f23847f138cd4c8e4e97840011e2ba6d632efbc3cc9aa2674fd5ce8763e6c230ca5a23216f0d73a58fba113bbd75df4102f078e1fe41f6a655bc9ad4a596f561e36dce7cf72bef25fb4206e67dacb251a7ec8ee3ab8de5dfe79e7f7a285099cf713a0d200ca1b819527f48d975c3fb3df6a1ab81c9f991e0df8732bf2ed8e38628321ae818e7e6cbb23ed4071031541e25740f0b1f0aad55abbcee10d076ff1f8b05bdfbfbc08fb26ac22e848a30c26c3ca9c253f2a066f468af848f099fc1febba688f8b154da079701da0b2f4114c9150aecd4a3191858b5c5c9d9fa633618479017542535b15661d19ac0a9ca5f24fa47075ce332a0bce66cac99b0daf650c6324bdb9242a7a1ac76fad51ffcbdfc4f7e48954fb5708137555207d750340d3d3912cc8a17b94ad33c9d436430ee922580f6d9f21dc215348742d07a68aa56651d2b17f6dc9680c3186f874fa5ed256a5fb9e3e25d5853d228b7782d3ec217fe5e61b9a298cc942fb83316be7f4c447d13339ef7aee60aa2997dee1371b8b2d33e3239d7f707bc17fc9b578e84ed7d518670683a907e5e945eb73595d492c9df65e12067be6ed0e96d9ee6b2dd30b20c8cfb471af3b0020b82302ff70a4270f1f20bfa9532f04082c726240d484c4520d20553155b4dafeaf9dae3eb95e35888a24efaa6a44ef4786a58109167129058c27b4bf3f0bffb4049ca6451fcbb611ea4d4789bb1b79d894819d582b0c820d49e0fac13d97bfd494c3daf9b3f7fd6d316fc41aa47fa24b8e96bab03b705a1281e772c08e0d0333be6884fb5592d77cdcd84d0e86679b0a22ed9f9ad6fb9671d3b2081ad50997ba850dcfd2adcb5029bf9c79ff813326f90845f658c87a6637b8355a0065f0be2f0b4dae514a1ea0ae7b393703d5d68b907ba5e9de684caf13b051cd8105c7dfcddbb01423a489c82d1c3aeebc274eff761671d8763f7c22e795efb460b39d9a738d2684491892839c8162a75725e71987b35d0bae38c383cbe011ff3bb0a8b562d5be851293d5187370e3206d115ef6420e7daa6e0f15656c03685f59e649f3ef7cabd8a622f48b6a5a91ef1026ce9a1e94ba0acd5a9d095d613489cae2bce901b5176e601ddda0adc1d8445a4d3b60e10ff4acee9635b2ef80152bab89f8";
        String key = body.attr("div.disposablePass","disposable").trim();
        String iv = data.substring(0,0x10).trim();
        String result = data.substring(0x10).trim();
//        Log.d("hrd data", data);
//        Log.d("hrd key", key);
//        Log.d("hrd iv", iv);
//        Log.d("hrd result", result);
        byte[] hexCode = Hex.decodeHex(result);
        String encode = Base64.encodeToString(hexCode,0,hexCode.length, Base64.NO_WRAP);
        //LogUtil.iLength("hrd result", encode);
        String jsonString=null;
        try {
            //jsonString =  aesDecrypt(result,key,iv);
            jsonString = DecryptionUtils.aesDecrypt(encode,key,iv);
            //Log.d("hrd jsonString", jsonString);

            JSONArray array = new JSONArray(jsonString);
            for (int i = 0; i < array.length(); ++i) {
                String url = array.getJSONObject(i).getString("url");
                //Log.d("hrd parseImages url", url);
                list.add(new ImageUrl(i + 1, url, false));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return list;
    }

    @Override
    public Request getCheckRequest(String cid) {return getInfoRequest(cid);}

    @Override
    public String parseCheck(String html) {
        Node body=new Node(html);
        String update=body.text("div.col-9.comicParticulars-title-right > ul > li:nth-child(5) > span.comicParticulars-right-txt");
        return update;
    }
}
