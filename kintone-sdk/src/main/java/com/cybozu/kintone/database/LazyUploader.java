package com.cybozu.kintone.database;

import com.cybozu.kintone.database.exception.DBException;

/**
 * An interface wrapped lazy file uploading.
 *
 */
public interface LazyUploader {

    /**
     * Upload file
     * @param conn
     *            the connection object
     * @return file key
     * @throws DBException
     */
    public String upload(Connection conn) throws DBException;
}
