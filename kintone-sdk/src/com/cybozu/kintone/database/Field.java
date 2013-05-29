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
    
    public Field(String name, FieldType type, Object value) {
        this.name = name.toLowerCase();
        this.fieldType = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name.toLowerCase();
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(FieldType fieldType) {
        this.fieldType = fieldType;
    }

    public Object getValue() {
        return value;
    }

    public boolean isEmpty() {
        return value == null;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getAsString() {
        if (isEmpty()) return null;
        
        if (value instanceof String) {
            return (String) value;
        }
        throw new TypeMismatchException();
    }

    public Long getAsLong() {
        if (isEmpty()) return null;
        
        if (value instanceof Long) {
            return (Long) value;
        }
        throw new TypeMismatchException();
    }

    @SuppressWarnings("unchecked")
    public List<String> getAsStringList() {
        if (isEmpty()) return null;
        
        if (value instanceof List) {
            return (List<String>) value;
        }
        throw new TypeMismatchException();
    }

    public UserDto getAsUserInfo() {
        if (isEmpty()) return null;
        
        if (value instanceof UserDto) {
            return (UserDto) value;
        }
        throw new TypeMismatchException();
    }

    @SuppressWarnings("unchecked")
    public List<FileDto> getAsFileList() {
        if (isEmpty()) return null;
        
        if (value instanceof List) {
            return (List<FileDto>) value;
        }
        throw new TypeMismatchException();
    }

    @SuppressWarnings("unchecked")
    public List<UserDto> getAsUserList() {
        if (isEmpty()) return null;
        
        if (value instanceof List) {
            return (List<UserDto>) value;
        }
        throw new TypeMismatchException();
    }
    
    @SuppressWarnings("unchecked")
    public List<Record> getAsSubtable() {
        if (isEmpty()) return null;
        
        if (value instanceof List) {
            return (List<Record>) value;
        }
        throw new TypeMismatchException();
    }
    
    public boolean isLazyUpload() {
        return lazyUploader != null;
    }

    public void setLazyUploader(LazyUploader lazyUploader) {;
        this.lazyUploader = lazyUploader;
    }

    public LazyUploader getLazyUploader() {
        return lazyUploader;
    }
}
