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
 * A data transfer object represents the file.
 */
public class AppDto {
    private long appId;
    private String code;
    private String name;
    private String description;
    private long spaceId;
    private long threadId;
    private UserDto creator;
    private String createdAt;
    private UserDto modifier;
    private String modifiedAt;
	/**
	 * @return the appId
	 */
	public long getAppId() {
		return appId;
	}
	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @return the spaceId
	 */
	public long getSpaceId() {
		return spaceId;
	}
	/**
	 * @return the threadId
	 */
	public long getThreadId() {
		return threadId;
	}
	/**
	 * @return the creator
	 */
	public UserDto getCreator() {
		return creator;
	}
	/**
	 * @return the createdAt
	 */
	public String getCreatedAt() {
		return createdAt;
	}
	/**
	 * @return the modifier
	 */
	public UserDto getModifier() {
		return modifier;
	}
	/**
	 * @return the modifiedAt
	 */
	public String getModifiedAt() {
		return modifiedAt;
	}	
    
}
