package com.hiroshi.cimoc.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.facebook.binaryresource.BinaryResource;
import com.facebook.binaryresource.FileBinaryResource;
import com.facebook.cache.common.CacheKey;
import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.internal.Supplier;
import com.facebook.datasource.BaseDataSubscriber;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.cache.CountingMemoryCache;
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory;
import com.facebook.imagepipeline.cache.ImageCacheStatsTracker;
import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.core.ImagePipelineFactory;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.BasePostprocessor;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;
import java.util.UUID;



/**
 * Created by Administrator on 2016/6/20 0020.
 * 注意:各方法需要添加的依赖以及写在方法上方文档注释中
 */
public class FrescoUtils {

    private static final String PHOTO_FRESCO = "frescocache";

    /**
     *
     * 需要添加依赖:
     *  compile 'jp.wasabeef:fresco-processors:2.0.0'
     *              或者自己拷贝那个类出来
     *
     *
     * 高斯模糊后显示
     * @param url
     * @param draweeView
     * @param width draweeView的宽
     * @param height draweeView的高
     * @param context
     * @param radius  高斯模糊的半径, 每一个像素都取周边(多少个)像素的平均值
     * @param sampling 采样率 原本是设置到BlurPostprocessor上的,因为高斯模糊本身对图片清晰度要求就不高,
     *                 所以此处直接设置到ResizeOptions上,直接让解码生成的bitmap就缩小,而BlurPostprocessor
     *                 内部sampling设置为1,无需再缩
     */
    /*public static void loadUrlInBlur(String url,SimpleDraweeView draweeView,
                                     int width,int height,Context context,int radius,int sampling){
        if (sampling<2){
            sampling = 2;
        }
        loadUrl(url,draweeView,new BlurPostprocessor(context,radius,1),width/sampling,height/sampling,null);
    }*/




    /**
     *  If the image has some ResizeOptions we put also the resized image into the cache with different key.
     *  currently don't support downsampling / resizing for GIFs.
     * @param url
     * @param draweeView
     * @param processor
     * @param width
     * @param height
     * @param listener
     */
    public static void loadUrl(String url, SimpleDraweeView draweeView,BasePostprocessor processor,int width,int height,
                               BaseControllerListener listener){

       load(Uri.parse(url),draweeView,processor,width,height,listener);

    }

    public static void loadFile(String file, SimpleDraweeView draweeView,BasePostprocessor processor,int width,int height,
                               BaseControllerListener listener){

        load(getFileUri(file),draweeView,processor,width,height,listener);

    }

    public static void loadFile(File file, SimpleDraweeView draweeView,BasePostprocessor processor,int width,int height,
                                BaseControllerListener listener){

        load(getFileUri(file),draweeView,processor,width,height,listener);

    }

    public static void loadRes(int resId, SimpleDraweeView draweeView,BasePostprocessor processor,int width,int height,
                                BaseControllerListener listener){

        load(getResUri(resId),draweeView,processor,width,height,listener);

    }


