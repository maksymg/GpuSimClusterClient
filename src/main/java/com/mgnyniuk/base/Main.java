package com.mgnyniuk.base;

import com.gpusim2.config.GridSimConfig;
import com.gpusim2.config.GridSimOutput;
import com.hazelcast.config.Config;
import com.hazelcast.core.*;
import com.mgnyniuk.parallel.ConfigurationUtil;
import com.mgnyniuk.parallel.Runner;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by maksym on 12/14/13.
 */
public class Main {

    public static ConcurrentMap<Integer, GridSimOutput> outputMap;

    public static void main(String[] args) throws ExecutionException, InterruptedException, FileNotFoundException, UnsupportedEncodingException {

        List<GridSimConfig> configs = ConfigurationUtil.createGridSimConfigs();

        Config config = new Config();

        config.setProperty("hazelcast.icmp.timeout", "2000000000");
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        IExecutorService executorService = hz.getExecutorService("default");
        outputMap = hz.getMap("outputMap");

        Set<HazelcastInstance> hazelcastInstanceSet = Hazelcast.getAllHazelcastInstances();

        Set<Member> memberSet = new HashSet<Member>();
        for(HazelcastInstance hazelcastInstance : hazelcastInstanceSet) {
            memberSet = hazelcastInstance.getCluster().getMembers();
        }

        List<Future<Boolean>> futuresList = new ArrayList<Future<Boolean>>();

        int memberSetSize = memberSet.size();
        int overallProcessesQuantity = 100;
        int partProcessQuantity = overallProcessesQuantity/memberSetSize;
        int toIndex = overallProcessesQuantity/memberSetSize;
        int fromIndex = 0;
        int i = 0;
        PrintWriter writer = new PrintWriter("ExecutionTime.txt", "UTF-8");
        long startTime =  System.currentTimeMillis();
        for(Member member : memberSet) {
            List<GridSimConfig> partConfigs = new ArrayList<GridSimConfig>(configs.subList(fromIndex, toIndex));
            Future<Boolean> future = executorService.submitToMember(new Runner(partProcessQuantity, partProcessQuantity, partConfigs, fromIndex), member);
            futuresList.add(future);
            i++;
            fromIndex = fromIndex + toIndex;
            toIndex = toIndex + toIndex;
        }

        //List<GridSimOutput> gridSimOutputList = new ArrayList<GridSimOutput>();

        for (Future<Boolean> future : futuresList) {
            // from cluster instance
            //List<GridSimOutput> outputs = future.get();
            //gridSimOutputList.addAll(outputs);
            System.out.println(future.get());
        }
        //outputMapp = hz.getMap("outputMap");


        ConfigurationUtil.deserializeOutputs(outputMap);

        long endTime =  System.currentTimeMillis();
        writer.println("ExecutionTime: " + (endTime - startTime));
        writer.println("outputMap: " + outputMap.size());
        writer.close();
    }
}
