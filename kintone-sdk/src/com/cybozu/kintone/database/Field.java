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

import java.util.List;

import com.cybozu.kintone.database.exception.TypeMismatchException;

/**
 * A field object stores the each value of the variable field types.
 * 
 */
public class Field {
    private String name;
    private FieldType fieldType;
    private Object value;

    private LazyUploader lazyUploader = null;
    
    /**
     * Constructor
     * @param name field name
     * @param type field type
     * @param value field value
     */
    public Field(String name, FieldType type, Object value) {
        this.name = name.toLowerCase();
        this.fieldType = type;
        this.value = value;
    }

    /**
     * Gets the field name.
     * @return field name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the field name.
     * @param name field name
     */
    public void setName(String name) {
        this.name = name.toLowerCase();
    }

    /**
     * Gets the field type.
     * @return field type
     */
    public FieldType getFieldType() {
        return fieldType;
    }

    /**
     * Sets the field type.
     * @param fieldType field type object
     */
    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    /**
     * Gets the field value.
     * @return field value
     */
    public Object getValue() {
        return value;
    }

    /**
     * returns if the value is empty.
     * @return true if the value is empty
     */
    public boolean isEmpty() {
        return value == null;
    }

    /**
     * Sets the field value.
     * @param value an object represents the field value
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Gets the field value as string object.
     * @return string object
     */
    public String getAsString() {
        if (isEmpty()) return null;
        
        if (value instanceof String) {
            return (String) value;
        }
        throw new TypeMismatchException();
    }

    /**
     * Gets the field value as long object.
     * @return long object
     */
    public Long getAsLong() {
        if (isEmpty()) return null;
        
        if (value instanceof Long) {
            return (Long) value;
        }
        throw new TypeMismatchException();
    }

    /**
     * Gets the field value as string list.
     * @return string list object
     */
    @SuppressWarnings("unchecked")
    public List<String> getAsStringList() {
        if (isEmpty()) return null;
        
        if (value instanceof List) {
            return (List<String>) value;
        }
        throw new TypeMismatchException();
    }

    /**
     * Gets the field value as user info object.
     * @return user info object
     */
    public UserDto getAsUserInfo() {
        if (isEmpty()) return null;
        
        if (value instanceof UserDto) {
            return (UserDto) value;
        }
        throw new TypeMismatchException();
    }

    /**
     * Gets the field value as file list object.
     * @return file list object
     */
    @SuppressWarnings("unchecked")
    public List<FileDto> getAsFileList() {
        if (isEmpty()) return null;
        
        if (value instanceof List) {
            return (List<FileDto>) value;
        }
        throw new TypeMismatchException();
    }

    /**
     * Gets the field value as user list object.
     * @return user list object
     */
    @SuppressWarnings("unchecked")
    public List<UserDto> getAsUserList() {
        if (isEmpty()) return null;
        
        if (value instanceof List) {
            return (List<UserDto>) value;
        }
        throw new TypeMismatchException();
    }
    
    /**
     * Gets the field value as a sub table.
     * @return record list object
     */
    @SuppressWarnings("unchecked")
    public List<Record> getAsSubtable() {
        if (isEmpty()) return null;
        
        if (value instanceof List) {
            return (List<Record>) value;
        }
        throw new TypeMismatchException();
    }
    
    /**
     * return whether the lazy uploader was set.
     * @return true if the lazy uploader wa set
     */
    public boolean isLazyUpload() {
        return lazyUploader != null;
    }

    /**
     * Sets the lazy uploader.
     * @param lazyUploader uploader
     */
    public void setLazyUploader(LazyUploader lazyUploader) {;
        this.lazyUploader = lazyUploader;
    }

    /**
     * Gets the lazy uploader.
     * @return uploader
     */
    public LazyUploader getLazyUploader() {
        return lazyUploader;
    }
}
