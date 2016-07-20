package com.cybozu.kintone.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A comment object represents a row of the record comment.
 * 
 */
public class Comment {
	private long id;
    private String text;
    private Date createdAt;
    private UserDto creator;
    private List<MentionDto> mentions = new ArrayList<MentionDto>();
    
    public Comment(long id, String text, Date createdAt, UserDto creator, List<MentionDto> mentions) {
        this.id = id;
        this.text = text;
        this.creator = creator;
        this.mentions = mentions;
    }
    
    /**
     * @return comment id
     */
	public long getId() {
		return id;
	}

	/**
     * @return comment text
     */
	public String getText() {
		return text;
	}
	
	/**
     * @return comment created at
     */
	public Date getCreatedAt() {
		return createdAt;
	}
	
	/**
     * @return creator object
     */
	public UserDto getCreator() {
		return creator;
	}
	
	/**
     * @return list of the mentions of the comment
     */
	public List<MentionDto> getMentions() {
		return mentions;
	}
}
