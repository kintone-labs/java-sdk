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

package com.cybozu.kintone.database.exception;

import com.cybozu.kintone.database.ErrorResponse;

public class DBException extends Exception {
    /**
     * An exception that provides information on kintone database access errors.
     */
    private static final long serialVersionUID = 8036246942261521021L;
    private ErrorResponse error;
    private int httpStatus;
    
    public DBException() {
        super();
    }

    public DBException(Throwable e) {
        super(e);
    }
    
    public DBException(Throwable e, String message) {
        super(message, e);
    }
    
    public DBException(String message) {
        super(message);
    }
    
    public DBException(int httpStatus, ErrorResponse error) {
        super(error.getMessage());
        this.error = error;
        this.httpStatus = httpStatus;
    }
    
    public ErrorResponse getErrorResponse() {
        return error;
    }
    
    public int getHttpStatus() {
    	return httpStatus;
    }
    
    @Override
    public String toString() {
        if (error == null) return super.toString();
        StringBuilder sb = new StringBuilder();
        sb.append("id: " + error.getId());
        sb.append(", code: " + error.getCode());
        sb.append(", message: " + error.getMessage());
        sb.append(", status: " + this.getHttpStatus());
        
        return sb.toString();
    }
}
