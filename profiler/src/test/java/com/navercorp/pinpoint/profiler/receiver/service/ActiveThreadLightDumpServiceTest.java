/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.receiver.service;

import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.common.util.ThreadMXBeanUtils;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceSnapshot;
import com.navercorp.pinpoint.profiler.context.active.DefaultActiveTraceSnapshot;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.thrift.dto.command.TActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDump;
import com.navercorp.pinpoint.thrift.dto.command.TCmdActiveThreadLightDumpRes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Taejin Koo
 */
public class ActiveThreadLightDumpServiceTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final int CREATE_SIZE = 10;

    private static final long DEFAULT_TIME_MILLIS = System.currentTimeMillis() - 1000000;
    private static final long TIME_DIFF_INTERVAL = 100;
    private static final long JOB_TIMEOUT = 1000 * 10;

    private final AtomicInteger idGenerator = new AtomicInteger();
    private final PinpointThreadFactory pinpointThreadFactory = new PinpointThreadFactory(this.getClass().getSimpleName());

    private final WaitingJobListFactory waitingJobListFactory = new WaitingJobListFactory();


    @AfterEach
    public void tearDown() throws Exception {
        waitingJobListFactory.close();
    }

    @Test
    public void basicFunctionTest1() {
        List<WaitingJob> waitingJobList = this.waitingJobListFactory.createList(CREATE_SIZE, JOB_TIMEOUT);

        List<ActiveTraceSnapshot> activeTraceInfoList = createMockActiveTraceInfoList(CREATE_SIZE, DEFAULT_TIME_MILLIS, TIME_DIFF_INTERVAL, waitingJobList);

        ActiveThreadLightDumpService service = createService(activeTraceInfoList);
        TCmdActiveThreadLightDumpRes response = (TCmdActiveThreadLightDumpRes) service.requestCommandService(createRequest(0, null, null));

        Assertions.assertEquals(CREATE_SIZE, response.getThreadDumpsSize());

    }

    @Test
    public void basicFunctionTest2() {
        List<WaitingJob> waitingJobList = this.waitingJobListFactory.createList(CREATE_SIZE, JOB_TIMEOUT);


        List<ActiveTraceSnapshot> activeTraceInfoList = createMockActiveTraceInfoList(CREATE_SIZE, DEFAULT_TIME_MILLIS, TIME_DIFF_INTERVAL, waitingJobList);

        TCmdActiveThreadLightDump tCmdActiveThreadDump = createRequest(0, null, Collections.singletonList(1L));

        ActiveThreadLightDumpService service = createService(activeTraceInfoList);
        TCmdActiveThreadLightDumpRes response = (TCmdActiveThreadLightDumpRes) service.requestCommandService(tCmdActiveThreadDump);

        Assertions.assertEquals(1, response.getThreadDumpsSize());

    }

    @Test
    public void basicFunctionTest3() {
        List<WaitingJob> waitingJobList = this.waitingJobListFactory.createList(CREATE_SIZE, JOB_TIMEOUT);

        int targetThreadNameSize = 3;

        List<ActiveTraceSnapshot> activeTraceInfoList = createMockActiveTraceInfoList(CREATE_SIZE, DEFAULT_TIME_MILLIS, TIME_DIFF_INTERVAL, waitingJobList);

        List<String> threadNameList = extractThreadNameList(activeTraceInfoList, targetThreadNameSize);
        TCmdActiveThreadLightDump tCmdActiveThreadDump = createRequest(0, threadNameList, null);

        ActiveThreadLightDumpService service = createService(activeTraceInfoList);
        TCmdActiveThreadLightDumpRes response = (TCmdActiveThreadLightDumpRes) service.requestCommandService(tCmdActiveThreadDump);

        Assertions.assertEquals(3, response.getThreadDumpsSize());

    }

    @Test
    public void basicFunctionTest4() {
        List<WaitingJob> waitingJobList = this.waitingJobListFactory.createList(CREATE_SIZE, JOB_TIMEOUT);


        List<ActiveTraceSnapshot> activeTraceInfoList = createMockActiveTraceInfoList(CREATE_SIZE, DEFAULT_TIME_MILLIS, TIME_DIFF_INTERVAL, waitingJobList);
        List<ActiveTraceSnapshot> activeTraceSnapshotList = shuffle(activeTraceInfoList);

        int targetThreadNameSize = 3;
        List<String> threadNameList = extractThreadNameList(activeTraceSnapshotList.subList(0, targetThreadNameSize), targetThreadNameSize);

        int targetTraceIdSize = 3;
        List<Long> localTraceIdList = extractLocalTraceIdList(activeTraceSnapshotList.subList(targetThreadNameSize, CREATE_SIZE), targetTraceIdSize);
        TCmdActiveThreadLightDump tCmdActiveThreadDump = createRequest(0, threadNameList, localTraceIdList);

        ActiveThreadLightDumpService service = createService(activeTraceInfoList);
        TCmdActiveThreadLightDumpRes response = (TCmdActiveThreadLightDumpRes) service.requestCommandService(tCmdActiveThreadDump);

        Assertions.assertEquals(targetThreadNameSize + targetTraceIdSize, response.getThreadDumpsSize());

    }

    @Test
    public void basicFunctionTest5() {
        List<WaitingJob> waitingJobList = this.waitingJobListFactory.createList(CREATE_SIZE, JOB_TIMEOUT);


        List<ActiveTraceSnapshot> activeTraceInfoList = createMockActiveTraceInfoList(CREATE_SIZE, DEFAULT_TIME_MILLIS, TIME_DIFF_INTERVAL, waitingJobList);

        int limit = 3;
        List<Long> oldTimeList = getOldTimeList(limit);

        TCmdActiveThreadLightDump tCmdActiveThreadDump = createRequest(limit, null, null);

        ActiveThreadLightDumpService service = createService(activeTraceInfoList);
        TCmdActiveThreadLightDumpRes response = (TCmdActiveThreadLightDumpRes) service.requestCommandService(tCmdActiveThreadDump);

        Assertions.assertEquals(limit, response.getThreadDumpsSize());

        for (TActiveThreadLightDump dump : response.getThreadDumps()) {
            assertThat(oldTimeList).contains(dump.getStartTime());
        }

    }


    private List<ActiveTraceSnapshot> createMockActiveTraceInfoList(int createActiveTraceRepositorySize, long currentTimeMillis, long diff, List<WaitingJob> waitingJobList) {
        List<ActiveTraceSnapshot> activeTraceInfoList = new ArrayList<>(createActiveTraceRepositorySize);
        for (int i = 0; i < createActiveTraceRepositorySize; i++) {
            ActiveTraceSnapshot activeTraceInfo = createActiveTraceInfo(currentTimeMillis + (diff * i), waitingJobList.get(i));
            activeTraceInfoList.add(activeTraceInfo);
        }
        return activeTraceInfoList;
    }

    private ActiveTraceSnapshot createActiveTraceInfo(long startTime, Runnable runnable) {
        Thread thread = pinpointThreadFactory.newThread(runnable);
        thread.start();
        long threadId = thread.getId();

        int id = idGenerator.incrementAndGet();
        LocalTraceRoot traceRoot = TraceRoot.local("agentId-" + id, startTime, id);
        traceRoot.getShared().setThreadId(threadId);

        return DefaultActiveTraceSnapshot.of(traceRoot);
    }

    private List<Long> getOldTimeList(int maxCount) {
        List<Long> startTimeMillisList = new ArrayList<>(maxCount);
        for (int i = 0; i < maxCount; i++) {
            startTimeMillisList.add(DEFAULT_TIME_MILLIS + (TIME_DIFF_INTERVAL * i));
        }
        return startTimeMillisList;
    }

    private List<String> extractThreadNameList(List<ActiveTraceSnapshot> activeTraceInfoList, int size) {
        List<ActiveTraceSnapshot> activeTraceSnapshotList = shuffle(activeTraceInfoList);

        List<String> threadNameList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            final ActiveTraceSnapshot activeTraceSnapshot = activeTraceSnapshotList.get(i);
            final long threadId = activeTraceSnapshot.getThreadId();
            ThreadInfo thread = ThreadMXBeanUtils.getThreadInfo(threadId);
            threadNameList.add(thread.getThreadName());
        }

        return threadNameList;
    }

    private List<Long> extractLocalTraceIdList(List<ActiveTraceSnapshot> activeTraceInfoList, int size) {
        List<ActiveTraceSnapshot> activeTraceSnapshotList = shuffle(activeTraceInfoList);

        List<Long> localTraceIdList = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            localTraceIdList.add(activeTraceSnapshotList.get(i).getLocalTransactionId());
        }

        return localTraceIdList;
    }

    private <E> List<E> shuffle(List<E> list) {
        List<E> result = new ArrayList<>(list);
        Collections.shuffle(result, ThreadLocalRandom.current());
        return result;
    }

    private ActiveThreadLightDumpService createService(List<ActiveTraceSnapshot> activeTraceInfoList) {
        ActiveTraceRepository activeTraceRepository = mock(ActiveTraceRepository.class);
        when(activeTraceRepository.snapshot()).thenReturn(activeTraceInfoList);

        ActiveThreadDumpCoreService activeThreadDump = new ActiveThreadDumpCoreService(activeTraceRepository);
        return new ActiveThreadLightDumpService(activeThreadDump);
    }

    private TCmdActiveThreadLightDump createRequest(int limit, List<String> threadNameList, List<Long> localTraceIdList) {
        TCmdActiveThreadLightDump request = new TCmdActiveThreadLightDump();
        if (limit > 0) {
            request.setLimit(limit);
        }
        if (CollectionUtils.hasLength(threadNameList)) {
            request.setThreadNameList(threadNameList);
        }
        if (CollectionUtils.hasLength(localTraceIdList)) {
            request.setLocalTraceIdList(localTraceIdList);
        }
        return request;
    }

}
