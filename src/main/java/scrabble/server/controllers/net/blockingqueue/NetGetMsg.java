package scrabble.server.controllers.net.blockingqueue;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import scrabble.protocols.Pack;
import scrabble.server.controllers.net.NetSendMsg;

import java.net.Socket;
import java.util.Hashtable;
import java.util.concurrent.*;

public class NetGetMsg implements Runnable {
    private Hashtable clientName;
    private boolean flag = true;
    private ThreadFactory threadForSocket;
    private ExecutorService pool;
    public NetGetMsg(BlockingQueue<Pack> fromCenter, Hashtable clientName) {
        this.fromCenter = fromCenter;
        this.clientName = clientName;
        threadForSocket = new ThreadFactoryBuilder()
                .setNameFormat("NetGetMsg-pool-%d").build();
        pool = new ThreadPoolExecutor(10,100,0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024),threadForSocket,new ThreadPoolExecutor.AbortPolicy());
    }

    private final BlockingQueue<Pack> fromCenter;
    @Override
    public void run() {
        while (flag){
            try {
                Pack message = fromCenter.take();
                pool.execute(new NetSendMsg(message,clientName));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void shutdown(){
        flag = false;
    }
}
