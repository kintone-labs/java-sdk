package com.cybozu.kintone.database;

/**
 * A data transfer object represents the mention data
 */
public class MentionDto {
	private String code;
    private String type;
    
    public MentionDto() {
    	
    }
    public MentionDto(String code, String type) {
    	this.code = code;
    	this.type = type;
    }
    
    /**
     * @return user/organization/group code
     */
	public String getCode() {
		return code;
	}
	
	/**
     * @param code
     *            user/organization/group code
     */
	public void setCode(String code) {
		this.code = code;
	}
	
	/**
     * @return "USER", "ORGANIZATION" or "GROUP"
     */

	public String getType() {
		return type;
	}
	
	/**
     * @param type
     * @return "USER", "ORGANIZATION" or "GROUP"
     */
	public void setType(String type) {
		this.type = type;
	}
    
    

}
