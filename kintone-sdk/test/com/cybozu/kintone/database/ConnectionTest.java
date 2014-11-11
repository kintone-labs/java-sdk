package com.cybozu.kintone.database;

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.cybozu.kintone.database.exception.DBException;

public class ConnectionTest {

    private Connection getConnection() {
        
        String domain = System.getenv("DOMAIN");
        String login = System.getenv("LOGIN");
        String password = System.getenv("PASSWORD");

        return new Connection(domain, login, password);
        
    }
    
    private long getAppId() {
        return Integer.valueOf(System.getenv("APP_ID"));
    }
    
    private long getGuestSpaceId() {
        return Integer.valueOf(System.getenv("GUEST_SPACE_ID"));
    }
    
    private long getGuestAppId() {
        return Integer.valueOf(System.getenv("GUEST_APP_ID"));
    }
    
    @Before
    public void initialize() {

        Connection db = getConnection();
        long app = getAppId();
        try {
            db.deleteByQuery(app, "Record_number > 0");
        } catch(Exception e) {
            fail("Can not initialize the app");
        }
    }
    
    private List<Long> insertRecords() throws DBException {
        
        Connection db = getConnection();
        long app = getAppId();
        ArrayList<Record> records = new ArrayList<Record>();
        
        Record record;
        record = new Record();
        record.setString("Single_line_text", "foo");
        records.add(record);
        record = new Record();
        record.setString("Single_line_text", "bar");
        records.add(record);
        record = new Record();
        record.setString("Single_line_text", "baz");
        records.add(record);
        
        List<Long> ids = db.insert(app, records);
        if (ids.size() != 3) {
            fail("invalid count");
        }
        return ids;
    }
    
    @Test
    public void testApiToken() {
        String domain = System.getenv("DOMAIN");
        String apiToken = "lzrxAeavY4RI99c9146xzZq4U7gkOj3uvMTMt5QS";
        
        Connection db = new Connection(domain, apiToken);
        //db.setProxy("127.0.0.1", 8888); // for fiddler2
        //db.setTrustAllHosts(true);
        long app = getAppId();
        try {
            db.select(app, "Record_number > 0");
        } catch(Exception e) {
            fail("Can not use api token");
        }
    }
    
    @Test
    public void testSelect() {
        Connection db = getConnection();
        long app = getAppId();
        try {
            ArrayList<Record> records = new ArrayList<Record>();
            
            Record record;
            record = new Record();
            record.setString("Single_line_text", "foo");
            records.add(record);
            record = new Record();
            record.setString("Single_line_text", "bar");
            records.add(record);
            record = new Record();
            record.setString("Single_line_text", "foo");
            records.add(record);
            record = new Record();
            record.setString("Single_line_text", "bar");
            records.add(record);
            record = new Record();
            record.setString("Single_line_text", "foo");
            records.add(record);
            db.insert(app, records);
            
            ResultSet rs = db.select(app, "Single_line_text = \"foo\"");
            if (rs.size() != 3) {
                fail("invalid count " + rs.size());
            }
        } catch(Exception e) {
            fail("failed to select");
        }
    }

    @Test
    public void testSelect2() {
        Connection db = getConnection();
        long app = getAppId();
        try {
            ArrayList<Record> records = new ArrayList<Record>();
            
            Record record;
            record = new Record();
            record.setString("文字列__1行_", "ほげ");
            records.add(record);
            record = new Record();
            record.setString("文字列__1行_", "ふが");
            records.add(record);
            record = new Record();
            record.setString("文字列__1行_", "ほげ");
            records.add(record);
            record = new Record();
            record.setString("文字列__1行_", "ふが");
            records.add(record);
            record = new Record();
            record.setString("文字列__1行_", "ほげ");
            records.add(record);
            db.insert(app, records);
            
            String[] columns = {"文字列__1行_"};
            ResultSet rs = db.select(app, "文字列__1行_ = \"ほげ\"", columns);
            if (rs.size() != 3) {
                fail("invalid count " + rs.size());
            }
            while (rs.next()) {
                assertEquals(rs.getString("文字列__1行_"), "ほげ");
            }
        } catch(DBException e) {
            fail("failed to select");
        }
    }
    
