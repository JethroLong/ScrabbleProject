package scrabble.server.controllers.controlcenter;

import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.log4j.Logger;
import scrabble.protocols.GamingProtocol.GamingOperationProtocol;
import scrabble.protocols.NonGamingProtocol.NonGamingProtocol;
import scrabble.protocols.Pack;
import scrabble.protocols.ScrabbleProtocol;
import scrabble.server.controllers.controlcenter.blockingqueue.CenterGetMsg;
import scrabble.server.controllers.controlcenter.blockingqueue.CenterPutMsg;
import scrabble.server.controllers.gameEngine.GameEngine;
import scrabble.server.controllers.net.Net;

import java.util.Scanner;
import java.util.concurrent.*;

public class ControlCenter implements Runnable{
    private String tag = "ControlCenter";
    private static Logger logger = Logger.getLogger(ControlCenter.class);
    private final BlockingQueue<Pack> fromNet;
    private final BlockingQueue<Pack> toEngine;
    private final BlockingQueue<Pack> fromEngine;
    private final BlockingQueue<Pack> toNet;
    private GameEngine gameEngine;
    private int portNumber;
    private boolean flag = true;
    private ThreadFactory threadForSocket;
    private ExecutorService pool;

    public ControlCenter() {
        this.fromNet = new LinkedBlockingQueue<>();
        toEngine = new LinkedBlockingQueue<>();
        fromEngine = new LinkedBlockingQueue<>();
        toNet = new LinkedBlockingQueue<>();
        initialServer();
        logger.info(tag+" Initial ControlCenter Complete!");
    }

    public ControlCenter(int port) {
        this.fromNet = new LinkedBlockingQueue<>();
        toEngine = new LinkedBlockingQueue<>();
        fromEngine = new LinkedBlockingQueue<>();
        toNet = new LinkedBlockingQueue<>();
        portNumber=port;
        initialServer();
        logger.info(tag+" Initial ControlCenter Complete!");
    }

    public void initialServer(){
        threadForSocket = new ThreadFactoryBuilder()
                .setNameFormat("Server-ControlCenter-pool-%d").build();
        pool = new ThreadPoolExecutor(8,100,0L,TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024),threadForSocket,new ThreadPoolExecutor.AbortPolicy());
        if(portNumber==0){
            pool.execute(Net.getInstance(fromNet,toNet));
        }else {
            pool.execute(Net.getInstance(fromNet,toNet,portNumber));
        }
        pool.execute(GameEngine.getInstance(toEngine,fromEngine));
        logger.info(tag+" Initial Server Competed");
    }
    @Override
    public void run() {
        pool.execute(new CenterGetMsg(fromNet,toEngine,fromEngine,toNet));
        pool.execute(new CenterPutMsg(fromNet,toEngine,fromEngine,toNet));
    }



    public void shutdown(){
        flag = false;
    }

}
