//   Copyright 2014 Cybozu
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.cybozu.kintone.database.exception.ParseException;

/**
 * Bulk request.
 * 
 */
public class BulkRequest {

    List<BulkRequestData> requests = new ArrayList<BulkRequestData>();
    
    /**
     * Constructor
     */
    public BulkRequest() {
    }
    

    /**
     * Adds bulk request for insert.
     * 
     * @param app
     *            application id
     * @param record
     *            The Record object to be inserted
     */
    public void insert(long app, Record record) {
        List<Record> list = new ArrayList<Record>();
        list.add(record);
        insert(app, list);        
    }
    
    /**
     * Adds bulk request for insert.
     * 
     * @param app
     *            application id
     * @param records
     *            The array of Record objects to be inserted
     */
    public void insert(long app, List<Record> records) {
        
        JsonParser parser = new JsonParser();
        String json;
        try {
            json = parser.recordsToJsonForInsert(app, records);
        } catch (IOException e) {
            throw new ParseException("failed to encode to json");
        }

        BulkRequestData request = new BulkRequestData("POST", "/k/v1/records.json", json);
        requests.add(request);
    }

    /**
     * Adds bulk request for update (deprecated).
     * 
     * @param app
     *            application id
     * @param id
     *            record number of the updated record
     * @param record
     *            updated record object
     */
    public void update(long app, long id, Record record) {
        List<Long> list = new ArrayList<Long>();
        list.add(id);
        update(app, list, record);
    }

    /**
     * Adds bulk request for update.
     * 
     * @param app
     *            application id
     * @param record
     *            updated record object
     */
    public void updateByRecord(long app, Record record) {
        List<Record> list = new ArrayList<Record>();
        list.add(record);
        updateByRecords(app, list);
    }

    /**
     * Adds bulk request for update 
     * 
     * @param app
     *            application id
     * @param ids
     *            an array of record numbers of the updated records
     * @param record
     *            updated record object
     */
    public void update(long app, List<Long> ids, Record record) {
        
        JsonParser parser = new JsonParser();
        String json;
        try {
            json = parser.recordsToJsonForUpdate(app, ids, record);
        } catch (IOException e) {
            throw new ParseException("failed to encode to json");
        }

        BulkRequestData request = new BulkRequestData("PUT", "/k/v1/records.json", json);
        requests.add(request);
    }

    /**
     * Adds bulk request for update 
     * 
     * @param app
     *            application id
     * @param records
     *            an array of the updated record object
     */
    public void updateByRecords(long app, List<Record> records) {
        
        JsonParser parser = new JsonParser();
        String json;
        try {
            json = parser.recordsToJsonForUpdate(app, records);
        } catch (IOException e) {
            throw new ParseException("failed to encode to json");
        }

        BulkRequestData request = new BulkRequestData("PUT", "/k/v1/records.json", json);
        requests.add(request);
    }

    /**
     * Adds bulk request for delete 
     * 
     * @param app
     *            application id
     * @param id
     *            record number to be deleted
     */
    public void delete(long app, long id)  {
        List<Long> list = new ArrayList<Long>();
        list.add(id);
        delete(app, list);
    }

    /**
     * Adds bulk request for delete 
     * 
     * @param app
     *            application id
     * @param record
     *            a record object to be deleted
     */
    public void deleteByRecord(long app, Record record) {
        List<Record> list = new ArrayList<Record>();
        list.add(record);
        deleteByRecords(app, list);
    }
    
    /**
     * Adds bulk request for delete 
     * 
     * @param app
     *            application id
     * @param records
     *            a list of the record object to be deleted
     */
    public void deleteByRecords(long app, List<Record> records) {
        
        JsonParser parser = new JsonParser();
        String json;
        try {
            json = parser.recordsToJsonForDelete(app, records);
        } catch (IOException e) {
            throw new ParseException("failed to encode to json");
        }
        
        BulkRequestData request = new BulkRequestData("DELETE", "/k/v1/records.json", json);
        requests.add(request);
    }

    /**
     * Adds bulk request for delete 
     * @param app
     *           application id
     * @param ids
     *           a list of record numbers to be deleted
     */
    public void delete(long app, List<Long> ids) {
        List<Record> records = new ArrayList<Record>();
        for (Long id : ids) {
            Record record = new Record();
            record.setId(id);
            records.add(record);
        }
        deleteByRecords(app, records);
    }

    public String getJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"requests\":[");
        
        int i = 0;
        for (BulkRequestData request: requests) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append("{");
            sb.append("\"method\": \"" + request.getMethod() + "\",");
            sb.append("\"api\": \"" + request.getApi() + "\",");
            sb.append("\"payload\": " + request.getPayload());
            sb.append("}");
            i++;
        }
        sb.append("]}");
        
        return sb.toString();
    }
}