    @Test
    public void testInsertLongRecord() {
        Connection db = getConnection();
        long app = getAppId();
        try {
            Record record;
            record = new Record();
            record.setString("Single_line_text", "foo");
            record.setLong("Number", 999);
            record.setString("Number_0", "999.99");
            record.setString("文字列__1行_", "ほげ");
            
            db.insert(app, record);
            
            ResultSet rs = db.select(app, "");
            if (rs.size() != 1) {
                fail("invalid count");
            }
            rs.next();
            assertEquals(rs.getString("Single_line_text"), "foo");
            assertEquals(rs.getLong("Number"), new Long(999));
            assertEquals(rs.getString("Number_0"), "999.99");
            assertEquals(rs.getString("文字列__1行_"), "ほげ");
            
        } catch(Exception e) {
            fail("db exception:" + e.getMessage());
        }
    }
    
    @Test
    public void testInsertCreatorModifier() {
        Connection db = getConnection();
        long app = getAppId();
        try {
            Record record;
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm");
            Date date = df.parse("2000-01-01 01:34");
            record = new Record();
            record.setUser("Created_by", "aono");
            record.setUser("Updated_by", "aono");
            record.setDateTime("Created_datetime", date);
            record.setDateTime("Updated_datetime", date);
            
            db.insert(app, record);
            
            ResultSet rs = db.select(app, "");
            if (rs.size() != 1) {
                fail("invalid count");
            }
            rs.first();
            if (!rs.getUser("Created_by").getCode().equals("aono")) {
                fail("failed to update created by");
            }
            if (!rs.getUser("Updated_by").getCode().equals("aono")) {
                fail("failed to update updated by");
            }
            if (!rs.getDateTime("Created_datetime").equals(date)) {
                fail("failed to update created datetime" + rs.getDate("Created_datetime").toString());
            }
            if (!rs.getDateTime("Updated_datetime").equals(date)) {
                fail("failed to update updated datetime" + rs.getDate("Updated_datetime").toString());
            }
            
        } catch(Exception e) {
            fail("db exception:" + e.getMessage());
        }
    }
    
    @Test
    public void testInsertLongListOfRecord() {
        Connection db = getConnection();
        long app = getAppId();
        try {
            insertRecords();
            
            ResultSet rs = db.select(app, "");
            if (rs.size() != 3) {
                fail("invalid count");
            }
            
        } catch(Exception e) {
            fail("db exception:" + e.getMessage());
        }
    }
    
    @Test
    public void testUpdateLongLongRecord() {
        Connection db = getConnection();
        long app = getAppId();
        try {
            Record record;
            List<Long> ids = insertRecords();
            record = new Record();
            record.setString("Single_line_text", "hoge");
            long id = ids.get(1);
            db.update(app, id, record);
            
            ResultSet rs = db.select(app, "Single_line_text = \"hoge\"");
            if (rs.size() != 1) {
                fail("failed to update");
            }
        } catch(Exception e) {
            fail("db exception:" + e.getMessage());
        }
    }
    
    @Test
    public void testUpdateLongListOfLongRecord() {
        Connection db = getConnection();
        long app = getAppId();
        try {
            Record record;
            List<Long> ids = insertRecords();
            
            record = new Record();
            record.setString("Single_line_text", "hoge");
            db.update(app, ids, record);
            
            ResultSet rs = db.select(app, "Single_line_text = \"hoge\"");
            if (rs.size() != 3) {
                fail("failed to update");
            }
        } catch(Exception e) {
            fail("db exception:" + e.getMessage());
        }
    }
    
    @Test
    public void testUpdateByRecord() {
        Connection db = getConnection();
        long app = getAppId();
        try {
            Record record = new Record();
            List<Long> ids = insertRecords();
            long id = ids.get(1);
            ResultSet rs = db.select(app, "Record_number = " + id);
            if (rs.size() != 1) {
                fail("invalid count");
            }
            rs.next();
            record.setId(rs.getId());
            record.setRevision(rs.getRevision());
            record.setString("Single_line_text", "hoge");
            db.updateByRecord(app, record);
            
            rs = db.select(app, "Single_line_text = \"hoge\"");
            if (rs.size() != 1) {
                fail("failed to update");
            }
            rs.next();
            if (rs.getId() != id) {
                fail("invalid id");
            }
        } catch(Exception e) {
            fail("db exception:" + e.getMessage());
        }
    }
    
