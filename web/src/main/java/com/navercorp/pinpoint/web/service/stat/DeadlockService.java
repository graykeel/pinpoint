/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.web.service.stat;

import com.navercorp.pinpoint.common.server.bo.stat.DeadlockThreadCountBo;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Service
public class DeadlockService implements AgentStatService<DeadlockThreadCountBo> {

    private final AgentStatDao<DeadlockThreadCountBo> deadlockDao;

    public DeadlockService(@Qualifier("deadlockDaoFactory") AgentStatDao<DeadlockThreadCountBo> deadlockDao) {
        this.deadlockDao = Objects.requireNonNull(deadlockDao, "deadlockDao");
    }

    @Override
    public List<DeadlockThreadCountBo> selectAgentStatList(String agentId, Range range) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(range, "range");

        return this.deadlockDao.getAgentStatList(agentId, range);
    }

}
