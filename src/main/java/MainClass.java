import com.gpusim2.config.GridSimConfig;
import com.gpusim2.config.GridSimOutput;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;
import com.mgnyniuk.parallel.ConfigurationUtil;
import com.mgnyniuk.parallel.Runner;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by maksym on 12/14/13.
 */
public class MainClass {

    public static void main(String[] args) throws ExecutionException, InterruptedException, FileNotFoundException, UnsupportedEncodingException {

        List<GridSimConfig> configs = ConfigurationUtil.createGridSimConfigs();

        Config config = new Config();

        config.setProperty("hazelcast.icmp.timeout", "2000000000");
        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        IExecutorService executorService = hz.getExecutorService("default");

        Set<HazelcastInstance> hazelcastInstanceSet = Hazelcast.getAllHazelcastInstances();

        Set<Member> memberSet = new HashSet<Member>();
        for(HazelcastInstance hazelcastInstance : hazelcastInstanceSet) {
            memberSet = hazelcastInstance.getCluster().getMembers();
        }

        List<Future<List<GridSimOutput>>> futuresList = new ArrayList<Future<List<GridSimOutput>>>();

        int memberSetSize = memberSet.size();
        int overallProcessesQuantity = 256;
        int partProcessQuantity = overallProcessesQuantity/memberSetSize;
        int toIndex = overallProcessesQuantity/memberSetSize;
        int fromIndex = 0;
        int i = 0;
        PrintWriter writer = new PrintWriter("ExecutionTime.txt", "UTF-8");
        long startTime =  System.currentTimeMillis();
        for(Member member : memberSet) {
            List<GridSimConfig> partConfigs = new ArrayList<GridSimConfig>(configs.subList(fromIndex, toIndex));
            Future<List<GridSimOutput>> future = executorService.submitToMember(new Runner(partProcessQuantity, 1, partConfigs, fromIndex), member);
            futuresList.add(future);
            i++;
            fromIndex = fromIndex + toIndex;
            toIndex = toIndex + toIndex;
        }

        List<GridSimOutput> gridSimOutputList = new ArrayList<GridSimOutput>();

        for (Future<List<GridSimOutput>> future : futuresList) {
            // from cluster instance
            List<GridSimOutput> outputs = future.get();
            gridSimOutputList.addAll(outputs);
        }

        ConfigurationUtil.deserializeOutputs(gridSimOutputList);

        long endTime =  System.currentTimeMillis();
        writer.println("ExecutionTime: " + (endTime - startTime));
        writer.close();
    }
}
