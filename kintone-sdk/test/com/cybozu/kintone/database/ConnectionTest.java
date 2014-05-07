package com.cybozu.kintone.database;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Before;
import org.junit.Test;

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
    public void testInsertLongRecord() {
        Connection db = getConnection();
        long app = getAppId();
        try {
            Record record;
            record = new Record();
            record.setString("Single_line_text", "foo");
            record.setLong("Number", 999);
            
            db.insert(app, record);
            
            ResultSet rs = db.select(app, "");
            if (rs.size() != 1) {
                fail("invalid count");
            }
            
        } catch(Exception e) {
            fail("failed to select");
        }
    }
    
    @Test
    public void testInsertLongListOfRecord() {
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
            record.setString("Single_line_text", "baz");
            records.add(record);
            db.insert(app, records);
            
            ResultSet rs = db.select(app, "");
            if (rs.size() != 3) {
                fail("invalid count");
            }
            
        } catch(Exception e) {
            fail("failed to select");
        }
    }


}
