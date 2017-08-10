package org.rs2server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.rs2server.http.HTTPServer;
import org.rs2server.http.WorldList;
import org.rs2server.ls.LoginServer;
import org.rs2server.ls.LoginServerPipelineFactory;
import org.rs2server.rs2.RS2Server;
import org.rs2server.rs2.WorldModule;
import org.rs2server.rs2.domain.service.api.content.cerberus.CerberusService;
import org.rs2server.rs2.domain.service.impl.content.cerberus.CerberusServiceImpl;
import org.rs2server.rs2.model.World;
import org.rs2server.rs2.model.map.RegionClipping;
import org.rs2server.rs2.plugin.PluginManager;
import org.rs2server.tools.TopVoter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * A class to start both the file and game servers.
 *
 * @author Graham Edgecombe
 */
public class Server {

	private static LoginServer loginServer = new LoginServer();
	private static final ClientBootstrap loginServerBootstrap = new ClientBootstrap();
	private static final ExecutorService networkExecutor = Executors.newCachedThreadPool();
	
	/**
	 * This is the ID of your node (world) on the login server network.
	 */
	public static int worldId = 1;
	
    /**
     * The protocol version.
     */
    public static final int VERSION = 83;//83
    

    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static Injector injector;

    /**
     * The entry point of the application.
     *
     * @param args The command line arguments.
     * @throws IOException
     */
    public static void main(String[] args) {
    	
    	if (worldId == 1) {
    		
    		HTTPServer.start();
    		logger.info("Starting OS-Anarchy Economy - (World "+ worldId +")");
    		//worldId = 1;
    		
    		injector = Guice.createInjector(new WorldModule());

            try {
                new RS2Server().bind((43594)).start();//RS2Server.PORT 43594
            } catch (Exception ex) {
                logger.error("Error starting server...", ex);
                System.exit(1);
            }
            World.getWorld();
    		return;
    	} else {
    		worldId = 2;
    		logger.info("Starting OS-Anarchy - (World "+ worldId +")");
    		injector = Guice.createInjector(new WorldModule());

            try {
                new RS2Server().bind((40000 + worldId)).start();//RS2Server.PORT
            } catch (Exception ex) {
                logger.error("Error starting server...", ex);
                System.exit(1);
            }
            World.getWorld();
    		return;
    		}
    }
    	


    public static Injector getInjector() {
        return injector;
    }
    
	public static LoginServer getLoginServer() {
		return loginServer;
	}

}
