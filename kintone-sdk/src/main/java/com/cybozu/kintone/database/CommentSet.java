package com.cybozu.kintone.database;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * CommentSet class Represents a data set of the comments. The data set is
 * read only.
 * 
 */
public class CommentSet {
    private List<Comment> comments = new ArrayList<Comment>();
    private int index = 0;
    private Comment current = null;
    private boolean newer = false;
    private boolean older = false;
    
    public CommentSet() {
    }
    
    public void setNewer(boolean newer) {
    	this.newer = newer;
    }
    
    public void setOlder(boolean older) {
    	this.older = older;
    }
    
    /**
     * Adds a new comment.
     * 
     * @param comment
     *            comment object
     */
    public void add(Comment comment) {
        comments.add(comment);
    }

    /**
     * Clears the result set.
     */
    public void clear() {
    	comments.clear();
        index = 0;
        current = null;
    }

    /**
     * Moves to the previous comment.
     * 
     * @return true if succeeded
     */
    public boolean previous() {
        if (index == 0)
            return false;
        current = comments.get(index);
        index--;
        return true;
    }

    /**
     * Moves to the next comment.
     * 
     * @return true if succeeded
     */
    public boolean next() {
        if (index >= comments.size())
            return false;
        current = comments.get(index);
        index++;
        return true;
    }

    /**
     * Moves to the first comment.
     * 
     * @return true if succeeded
     */
    public boolean first() {
        if (size() == 0)
            return false;
        index = 0;
        current = comments.get(index);
        return true;
    }

    /**
     * Moves to the last comment.
     * 
     * @return true if succeeded
     */
    public boolean last() {
        if (size() == 0)
            return false;
        index = size() - 1;
        current = comments.get(index);
        return true;
    }

    /**
     * Gets the id number of the comment.
     * 
     * @return comment id
     */
    public Long getId() {
        return current.getId();
    }
    
    /**
     * Gets the count of the comment set.
     * 
     * @return record count
     */
    public int size() {
        return comments.size();
    }
    
    /**
     * Gets the comment text of current comment
     * @return comment text
     */
	public String getText() {
		return current.getText();
	}
	
	/**
	 * Gets the comment created at
     * @return comment created at
     */
	public Date getCreatedAt() {
		return current.getCreatedAt();
	}
	
	/**
	 * Gets the comment creator
     * @return creator object
     */
	public UserDto getCreator() {
		return current.getCreator();
	}
	
	/**
	 * Gets the mentions of the current comment
     * @return list of the mentions of the comment
     */
	public List<MentionDto> getMentions() {
		return current.getMentions();
	}

	/**
	 * Gets whether a newer comment exists
     * @return boolean
     */
	public boolean hasNewer() {
		return this.newer;
	}

	/**
	 * Gets whether an older comment exists
     * @return boolean
     */
	public boolean hasOlder() {
		return this.older;
	}

}
