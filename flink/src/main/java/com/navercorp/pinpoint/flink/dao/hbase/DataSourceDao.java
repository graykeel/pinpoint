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
package com.navercorp.pinpoint.flink.dao.hbase;

import com.navercorp.pinpoint.common.hbase.HbaseTable;
import com.navercorp.pinpoint.common.hbase.HbaseTemplate2;
import com.navercorp.pinpoint.common.hbase.TableNameProvider;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.ApplicationStatHbaseOperationFactory;
import com.navercorp.pinpoint.common.server.bo.serializer.stat.join.ApplicationStatSerializer;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinApplicationStatBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceListBo;
import com.navercorp.pinpoint.common.server.bo.stat.join.StatType;

/**
 * @author minwoo.jung
 */
public class DataSourceDao extends DefaultApplicationMetricDao<JoinDataSourceListBo> {

    public DataSourceDao(ApplicationStatSerializer<JoinDataSourceListBo> serializer,
                         HbaseTemplate2 hbaseTemplate2,
                         ApplicationStatHbaseOperationFactory operationFactory,
                         TableNameProvider tableNameProvider) {
        super(StatType.APP_DATA_SOURCE, JoinApplicationStatBo::getJoinDataSourceListBoList, serializer, HbaseTable.APPLICATION_STAT_AGGRE,
                hbaseTemplate2, operationFactory, tableNameProvider);
    }


}
