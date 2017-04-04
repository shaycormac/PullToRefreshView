package com.shay.base.urlimageviewhelper;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import com.shay.base.utils.VolleyLog;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;

public final class UrlImageViewHelper {

    /**
     * UrlImageViewHelper 存放图片的默认文件夹
     */
    private static final String HOME_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()
            + File.separator + "shayCormac" + File.separator + "urlImage";

    /**
     * 存放图片的默认文件夹
     *
     * @return
     */
    public static String getHomeDirectory() {
        File dir = new File(HOME_DIR);
        if (!dir.exists() || !dir.isDirectory())
            dir.mkdirs();
        return HOME_DIR;
    }

    //打印相关日志
    static void clog(String format, Object... args) 
    {
        String log;
        if (args.length == 0)
            log = format;
        else
            log = String.format(format, args);
        if (Constants.LOG_ENABLED)
            Log.i(Constants.LOGTAG, log);
    }

    //拷贝字节，每次8个字节。返回执行的次数
    public static int copyStream(final InputStream input, final OutputStream output) throws IOException
    {
        final byte[] stuff = new byte[8192];
        int read;
        int total = 0;
        while ((read = input.read(stuff)) != -1) {
            output.write(stuff, 0, read);
            total += read;
        }
        return total;
    }
   //资源
    static Resources mResources;
    //屏幕分辨率
    static DisplayMetrics mMetrics;

    private static void prepareResources(final Context context) {
        //只有分辨率类没有才执行这个方法
        if (mMetrics != null) {
            return;
        }
        mMetrics = new DisplayMetrics();
        // final Activity act = (Activity)context;
        // act.getWindowManager().getDefaultDisplay().getMetrics(mMetrics);
        ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(mMetrics);
        final AssetManager mgr = context.getAssets();
        mResources = new Resources(mgr, mMetrics, context.getResources().getConfiguration());
    }
  //是否使用BitMap缩放，默认是的
    private static boolean mUseBitmapScaling = true;

    /**
     * Bitmap scaling will use smart/sane values to limit the maximum dimension
     * of the bitmap during decode. This will prevent any dimension of the
     * bitmap from being larger than the dimensions of the device itself. Doing
     * this will conserve memory.
     *
     * @param useBitmapScaling Toggle for smart resizing.
     */
    public static void setUseBitmapScaling(boolean useBitmapScaling) {
        mUseBitmapScaling = useBitmapScaling;
    }

    /**
     * Bitmap scaling will use smart/sane values to limit the maximum dimension
     * of the bitmap during decode. This will prevent any dimension of the
     * bitmap from being larger than the dimensions of the device itself. Doing
     * this will conserve memory.
     */
    public static boolean getUseBitmapScaling() {
        return mUseBitmapScaling;
    }

    /**
     * 加载图片
     *
     * @param context
     * @param url
     * @param filename
     * @param targetWidth
     * @param targetHeight
     * @param bArea        是否以面积的方式加载图片(对于长宽比严重失衡的图片非常有效) 尽量原图加载
     * @return
     */
    private static Bitmap loadBitmapFromStream(final Context context,
                                               final String url, final String filename, final int targetWidth,
                                               final int targetHeight, boolean bArea) 
    {
        prepareResources(context);

        // Log.v(Constants.LOGTAG,targetWidth);
        // Log.v(Constants.LOGTAG,targetHeight);
        InputStream stream = null;
        clog("Decoding: " + url + " " + filename);
        try {
            Options o = null;
            //使用图片压缩
            if (mUseBitmapScaling) 
            {
                o = new Options();
                o.inJustDecodeBounds = true;
                stream = new BufferedInputStream(new FileInputStream(filename), 8192);
                BitmapFactory.decodeStream(stream, null, o);
                VolleyLog.d("原图片的大小为：%d,%d",o.outWidth,o.outHeight);
                stream.close();
                int scale = 0;
                //不已面积加载
                if (!bArea) 
                {
                    //循环，scale不断增加，向右移动位数，和o.outWidth/targetWidth一样，只是这样计算机执行更快
                    while ((o.outWidth >> scale) > targetWidth || (o.outHeight >> scale) > targetHeight) 
                    {
                        scale++;
                    }
                    o = new Options();
                    //最终的缩放尺寸，1往左移动的位数（可能为0，即不缩放，或者2，4，8）
                    o.inSampleSize = 1 << scale;
                } 
                else 
                {
                    //以面积方式加载缩略图
                    float r = (o.outWidth * o.outHeight) / (float) (targetWidth * targetHeight);
                    o = new Options();
                    o.inSampleSize = (int) Math.floor(r);
                }

            }

            Bitmap bitmap = null;
            final int sampleSize = o.inSampleSize;
            while (true)
            {
                try {
                    stream = new BufferedInputStream(new FileInputStream(filename), 8192);
                    bitmap = BitmapFactory.decodeStream(stream, null, o);
                    clog(String.format("Loaded bitmap (%dx%d).", bitmap.getWidth(), bitmap.getHeight()));
                } catch (Throwable e) 
                {
                    e.printStackTrace();
                    bitmap = null;
                    o.inSampleSize++;
                    if (stream != null) 
                    {
                        try 
                        {
                            stream.close();
                            stream = null;
                        } catch (IOException e2) {
                        }
                    }
                    //溢出的情况下，缩放比例再次放大。
                    if (o.inSampleSize - sampleSize <= 3)
                        continue;
                    else
                        break;
                }
                break;
            }
            if (bitmap!=null)
                VolleyLog.d("最终返回的bitmap的大小为：%d--%d",bitmap.getWidth(),bitmap.getHeight());
            return bitmap;
        } catch (final IOException e)
        {
            return null;
        } finally 
        {
            if (stream != null) {
                try {
                    stream.close();
                    stream = null;
                } catch (IOException e) {
                    Log.w(Constants.LOGTAG, "Failed to close FileInputStream", e);
                }
            }
        }
    }

