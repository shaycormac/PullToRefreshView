package com.shay.base.urlimageviewhelper;

import java.io.File;

public interface ImageFileCallback {

	/**
	 * 
	 * @param picFile
	 * @param url
	 * @param loadedFromCache
	 */
	void onLoaded(File picFile, String url, boolean loadedFromCache);
	
}
