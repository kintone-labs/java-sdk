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
//
//   Google Gson
//   Copyright (c) 2008-2009 Google Inc. 
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cybozu.kintone.database.exception.TypeMismatchException;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * JsonParser class converts JSON string to Java object and also Java object to
 * JSON string. Although this class uses Google Gson library, developers don't
 * have to care about that. See the detail of Google Gson.
 * https://code.google.com/p/google-gson/
 */
public class JsonParser {

    public JsonParser() {

    }

    /**
     * Convert json string to error response object
     * @param json
     *            a json string
     * @return error response object
     */
    public ErrorResponse jsonToErrorResponse(String json) {
        Gson gson = new Gson();
        try {
            return gson.fromJson(json, ErrorResponse.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }
    
    /**
     * Convert json string to resultset
     * @param con
     *            a connection object
     * @param json
     *            a json string
     * @return resultset object
     * @throws IOException
     */
    public ResultSet jsonToResultSet(Connection con, String json)
            throws IOException {

        ResultSet rs = new ResultSet(con);
        com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
        JsonElement root = parser.parse(json);
        
        if (root.isJsonObject()) {
            JsonArray records = root.getAsJsonObject().get("records").getAsJsonArray();
            for (JsonElement elem: records) {
                Record record = readRecord(elem);
                if (record != null) {
                    rs.add(record);
                }
            }
        }
        return rs;
    }
    
    /**
     * Read and parse each record element
     * @param elem
     *            a json element represents a record object
     * @return the record object created
     * @throws IOException
     */
    private Record readRecord(JsonElement elem) throws IOException {

        Record record = new Record();

        if (elem.isJsonObject()) {
            JsonObject obj = elem.getAsJsonObject();
            Set<Map.Entry<String,JsonElement>> set = obj.entrySet();
            for (Map.Entry<String,JsonElement> entry: set) {
                Field field = readField(entry.getKey(), entry.getValue());
                if (field != null) {
                    record.addField(field.getName(), field);
                }
            }
        }

        return record;
    }

    /**
     * Read and parse each field element
     * @param fieldName
     *            the field name
     * @param elem
     *            a json element represents a record object
     * @return the field object created
     * @throws IOException
     */
    private Field readField(String fieldName, JsonElement fieldElem) throws IOException {

        Field field = null;

        
        if (!fieldElem.isJsonObject()) return null;
        JsonObject obj = fieldElem.getAsJsonObject();
        
        FieldType type = FieldType.getEnum(obj.get("type").getAsString());
        JsonElement element = obj.get("value");

        Object object = null;
        String strVal = null;
        if (type == null || element == null)
            return null;

        if (!element.isJsonNull()) {
            switch (type) {
            case SINGLE_LINE_TEXT:
            case CALC:
            case MULTI_LINE_TEXT:
            case RICH_TEXT:
            case RADIO_BUTTON:
            case DROP_DOWN:
            case LINK:
            case STATUS:
                object = element.getAsString();
                break;
            case NUMBER:
            case RECORD_NUMBER:
                strVal = element.getAsString();
                try {
                    object = Long.valueOf(strVal);
                } catch (NumberFormatException e) {
                }
                break;
            case DATE:
            case TIME:
            case DATETIME:
            case CREATED_TIME:
            case UPDATED_TIME:
                object = element.getAsString();
                break;
            case CHECK_BOX:
            case MULTI_SELECT:
            case CATEGORY:
                object = jsonToStringArray(element);
                break;
            case FILE:
                object = jsonToFileArray(element);
                break;
            case CREATOR:
            case MODIFIER:
                if (element.isJsonObject()) {
                    Gson gson = new Gson();
                    object = gson.fromJson(element, UserDto.class);
                }
                break;
            case USER_SELECT:
            case STATUS_ASSIGNEE:
                object = jsonToUserArray(element);
                break;
            case SUBTABLE:
                object = jsonToSubtable(element);
                break;
            }
        }
        field = new Field(fieldName, type, object);

        return field;
    }

    private List<Record> jsonToSubtable(JsonElement element) throws IOException {
        List<Record> rs = new ArrayList<Record>();
        
        if (!element.isJsonArray()) return null;
        
        JsonArray records = element.getAsJsonArray();
        for (JsonElement elem: records) {
            if (elem.isJsonObject()) {
                JsonObject obj = elem.getAsJsonObject();
                String id = obj.get("id").getAsString();
                JsonElement value = obj.get("value");
                Record record = readRecord(value);
                if (record != null) {
                    try {
                        record.setId(Long.valueOf(id));
                    } catch (NumberFormatException e) {
                    }
                    rs.add(record);
                }
            }
        }
        
        return rs;
    }
    
    private List<String> jsonToStringArray(JsonElement element) {
        if (!element.isJsonArray())
            return null;
        Type collectionType = new TypeToken<Collection<String>>() {
        }.getType();
        Gson gson = new Gson();

        return gson.fromJson(element, collectionType);
    }

    private List<UserDto> jsonToUserArray(JsonElement element) {
        if (!element.isJsonArray())
            return null;
        Type collectionType = new TypeToken<Collection<UserDto>>() {
        }.getType();
        Gson gson = new Gson();

        return gson.fromJson(element, collectionType);
    }

    private List<FileDto> jsonToFileArray(JsonElement element) {
        if (!element.isJsonArray())
            return null;
        Type collectionType = new TypeToken<Collection<FileDto>>() {
        }.getType();
        Gson gson = new Gson();

        return gson.fromJson(element, collectionType);
    }

    private void writeField(JsonWriter writer, Field field) throws IOException {
        writer.name(field.getName());
        writer.beginObject();
        writer.name("value");
        FieldType type = field.getFieldType();

        if (field.isEmpty()) {
            writer.value("");
        } else {

            switch (type) {
            case SINGLE_LINE_TEXT:
            case CALC:
            case MULTI_LINE_TEXT:
            case RICH_TEXT:
            case RADIO_BUTTON:
            case DROP_DOWN:
            case LINK:
            case STATUS:
                writer.value(field.getAsString());
                break;
            case NUMBER:
            case RECORD_NUMBER:
                writer.value(String.valueOf(field.getAsLong()));
                break;
            case DATE:
            case TIME:
            case DATETIME:
            case CREATED_TIME:
            case UPDATED_TIME:
                writer.value(field.getAsString());
                break;
            case CHECK_BOX:
            case MULTI_SELECT:
            case CATEGORY:
                writer.beginArray();
                Iterable<String> strList = field.getAsStringList();
                for (String strVal : strList) {
                    writer.value(strVal);
                }
                writer.endArray();
                break;
            case FILE:
                writer.beginArray();
                Iterable<FileDto> fileList = field.getAsFileList();
                for (FileDto file : fileList) {
                    writer.beginObject();
                    writer.name("fileKey").value(file.getFileKey());
                    writer.endObject();
                }
                writer.endArray();
                break;
            case USER_SELECT:
                writer.beginArray();
                Iterable<UserDto> userList = field.getAsUserList();
                for (UserDto user : userList) {
                    writer.beginObject();
                    writer.name("code").value(user.getCode());
                    writer.endObject();
                }
                writer.endArray();
                break;
            case CREATOR:
            case MODIFIER:
                writer.beginObject();
                writer.name("code").value(field.getAsUserInfo().getCode());
                writer.endObject();
                break;
            case SUBTABLE:
                writer.beginArray();
                List<Record> subtable = field.getAsSubtable();
                writeSubtable(writer, subtable);
                break;
            default:
                writer.value("");
            }
        }
        writer.endObject();
    }

    /**
     * Write subtable value to json
     * @param writer
     *            a json writer
     * @param subtable
     *            a subtable object
     */
    private void writeSubtable(JsonWriter writer, Iterable<Record> subtable) throws IOException {
        for (Record record : subtable) {
            writer.beginObject();
            //writer.name("id").value(record.getId());
            writer.name("value");
            writer.beginObject();
            for (String fieldName : record.getFieldNames()) {
                Field field = record.getField(fieldName);
                try {
                    writeField(writer, field);
                } catch (TypeMismatchException e) {
                    e.printStackTrace();
                }
            }
            writer.endObject();
            writer.endObject();
        }
    }
    /**
     * Generate json string for insert method
     * @param app
     *            the application id
     * @param records
     *            the array of the record object
     * @return the json string
     * @throws IOException
     */
    public String recordsToJsonForInsert(long app, List<Record> records)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(baos));

        writer.beginObject();
        writer.name("app").value(app);
        writer.name("records");

        writer.beginArray();
        for (Record record : records) {
            writer.beginObject();
            Set<Map.Entry<String,Field>> set = record.getEntrySet();
            for (Map.Entry<String,Field> entry: set) {
                Field field = entry.getValue();
                try {
                    writeField(writer, field);
                } catch (TypeMismatchException e) {
                    e.printStackTrace();
                }
            }
            writer.endObject();
        }
        writer.endArray();

        writer.endObject();

        writer.close();
        return new String(baos.toByteArray());
    }

