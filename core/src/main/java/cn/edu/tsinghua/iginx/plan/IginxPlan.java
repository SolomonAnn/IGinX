/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package cn.edu.tsinghua.iginx.plan;

import java.util.List;

public abstract class IginxPlan {

	private IginxPlanType iginxPlanType;

	private boolean isQuery;

	private boolean canBeSplit;

	private List<IginxPlan> splitPlans;

	private boolean isSync;

	private long databaseId;

	public IginxPlan(IginxPlanType iginxPlanType, boolean isQuery) {
		this.iginxPlanType = iginxPlanType;
		this.isQuery = isQuery;
	}

	public IginxPlanType getIginxPlanType() {
		return iginxPlanType;
	}

	public boolean isQuery() {
		return isQuery;
	}

	public boolean canBeSplit() {
		return canBeSplit;
	}

	public List<IginxPlan> getSplitPlans() {
		return splitPlans;
	}

	public boolean isSync() {
		return isSync;
	}

	public void setQuery(boolean isQuery) {
		this.isQuery = isQuery;
	}

	public void setCanBeSplit(boolean canBeSplit) {
		this.canBeSplit = canBeSplit;
	}

	public void setSplitPlans(List<IginxPlan> splitPlans) {
		this.splitPlans = splitPlans;
	}

	public void setSync(boolean isSync) {
		this.isSync = isSync;
	}

	public long getDatabaseId() {
		return databaseId;
	}

	public void setDatabaseId(long databaseId) {
		this.databaseId = databaseId;
	}

	public enum IginxPlanType {
		CREATE_DATABASE, DROP_DATABASE, ADD_COLUMNS, DELETE_COLUMNS, INSERT_RECORDS,
		DELETE_DATA_IN_COLUMNS, QUERY_DATA;
	}
}