    public static final int CACHE_DURATION_INFINITE = Integer.MAX_VALUE;
    public static final int CACHE_DURATION_ONE_DAY = 1000 * 60 * 60 * 24;
    public static final int CACHE_DURATION_TWO_DAYS = CACHE_DURATION_ONE_DAY * 2;
    public static final int CACHE_DURATION_THREE_DAYS = CACHE_DURATION_ONE_DAY * 3;
    public static final int CACHE_DURATION_FOUR_DAYS = CACHE_DURATION_ONE_DAY * 4;
    public static final int CACHE_DURATION_FIVE_DAYS = CACHE_DURATION_ONE_DAY * 5;
    public static final int CACHE_DURATION_SIX_DAYS = CACHE_DURATION_ONE_DAY * 6;
    /**
     * 默认缓存是一周，超过这个时间，就进行清除（在放置图片方法里进行）
     */
    public static final int CACHE_DURATION_ONE_WEEK = CACHE_DURATION_ONE_DAY * 7;

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView       The {@link ImageView} to display the image to after it is
     *                        loaded.
     * @param url             The URL of the image that should be loaded.
     * @param defaultResource The Android resid of the {@link Drawable} that should be
     *                        displayed while the image is being downloaded.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url, final int defaultResource) 
    {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultResource, CACHE_DURATION_ONE_WEEK);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView} once it finishes loading.
     *
     * @param imageView The {@link ImageView} to display the image to after it is
     *                  loaded.
     * @param url       The URL of the image that should be loaded.
     */
    public static void setUrlDrawable(final ImageView imageView, final String url) 
    {
        setUrlDrawable(imageView.getContext(), imageView, url, null, CACHE_DURATION_ONE_WEEK, null);
    }

    /**
     * 2017/3/13 方法改进，以指定的宽高比压缩图片
     * @param imageView
     * @param url
     * @param requiredWidth 需求的宽度（像素值）
     * @param requiredHeight 需求的高度（像素值）
     */
    public static void setUrlDrawable(final ImageView imageView, final String url,
                                      int  requiredWidth, int  requiredHeight)
    {
        setUrlDrawable(imageView.getContext(), imageView, url, null, CACHE_DURATION_ONE_WEEK, false, null, requiredWidth, requiredHeight);
    }

    public static void loadUrlDrawable(final Context context, final String url) {
        setUrlDrawable(context, null, url, null, CACHE_DURATION_THREE_DAYS,
                null);
    }

    public static void setUrlDrawable(final ImageView imageView,
                                      final String url, final int defaultResource, final boolean bArea,
                                      final UrlImageViewCallback callback) {
        Drawable d = null;
        if (defaultResource != 0) {
            d = imageView.getResources().getDrawable(defaultResource);
        }
        setUrlDrawable(imageView.getContext(), imageView, url, d,
                CACHE_DURATION_ONE_WEEK, bArea, callback);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView       The {@link ImageView} to display the image to after it is
     *                        loaded.
     * @param url             The URL of the image that should be loaded.
     * @param defaultDrawable A {@link Drawable} that should be displayed in
     *                        {@code imageView} while the image has not been loaded. This
     *                        image will also be displayed if the image fails to load. This
     *                        can be set to {@code null}.
     */
    public static void setUrlDrawable(final ImageView imageView,
                                      final String url, final Drawable defaultDrawable) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable,
                CACHE_DURATION_ONE_WEEK, null);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView       The {@link ImageView} to display the image to after it is
     *                        loaded.
     * @param url             The URL of the image that should be loaded.
     * @param defaultResource The Android resid of the {@link Drawable} that should be
     *                        displayed while the image is being downloaded.
     * @param cacheDurationMs The length of time, in milliseconds, that this image should be
     *                        cached locally.
     */
    public static void setUrlDrawable(final ImageView imageView,
                                      final String url, final int defaultResource,
                                      final long cacheDurationMs) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultResource,
                cacheDurationMs);
    }

    public static void loadUrlDrawable(final Context context, final String url,
                                       final long cacheDurationMs) {
        setUrlDrawable(context, null, url, null, cacheDurationMs, null);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView       The {@link ImageView} to display the image to after it is
     *                        loaded.
     * @param url             The URL of the image that should be loaded.
     * @param defaultDrawable A {@link Drawable} that should be displayed in
     *                        {@code imageView} while the image has not been loaded. This
     *                        image will also be displayed if the image fails to load. This
     *                        can be set to {@code null}.
     * @param cacheDurationMs The length of time, in milliseconds, that this image should be
     *                        cached locally.
     */
    public static void setUrlDrawable(final ImageView imageView,
                                      final String url, final Drawable defaultDrawable,
                                      final long cacheDurationMs) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable, cacheDurationMs, null);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param context         A {@link Context} to allow setUrlDrawable to load and save
     *                        files.
     * @param imageView       The {@link ImageView} to display the image to after it is
     *                        loaded.
     * @param url             The URL of the image that should be loaded.
     * @param defaultResource The Android resid of the {@link Drawable} that should be
     *                        displayed while the image is being downloaded.
     * @param cacheDurationMs The length of time, in milliseconds, that this image should be
     *                        cached locally.
     */
    private static void setUrlDrawable(final Context context,
                                       final ImageView imageView, final String url,
                                       final int defaultResource, final long cacheDurationMs) {
        Drawable d = null;
        if (defaultResource != 0) {
            d = imageView.getResources().getDrawable(defaultResource);
        }
        setUrlDrawable(context, imageView, url, d, cacheDurationMs, null);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView       The {@link ImageView} to display the image to after it is
     *                        loaded.
     * @param url             The URL of the image that should be loaded.
     * @param defaultResource The Android resid of the {@link Drawable} that should be
     *                        displayed while the image is being downloaded.
     * @param callback        An instance of {@link UrlImageViewCallback} that is called
     *                        when the image successfully finishes loading. This value can
     *                        be null.
     */
    public static void setUrlDrawable(final ImageView imageView,
                                      final String url, final int defaultResource,
                                      final UrlImageViewCallback callback) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultResource,
                CACHE_DURATION_ONE_WEEK, callback);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView The {@link ImageView} to display the image to after it is
     *                  loaded.
     * @param url       The URL of the image that should be loaded.
     * @param callback  An instance of {@link UrlImageViewCallback} that is called
     *                  when the image successfully finishes loading. This value can
     *                  be null.
     */
    public static void setUrlDrawable(final ImageView imageView,
                                      final String url, final UrlImageViewCallback callback) {
        setUrlDrawable(imageView.getContext(), imageView, url, null, CACHE_DURATION_ONE_WEEK, callback);
    }

    public static void loadUrlDrawable(final Context context, final String url, final UrlImageViewCallback callback) {
        setUrlDrawable(context, null, url, null, CACHE_DURATION_ONE_WEEK, callback);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView       The {@link ImageView} to display the image to after it is
     *                        loaded.
     * @param url             The URL of the image that should be loaded.
     * @param defaultDrawable A {@link Drawable} that should be displayed in
     *                        {@code imageView} while the image has not been loaded. This
     *                        image will also be displayed if the image fails to load. This
     *                        can be set to {@code null}.
     * @param callback        An instance of {@link UrlImageViewCallback} that is called
     *                        when the image successfully finishes loading. This value can
     *                        be null.
     */
    public static void setUrlDrawable(final ImageView imageView,
                                      final String url, final Drawable defaultDrawable,
                                      final UrlImageViewCallback callback) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable,
                CACHE_DURATION_ONE_WEEK, callback);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView       The {@link ImageView} to display the image to after it is
     *                        loaded.
     * @param url             The URL of the image that should be loaded.
     * @param defaultResource The Android resid of the {@link Drawable} that should be
     *                        displayed while the image is being downloaded.
     * @param cacheDurationMs The length of time, in milliseconds, that this image should be
     *                        cached locally.
     * @param callback        An instance of {@link UrlImageViewCallback} that is called
     *                        when the image successfully finishes loading. This value can
     *                        be null.
     */
    public static void setUrlDrawable(final ImageView imageView,
                                      final String url, final int defaultResource,
                                      final long cacheDurationMs, final UrlImageViewCallback callback) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultResource,
                cacheDurationMs, callback);
    }

    public static void loadUrlDrawable(final Context context, final String url,
                                       final long cacheDurationMs, final UrlImageViewCallback callback) {
        setUrlDrawable(context, null, url, null, cacheDurationMs, callback);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param imageView       The {@link ImageView} to display the image to after it is
     *                        loaded.
     * @param url             The URL of the image that should be loaded.
     * @param defaultDrawable A {@link Drawable} that should be displayed in
     *                        {@code imageView} while the image has not been loaded. This
     *                        image will also be displayed if the image fails to load. This
     *                        can be set to {@code null}.
     * @param cacheDurationMs The length of time, in milliseconds, that this image should be
     *                        cached locally.
     * @param callback        An instance of {@link UrlImageViewCallback} that is called
     *                        when the image successfully finishes loading. This value can
     *                        be null.
     */
    public static void setUrlDrawable(final ImageView imageView,
                                      final String url, final Drawable defaultDrawable,
                                      final long cacheDurationMs, final UrlImageViewCallback callback) {
        setUrlDrawable(imageView.getContext(), imageView, url, defaultDrawable,
                cacheDurationMs, callback);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param context         A {@link Context} to allow setUrlDrawable to load and save
     *                        files.
     * @param imageView       The {@link ImageView} to display the image to after it is
     *                        loaded.
     * @param url             The URL of the image that should be loaded.
     * @param defaultResource The Android resid of the {@link Drawable} that should be
     *                        displayed while the image is being downloaded.
     * @param cacheDurationMs The length of time, in milliseconds, that this image should be
     *                        cached locally.
     * @param callback        An instance of {@link UrlImageViewCallback} that is called
     *                        when the image successfully finishes loading. This value can
     *                        be null.
     */
    private static void setUrlDrawable(final Context context,
                                       final ImageView imageView, final String url,
                                       final int defaultResource, final long cacheDurationMs,
                                       final UrlImageViewCallback callback) {
        Drawable d = null;
        if (defaultResource != 0) {
            d = imageView.getResources().getDrawable(defaultResource);
        }
        setUrlDrawable(context, imageView, url, d, cacheDurationMs, callback);
    }

    private static boolean isNullOrEmpty(final CharSequence s) {
        return (s == null || s.equals("") || s.equals("null") || s
                .equals("NULL"));
    }
  //仅在app首次运行，执行setUrl方法才运行清除文件里面的缓存图片，其他时候不运行
    private static boolean mHasCleaned = false;
  //以url的hashCode
    public static String getFilenameForUrl(final String url) 
    {
        return url.hashCode() + ".urlimage";
    }

    /**
     * 得到以url为key的图片所在文件夹的位置
     * @param url
     * @return
     */
    public static String getAbsolutePath(final String url) 
    {
        return getHomeDirectory() + File.separator + getFilenameForUrl(url);
    }

    /**
     * Clear out cached images.
     *
     * @param context
     * @param age     The max age of a file. Files older than this age will be
     *                removed.
     */
    public static void cleanup(final Context context, long age) 
    {
        //使用一个全局变量，目的是这个方法仅仅执行一次即可，仅在app运行的时候，执行一次，生命周期和app一样
        if (mHasCleaned) 
        {
            return;
        }
        mHasCleaned = true;
        try {
            // purge any *.urlimage files over a week old
            final String[] files = context.getFilesDir().list();
            if (files == null) {
                return;
            }
            for (final String file : files) {
                if (!file.endsWith(".urlimage")) {
                    continue;
                }

                final File f = new File(context.getFilesDir().getAbsolutePath() + '/' + file);
                //当前时间超过文件上次修改的时间和默认缓存时间的总和，就删除该文件
                if (System.currentTimeMillis() > f.lastModified() + age) {
                    f.delete();
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Clear out all cached images older than a week. The same as calling
     * cleanup(context, CACHE_DURATION_ONE_WEEK);
     * 默认清除一周未使用的内存卡上的图片
     *
     * @param context
     */
    public static void cleanup(final Context context) {
        cleanup(context, CACHE_DURATION_ONE_WEEK);
    }

    /**
     * 检测缓存是否还有效
     * @param file
     * @param cacheDurationMs
     * @return
     */
    private static boolean checkCacheDuration(File file, long cacheDurationMs) {
        return cacheDurationMs == CACHE_DURATION_INFINITE
                || System.currentTimeMillis() < file.lastModified() + cacheDurationMs;
    }

    /**
     * 返回缓存的Bitmap
     * @param url
     * @return
     */
    public static Bitmap getCachedBitmap(String url) 
    {
        if (url == null)
            return null;
        Bitmap ret = null;
        if (mDeadCache != null)
            ret = mDeadCache.get(url);
        if (ret != null)
            return ret;
        if (mLiveCache != null) {
            Drawable drawable = mLiveCache.get(url);
            if (drawable instanceof ZombieDrawable)
                return ((ZombieDrawable) drawable).getBitmap();
        }
        return null;
    }

    private static void setUrlDrawable(final Context context,
                                       final ImageView imageView, final String url,
                                       final Drawable defaultDrawable, final long cacheDurationMs,
                                       final UrlImageViewCallback callback) {
        setUrlDrawable(context, imageView, url, defaultDrawable, cacheDurationMs, false, callback);
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param context         A {@link Context} to allow setUrlDrawable to load and save
     *                        files.
     * @param imageView       The {@link ImageView} to display the image to after it is
     *                        loaded.
     * @param url             The URL of the image that should be loaded.
     * @param defaultDrawable A {@link Drawable} that should be displayed in
     *                        {@code imageView} while the image has not been loaded. This
     *                        image will also be displayed if the image fails to load. This
     *                        can be set to {@code null}.
     * @param cacheDurationMs The length of time, in milliseconds, that this image should be
     *                        cached locally.
     * @param callback        An instance of {@link UrlImageViewCallback} that is called
     *                        when the image successfully finishes loading. This value can
     *                        be null.
     * @param bArea           是否以面积的方式加载图片(对于长宽比严重失衡的图片非常有效)
     */
    private static void setUrlDrawable(final Context context,
                                       final ImageView imageView, final String url,
                                       final Drawable defaultDrawable, final long cacheDurationMs,
                                       final boolean bArea, final UrlImageViewCallback callback) 
    {
        setUrlDrawable(context,imageView,url,defaultDrawable,cacheDurationMs,false,callback,0,0);
       
    }

    /**
     * Download and shrink an Image located at a specified URL, and display it
     * in the provided {@link ImageView}.
     *
     * @param context         A {@link Context} to allow setUrlDrawable to load and save
     *                        files.
     * @param imageView       The {@link ImageView} to display the image to after it is
     *                        loaded.
     * @param url             The URL of the image that should be loaded.
     * @param defaultDrawable A {@link Drawable} that should be displayed in
     *                        {@code imageView} while the image has not been loaded. This
     *                        image will also be displayed if the image fails to load. This
     *                        can be set to {@code null}.
     * @param cacheDurationMs The length of time, in milliseconds, that this image should be
     *                        cached locally.
     * @param callback        An instance of {@link UrlImageViewCallback} that is called
     *                        when the image successfully finishes loading. This value can
     *                        be null.
     * @param bArea           是否以面积的方式加载图片(对于长宽比严重失衡的图片非常有效)
     *                        
     * @param requiredWidth 需要压缩成的宽度
     * @param requiredHeight 需要压缩成的高度
     */
    private static void setUrlDrawable(final Context context, final ImageView imageView, final String url,
                                       final Drawable defaultDrawable, final long cacheDurationMs,
                                       final boolean bArea, final UrlImageViewCallback callback, int requiredWidth, int requiredHeight)
    {
        assert (Looper.getMainLooper().getThread() == Thread.currentThread()) : "setUrlDrawable and loadUrlDrawable should only be called from the main thread.";
        //仅在这个方法第一次运行的时候，执行,清除一周未修改的图片（在下载的图片文件夹中寻找，当前时间和文件上次修改时间加上1周之和比对）
        cleanup(context);
        // disassociate this ImageView from any pending downloads
        //不合法的直接使用占位图，并从集合中清理出去即可
        if (isNullOrEmpty(url))
        {
            if (imageView != null) {
                mPendingViews.remove(imageView);
                //设置默认的图片加载位置
                if (defaultDrawable!=null)
                VolleyLog.d("默认的图片宽高：%d,%d",defaultDrawable.getIntrinsicWidth(),defaultDrawable.getIntrinsicHeight());
                //直接加载默认的图片，结束
                imageView.setImageDrawable(defaultDrawable);
            }
            return;
        }
        //屏幕的宽高
        final int tw;
        final int th;
        if (mMetrics == null)
            prepareResources(context);
        //重新判断
        if (requiredWidth == 0 || requiredHeight == 0) 
        {
   //不加参数，就默认采用屏幕宽高，其实并不建议这样，除非使用全屏视图，否则占用太大
            tw = mMetrics.widthPixels;
            th = mMetrics.heightPixels;
        }else 
        {
            //需要的宽高度
            tw = requiredWidth;
            th = requiredHeight;
        }

		/*
         * final String filename =
		 * context.getFileStreamPath(getFilenameForUrl(url)) .getAbsolutePath();
		 */
        final String filename = getHomeDirectory() + File.separator + getFilenameForUrl(url);
        final File file = new File(filename);

        // check the dead and live cache to see if we can find this url's bitmap
        if (mDeadCache == null) {
            //内存缓存
            mDeadCache = new LruBitmapCache(getHeapSize(context) / 8);
        }
        Drawable drawable = null;
        //获取内存缓存的bitmap
        Bitmap bitmap = mDeadCache.remove(url);
        if (bitmap != null) {
            clog("zombie load: " + url);
        } else {
            drawable = mLiveCache.get(url);
        }

        // if something was found, verify it was fresh.
        if (drawable != null || bitmap != null) 
        {
            clog("Cache hit on: " + url);
            // if the file age is older than the cache duration, force a
            // refresh.
            // note that the file must exist, otherwise it is using a default.
            // not checking for file existence would do a network call on every
            // 404 or failed load.
            if (file.exists() && !checkCacheDuration(file, cacheDurationMs))
            {
                clog("Cache hit, but file is stale. Forcing reload: " + url);
                if (drawable != null && drawable instanceof ZombieDrawable)
                    ((ZombieDrawable) drawable).headshot();
                drawable = null;
                bitmap = null;
            } else {
                clog("Using cached: " + url);
            }
        }

        // if the bitmap is fresh, set the imageview
        if (drawable != null || bitmap != null) {
            if (imageView != null) {
                mPendingViews.remove(imageView);
                if (drawable instanceof ZombieDrawable)
                    drawable = ((ZombieDrawable) drawable).clone(mResources);
                else if (bitmap != null)
                {

                    drawable = new ZombieDrawable(url, mResources, bitmap);
                    VolleyLog.d("bitMap的宽高分别为：%d,%d",bitmap.getWidth(),bitmap.getHeight());
                }
                //从内存缓存中直接取出使用，加载使用，结束。
                imageView.setImageDrawable(drawable);
            }
            // invoke any bitmap callbacks
            if (callback != null) {
                // when invoking the callback from cache, check to see if this
                // was
                // a drawable that was successfully loaded from the filesystem
                // or url.
                // this will be indicated by it being a ZombieDrawable (ie,
                // something we are
                // managing).
                // The default drawables will be BitmapDrawables (or whatever
                // else the user passed
                // in).
                if (bitmap == null && drawable instanceof ZombieDrawable)
                    bitmap = ((ZombieDrawable) drawable).getBitmap();
                //调用回掉方法，说明这张图片是从内存缓存中取出来的
                callback.onLoaded(imageView, bitmap, url, true);
            }
            //结束
            return;
        }

        // oh noes, at this point we definitely do not have the file available
        // in memory
        // let's prepare for an asynchronous load of the image.

        // null it while it is downloading
        // since listviews reuse their views, we need to
        // take note of which url this view is waiting for.
        // This may change rapidly as the list scrolls or is filtered, etc.
        clog("Waiting for " + url + " " + imageView);
        if (imageView != null) 
        {
            //先使用默认的缓存图片占位，等待图片加载完毕（调用sd卡或者网络）
            imageView.setImageDrawable(defaultDrawable);
            mPendingViews.put(imageView, url);
        }

        final ArrayList<ImageView> currentDownload = mPendingDownloads.get(url);
        if (currentDownload != null && currentDownload.size() != 0) {
            // Also, multiple vies may be waiting for this url.
            // So, let's maintain a list of these views.
            // When the url is downloaded, it sets the imagedrawable for
            // every view in the list. It needs to also validate that
            // the imageview is still waiting for this url.
            if (imageView != null) {
                currentDownload.add(imageView);
            }
            return;
        }

        final ArrayList<ImageView> downloads = new ArrayList<ImageView>();
        if (imageView != null) {
            downloads.add(imageView);
        }
        mPendingDownloads.put(url, downloads);
        //获取想要缩放的宽高比例（可以看出，不出意外，采用屏幕的宽高）
        final int targetWidth = tw <= 0 ? Integer.MAX_VALUE : tw;
        final int targetHeight = th <= 0 ? Integer.MAX_VALUE : th;
        //回调
        final Loader loader = new Loader() {
            @Override
            public void onDownloadComplete(UrlDownloader downloader, InputStream in, String existingFilename) {
                try {
                    assert (in == null || existingFilename == null);
                    if (in == null && existingFilename == null)
                        return;
                    String targetFilename = filename;
                    if (in != null) 
                    {
                        //网络流或者其他资源类
                        in = new BufferedInputStream(in, 8192);
                        //文件filename里面有东西了
                        OutputStream fout = new BufferedOutputStream(new FileOutputStream(filename), 8192);
                        copyStream(in, fout);
                        fout.close();
                    } else 
                    {
                        //说明是从文件加载过来的
                        targetFilename = existingFilename;
                    }
                    //压缩图片，默认采用屏幕的宽高比例为基准进行缩放（即自身得到宽高比例除以屏幕的宽高）
                    VolleyLog.d("压缩的宽高比例分别为：%d,%d",targetWidth,targetHeight);
                    result = loadBitmapFromStream(context, url, targetFilename,
                            targetWidth, targetHeight, bArea);
                } catch (final Exception ex) 
                {
                    // always delete busted files when we throw.
                    new File(filename).delete();
                    if (Constants.LOG_ENABLED)
                        Log.e(Constants.LOGTAG, "Error loading " + url, ex);
                } finally 
                {
                    // if we're not supposed to cache this thing, delete the
                    // temp file.除了网络加载的图片。
                    if (downloader != null && !downloader.allowCache())
                        new File(filename).delete();
                }
            }
        };

        final Runnable completion = new Runnable() {
            @Override
            public void run() {
                assert (Looper.myLooper().equals(Looper.getMainLooper()));
                //得到压缩过的图片
                Bitmap bitmap = loader.result;
                Drawable usableResult = null;
                if (bitmap != null) 
                {
                    usableResult = new ZombieDrawable(url, mResources, bitmap);
                }
                if (usableResult == null) 
                {
                    clog("No usable result, defaulting " + url);
                    new File(filename).delete();
                    usableResult = defaultDrawable;
//					mLiveCache.put(url, usableResult);//错误图片不添加到缓存
                }

                mPendingDownloads.remove(url);
                // mLiveCache.put(url, usableResult);
                if (callback != null && imageView == null)
                    //说明不是从缓存中取出来的
                    callback.onLoaded(null, loader.result, url, false);
                int waitingCount = 0;
                for (final ImageView iv : downloads) {
                    // validate the url it is waiting for
                    final String pendingUrl = mPendingViews.get(iv);
                    if (!url.equals(pendingUrl)) {
                        clog("Ignoring out of date request to update view for "
                                + url + " " + pendingUrl + " " + iv);
                        continue;
                    }
                    waitingCount++;
                    mPendingViews.remove(iv);
                    if (usableResult != null) {
                        // System.out.println(String.format("imageView: %dx%d, %dx%d",
                        // imageView.getMeasuredWidth(),
                        // imageView.getMeasuredHeight(),
                        // imageView.getWidth(), imageView.getHeight()));
                        //设置图图片
                        iv.setImageDrawable(usableResult);
                        // System.out.println(String.format("imageView: %dx%d, %dx%d",
                        // imageView.getMeasuredWidth(),
                        // imageView.getMeasuredHeight(),
                        // imageView.getWidth(), imageView.getHeight()));
                        // onLoaded is called with the loader's result (not what
                        // is actually used).
                        // null indicates failure.
                    }
                    if (callback != null && iv == imageView)
                        callback.onLoaded(iv, loader.result, url, false);
                }
                clog("Populated: " + waitingCount);
            }
        };
        //图片在sd卡中
        if (file.exists()) 
        {
            try {
                if (checkCacheDuration(file, cacheDurationMs)) {
                    clog("File Cache hit on: " + url + ". " + (System.currentTimeMillis() - file.lastModified()) + "ms old.");

                    final AsyncTask<Void, Void, Void> fileloader = new AsyncTask<Void, Void, Void>()
                    {
                        @Override
                        protected Void doInBackground(final Void... params) {
                            loader.onDownloadComplete(null, null, filename);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(final Void result) {
                            completion.run();
                        }
                    };
                    executeTask(fileloader);
                    return;
                } else {
                    clog("File cache has expired. Refreshing.");
                }
            } catch (final Exception ex) {
            }
        }

        for (UrlDownloader downloader : mDownloaders) {
            if (downloader.canDownloadUrl(url)) {
                downloader.download(context, url, filename, loader, completion);
                return;
            }
        }
      //上面都错误的情况下，加载默认的图片
        imageView.setImageDrawable(defaultDrawable);
    }

    private static abstract class Loader implements UrlDownloader.UrlDownloaderCallback 
    {
        Bitmap result;
    }

    private static abstract class FileLoader implements
            UrlDownloader.UrlDownloaderCallback {
        File file;
    }

    public static void downloadPicFile(final Context context, final String url,
                                       final ImageFileCallback callback) {
        downloadPicFile(context, url, CACHE_DURATION_ONE_WEEK, callback);
    }

    /**
     * 下载图片文件可能与直接使用 setUrlDrawable下载重复
     *
     * @param context
     * @param url
     * @param cacheDurationMs
     * @param callback
     */
    public static void downloadPicFile(final Context context, final String url,
                                       final long cacheDurationMs, final ImageFileCallback callback) {
        assert (Looper.getMainLooper().getThread() == Thread.currentThread()) : "should only be called from the main thread.";
        cleanup(context);
        if (isNullOrEmpty(url)) {
            return;
        }

        final String filename = getHomeDirectory() + File.separator
                + getFilenameForUrl(url);
        final File file = new File(filename);
        if (file.exists() && file.isFile()) {
            if (checkCacheDuration(file, cacheDurationMs)) {
                if (callback != null)
                    callback.onLoaded(file, url, true);
                return;
            }

        }

        ArrayList<ImageFileCallback> callbacks = mPendingFileDownloads.get(url);
        if (callbacks == null || callbacks.isEmpty()) {
            callbacks = new ArrayList<ImageFileCallback>();
            callbacks.add(callback);
            mPendingFileDownloads.put(url, callbacks);
        } else {
            callbacks.add(callback);
            mPendingFileDownloads.put(url, callbacks);
            return;
        }

        final FileLoader loader = new FileLoader() {
            @Override
            public void onDownloadComplete(UrlDownloader downloader,
                                           InputStream in, String existingFilename) {
                try {
                    assert (in == null || existingFilename == null);
                    if (in == null && existingFilename == null)
                        return;
                    String targetFilename = filename;
                    if (in != null) {
                        in = new BufferedInputStream(in, 8192);
                        OutputStream fout = new BufferedOutputStream(
                                new FileOutputStream(filename), 8192);
                        copyStream(in, fout);
                        fout.close();
                    } else {
                        targetFilename = existingFilename;
                    }
                    this.file = new File(targetFilename);
                } catch (final Exception ex) {
                    // always delete busted files when we throw.
                    new File(filename).delete();
                    if (Constants.LOG_ENABLED)
                        Log.e(Constants.LOGTAG, "Error loading " + url, ex);
                } finally {
                    // if we're not supposed to cache this thing, delete the
                    // temp file.
                    if (downloader != null && !downloader.allowCache())
                        new File(filename).delete();
                }
            }
        };

        final Runnable completion = new Runnable() {
            @Override
            public void run() {
                assert (Looper.myLooper().equals(Looper.getMainLooper()));

                ArrayList<ImageFileCallback> callbacks = mPendingFileDownloads
                        .remove(url);
                if (callbacks != null && !callbacks.isEmpty())
                    for (ImageFileCallback cb : callbacks)
                        if (cb != null)
                            cb.onLoaded(loader.file, url, false);

            }
        };

        for (UrlDownloader downloader : mDownloaders) {
            if (downloader.canDownloadUrl(url)) {
                downloader.download(context, url, filename, loader, completion);
                return;
            }
        }

    }

    private static HttpUrlDownloader mHttpDownloader = new HttpUrlDownloader();
    private static ContentUrlDownloader mContentDownloader = new ContentUrlDownloader();
    private static ContactContentUrlDownloader mContactDownloader = new ContactContentUrlDownloader();
    private static AssetUrlDownloader mAssetDownloader = new AssetUrlDownloader();
    private static FileUrlDownloader mFileDownloader = new FileUrlDownloader();
    private static ArrayList<UrlDownloader> mDownloaders = new ArrayList<UrlDownloader>();

    public static ArrayList<UrlDownloader> getDownloaders() {
        return mDownloaders;
    }
  //静态代码块
    static {
        mDownloaders.add(mHttpDownloader);
        mDownloaders.add(mContactDownloader);
        mDownloaders.add(mContentDownloader);
        mDownloaders.add(mAssetDownloader);
        mDownloaders.add(mFileDownloader);
    }

    /**
     * 静态的请求参数回调接口，将请求头相关信息传出去
     */
    public static interface RequestPropertiesCallback {
        //以键值对来接受请求头参数
        public ArrayList<NameValuePair> getHeadersForRequest(Context context, String url);
    }

    private static RequestPropertiesCallback mRequestPropertiesCallback;

    public static RequestPropertiesCallback getRequestPropertiesCallback() {
        return mRequestPropertiesCallback;
    }

    public static void setRequestPropertiesCallback(
            final RequestPropertiesCallback callback) {
        mRequestPropertiesCallback = callback;
    }

    private static DrawableCache mLiveCache = DrawableCache.getInstance();
    private static LruBitmapCache mDeadCache;
    private static HashSet<Bitmap> mAllCache = new HashSet<Bitmap>();

    private static int getHeapSize(final Context context) {
        return ((ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass() * 1024 * 1024;
    }

    /***
     * Remove a url from the cache
     *
     * @param url
     * @return The bitmap removed, if any.
     */
    public static Bitmap remove(String url) {
        new File(getFilenameForUrl(url)).delete();

        Drawable drawable = mLiveCache.remove(url);
        if (drawable instanceof ZombieDrawable) {
            ZombieDrawable zombie = (ZombieDrawable) drawable;
            Bitmap ret = zombie.getBitmap();
            zombie.headshot();
            return ret;
        }

        return null;
    }

    /***
     * Remove a url from both memory and sdcard cache
     *
     * @param url
     * @param context
     * @return The bitmap removed, if any.
     */
    public static Bitmap remove_(Context context, String url) {
        final String fileName = getFilenameForUrl(url);
        // final String filePath =
        // context.getFileStreamPath(fileName).getAbsolutePath();
        final String filePath = getHomeDirectory() + File.separator + fileName;
        new File(filePath).delete();

        Drawable drawable = mLiveCache.remove(url);
        if (drawable instanceof ZombieDrawable) {
            ZombieDrawable zombie = (ZombieDrawable) drawable;
            Bitmap ret = zombie.getBitmap();
            zombie.headshot();
            return ret;
        }

        return null;
    }

    /***
     * ZombieDrawable refcounts Bitmaps by hooking the finalizer.
     */
    private static class ZombieDrawable extends BitmapDrawable {
        private static class Brains {
            int mRefCounter;
            boolean mHeadshot;
        }

        public ZombieDrawable(final String url, Resources resources,
                              final Bitmap bitmap) {
            this(url, resources, bitmap, new Brains());
        }

        Brains mBrains;

        private ZombieDrawable(final String url, Resources resources,
                               final Bitmap bitmap, Brains brains) {
            super(resources, bitmap);
            mUrl = url;
            mBrains = brains;

            mAllCache.add(bitmap);
            mDeadCache.remove(url);
            mLiveCache.put(url, this);

            mBrains.mRefCounter++;
        }

        public ZombieDrawable clone(Resources resources) {
            return new ZombieDrawable(mUrl, resources, getBitmap(), mBrains);
        }

        String mUrl;

        @Override
        protected void finalize() throws Throwable {
            super.finalize();

            mBrains.mRefCounter--;
            if (mBrains.mRefCounter == 0) {
                if (!mBrains.mHeadshot)
                    mDeadCache.put(mUrl, getBitmap());
                mAllCache.remove(getBitmap());
                mLiveCache.remove(mUrl);
                clog("Zombie GC event " + mUrl);
            }
        }

        // kill this zombie, forever.
        public void headshot() {
            clog("BOOM! Headshot: " + mUrl);
            mBrains.mHeadshot = true;
            mLiveCache.remove(mUrl);
            mAllCache.remove(getBitmap());
        }
    }
   //执行异步加载图片
    static void executeTask(final AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT < Constants.HONEYCOMB) {
            task.execute();
        } else {
            executeTaskHoneycomb(task);
        }
    }

    @TargetApi(Constants.HONEYCOMB)
    private static void executeTaskHoneycomb(
            final AsyncTask<Void, Void, Void> task) {
        //大于api11之后采用线程池的加载。
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public static int getPendingDownloads() {
        return mPendingDownloads.size();
    }

    private static Hashtable<ImageView, String> mPendingViews = new Hashtable<ImageView, String>();
    private static Hashtable<String, ArrayList<ImageView>> mPendingDownloads = new Hashtable<String, ArrayList<ImageView>>();
    /**
     * 单独下载图片，与ImageView无关联
     */
    private static Hashtable<String, ArrayList<ImageFileCallback>> mPendingFileDownloads = new Hashtable<String, ArrayList<ImageFileCallback>>();
}
