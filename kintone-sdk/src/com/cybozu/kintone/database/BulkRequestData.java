//   Copyright 2014 Cybozu
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
 * Bulk request data.
 * 
 */
public class BulkRequestData {
    private String method;
    private String api;
    private String payload;
    
    
    /**
     * Constructor
     */
    public BulkRequestData(String method, String api, String payload) {
        this.method = method;
        this.api = api;
        this.payload = payload;
    }
    
    /**
     * @return method
     */
    public String getMethod() {
        return method;
    }
    /**
     * @return api
     */
    public String getApi() {
        return api;
    }
    /**
     * @return payload
     */
    public String getPayload() {
        return payload;
    }    
}
