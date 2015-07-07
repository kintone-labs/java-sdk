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

/**
 * Defines the field types which kintone official api provides.
 * 
 */
public enum FieldType {

    SINGLE_LINE_TEXT, 
    NUMBER, 
    CALC, 
    MULTI_LINE_TEXT, 
    RICH_TEXT, 
    CHECK_BOX, 
    RADIO_BUTTON, 
    DROP_DOWN, 
    MULTI_SELECT, 
    FILE, 
    DATE, 
    TIME, 
    DATETIME, 
    USER_SELECT, 
    LINK, 
    CATEGORY, 
    STATUS, 
    RECORD_NUMBER, 
    CREATOR, 
    CREATED_TIME, 
    MODIFIER, 
    UPDATED_TIME, 
    STATUS_ASSIGNEE, 
    SUBTABLE,
    __REVISION__,
    __ID__;

    public static FieldType getEnum(String str) {
        FieldType[] values = FieldType.values();

        for (FieldType value : values) {
            if (str.equals(value.toString())) {
                return value;
            }
        }
        return null;
    }
}
