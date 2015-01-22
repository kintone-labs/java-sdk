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
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import com.cybozu.kintone.database.exception.TypeMismatchException;

/**
 * A record object represents a row of the kintone application. You can retrieve
 * or store data by using this class.
 * 
 */
public class Record implements Cloneable {
    static public final String DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    static public final String DATE_PATTERN = "yyyy-MM-dd";

    private long id;
    private long revision;

    private HashMap<String, Field> fields = new HashMap<String, Field>();

    public Record() {
        this.id = -1;
        this.revision = -1;
    }

    public Record(long id) {
        this.id = id;
    }

    public Record(long id, long revision) {
        this.id = id;
        this.revision = revision;
    }

    /**
     * Clones this instance.
     * 
     * @return the duplicated record object
     */
    public Object clone() {
        Record record = new Record(this.id, this.revision);

        for (String key : fields.keySet()) {
            Field field = fields.get(key);
            record.addField(key, field);
        }
        return record;
    }

    /**
     * Gets the entry set of the record.
     * 
     * @return the entry set of the record
     */
    public Set<Map.Entry<String, Field>> getEntrySet() {
        return fields.entrySet();
    }

    /**
     * Gets the field name collection.
     * 
     * @return the field names
     */
    public Set<String> getFieldNames() {
        return fields.keySet();
    }

    /**
     * Gets the field object.
     * 
     * @param name
     *            field name
     * @return the field object
     */
    public Field getField(String name) {
        return fields.get(name);
    }

    /**
     * Adds a new field.
     * 
     * @param name
     *            field name
     * @param field
     *            field object
     */
    public void addField(String name, Field field) {
        if (field.getFieldType() == FieldType.__ID__) {
            try {
                setId(field.getAsLong());
            } catch (TypeMismatchException e) {
                e.printStackTrace();
            }
        } else if (field.getFieldType() == FieldType.__REVISION__) {
            try {
                setRevision(field.getAsLong());
            } catch (TypeMismatchException e) {
                e.printStackTrace();
            }
        } else {
            fields.put(name, field);
        }
    }

    /**
     * Sets the record id.
     * 
     * @param id
     *            record id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets the record id.
     * 
     * @return the record id
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Sets the revision number.
     * 
     * @param revision
     *            revision number
     */
    public void setRevision(long revision) {
        this.revision = revision;
    }

    /**
     * Gets the revision number.
     * 
     * @return the revision number
     */
    public Long getRevision() {
        return this.revision;
    }

    /**
     * Returns true if the revision number is set.
     * 
     * @return true if the revision number is set.
     */
    public boolean hasRevision() {
        return this.revision >= 0;
    }

    /**
     * Returns true if the field value is empty.
     * 
     * @param name
     *            field name
     * @return true if the field value is empty
     */
    public boolean isEmpty(String name) {
        return fields.get(name).isEmpty();
    }

    /**
     * Returns true if the field exists.
     * 
     * @param name
     *            field name
     * @return true if the field exists
     */
    public boolean hasField(String name) {
        return fields.containsKey(name);
    }

    /**
     * Gets the field value as long.
     * 
     * @param name
     *            field name
     * @return the long value of the field
     */
    public Long getLong(String name) {

        return fields.get(name).getAsLong();
    }

    /**
     * Gets the field value as string.
     * 
     * @param name
     *            field name
     * @return the string value of the field
     */
    public String getString(String name) {

        return fields.get(name).getAsString();
    }

    /**
     * Gets the field value as string list.
     * 
     * @param name
     *            field name
     */
    public List<String> getStrings(String name) {
        return fields.get(name).getAsStringList();
    }

    /**
     * Gets the field value as file list.
     * 
     * @param name
     *            field name
     */
    public List<FileDto> getFiles(String name) {

        return fields.get(name).getAsFileList();
    }

    /**
     * Gets the field value as a user object.
     * 
     * @param name
     *            field name
     */

    public UserDto getUser(String name) {
        return fields.get(name).getAsUserInfo();
    }

    /**
     * Gets the field value as user list.
     * 
     * @param name
     *            field name
     */
    public List<UserDto> getUsers(String name) {
        return fields.get(name).getAsUserList();
    }

    /**
     * Gets the field value as a date object.
     * 
     * @param name
     *            field name
     */
    public Date getDateTime(String name) {
        String strDate = fields.get(name).getAsString();
        if (strDate == null || strDate.isEmpty())
            return null;
        try {
            DateFormat df = new SimpleDateFormat(DATETIME_PATTERN);
            df.setTimeZone(TimeZone.getTimeZone("UTC"));
            return df.parse(strDate);
        } catch (ParseException e) {
            throw new TypeMismatchException();
        }
    }

    /**
     * Gets the field value as a date object.
     * 
     * @param name
     *            field name
     */
    public Date getDate(String name) {
        String strDate = fields.get(name).getAsString();
        if (strDate == null || strDate.isEmpty())
            return null;
        try {
            DateFormat df = new SimpleDateFormat(DATE_PATTERN);
            //df.setTimeZone(TimeZone.getTimeZone("UTC"));
            return df.parse(strDate);
        } catch (ParseException e) {
            throw new TypeMismatchException();
        }
    }

