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

package com.cybozu.kintone.samples;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.cybozu.kintone.database.BulkRequest;
import com.cybozu.kintone.database.CommentSet;
import com.cybozu.kintone.database.Connection;
import com.cybozu.kintone.database.FileDto;
import com.cybozu.kintone.database.MentionDto;
import com.cybozu.kintone.database.Record;
import com.cybozu.kintone.database.ResultSet;
import com.cybozu.kintone.database.UserDto;
import com.cybozu.kintone.database.exception.DBException;

public class SdkSample {

    /**
     * SDK sample code
     * 
     * @param args
     */
    public static void main(String[] args) {
        // set domain, application id, login name and password
        String domain = args[0];
        int app = Integer.valueOf(args[1]);
        String login = args[2];
        String password = args[3];

        // create connection
        Connection db;
        try {
            db = new Connection(domain, login, password);
            // db.setClientCert("foobar.pfx", "password");
            // db.setProxy("127.0.0.1", 8888); // for fiddler2
            // db.setTrustAllHosts(true);
        } catch (Exception e) {
            return;
        }

        // add headers
        db.addHeader("X-New-Header", "brah");

        String query = "code = 456";

        // select records
        ResultSet rs = null;
        try {
            rs = db.select(app, query);
        } catch (DBException e1) {
            e1.printStackTrace();
        }
        while (rs.next()) {
            try {
                // record number
                long recNo = rs.getId();
                // number field
                long code = rs.getLong("code");
                // date field
                Date created = rs.getDate("created_time");
                // user field
                UserDto creator = rs.getUser("creator");
                // string field
                String name = rs.getString("name");
                // check box
                List<String> strings = rs.getStrings("checkbox");
                // sub table
                List<Record> subtable = rs.getSubtable("table1");
                
                // download file
                List<FileDto> files = rs.getFiles("file");
                if (files.size() > 0) {
                    File f = rs.downloadFile("file", 0);
                    f.renameTo(new File("c:\\tmp\\" + files.get(0).getName()));
                }
            } catch (DBException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        try {
        	// get records with total count
            rs = db.selectWithTotalCount(app, query);
        } catch (DBException e1) {
            e1.printStackTrace();
        }
        long totalCount = rs.getTotalCount();

        ArrayList<Record> list = new ArrayList<Record>();

        // insert new record

        Record record;
        record = new Record();
        record.setString("code", "123");
        record.setString("name", "tom");

        try {
            db.insert(app, record);
        } catch (DBException e1) {
            e1.printStackTrace();
        }

        // insert new records
        // upload file
        String fileKey = null;
        try {
            File file = new File("c:\\tmp\\upload1.jpg");
            fileKey = db.uploadFile(file);
        } catch (DBException e) {
            e.printStackTrace();
        }

        record.setString("code", "456");
        List<String> files = new ArrayList<String>();
        files.add(fileKey);
        record.setFiles("file", files);
        list.add(record);
        try {
            db.insert(app, list);
        } catch (DBException e1) {
            e1.printStackTrace();
        }
        
        // update a record
        record = new Record();
        record.setString("name", "bobby");
        record.setFile("file", new File("c:\\tmp\\upload2.jpg"), "image/jpeg");
        try {
            db.update(app, 2, record);
        } catch (DBException e1) {
            e1.printStackTrace();
        }

        // update records
        ArrayList<Long> ids = new ArrayList<Long>();
        ids.add(Long.valueOf(1));
        try {
            db.update(app, ids, record);
        } catch (DBException e1) {
            e1.printStackTrace();
        }

        // update records
        try {
            db.updateByQuery(app, query, record);
        } catch (DBException e1) {
            e1.printStackTrace();
        }
        
        // update record by specified key
        try {
        	db.updateRecordByKey(app, "name", record);
        } catch (DBException e1) {
            e1.printStackTrace();
        }

        // update status
        try {
            db.updateStatus(app, 1, "処理開始", "user1");
        } catch (DBException e1) {
            e1.printStackTrace();
        }
        
        // update assignee
        try {
            List<String> codes = new ArrayList<String>();
    		codes.add("Administrator");
    		db.updateAssignees(app, 1, codes);
        } catch (DBException e1) {
            e1.printStackTrace();
        }
        
        // delete a record
        try {
            db.delete(app, 1);
        } catch (DBException e1) {
            e1.printStackTrace();
        }

        // delete records
        try {
            db.delete(app, ids);
        } catch (DBException e1) {
            e1.printStackTrace();
        }

        // delete records
        try {
            db.deleteByQuery(app, "code = 123");
        } catch (DBException e1) {
            e1.printStackTrace();
        }

        // bulk request
        List<Record> records = new ArrayList<Record>();
        try {
            rs = db.select(app, "");
            while (rs.next()) {
                record = new Record();
                record.setId(rs.getId());
                record.setRevision(rs.getRevision());
                record.setString("name", "hoge");
                
                records.add(record);
            }
            
            BulkRequest bulk = new BulkRequest();
            bulk.updateByRecords(app, records);
            
            records = new ArrayList<Record>();
    
            record = new Record();
            record.setString("name", "fuga");
            records.add(record);
            record = new Record();
            record.setString("name", "piyo");
            records.add(record);
            bulk.insert(app, records);
            
            bulk.delete(app, ids);
            
            db.bulkRequest(bulk);
        } catch (DBException e1) {
            e1.printStackTrace();
        }
        
        // record comment
		long recordId = 1;
		try {
			// get comments
			CommentSet cs = db.getComments(app, recordId, true);
			while (cs.next()) {
				String text1 = cs.getText();
			}
			cs.next();
			
			// add comment
	        List<MentionDto> mentions = new ArrayList<MentionDto>();
			mentions.add(new MentionDto("user1", "USER"));
			mentions.add(new MentionDto("user2", "USER"));
			String text = "this is a comment";
			long id = db.addComment(app, recordId, text, mentions);
			
			// remove comment
			db.deleteComment(app,  recordId,  id);
		} catch (DBException e1) {
            e1.printStackTrace();
        }
		
        db.close();

    }

}
