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
import java.util.List;

/**
 * Resultset class Represents a kintone database result set. The data set is read only.
 *
 */
public class ResultSet {
	
	private List<Record> records = new ArrayList<Record>();
	private int index = 0;
	private Record current = null;
	private Connection connection = null;
	
	/**
	 * @param connection The database connection which retrieved this result set.
	 */
	public ResultSet(Connection connection) {
		this.connection = connection;
	}
	
	/**
	 * Add a new record
	 * @param record record object
	 */
	public void add(Record record) {
		records.add(record);
	}
	
	/**
	 * Clear result set.
	 */
	public void clear() {
		records.clear();
		index = 0;
		current = null;
	}
	
	/**
	 * Move to the previous record.
	 * @return true if succeeded
	 */
	public boolean previous() {
		if (index == 0) return false;
		current = records.get(index);
		index--;
		return true;
	}
	
	/**
	 * Move to the next record.
	 * @return true if succeeded
	 */
	public boolean next() {
		if (index >= records.size()) return false;
		current = records.get(index);
		index++;
		return true;
	}
	
	/**
	 * Move to the first record.
	 * @return true if succeeded
	 */
	public boolean first() {
		if (size() == 0) return false;
		index = 0;
		current = records.get(index);
		return true;
	}
	
	/**
	 * Move to the last record.
	 * @return true if succeeded
	 */
	public boolean last() {
		if (size() == 0) return false;
		index = size() - 1;
		current = records.get(index);
		return true;
	}
	
	/**
	 * Get record number of the record
	 * @return record number
	 */
	public Long getId() {
		return current.getId();
	}
	
	/**
	 * Get the count of the record set
	 * @return record count
	 */
	public int size() {
		return records.size();
	}
	/**
	 * Check if the record has the field
	 * @param name field name
	 * @return true if the field exists
	 */
	public boolean hasField(String name) {
		return current.hasField(name);
	}
	/**
	 * Get field value as long
	 * @param name field name
	 * @return a long value of the field
	 */
	public long getLong(String name) {
		
		return current.getLong(name);
	}
	
	/**
	 * Get field value as string
	 * @param name field name
	 * @return a string value of the field
	 */
	public String getString(String name) {
		
		return current.getString(name);
	}
	
	/**
	 * Get field value as user object
	 * @param name field name
	 * @return a user object
	 */
	public UserDto getUser(String name) {
		return current.getUser(name);
	}
	
	/**
	 * Get field value as user object array
	 * @param name field name
	 * @return a user object
	 */
	public UserDto[] getUsers(String name) {
		return current.getUsers(name).toArray(new UserDto[0]);
	}
	
	
	/**
	 * Get field value as file object
	 * @param name field name
	 * @return an array of file objects
	 */
	public FileDto[] getFiles(String name) {
		
		return current.getFiles(name).toArray(new FileDto[0]);
	}
	
	/**
	 * Get field value as string array
	 * @param name field name
	 * @return an array of strings
	 */
	public String[] getStrings(String name) {
		return current.getStrings(name).toArray(new String[0]);
	}
	
	/**
	 * Download file body
	 * @param name field name
	 * @param index index of the download file
	 * @return downloaded file
	 * @throws IOException
	 * @throws DBException
	 */
	public File downloadFile(String name, int index) throws IOException, DBException {
		FileDto[] files = getFiles(name);
		return connection.downloadFile(files[index].getFileKey());
	}
}
