package com.shay.base.urlimageviewhelper;

import android.content.Context;
import android.os.AsyncTask;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class HttpUrlDownloader implements UrlDownloader {
	private UrlImageViewHelper.RequestPropertiesCallback mRequestPropertiesCallback;

	public UrlImageViewHelper.RequestPropertiesCallback getRequestPropertiesCallback() {
		return mRequestPropertiesCallback;
	}

	public void setRequestPropertiesCallback(final UrlImageViewHelper.RequestPropertiesCallback callback) {
		mRequestPropertiesCallback = callback;
	}

	@Override
	public void download(final Context context, final String url, final String filename,
						 final UrlDownloaderCallback callback, final Runnable completion) {
		final AsyncTask<Void, Void, Void> downloader = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(final Void... params) {
				try {
					InputStream is = null;

					String thisUrl = url;
					//使用HttpURLConnection方式连接
					HttpURLConnection urlConnection;
					while (true) {
						final URL u = new URL(thisUrl);
						urlConnection = (HttpURLConnection) u.openConnection();
						urlConnection.setInstanceFollowRedirects(true);

						if (mRequestPropertiesCallback != null) {
							//通过回调得到请求头集合
							final ArrayList<NameValuePair> props = mRequestPropertiesCallback.getHeadersForRequest(context, url);
							if (props != null) {
								for (final NameValuePair pair : props) {
									//添加请求头
									urlConnection.addRequestProperty(pair.getName(), pair.getValue());
								}
							}
						}
                       //status code 301 302
						if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_MOVED_TEMP
								&& urlConnection.getResponseCode() != HttpURLConnection.HTTP_MOVED_PERM)
							break;
						thisUrl = urlConnection.getHeaderField("Location");
					}

					if (urlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
						UrlImageViewHelper.clog("Response Code: "
								+ urlConnection.getResponseCode());
						return null;
					}
					//如果都是ok的，得到网络输入流。
					is = urlConnection.getInputStream();
					//将流传出去供使用
					callback.onDownloadComplete(HttpUrlDownloader.this, is, null);
					return null;
				} catch (final Throwable e) {
					e.printStackTrace();
					return null;
				}
			}

			@Override
			protected void onPostExecute(final Void result) {
				completion.run();
			}
		};

		UrlImageViewHelper.executeTask(downloader);
	}

	//只有它可以分配预留内存
	@Override
	public boolean allowCache() {
		return true;
	}

	//http协议啊，其他不行，包括https??
	@Override
	public boolean canDownloadUrl(String url) {
		return url.startsWith("http");
	}
}
