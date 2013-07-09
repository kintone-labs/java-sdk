package com.cybozu.kintone.database;

import java.io.File;

import com.cybozu.kintone.database.exception.DBException;

/**
 * Implemented class of lazy file uploader for using File.
 *
 */
public class FileLazyUploader implements LazyUploader {

    private File file;
    private String contentType;
    
    FileLazyUploader(File file, String contentType) {
        this.file = file;
        this.contentType = contentType;
    }
    
    @Override
    public String upload(Connection conn) throws DBException {
       return conn.uploadFile(file, contentType);
    }

}
