import com.gpusim2.config.GridSimConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IExecutorService;
import com.hazelcast.core.Member;

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

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        List<GridSimConfig> configs = ConfigurationUtil.createGridSimConfigs ();

        Config config = new Config();

        HazelcastInstance hz = Hazelcast.newHazelcastInstance(config);
        IExecutorService executorService = hz.getExecutorService("default");

        Set<HazelcastInstance> hazelcastInstanceSet = Hazelcast.getAllHazelcastInstances();

        Set<Member> memberSet = new HashSet<Member>();
        for(HazelcastInstance hazelcastInstance : hazelcastInstanceSet) {
            memberSet = hazelcastInstance.getCluster().getMembers();
        }

        List<Future<Boolean>> futuresList = new ArrayList<Future<Boolean>>();

        int i = 0;
        for(Member member : memberSet) {
            Future<Boolean> future = executorService.submitToMember(new Configuration(configs), member);
            futuresList.add(future);
            i++;
        }

        for (Future<Boolean> future : futuresList) {
            Boolean echoResult = future.get();
            System.out.println(echoResult);
            // ...
        }
    }

}
