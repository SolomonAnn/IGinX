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
package cn.edu.tsinghua.iginx.query;

import cn.edu.tsinghua.iginx.conf.ConfigDescriptor;
import cn.edu.tsinghua.iginx.core.IService;
import cn.edu.tsinghua.iginx.core.context.RequestContext;
import cn.edu.tsinghua.iginx.plan.*;
import cn.edu.tsinghua.iginx.query.aysnc.queue.AsyncTaskQueue;
import cn.edu.tsinghua.iginx.query.aysnc.queue.MemoryAsyncTaskQueue;
import cn.edu.tsinghua.iginx.query.aysnc.task.AsyncTask;
import cn.edu.tsinghua.iginx.query.result.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static cn.edu.tsinghua.iginx.utils.CheckedFunction.wrap;

public abstract class AbstractPlanExecutor implements IPlanExecutor, IService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractPlanExecutor.class);

    private final AsyncTaskQueue asyncTaskQueue;

    private final ExecutorService asyncTaskDispatcher;

    private final ExecutorService asyncTaskExecuteThreadPool;

    private final ExecutorService syncExecuteThreadPool;

    protected AbstractPlanExecutor() {
        asyncTaskQueue = new MemoryAsyncTaskQueue();
        asyncTaskExecuteThreadPool = Executors.newFixedThreadPool(ConfigDescriptor.getInstance().getConfig().getAsyncExecuteThreadPool());
        asyncTaskDispatcher = Executors.newSingleThreadExecutor();
        asyncTaskDispatcher.submit(() -> {
            while (true) {
                AsyncTask asyncTask = asyncTaskQueue.getAsyncTask();
                asyncTaskExecuteThreadPool.submit(() -> {
                    IginxPlan plan = asyncTask.getIginxPlan();
                    SyncPlanExecuteResult planExecuteResult = null;
                    switch (plan.getIginxPlanType()) {
                        case INSERT_RECORDS:
                            planExecuteResult = syncExecuteInsertRecordsPlan((InsertRecordsPlan) plan);
                            break;
                        default:
                            logger.info("unimplemented method: " + plan.getIginxPlanType());
                    }
                    if (planExecuteResult == null || planExecuteResult.getStatusCode() != PlanExecuteResult.SUCCESS) { // 异步任务执行失败后再次执行，直到到达预设的最大执行次数
                        asyncTask.addRetryTimes();
                        if (asyncTask.getRetryTimes() < ConfigDescriptor.getInstance().getConfig().getMaxAsyncRetryTimes()) {
                            asyncTaskQueue.addAsyncTask(asyncTask);
                        }
                    }
                });
            }
        });
        syncExecuteThreadPool = Executors.newFixedThreadPool(ConfigDescriptor.getInstance().getConfig().getSyncExecuteThreadPool());
    }


    protected Future<InsertRecordsPlanExecuteResult> executeInsertRecordsPlan(InsertRecordsPlan plan) {
        if (plan.isSync()) {
            return syncExecuteThreadPool.submit(() -> syncExecuteInsertRecordsPlan(plan));
        }
        return null;
    }

    protected Future<QueryDataPlanExecuteResult> executeQueryDataPlan(QueryDataPlan plan) {
        if (plan.isSync()) {
            return syncExecuteThreadPool.submit(() -> syncExecuteQueryDataPlan(plan));
        }
        return null;
    }

    protected abstract InsertRecordsPlanExecuteResult syncExecuteInsertRecordsPlan(InsertRecordsPlan plan);

    protected abstract QueryDataPlanExecuteResult syncExecuteQueryDataPlan(QueryDataPlan plan);

    protected abstract AddColumnsPlanExecuteResult syncExecuteAddColumnsPlan(AddColumnsPlan plan);

    protected abstract DeleteColumnsPlanExecuteResult syncExecuteDeleteColumnsPlan(DeleteColumnsPlan plan);

    protected abstract DeleteDataInColumnsPlanExecuteResult syncExecuteDeleteDataInColumnsPlan(DeleteDataInColumnsPlan plan);

    protected abstract CreateDatabasePlanExecuteResult syncExecuteCreateDatabasePlan(CreateDatabasePlan plan);

    protected abstract DropDatabasePlanExecuteResult syncExecuteDropDatabasePlan(DropDatabasePlan dropDatabasePlan);

    protected AsyncPlanExecuteResult executeAsyncTask(IginxPlan iginxPlan) {
        return AsyncPlanExecuteResult.getInstance(asyncTaskQueue.addAsyncTask(new AsyncTask(iginxPlan, 0)));
    }

    @Override
    public List<PlanExecuteResult> executeIginxPlans(RequestContext requestContext) {
        List<PlanExecuteResult> planExecuteResults = requestContext.getIginxPlans().stream().filter(e -> !e.isSync()).map(this::executeAsyncTask).collect(Collectors.toList());
        switch (requestContext.getType()) {
            case InsertRecords:
                planExecuteResults.addAll(requestContext.getIginxPlans().stream().filter(IginxPlan::isSync).map(InsertRecordsPlan.class::cast).map(this::executeInsertRecordsPlan).map(wrap(Future::get)).collect(Collectors.toList()));
                break;
            case QueryData:
                planExecuteResults.addAll(requestContext.getIginxPlans().stream().filter(IginxPlan::isSync).map(QueryDataPlan.class::cast).map(this::executeQueryDataPlan).map(wrap(Future::get)).collect(Collectors.toList()));
                break;
            default:
                logger.info("unimplemented method: " + requestContext.getType());
                break;
        }
        return planExecuteResults;
    }

    @Override
    public void shutdown() throws Exception {
        asyncTaskDispatcher.shutdown();
        asyncTaskExecuteThreadPool.shutdown();
    }
}