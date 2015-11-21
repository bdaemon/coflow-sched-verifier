package Simulation.Communicators;

import Simulation.Data.VarysListener;
import varys.framework.CoflowDescription;
import varys.framework.CoflowType;
import varys.framework.client.ClientListener;
import varys.framework.client.VarysClient;

import java.util.concurrent.CountDownLatch;

/**
 * Created by stanislavmushits on 21/11/15.
 */
public class VarysRegistrator{

    private final long size;
    private VarysClient client;
    private String masterUrl;
    private String clientName;
    private int numOfSlaves;
    String coflowId;

    public VarysRegistrator(String url, String name, int numOfSlaves, long size){
        masterUrl = url;
        this.clientName = name;
        this.numOfSlaves = numOfSlaves;
        this.size = size;
    }

    public String registerCoflow() {
        final CountDownLatch latch = new CountDownLatch(1);

        Thread clientThread = new Thread(clientName){
            @Override
            public void run(){
                int deadlineMillis = 10000;

                VarysListener listener = new VarysListener();
                client = new VarysClient(clientName, masterUrl, listener);
                client.start();

                CoflowDescription desc = new CoflowDescription("DEFAULT", CoflowType.DEFAULT(), numOfSlaves, size, deadlineMillis);
                coflowId = client.registerCoflow(desc);

                safePrintln("Registered coflow " + coflowId);
                latch.countDown();
                client.awaitTermination();
            }
        };

        clientThread.start();
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return coflowId;

    }

    public void safePrintln(String s) {
        synchronized (System.out) {
            System.out.println(s);
        }
    }
}
