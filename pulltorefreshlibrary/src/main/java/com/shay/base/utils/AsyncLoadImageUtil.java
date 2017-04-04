package com.shay.base.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.ImageView;
import com.shay.base.urlimageviewhelper.ImageFileCallback;
import com.shay.base.urlimageviewhelper.UrlImageViewCallback;
import com.shay.base.urlimageviewhelper.UrlImageViewHelper;


/**
 * Created by Android2 on 2016/3/11.
 */
public class AsyncLoadImageUtil {
    /**
     * 异步加载图片
     *
     * @param imageView       ImageView控件
     * @param url             图片url
     * @param defaultResource 默认图片的 resourceId
     */
    public static void setUrlDrawable(final ImageView imageView, final String url, final int defaultResource) {
        UrlImageViewHelper.setUrlDrawable(imageView, url, defaultResource);
    }

    /**
     * 异步加载图片
     *
     * @param imageView ImageView控件
     * @param url       图片url
     */
    public static void setUrlDrawable(final ImageView imageView,
                                      final String url) {
        UrlImageViewHelper.setUrlDrawable(imageView, url);
    }

    /**
     * 设置本地file文件path图片
     *
     * @param imageView
     * @param path      本地图片path
     */
    public static void setLocalDrawable(ImageView imageView, String path) 
    {
       setLocalDrawable(imageView,path,0,0);
    }
    /**
     * 设置本地file文件path图片
     *
     * @param imageView
     * @param path      本地图片path
     */
    public static void setLocalDrawable(ImageView imageView, String path, int  requiredWidth, int  requiredHeight) 
    {
        if (!TextUtils.isEmpty(path) && path.startsWith("/")) {
            path = "file://" + path;
        }
        UrlImageViewHelper.setUrlDrawable(imageView, path,requiredWidth,requiredHeight);
    }

    /**
     * 异步加载图片
     *
     * @param imageView
     * @param url
     * @param defaultResource
     * @param bArea           是否以面积的方式加载图片(对于长宽比严重失衡的图片非常有效)
     * @param callback
     */
    public static void setUrlDrawable(final ImageView imageView,
                                      final String url, final int defaultResource, final boolean bArea,
                                      final UrlImageViewCallback callback) {
        UrlImageViewHelper.setUrlDrawable(imageView, url, defaultResource, bArea, callback);
    }

    /**
     * 异步加载图片
     *
     * @param imageView
     * @param url
     * @param defaultDrawable
     */
    public static void setUrlDrawable(final ImageView imageView,
                                      final String url, final Drawable defaultDrawable) {
        UrlImageViewHelper.setUrlDrawable(imageView, url, defaultDrawable);
    }


    /**
     * 移除本地图片Cache缓存
     *
     * @param path
     */
    public static void removeLocalDrawableCache(String path) {
        if (!TextUtils.isEmpty(path) && path.startsWith("/")) {
            path = "file://" + path;
        }
        UrlImageViewHelper.remove(path);
    }

    /**
     * 移除网络图片Cache缓存
     *
     * @param url 网络图片Url
     */
    public static void removeUrlDrawableCache(String url) {
        UrlImageViewHelper.remove(url);
    }


    /**
     * 下载图片文件，不与ImageView绑定
     *
     * @param context
     * @param picFilePath
     * @param callback
     */
    public static void downloadPicFile(Context context, String picFilePath, ImageFileCallback callback) {
        UrlImageViewHelper.downloadPicFile(context, picFilePath, callback);
    }

    /**
     * 获取url对应的文件路径
     *
     * @param url
     * @return
     */
    public static String getAbsolutePath(String url) {
        return UrlImageViewHelper.getAbsolutePath(url);
    }
}
