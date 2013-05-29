package com.cybozu.kintone.database;

/**
 * Represents an error response by kintone
 *
 */
public class ErrorResponse {
    private String message;
    private String id;
    private String code;
    
    /**
     * @return error message
     */
    public String getMessage() {
        return message;
    }
    
    /**
     * @param message
     *             an error message
     */
    public void setMessage(String message) {
        this.message = message;
    }
    /**
     * @return error id
     */
    public String getId() {
        return id;
    }
    /**
     * @param id
     *           error id
     */
    public void setId(String id) {
        this.id = id;
    }
    /**
     * @return error code
     */
    public String getCode() {
        return code;
    }
    /**
     * @param code
     *            error code
     */
    public void setCode(String code) {
        this.code = code;
    }

    
}
