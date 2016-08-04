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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cybozu.kintone.database.exception.DBException;

/**
 * Resultset class Represents a kintone database result set. The data set is
 * read only.
 * 
 */
public class ResultSet {

    private List<Record> records = new ArrayList<Record>();
    private int index = 0;
    private Long totalCount = null;
    private Record current = null;
    private Connection connection = null;

    /**
     * @param connection
     *            The database connection which retrieved this result set.
     */
    public ResultSet(Connection connection) {
        this.connection = connection;
    }

    /**
     * Adds a new record.
     * 
     * @param record
     *            record object
     */
    public void add(Record record) {
        records.add(record);
    }

    /**
     * Clears the result set.
     */
    public void clear() {
        records.clear();
        index = 0;
        current = null;
    }

    /**
     * Moves to the previous record.
     * 
     * @return true if succeeded
     */
    public boolean previous() {
        if (index == 0)
            return false;
        current = records.get(index);
        index--;
        return true;
    }

    /**
     * Moves to the next record.
     * 
     * @return true if succeeded
     */
    public boolean next() {
        if (index >= records.size())
            return false;
        current = records.get(index);
        index++;
        return true;
    }

    /**
     * Moves to the first record.
     * 
     * @return true if succeeded
     */
    public boolean first() {
        if (size() == 0)
            return false;
        index = 0;
        current = records.get(index);
        return true;
    }

    /**
     * Moves to the last record.
     * 
     * @return true if succeeded
     */
    public boolean last() {
        if (size() == 0)
            return false;
        index = size() - 1;
        current = records.get(index);
        return true;
    }

    /**
     * Gets the record number of the record.
     * 
     * @return record number
     */
    public Long getId() {
        return current.getId();
    }
    
    /**
     * Gets the revision number of the record.
     * 
     * @return revision number
     */
    public Long getRevision() {
        return current.getRevision();
    }

    /**
     * Gets the count of the record set.
     * 
     * @return record count
     */
    public int size() {
        return records.size();
    }

    /**
     * Checks if the record has the field.
     * 
     * @param name
     *            field name
     * @return true if the field exists
     */
    public boolean hasField(String name) {
        return current.hasField(name);
    }

    /**
     * Checks if the record value is empty.
     * @param name
     *            field name
     * @return true if the field value is empty
     */
    public boolean isEmpty(String name) {
        return current.isEmpty(name);
    }
    
    /**
     * Gets total count.
     * 
     * @return total count
     */
    public Long getTotalCount() {
    	return this.totalCount;
    }
    
    /**
     * Sets total count
     * 
     * @param totalCount
     *            total count
     */
    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }
    
    /**
     * Gets the field value as long.
     * 
     * @param name
     *            field name
     * @return a long value of the field
     */
    public Long getLong(String name) {

        return current.getLong(name);
    }

    /**
     * Gets the field value as string.
     * 
     * @param name
     *            field name
     * @return a string value of the field
     */
    public String getString(String name) {

        return current.getString(name);
    }

    /**
     * Gets the field value as user object.
     * 
     * @param name
     *            field name
     * @return a user object
     */
    public UserDto getUser(String name) {
        return current.getUser(name);
    }

    /**
     * Gets the field value as user object array.
     * 
     * @param name
     *            field name
     * @return a list of user objects
     */
    public List<UserDto> getUsers(String name) {
        return current.getUsers(name);
    }

    /**
     * Gets the field value as file object.
     * 
     * @param name
     *            field name
     * @return a list of file objects
     */
    public List<FileDto> getFiles(String name) {

        return current.getFiles(name);
    }

    /**
     * Gets the field value as string array.
     * 
     * @param name
     *            field name
     * @return a list of strings
     */
    public List<String> getStrings(String name) {
        return current.getStrings(name);
    }

    /**
     * Gets the field value as date.
     * 
     * @param name
     *            field name
     * @return a date object
     */
    public Date getDate(String name) {
        return current.getDate(name);
    }
    
    /**
     * Gets the field value as date time.
     * 
     * @param name
     *            field name
     * @return a date object
     */
    public Date getDateTime(String name) {
        return current.getDateTime(name);
    }

    /**
     * Gets the field value as a sub table.
     * @param name
     * @return a sub table object
     */
    public List<Record> getSubtable(String name) {
        return current.getSubtable(name);
    }
    
    /**
     * Gets the field type of the specified field.
     * 
     * @param name
     *            field name
     * @return 
     * 			  field type
     */
    public FieldType getFieldType(String name) {
    	return current.getFieldType(name);
    }
    
    /**
     * Gets the set of the field name.
     * @return set of the field name
     */
    public Set<String> getFieldNames() {
        if (size() == 0)
            return null;
        
        return records.get(0).getFieldNames();
    }
    
    /**
     * Gets the entry set of the record.
     * 
     * @return the entry set of the record
     */
    public Set<Map.Entry<String, Field>> getEntrySet() {
        return current.getEntrySet();
    }

    /**
     * Downloads the file body.
     * 
     * @param name
     *            field name
     * @param index
     *            index of the download file
     * @return downloaded file
     * @throws IOException
     * @throws DBException
     */
    public File downloadFile(String name, int index) throws IOException,
            DBException {
        List<FileDto> files = getFiles(name);
        return connection.downloadFile(files.get(index).getFileKey());
    }
}
