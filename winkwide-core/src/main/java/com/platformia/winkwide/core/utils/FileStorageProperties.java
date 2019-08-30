package com.platformia.winkwide.core.utils;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {

	private String uploadDir;
    
//    private String downloadServer;

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

/*	public String getDownloadServer() {
		return downloadServer;
	}

	public void setDownloadServer(String downloadServer) {
		this.downloadServer = downloadServer;
	}
*/    
    
}