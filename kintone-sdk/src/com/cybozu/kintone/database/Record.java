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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import com.cybozu.kintone.database.exception.DBTypeMismatchException;

/**
 * A record object represents a row of the kintone application.
 * You can retrieve or store data by using this class.
 *
 */
public class Record {
	static public final String DATE_PATTERN ="yyyy-MM-dd'T'HH:mm:ss'Z'";

	private long id;
	
	private HashMap<String, Field> fields = new HashMap<String, Field>();
	
	public Record() {
		this.id = 0;
	}
	
	public Record(long id) {
		this.id = id;
	}
	
	public Set<String> getFieldNames() {
		return fields.keySet();
	}
	
	public Field getField(String name) {
		return fields.get(name.toLowerCase());
	}
	
	public void addField(String name, Field field){
		if (field.getFieldType() == FieldType.RECORD_NUMBER) {
			try {
				setId(field.getAsLong());
			} catch (DBTypeMismatchException e) {
				e.printStackTrace();
			}
		} else {
			fields.put(name.toLowerCase(), field);
		}
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public Long getId() {
		return this.id;
	}
	
	public boolean hasField(String name) {
		return fields.containsKey(name.toLowerCase());
	}
	
	public long getLong(String name) throws DBTypeMismatchException {
		
		return fields.get(name.toLowerCase()).getAsLong();
	}
	
	public String getString(String name) throws DBTypeMismatchException {
		
		return fields.get(name.toLowerCase()).getAsString();
	}
	
	public List<String> getStrings(String name) throws DBTypeMismatchException {
		return fields.get(name.toLowerCase()).getAsStringList();
	}
	public List<FileDto> getFiles(String name) throws DBTypeMismatchException {
		
		return fields.get(name.toLowerCase()).getAsFileList();
	}
	
	public UserDto getUser(String name) throws DBTypeMismatchException {
		return fields.get(name.toLowerCase()).getAsUserInfo();
	}
	
	public List<UserDto> getUsers(String name) throws DBTypeMismatchException {
		return fields.get(name.toLowerCase()).getAsUserList();
	}
	
	
	public Date getDate(String name) throws DBTypeMismatchException {
		String strDate = fields.get(name.toLowerCase()).getAsString();
		try {
			DateFormat df = new SimpleDateFormat(DATE_PATTERN);
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
			return df.parse(strDate);
		} catch (ParseException e) {
			throw new DBTypeMismatchException();
		}
	}
	public void setString(String name, String value) {
		Field field = new Field(name, FieldType.SINGLE_LINE_TEXT, value);
		addField(name, field);
	}
	
	public void setLong(String name, long value) {
		Field field = new Field(name, FieldType.NUMBER, Long.valueOf(value));
		addField(name, field);
	}

	public void setUsers(String name, String[] codes) {
		List<UserDto> list = new ArrayList<UserDto>();
		for (String code: codes) {
			UserDto user = new UserDto();
			user.setCode(code);
			list.add(user);
		}
		Field field = new Field(name, FieldType.USER_SELECT, list);
		addField(name, field);	
	}
	
	public void setFiles(String name, String[] fileKeys) {
		List<FileDto> list = new ArrayList<FileDto>();
		for (String key: fileKeys) {
			FileDto file = new FileDto();
			file.setFileKey(key);
			list.add(file);
		}
		Field field = new Field(name, FieldType.FILE, list);
		addField(name, field);
	}
	
	public void setStrings(String name, String[] values) {
		List<String> list = new ArrayList<String>();
		for (String value: values) {
			list.add(value);
		}
		Field field = new Field(name, FieldType.MULTI_SELECT, list);
		addField(name, field);
	}
	
	public void setDate(String name, Date date) {
		DateFormat df = new SimpleDateFormat(DATE_PATTERN);
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		String strDate = df.format(date);
		setString(name, strDate);
	}
}
