package com.shay.base.urlimageviewhelper;

import android.content.Context;

import java.io.InputStream;

/**
 * 实现所有图片加载基类
 */
public interface UrlDownloader {
	//静态的回调接口，加载图片完毕后，调用这个返回相应的接受类。可能是流或者文件
	public static interface UrlDownloaderCallback {
		public void onDownloadComplete(UrlDownloader downloader, InputStream in, String filename);
	}

	/**
	 * 加载图片
	 * @param context
	 * @param url 地址
	 * @param filename 文件名
	 * @param callback 回掉接口
     * @param completion 运行所在的线程
     */
	public void download(Context context, String url, String filename, UrlDownloaderCallback callback, Runnable completion);

	public boolean allowCache();

	/**
	 * 是否可以加载
	 * @param url
     * @return
     */
	public boolean canDownloadUrl(String url);
}