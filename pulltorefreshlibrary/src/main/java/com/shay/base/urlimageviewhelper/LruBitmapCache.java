package com.shay.base.urlimageviewhelper;

import android.graphics.Bitmap;

public class LruBitmapCache extends LruCache<String, Bitmap> {
	public LruBitmapCache(int maxSize) {
		super(maxSize);
	}
	@Override
	protected int sizeOf(String key, Bitmap value) {
		//1、getRowBytes：Since API Level 1，用于计算位图每一行所占用的内存字节数。
		//2、getByteCount：Since API Level 12，用于计算位图所占用的内存字节数
		//经实测发现：getByteCount() = getRowBytes() * getHeight()，
		// 也就是说位图所占用的内存空间数等于位图的每一行所占用的空间数乘以位图的行数。
		//因为getByteCount要求的API版本较高，因此对于使用较低版本的开发者，在计算位图所占空间时上面的方法或许有帮助
		return value.getRowBytes() * value.getHeight();
	}
}