    /**
     * Retrieve the array of the Long values from json
     * @param json
     *            a json string
     * @return the array of the long value
     */
    public List<Long> jsonToLongArray(String json) {
        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Long>>() {}.getType();
        return gson.fromJson(json, listType);
    }

    /**
     * Retrieve the array of the ids from json string
     * @param json
     *            a json string
     * @return the array of the id
     * @throws IOException
     */
    public List<Long> jsonToIDs(String json) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(json.getBytes());
        JsonReader reader = new JsonReader(new InputStreamReader(in));

        reader.beginObject();
        reader.nextName();

        Gson gson = new Gson();
        Type listType = new TypeToken<ArrayList<Long>>() {}.getType();
        List<Long> ids = gson.fromJson(reader, listType);

        reader.endObject();

        return ids;
    }

    /**
     * Generate json string for update method
     * @param app
     *            the application id
     * @param ids
     *            the array of the record id to be updated
     * @param record
     *            the values of updated records
     * @return
     *        json string
     * @throws IOException
     */
    public String recordsToJsonForUpdate(long app, List<Long> ids, Record record)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(baos));

        writer.beginObject();
        writer.name("app").value(app);
        writer.name("records");

        writer.beginArray();
        for (long id : ids) {
            writer.beginObject();
            writer.name("id").value(id);
            writer.name("record");
            writer.beginObject();
            for (String fieldName : record.getFieldNames()) {
                Field field = record.getField(fieldName);
                try {
                    writeField(writer, field);
                } catch (TypeMismatchException e) {
                    e.printStackTrace();
                }
            }
            writer.endObject();
            writer.endObject();
        }
        writer.endArray();

        writer.endObject();

        writer.close();
        return new String(baos.toByteArray());
    }
    
    /**
     * Retrieve file key string from json
     * @param json
     *            a json string
     * @return the file key
     * @throws IOException
     */
    public String jsonToFileKey(String json) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(json.getBytes());
        JsonReader reader = new JsonReader(new InputStreamReader(in));

        reader.beginObject();
        reader.nextName();
        String fileKey = reader.nextString();
        reader.endObject();
        reader.close();
        return fileKey;
    }
}
