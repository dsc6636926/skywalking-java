/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.apm.testcase.jedis.controller;

import java.util.Arrays;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.HostAndPort;

@RestController
@RequestMapping("/case")
public class CaseController {

    private static final String SUCCESS = "Success";

    @Value("${redis.host:127.0.0.1}")
    private String redisHost;

    @Value("${redis.port:6379}")
    private Integer redisPort;

    @Value("${redis.cluster.m1.host:127.0.0.1}")
    private String redisClusterM1Host;
    @Value("${redis.cluster.m1.port:6379}")
    private Integer redisClusterM1Port;

    @Value("${redis.cluster.m2.host:127.0.0.1}")
    private String redisClusterM2Host;
    @Value("${redis.cluster.m2.port:6379}")
    private Integer redisClusterM2Port;

    @Value("${redis.cluster.m3.host:127.0.0.1}")
    private String redisClusterM3Host;
    @Value("${redis.cluster.m3.port:6379}")
    private Integer redisClusterM3Port;

    @RequestMapping("/jedis-scenario")
    @ResponseBody
    public String testcase() throws Exception {
        try (RedisCommandExecutor command = new RedisCommandExecutor(redisHost, redisPort)) {
            command.set("a", "a");
            command.get("a");
            command.del("a");
        }
        try (RedisPipelineCommandExecutor command = new RedisPipelineCommandExecutor(redisHost, redisPort)) {
            command.pipelineExecute();
        }

        try (RedisTransactionCommandExecutor command = new RedisTransactionCommandExecutor(redisHost, redisPort)) {
            command.multiExecute();
            command.multiDiscard();
        }
        try (RedisStreamCommandExecutor executor = new RedisStreamCommandExecutor(redisHost, redisPort)) {
            executor.exec();
        }
        try (ShardingExecutor shardingExecutor = new ShardingExecutor(
            Arrays.asList(new HostAndPort(redisHost, redisPort), new HostAndPort(redisHost, redisPort)))) {
            shardingExecutor.exec();
        }
        try (ClusterExecutor clusterExecutor = new ClusterExecutor(new HashSet<>(
            Arrays.asList(
                new HostAndPort(redisClusterM1Host, redisClusterM1Port),
                new HostAndPort(redisClusterM2Host, redisClusterM2Port),
                new HostAndPort(redisClusterM3Host, redisClusterM3Port)
            )))) {
            clusterExecutor.exec();
        }
        return SUCCESS;
    }

    @RequestMapping("/healthCheck")
    @ResponseBody
    public String healthCheck() throws Exception {
        return SUCCESS;
    }
}
