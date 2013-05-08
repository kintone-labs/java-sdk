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

import com.cybozu.kintone.database.Connection;
import com.cybozu.kintone.database.DBException;
import com.cybozu.kintone.database.FileDto;
import com.cybozu.kintone.database.Record;
import com.cybozu.kintone.database.ResultSet;
import com.cybozu.kintone.database.UserDto;

public class SdkSample {

	/**
	 * SDK sample code
	 * @param args
	 */
	public static void main(String[] args) {
		// set domain, login name and password
		String domain = args[0];
		int app = Integer.valueOf(args[1]);
		String login = args[2];
		String password = args[3];
		
		// create connection
		Connection db;
		try {
			db = new Connection(domain, login, password);
			// db.setClientCert("foobaa.pfx", "password");
			// db.setProxy("127.0.0.1", 8888); // for fiddler2
			// db.setTrustAllHosts(true);
		} catch (Exception e) {
			return;
		}
		
		// add headers
		db.addHeader("X-New-Header", "brah");
		
		String query = "code = 2342";
		
		// select records
		ResultSet rs = null;
		try {
			rs = db.select(app, query, null);
		} catch (DBException e1) {
			e1.printStackTrace();
		}
		while(rs.next()) {
			long code = rs.getLong("code");
			String created = rs.getString("creator");
			UserDto creator = rs.getUser("created_time");
            String name = rs.getString("name");
            
            System.out.println(code + "," + created + "," + name + "," + creator.getName());
            
            // download file
            FileDto[] files = rs.getFiles("file");
            if (files.length > 0) {
	            try {
					try {
						rs.downloadFile("file", 0);
					} catch (DBException e) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        }

		ArrayList<Record> list = new ArrayList<Record>();

		// insert new record

		Record record;
		record= new Record();
		record.setString("code", "123");
		record.setString("name", "tom");

		try {
			db.insert(app, record);
		} catch (DBException e1) {
			e1.printStackTrace();
		}

		// insert new records

		record.setString("code", "456");
		list.add(record);
		try {
			db.insert(app, list.toArray(new Record[0]));
		} catch (DBException e1) {
			e1.printStackTrace();
		}

		// upload file
		String fileKey = null;
		try {
			File file = new File("c:\\tmp\\upload.jpg");
			fileKey = db.uploadFile("image/jpeg", file);
		} catch (DBException e) {
			e.printStackTrace();
		}

		// update a record
		record = new Record();
		record.setString("name", "bobby");
		record.setFiles("file", new String[]{fileKey});
		try {
			db.update(app, 2, record);
		} catch (DBException e1) {
			e1.printStackTrace();
		}

		// update records
		ArrayList<Long> ids = new ArrayList<Long>();
		ids.add(Long.valueOf(1));
		try {
			db.update(app, ids.toArray(new Long[0]), record);
		} catch (DBException e1) {
			e1.printStackTrace();
		}

		// update records
		try {
			db.update(app, query, record);
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
			db.delete(app, ids.toArray(new Long[0]));
		} catch (DBException e1) {
			e1.printStackTrace();
		}

		// delete records
		try {
			db.delete(app, "code = 123");
		} catch (DBException e1) {
			e1.printStackTrace();
		}

		db.close();

	}

}
