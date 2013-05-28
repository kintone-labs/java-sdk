//   Copyright 2013 Cybozu
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package com.cybozu.kintone.database;

/**
 * A data transfer object represents the file.
 */
public class FileDto {
    private String contentType;
    private String url;
    private String fileKey;
    private String name;
    private long size;

    /**
     * @return content type
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * @param contentType
     *            content type
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * @return file url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url
     *            file url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return file key
     */
    public String getFileKey() {
        return fileKey;
    }

    /**
     * @param fileKey
     *            file key
     */
    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    /**
     * @return file name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            file name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return file size
     */
    public long getSize() {
        return size;
    }

    /**
     * @param size
     *            file size
     */
    public void setSize(long size) {
        this.size = size;
    }
}
