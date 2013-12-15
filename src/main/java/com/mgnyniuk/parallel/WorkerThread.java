package com.mgnyniuk.parallel;

/**
 * Created by maksym on 12/15/13.
 */
public class WorkerThread extends NotifyingThread {

    private String runningJar;
    private String configFile;
    private String outputFile;

    public WorkerThread(String runningJar, String configFile, String outputFile) {
        this.runningJar = runningJar;
        this.configFile = configFile;
        this.outputFile = outputFile;
    }

    @Override
    public void doRun() {
        try {
            Process process = Runtime.getRuntime().exec("java -jar " + runningJar + " " + configFile + " " + outputFile);
            System.out.println("Process with " + configFile + " is started: " + process + "==== Start time: " + System.currentTimeMillis());
            process.waitFor();
            System.out.println("Exit value of process with " + configFile + ": " + process.exitValue() + "==== Exit time: " + System.currentTimeMillis());
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