    /**
     * Gets the field value as a sub table.
     * 
     * @param name
     *            field name
     */
    public List<Record> getSubtable(String name) {
        return fields.get(name).getAsSubtable();
    }

    /**
     * Adds a new field and sets the string value.
     * 
     * @param name
     *            field name
     * @param value
     *            field value
     */
    public void setString(String name, String value) {
        Field field = new Field(name, FieldType.SINGLE_LINE_TEXT, value);
        addField(name, field);
    }

    /**
     * Adds a new field and sets the long value.
     * 
     * @param name
     *            field name
     * @param value
     *            field value
     */
    public void setLong(String name, long value) {
        Field field = new Field(name, FieldType.NUMBER, Long.valueOf(value));
        addField(name, field);
    }

    /**
     * Adds a new field and sets the user object.
     * 
     * @param name
     *            field name
     * @param code
     *            field value
     */
    public void setUser(String name, String code) {
        UserDto user = new UserDto();
        user.setCode(code);
        
        Field field = new Field(name, FieldType.CREATOR, user);
        addField(name, field);
    }
    
    /**
     * Adds a new field and sets the user object.
     * 
     * @param name
     *            field name
     * @param codes
     *            field value
     */
    public void setUsers(String name, List<String> codes) {
        List<UserDto> list = new ArrayList<UserDto>();
        for (String code : codes) {
            UserDto user = new UserDto();
            user.setCode(code);
            list.add(user);
        }
        Field field = new Field(name, FieldType.USER_SELECT, list);
        addField(name, field);
    }

    /**
     * Adds a new field and sets the file object.
     * 
     * @param name
     * @param file
     *            field value
     */
    public void setFile(String name, File file) {
        setFile(name, file, null);
    }

    /**
     * Adds a new field and sets the file object.
     * 
     * @param name
     *            field name
     * @param file
     *            file object
     * @param contentType
     *            content type
     */
    public void setFile(String name, File file, String contentType) {
        Field field = new Field(name, FieldType.FILE, null);
        LazyUploader uploader = new FileLazyUploader(file, contentType);
        field.setLazyUploader(uploader);
        addField(name, field);
    }

    /**
     * Adds a new field and sets the file stream.
     * 
     * @param name
     *            field name
     * @param file
     *            file object
     * @param fileName
     *            file name
     */
    public void setFile(String name, InputStream file, String fileName) {
        setFile(name, file, fileName, null);
    }

    /**
     * Adds a new field and sets the file stream.
     * 
     * @param name
     *            field name
     * @param file
     *            file stream
     * @param fileName
     *            file name
     * @param contentType
     *            content type
     */
    public void setFile(String name, InputStream file, String fileName,
            String contentType) {
        Field field = new Field(name, FieldType.FILE, null);
        LazyUploader uploader = new InputStreamLazyUploader(file, fileName,
                contentType);
        field.setLazyUploader(uploader);
        addField(name, field);
    }

    /**
     * Adds a new field and sets the file keys.
     * 
     * @param name
     *            field name
     * @param fileKeys
     *            list of the file keys
     */
    public void setFiles(String name, List<String> fileKeys) {
        List<FileDto> list = new ArrayList<FileDto>();
        for (String key : fileKeys) {
            FileDto file = new FileDto();
            file.setFileKey(key);
            list.add(file);
        }
        Field field = new Field(name, FieldType.FILE, list);
        addField(name, field);
    }

    /**
     * Adds a new field and sets the string list.
     * 
     * @param name
     *            field name
     * @param values
     *            field value
     */
    public void setStrings(String name, List<String> values) {
        List<String> list = new ArrayList<String>();
        for (String value : values) {
            list.add(value);
        }
        Field field = new Field(name, FieldType.MULTI_SELECT, list);
        addField(name, field);
    }

    /**
     * Adds a new field and sets the date time value.
     * 
     * @param name
     *            field name
     * @param date
     *            field value
     */
    public void setDateTime(String name, Date date) {
        DateFormat df = new SimpleDateFormat(DATETIME_PATTERN);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String strDate = df.format(date);
        setString(name, strDate);
    }

    /**
     * Adds a new field and sets the date value.
     * 
     * @param name
     *            field name
     * @param date
     *            field value
     */
    public void setDate(String name, Date date) {
        DateFormat df = new SimpleDateFormat(DATE_PATTERN);
        //df.setTimeZone(TimeZone.getTimeZone("UTC"));
        String strDate = df.format(date);
        setString(name, strDate);
    }

    /**
     * Adds a new field and sets the sub table.
     * 
     * @param name
     *            field name
     * @param value
     *            field value
     */
    public void setSubtable(String name, List<Record> value) {
        Field field = new Field(name, FieldType.SUBTABLE, value);
        addField(name, field);
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
    	return fields.get(name).getFieldType();
    }
}
