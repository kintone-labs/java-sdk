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
import java.util.Collection;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * JsonParser class converts JSON string to Java object and also Java object to JSON string.
 * Although this class uses Google Gson library, developers don't have to care about that.
 * See the detail of Google Gson. https://code.google.com/p/google-gson/
 */
public class JsonParser {

	public JsonParser() {

	}

	public ResultSet jsonToResultSet(Connection con, String json) throws IOException {

		ResultSet rs = new ResultSet(con);
		ByteArrayInputStream in = new ByteArrayInputStream(json.getBytes());
		JsonReader reader = new JsonReader(new InputStreamReader(in));
		
		reader.beginObject();
		reader.nextName();
		reader.beginArray();
		while (reader.hasNext()) {
			Record record = readRecord(reader);
			if (record != null) {
				rs.add(record);
			}
		}
		reader.endArray();
		reader.endObject();
		return rs;
	}

	private Record readRecord(JsonReader reader) throws IOException {

		Record record = new Record();

		reader.beginObject();
		while (reader.hasNext()) {
			Field field = readField(reader);
			if (field != null) {
				record.addField(field.getName(), field);
			}
		}
		reader.endObject();

		return record;
	}

	private Field readField(JsonReader reader) throws IOException {

		Field field = null;

		FieldType type = null;
		JsonElement element = null;
		
		String fieldName = reader.nextName();
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("type")) {
				String strType = reader.nextString();
				type = FieldType.getEnum(strType);
			} else if (name.equals("value")) {
				com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
				element = parser.parse(reader);
			} else {
				reader.skipValue();
			}
		}
		
		reader.endObject();

		Object object = null;
		String strVal = null;
		if (type == null || element == null) return null;
		
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
			case USER_SELECT:
			case CREATOR:
			case MODIFIER:
				if (element.isJsonObject()) {
					Gson gson = new Gson();
					object = gson.fromJson(element, UserDto.class);
				}
				break;
			case STATUS_ASSIGNEE:
				object = jsonToUserArray(element);
				break;
			case SUBTABLE:
				break;
			}
		}
		field = new Field(fieldName, type, object);
		
		return field;
	}
	
	private List<String> jsonToStringArray(JsonElement element) {
		if (!element.isJsonArray()) return null;
		Type collectionType = new TypeToken<Collection<String>>(){}.getType();
		Gson gson = new Gson();
		
		return gson.fromJson(element, collectionType);
	}
	
	private List<UserDto> jsonToUserArray(JsonElement element) {
		if (!element.isJsonArray()) return null;
		Type collectionType = new TypeToken<Collection<UserDto>>(){}.getType();
		Gson gson = new Gson();
		
		return gson.fromJson(element, collectionType);
	}
	
	private List<FileDto> jsonToFileArray(JsonElement element) {
		if (!element.isJsonArray()) return null;
		Type collectionType = new TypeToken<Collection<FileDto>>(){}.getType();
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
    			for (String strVal: strList) {
    				writer.value(strVal);
    			}
    			writer.endArray();
    			break;
    		case FILE:
    			writer.beginArray();
    			Iterable<FileDto> fileList = field.getAsFileList();
    			for (FileDto file: fileList) {
    				writer.beginObject();
    				writer.name("fileKey").value(file.getFileKey());
    				writer.endObject();
    			}
    			writer.endArray();
    			break;
    		case USER_SELECT:
    			writer.beginArray();
    			Iterable<UserDto> userList = field.getAsUserList();
    			for (UserDto user: userList) {
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
    		default:
    			writer.value("");
    		}
		}
		writer.endObject();
	}
	public String recordsToJsonForInsert(long app, Record[] records) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(baos));
        
        writer.beginObject();
        writer.name("app").value(app);
        writer.name("records");
        
        writer.beginArray();
        for (Record record: records) {
        	writer.beginObject();
        	for (String fieldName: record.getFieldNames()) {
        		Field field = record.getField(fieldName);
        		writeField(writer, field);
        	}
        	writer.endObject();
        }
        writer.endArray();
    
        writer.endObject();
        
        writer.close();
        return new String(baos.toByteArray());
	}
	
	public Long[] jsonToLongArray(String json) {
		Gson gson = new Gson();
		
		return gson.fromJson(json, Long[].class);
	}
	
	public Long[] jsonToIDs(String json) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(json.getBytes());
		JsonReader reader = new JsonReader(new InputStreamReader(in));
		
		reader.beginObject();
		reader.nextName();
		
		Gson gson = new Gson();
		
		Long[] ids = gson.fromJson(reader, Long[].class);
		
		reader.endObject();
		
		return ids;
	}
	
	public String recordsToJsonForUpdate(long app, Long[] ids, Record record) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(baos));
        
        writer.beginObject();
        writer.name("app").value(app);
        writer.name("records");
        
        writer.beginArray();
        for (long id: ids) {
        	writer.beginObject();
        	writer.name("id").value(id);
        	writer.name("record");
        	writer.beginObject();
        	for (String fieldName: record.getFieldNames()) {
        		Field field = record.getField(fieldName);
        		writeField(writer, field);
        	}
        	writer.endObject();
        	writer.endObject();
        }
        writer.endArray();
    
        writer.endObject();
        
        writer.close();
        return new String(baos.toByteArray());
	}
	
	public String jsonToFileKey(String json) throws IOException {
		ByteArrayInputStream in = new ByteArrayInputStream(json.getBytes());
		JsonReader reader = new JsonReader(new InputStreamReader(in));
		
		reader.beginObject();
		reader.nextName();
		String fileKey = reader.nextString();
		reader.endObject();
		return fileKey;
	}
}