    @Test
    public void testUpdateByRecords() {
        Connection db = getConnection();
        long app = getAppId();
        try {
            Record record;
            List<Record> records = new ArrayList<Record>();
            List<Long> ids = insertRecords();
            ResultSet rs = db.select(app, "");
            if (rs.size() != 3) {
                fail("invalid count");
            }
            while (rs.next()) {
                record = new Record();
                record.setId(rs.getId());
                record.setRevision(rs.getRevision());
                record.setString("Single_line_text", "hoge");
                
                records.add(record);
            }
            db.updateByRecords(app, records);
            
            rs = db.select(app, "Single_line_text = \"hoge\"");
            if (rs.size() != 3) {
                fail("failed to update");
            }
        } catch(Exception e) {
            fail("db exception:" + e.getMessage());
        }
    }
    
    @Test
    public void testUpdateByRecords2() {
        Connection db = getConnection();
        long app = getAppId();
        Record record;
        List<Record> records = new ArrayList<Record>();
        ResultSet rs = null;
        
        try {
            
            List<Long> ids = insertRecords();
            rs = db.select(app, "");
            if (rs.size() != 3) {
                fail("invalid count");
            }
            
            record = new Record();
            rs.next();
            record.setId(rs.getId());
            record.setString("Single_line_text", "hoge");
            db.updateByRecord(app, record);
            
        } catch(Exception e) {
            fail("db exception");
        }
        
        try {
            rs.first();
            while (rs.next()) {
                record = new Record();
                record.setId(rs.getId());
                record.setRevision(rs.getRevision());
                record.setString("Single_line_text", "hoge");
                
                records.add(record);
            }
            db.updateByRecords(app, records);
            fail("no conflict");
        } catch(Exception e) {
        }
    }
    
    @Test
    public void testUpdateByQuery() {
        Connection db = getConnection();
        long app = getAppId();
        try {
            Record record;
            record = new Record();
            record.setString("Single_line_text", "hoge");
            
            List<Long> ids = insertRecords();
            db.updateByQuery(app, "Single_line_text = \"foo\"", record);
            
            ResultSet rs = db.select(app, "Single_line_text = \"hoge\"");
            if (rs.size() != 1) {
                fail("failed to update");
            }
        } catch(Exception e) {
            fail("db exception:" + e.getMessage());
        }
    }
    
    @Test
    public void testDeleteLongLong() {
        Connection db = getConnection();
        long app = getAppId();
        try {
            List<Long> ids = insertRecords();
            db.delete(app, ids.get(1));
            
            ResultSet rs = db.select(app, "");
            if (rs.size() != 2) {
                fail("failed to delete");
            }
        } catch(Exception e) {
            fail("db exception:" + e.getMessage());
        }
    }
    
    @Test
    public void testDeleteLongListOfLong() {
        Connection db = getConnection();
        long app = getAppId();
        try {
            List<Long> ids = insertRecords();
            db.delete(app, ids);
            
            ResultSet rs = db.select(app, "");
            if (rs.size() != 0) {
                fail("failed to delete");
            }
        } catch(Exception e) {
            fail("db exception:" + e.getMessage());
        }
    }

    @Test
    public void testDeleteByRecord() {
        Connection db = getConnection();
        long app = getAppId();
        try {
            Record record = new Record();
            List<Long> ids = insertRecords();
            long id = ids.get(1);
            ResultSet rs = db.select(app, "Record_number = " + id);
            if (rs.size() != 1) {
                fail("invalid count");
            }
            rs.next();
            record.setId(rs.getId());
            record.setRevision(rs.getRevision());
            db.deleteByRecord(app, record);
            
            rs = db.select(app, "");
            if (rs.size() != 2) {
                fail("failed to delete");
            }
        } catch(Exception e) {
            fail("db exception:" + e.getMessage());
        }
    }
    
    @Test
    public void testDeleteByRecords() {
        Connection db = getConnection();
        long app = getAppId();
        try {
            Record record;
            List<Record> records = new ArrayList<Record>();
            List<Long> ids = insertRecords();
            ResultSet rs = db.select(app, "");
            if (rs.size() != 3) {
                fail("invalid count");
            }
            while (rs.next()) {
                record = new Record();
                record.setId(rs.getId());
                record.setRevision(rs.getRevision());
                
                records.add(record);
            }
            db.deleteByRecords(app, records);
            
            rs = db.select(app, "");
            if (rs.size() != 0) {
                fail("failed to delete");
            }
        } catch(Exception e) {
            fail("db exception:" + e.getMessage());
        }
    }
    