    public static void load(Uri uri,SimpleDraweeView draweeView,BasePostprocessor processor,int width,int height,
                                BaseControllerListener listener){
        ResizeOptions resizeOptions = null;
        if (width >0 && height > 0){
            resizeOptions = new ResizeOptions(width,height);
        }
        ImageRequest request =
                ImageRequestBuilder.newBuilderWithSource(uri)
                        .setPostprocessor(processor)
                        .setResizeOptions(resizeOptions)
                        //缩放,在解码前修改内存中的图片大小, 配合Downsampling可以处理所有图片,否则只能处理jpg,
                        // 开启Downsampling:在初始化时设置.setDownsampleEnabled(true)
                        .setProgressiveRenderingEnabled(true)//支持图片渐进式加载
                        .setAutoRotateEnabled(true) //如果图片是侧着,可以自动旋转
                        .build();

        PipelineDraweeController controller =
                (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                        .setImageRequest(request)
                        .setControllerListener(listener)
                        .setOldController(draweeView.getController())
                        .setAutoPlayAnimations(true) //自动播放gif动画
                        .build();



        draweeView.setController(controller);
    }



    public static Uri getFileUri(File file){
        return Uri.fromFile(file);
    }

    public static Uri getFileUri(String filePath){
        return Uri.fromFile(new File(filePath));
    }

    public static Uri getResUri(int resId){
       return Uri.parse("res://xxyy/" + resId);
    }




    /**
     * 当设置roundAsCircle为true无效时,采用这个方法,常用在gif的圆形效果上
     *
     * 或者在xml中设置:fresco:roundWithOverlayColor="@color/you_color_id"
     "you_color_id"是指你的背景色，这样也可以实现圆角、圆圈效果
     *
     *roundAsCircle的局限性:
     * 当使用BITMAP_ONLY（默认）模式时的限制：
     并非所有的图片分支部分都可以实现圆角，目前只有占位图片和实际图片可以实现圆角，我们正在努力为背景图片实现圆角功能。
     只有BitmapDrawable 和 ColorDrawable类的图片可以实现圆角。我们目前不支持包括NinePatchDrawable和 ShapeDrawable在内的其他类型图片。（无论他们是在XML或是程序中声明的）
     动画不能被圆角。
     由于Android的BitmapShader的限制，当一个图片不能覆盖全部的View的时候，边缘部分会被重复显示，而非留白。对这种情况可以使用不同的缩放类型
     （比如centerCrop）来保证图片覆盖了全部的View。 OVERLAY_COLOR模式没有上述限制，但由于这个模式使用在图片上覆盖一个纯色图层的方式来模拟圆角效果，
     因此只有在图标背景是静止的并且与图层同色的情况下才能获得较好的效果。
     * @param draweeView
     * @param bgColor 圆形遮罩的颜色,应该与背景色一致
     */
    public static void setCircle( SimpleDraweeView draweeView,int bgColor){
        RoundingParams roundingParams = RoundingParams.asCircle();//这个方法在某些情况下无法成圆,比如gif
        roundingParams.setOverlayColor(bgColor);//加一层遮罩
        draweeView.getHierarchy().setRoundingParams(roundingParams);
    }


    /**
     * 暂停网络请求
     * 在listview快速滑动时使用
     */
    public static void pause(){
        Fresco.getImagePipeline().pause();
    }


    /**
     * 恢复网络请求
     * 当滑动停止时使用
     */
    public static void resume(){
        Fresco.getImagePipeline().resume();
    }



    /**
     * 初始化操作，建议在子线程中进行
     * 添加的依赖：
     *  compile 'com.facebook.fresco:fresco:0.10.0+'
        compile 'com.facebook.fresco:animated-webp:0.10.0'
        compile 'com.facebook.fresco:animated-gif:0.10.0'
     * @param context
     * @param cacheSizeInM  磁盘缓存的大小，以M为单位
     */
    public static void init(final Context context,int cacheSizeInM){


        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(context)
                .setMaxCacheSize(cacheSizeInM*1024*1024)
                .setBaseDirectoryName(PHOTO_FRESCO)
                .setBaseDirectoryPathSupplier(new Supplier<File>() {
                    @Override
                    public File get() {
                        return context.getCacheDir();
                    }
                })
                .build();
        MyImageCacheStatsTracker imageCacheStatsTracker = new MyImageCacheStatsTracker();
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(context)
                .setMainDiskCacheConfig(diskCacheConfig)
                .setImageCacheStatsTracker(imageCacheStatsTracker)
                .setDownsampleEnabled(true)//Downsampling，它处理图片的速度比常规的裁剪更快，
                // 并且同时支持PNG，JPG以及WEP格式的图片，非常强大,与ResizeOptions配合使用
                .setBitmapsConfig(Bitmap.Config.RGB_565)
                .build();
        Fresco.initialize(context, config);
    }





    /**
     * 清除磁盘缓存
     */
    public static void clearDiskCache(){
        Fresco.getImagePipeline().clearDiskCaches();
    }


    /**
     * 清除单张图片的磁盘缓存
     * @param url
     */
    public static void clearCacheByUrl(String url){
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        Uri uri = Uri.parse(url);
       // imagePipeline.evictFromMemoryCache(uri);
        imagePipeline.evictFromDiskCache(uri);
        //imagePipeline.evictFromCache(uri);//这个包含了从内存移除和从硬盘移除
    }

    /**
     * 从fresco的本地缓存拿到图片,注意文件的结束符并不是常见的.jpg,.png等，如果需要另存，可自行另存
     *
     * @param url
     */
    public static File getFileFromDiskCache(String url) throws NullPointerException{
        File localFile = null;
        if (!TextUtils.isEmpty(url)) {
            CacheKey cacheKey = DefaultCacheKeyFactory.getInstance().getEncodedCacheKey(ImageRequest.fromUri(url), null);
            if (ImagePipelineFactory.getInstance().getMainFileCache().hasKey(cacheKey)) {
                BinaryResource resource = ImagePipelineFactory.getInstance().getMainFileCache().getResource(cacheKey);
                localFile = ((FileBinaryResource) resource).getFile();
            } else if (ImagePipelineFactory.getInstance().getSmallImageFileCache().hasKey(cacheKey)) {
                BinaryResource resource = ImagePipelineFactory.getInstance().getSmallImageFileCache().getResource(cacheKey);
                localFile = ((FileBinaryResource) resource).getFile();
            }
        }
        return localFile;
    }

    /**
     * 拷贝缓存文件,指定目标路径和文件名
     * @param url
     * @param dir
     * @param fileName
     * @return
     */
    public static boolean copyCacheFile(String url,File dir,String fileName){
        File path = new File(dir,fileName);
      return   copyCacheFile(url,path);
    }

    /**
     *拷贝到某一个文件,已指定文件名
     * @param url 图片的完整url
     * @param path 目标文件路径
     * @return
     */
    public static boolean copyCacheFile(String url,File path){
        if (path == null ){
            return false;
        }
        File file = getFileFromDiskCache(url);
        if (file == null){
            return false;
        }

        if (path.isDirectory()){
            throw  new RuntimeException(path + "is a directory,you should call copyCacheFileToDir(String url,File dir)");
        }
        boolean isSuccess =   file.renameTo(path);

        return isSuccess;
    }

    /**
     * 拷贝到某一个目录中,自动命名
     * @param url
     * @param dir
     * @return
     */
    public static File copyCacheFileToDir(String url,File dir){

        if (dir == null ){
            return null;
        }
        if (!dir.isDirectory()){
            throw  new RuntimeException(dir + "is not a directory,you should call copyCacheFile(String url,File path)");
        }
        if (!dir.exists()){
            dir.mkdirs();
        }
        String fileName = URLUtil.guessFileName(url,"","");//android SDK 提供的方法.
        // 注意不能直接采用file的getName拿到文件名,因为缓存文件是用cacheKey命名的
        if (TextUtils.isEmpty(fileName)){
            fileName = UUID.randomUUID().toString();
        }
        File newFile = new File(dir,fileName);

       boolean isSuccess =  copyCacheFile(url,newFile);
        if (isSuccess){
            return newFile;
        }else {
            return null;
        }

    }

    /**
     *this method is return very fast, you can use it in UI thread.
     * @param url
     * @return 该url对应的图片是否已经缓存到本地
     */
    public static boolean isCached(String url) {

      // return Fresco.getImagePipeline().isInDiskCache(Uri.parse(url));

        ImageRequest imageRequest = ImageRequest.fromUri(url);
        CacheKey cacheKey = DefaultCacheKeyFactory.getInstance()
                .getEncodedCacheKey(imageRequest,null);
        return ImagePipelineFactory.getInstance()
                .getMainFileCache().hasKey(cacheKey);
    }





    /**
     * 文件下载到文件夹中：将图片缓存到本地后，将缓存的图片文件copy到另一个文件夹中
     *
     * 容易发生如下异常，progress在100处停留时间长
     * dalvikvm: Could not find method android.graphics.Bitmap.getAllocationByteCount,
     * referenced from method com.facebook.imageutils.BitmapUtil.getSizeInBytes
     06-21 16:15:39.547 3043-3244/com.hss01248.tools W/dalvikvm: VFY:
     unable to resolve virtual method 569: Landroid/graphics/Bitmap;.getAllocationByteCount ()I
     * @param url
     * @param context
     * @param dir 保存图片的文件夹
     * @param listener 自己定义的回调
     */
    public static void download(final String url, Context context, final File dir, final DownloadListener listener){
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(url))
                .build();

        final ImagePipeline imagePipeline = Fresco.getImagePipeline();


        DataSource<Void> dataSource = imagePipeline.prefetchToDiskCache(imageRequest, context, Priority.HIGH);
        dataSource.subscribe(new BaseDataSubscriber<Void>() {
            @Override
            protected void onNewResultImpl(DataSource<Void> dataSource) {


              File  file  =   copyCacheFileToDir(url,dir);
                clearCacheByUrl(url);//清除缓存
                if (file == null || !file.exists()){
                    listener.onFail();
                }else {
                    listener.onSuccess(file);
                }

            }

            @Override
            public void onProgressUpdate(DataSource<Void> dataSource) {
                super.onProgressUpdate(dataSource);
                listener.onProgress(dataSource.getProgress());
            }

            @Override
            protected void onFailureImpl(DataSource<Void> dataSource) {
                listener.onFail();
            }
        }, CallerThreadExecutor.getInstance());



    }

