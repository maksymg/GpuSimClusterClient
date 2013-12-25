package com.mgnyniuk.parallel;

import com.gpusim2.config.GridSimConfig;
import com.gpusim2.config.GridSimOutput;
import com.mgnyniuk.base.Main;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by maksym on 12/14/13.
 */
public class Runner implements Callable<Boolean>, Serializable {

    private final String CONFIG = "config%s.xml";
    private final String OUTPUT = "output%s.xml";
    private int overallProcessesQuantity;
    private int partProcessesQuantity;
    private List<GridSimConfig> gridSimConfigList;
    private int startIndex;

    public Runner() {
    }

    public Runner(int overallProcessesQuantity, int partProcessesQuantity, List<GridSimConfig> gridSimConfigList, int startIndex) {
        this.overallProcessesQuantity = overallProcessesQuantity;
        this.partProcessesQuantity = partProcessesQuantity;
        this.gridSimConfigList = gridSimConfigList;
        this.startIndex = startIndex;
    }

    public Boolean call() throws IOException {
        ConfigurationUtil.deserializeConfigs(gridSimConfigList, startIndex);
        ThreadListener threadListener = new ThreadListener();

        int bigCycle = overallProcessesQuantity / partProcessesQuantity;
        int innerCyclePartProcessesQuantity = partProcessesQuantity;
        if ((overallProcessesQuantity % partProcessesQuantity > 0) && (overallProcessesQuantity % partProcessesQuantity < partProcessesQuantity)) {
            bigCycle = bigCycle + 1;
        }
        for (int j = 0; j < bigCycle; j++) {
            if (overallProcessesQuantity % partProcessesQuantity > 0) {
            if (j == bigCycle - 1) {
                innerCyclePartProcessesQuantity = overallProcessesQuantity % partProcessesQuantity;
            }
            }
            for (int i = startIndex; i < startIndex + innerCyclePartProcessesQuantity; i++) {

                NotifyingThread notifyingThread = new WorkerThread("GpuSimV2.jar", String.format(CONFIG, (i + j * partProcessesQuantity)),
                        String.format(OUTPUT, (i + j * partProcessesQuantity)));
                notifyingThread.addListener(threadListener);
                notifyingThread.start();
            }

            while (threadListener.quantityOfEndedThreads != innerCyclePartProcessesQuantity) {

                System.out.print(threadListener.quantityOfEndedThreads);

                continue;
            }

            threadListener.quantityOfEndedThreads = 0;

            //System.out.println("LLLLLLLL");
        }
        ConfigurationUtil.loadOutputs(startIndex, partProcessesQuantity);
        //return ConfigurationUtil.loadConfigs(startIndex, partProcessesQuantity);
        return true;
    }
}