    @Test
    public void testDeleteByRecords2() {
        Connection db = getConnection();
        long app = getAppId();
        Record record;
        List<Record> records = new ArrayList<Record>();
        ResultSet rs = null;
        
        try {
            
            List<Long> ids = insertRecords();
            rs = db.select(app, "");
            if (rs.size() != 3) {
                fail("invalid count");
            }
            
            record = new Record();
            rs.next();
            record.setId(rs.getId());
            record.setString("Single_line_text", "hoge");
            db.updateByRecord(app, record);
            
        } catch(Exception e) {
            fail("db exception:" + e.getMessage());
        }
        
        try {
            rs.first();
            while (rs.next()) {
                record = new Record();
                record.setId(rs.getId());
                record.setRevision(rs.getRevision());
                record.setString("Single_line_text", "hoge");
                
                records.add(record);
            }
            db.deleteByRecords(app, records);
            fail("no conflict");
        } catch(Exception e) {
        }
    }
    
    @Test
    public void testDeleteByQuery() {
        Connection db = getConnection();
        long app = getAppId();
        try {
            List<Long> ids = insertRecords();
            db.deleteByQuery(app, "Single_line_text = \"foo\"");
            
            ResultSet rs = db.select(app, "");
            if (rs.size() != 2) {
                fail("failed to delete");
            }
        } catch(Exception e) {
            fail("db exception");
        }
    }
    
    @Test
    public void testBulkRequest() {
        Connection db = getConnection();
        long app = getAppId();
        try {
            Record record;
            List<Record> records = new ArrayList<Record>();
            List<Long> ids = insertRecords();
            
            ResultSet rs = db.select(app, "order by Record_number asc");
            if (rs.size() != 3) {
                fail("invalid count");
            }
            int i = 0;
            while (rs.next()) {
                record = new Record();
                record.setId(rs.getId());
                record.setRevision(rs.getRevision());
                record.setString("Single_line_text", "hoge" + i);
                
                records.add(record);
                i++;
            }
            
            BulkRequest bulk = new BulkRequest();
            bulk.updateByRecords(app, records);
            
            records = new ArrayList<Record>();

            record = new Record();
            record.setString("Single_line_text", "fuga");
            records.add(record);
            record = new Record();
            record.setString("Single_line_text", "piyo");
            records.add(record);
            bulk.insert(app, records);
            
            bulk.delete(app, ids.get(1));
            
            db.bulkRequest(bulk);
            
            rs = db.select(app, "order by Record_number asc");
            if (rs.size() != 4) {
                fail("invalid count");
            }
            i = 0;
            String[] values = {"hoge0", "hoge2", "fuga", "piyo"};
            while (rs.next()) {
                if (!rs.getString("Single_line_text").equals(values[i])) {
                    fail("invalid value:" + values[i]);
                }
                i++;
            }
            
        } catch(Exception e) {
            fail("db exception:" + e.getMessage());
        }
    }
    
    @Test
    public void testGetApp() {
        Connection db = getConnection();
        try {
        	AppDto app = db.getApp(1);
        	System.out.println(app.getAppId());
        	System.out.println(app.getCode());
        	System.out.println(app.getName());
        	System.out.println(app.getDescription());
        } catch(Exception e) {
            fail("db exception:" + e.getMessage());
        }
    }
    
    @Test
    public void testGetApps() {
        Connection db = getConnection();
        try {
        	List<AppDto> apps = db.getApps("hoge");
        	for (AppDto app: apps) {
        		System.out.println(app.getName());
        	}
        } catch(Exception e) {
            fail("db exception:" + e.getMessage());
        }
    }
    
    @Test
    public void testGuestSpace() {
        Connection db = getConnection();
        db.setGuestSpaceId(getGuestSpaceId());
        long app = getGuestAppId();
        try {
            ResultSet rs = db.select(app, "");
            if (rs.size() != 1) {
                fail("invalid count");
            }
            rs.next();
            assertEquals(rs.getString("Single_line_text"), "test");
            assertEquals(rs.getLong("Number"), new Long(3));
            
        } catch(Exception e) {
            fail("db exception:" + e.getMessage());
        }
    }
}