    /**
     * Created by hss01248 on 11/26/2015.
     */
    public static class MyImageCacheStatsTracker implements ImageCacheStatsTracker {
        @Override
        public void onBitmapCachePut() {

        }

        @Override
        public void onBitmapCacheHit(CacheKey cacheKey) {

        }

        @Override
        public void onBitmapCacheMiss() {

        }

        @Override
        public void onMemoryCachePut() {

        }

        @Override
        public void onMemoryCacheHit(CacheKey cacheKey) {

        }

        @Override
        public void onMemoryCacheMiss() {

        }

        @Override
        public void onStagingAreaHit(CacheKey cacheKey) {

        }

        @Override
        public void onStagingAreaMiss() {

        }

        @Override
        public void onDiskCacheHit(CacheKey cacheKey) {
            //Logger.e("ImageCacheStatsTracker---onDiskCacheHit");
        }

        @Override
        public void onDiskCacheMiss() {
            //Logger.e("ImageCacheStatsTracker---onDiskCacheMiss");
        }

        @Override
        public void onDiskCacheGetFail() {
            //Logger.e("ImageCacheStatsTracker---onDiskCacheGetFail");
        }

        @Override
        public void registerBitmapMemoryCache(CountingMemoryCache<?, ?> countingMemoryCache) {

        }

        @Override
        public void registerEncodedMemoryCache(CountingMemoryCache<?, ?> countingMemoryCache) {

        }
    }



    public interface BitmapListener{
        void onSuccess(Bitmap bitmap);
        void onFail();
    }

    public interface  DownloadListener{
        void onSuccess(File file);
        void onFail();

        void onProgress(float progress);
    }
}