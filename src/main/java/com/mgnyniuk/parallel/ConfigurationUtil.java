package com.mgnyniuk.parallel;

import com.gpusim2.config.*;
import com.mgnyniuk.base.Main;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by maksym on 12/14/13.
 */
public class ConfigurationUtil {

    private static double loadOperationCost = 0.0001800;
    private static double saveOperationCost = 0.0009360;

    static List<Integer> blockSizeList = new ArrayList<Integer>();
    static List<Integer> matrixSizeList = new ArrayList<Integer>();

    static GridSimResourceConfig gridSimResourceConfig;

    private static void initGridSimResourceConfig() {
        for (int i = 1; i <= 4096 / 16; i++) {
            matrixSizeList.add(16 * i);
            blockSizeList.add(16);
        }

        gridSimResourceConfig = new GridSimResourceConfig();
        gridSimResourceConfig.setArch("gpusim.MatrixMultiply-ExperimentPlugin.Arch");
        gridSimResourceConfig.setOs("gpusim.MatrixMultiply-ExperimentPlugin.OS");
        gridSimResourceConfig.setCostPerSec(1);
        gridSimResourceConfig.setTimeZone(0);
        gridSimResourceConfig.setAllocPolicy(0);
        gridSimResourceConfig.setBaudRate(10000000000.0);
        gridSimResourceConfig.setCount(1);
        gridSimResourceConfig.setMachines(new LinkedList<GridSimMachineConfig>());

        // First Machine
        GridSimMachineConfig gridSimMachineConfig1 = new GridSimMachineConfig();
        gridSimMachineConfig1.setPeCount(384);
        gridSimMachineConfig1.setPeRating(10000);
        gridSimMachineConfig1.setCount(1);

        // Second Machine
        GridSimMachineConfig gridSimMachineConfig2 = new GridSimMachineConfig();
        gridSimMachineConfig2.setPeCount(8);
        gridSimMachineConfig2.setPeRating(1000);
        gridSimMachineConfig2.setCount(1);

        gridSimResourceConfig.getMachines().add(gridSimMachineConfig1);
        gridSimResourceConfig.getMachines().add(gridSimMachineConfig2);
    }

    public static List<GridSimConfig> createGridSimConfigs() {
        initGridSimResourceConfig();

        List<GridSimConfig> gridSimConfigList = new ArrayList<GridSimConfig>();

        for (int i = 0; i < matrixSizeList.size(); i++) {
            GridSimConfig gridSimConfig = new GridSimConfig();
            gridSimConfig.setVersion(1);
            gridSimConfig.setLinkBaudRate(10000000000.0);
            gridSimConfig.setResources(new LinkedList<GridSimResourceConfig>());
            gridSimConfig.getResources().add(gridSimResourceConfig);
            gridSimConfig.setGridlets(new LinkedList<GridSimGridletConfig>());

            GridSimGridletConfig gridSimGridletConfig = new GridSimGridletConfig();
            double length = blockSizeList.get(i) * Math.pow(matrixSizeList.get(i), 2) * saveOperationCost +
                    2 * Math.pow(matrixSizeList.get(i), 3) * loadOperationCost;
            long inputSize = 3 * blockSizeList.get(i);
            long outputSize = blockSizeList.get(i);
            int count = matrixSizeList.get(i) / blockSizeList.get(i);

            gridSimGridletConfig.setLength(length);
            gridSimGridletConfig.setInputSize(inputSize);
            gridSimGridletConfig.setOutputSize(outputSize);
            gridSimGridletConfig.setCount(count);

            gridSimConfig.getGridlets().add(gridSimGridletConfig);

            gridSimConfigList.add(gridSimConfig);
        }

        return gridSimConfigList;
    }

    public static void deserializeConfigs(List<GridSimConfig> gridSimConfigList, int startIndex) throws FileNotFoundException {
        int i = startIndex;
        for (GridSimConfig gridSimConfig : gridSimConfigList) {
            FileOutputStream out = new FileOutputStream("config" + i + ".xml");
            XMLEncoder xmlEncoder = new XMLEncoder(out);
            xmlEncoder.writeObject(gridSimConfig);
            xmlEncoder.flush();
            xmlEncoder.close();

            i++;

        }
    }

    public static List<GridSimOutput> loadOutputs(int startIndex, int partProcessesQuantity) throws FileNotFoundException, IncompatibleVersionException {

        GridSimOutput gridSimOutput;
        List<GridSimOutput> gridSimOutputList = new ArrayList<GridSimOutput>();

        for (int i = startIndex; i < startIndex + partProcessesQuantity; i++) {
            FileInputStream in = new FileInputStream("output" + i + ".xml");
            XMLDecoder xmlDecoder = new XMLDecoder(in);
            gridSimOutput = (GridSimOutput) xmlDecoder.readObject();
            xmlDecoder.close();
            Main.outputMap.put(i, gridSimOutput);
            gridSimOutputList.add(gridSimOutput);
        }

        return gridSimOutputList;
    }

    public static void deserializeOutputs(ConcurrentMap<Integer, GridSimOutput> gridSimOutputMap) throws FileNotFoundException {
        int i = 0;
        for (Map.Entry<Integer, GridSimOutput> gridSimOutputMapEntry : gridSimOutputMap.entrySet()) {
            FileOutputStream out = new FileOutputStream("output" + gridSimOutputMapEntry.getKey() + ".xml");
            XMLEncoder xmlEncoder = new XMLEncoder(out);
            xmlEncoder.writeObject(gridSimOutputMapEntry.getValue());
            xmlEncoder.flush();
            xmlEncoder.close();

            i++;

        }
    }
}
