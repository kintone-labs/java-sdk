package com.cybozu.kintone.database;

import java.io.InputStream;

import com.cybozu.kintone.database.exception.DBException;

/**
 * Implemented class of lazy file uploader for using InputStream.
 *
 */
public class InputStreamLazyUploader implements LazyUploader {

    private InputStream file;
    private String fileName;
    private String contentType;
    
    InputStreamLazyUploader(InputStream file, String fileName, String contentType) {
        this.file = file;
        this.fileName = fileName;
        this.contentType = contentType;
    }
    
    @Override
    public String upload(Connection conn) throws DBException {
       return conn.uploadFile(contentType, file, fileName);
    }

}
