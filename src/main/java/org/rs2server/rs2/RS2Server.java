package org.rs2server.rs2;

import com.rspserver.motivote.Motivote;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.transport.socket.SocketSessionConfig;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
import org.rs2server.Server;
import org.rs2server.rs2.content.api.GameWorldLoadedEvent;
import org.rs2server.rs2.content.misc.VoteRewardHandler;
import org.rs2server.rs2.domain.service.api.EngineService;
import org.rs2server.rs2.domain.service.api.HookService;
import org.rs2server.rs2.model.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;


/**
 * Starts everything else including MINA and the <code>GameEngine</code>.
 * @author Graham Edgecombe
 * 
 */
public class RS2Server {

	/**
	 * The port to listen on.
	 */
	public static final int PORT = 43594;
	
	public static final int WORLD_PORT = 40000;

	/**
	 * Logger instance.
	 */
	private static final Logger logger = LoggerFactory.getLogger(RS2Server.class);

	/**
	 * The <code>IoAcceptor</code> instance.
	 */
	private final IoAcceptor acceptor = new NioSocketAcceptor();

	private final EngineService engineService;

	/**
	 * Creates the server and the <code>GameEngine</code> and initializes the
	 * <code>World</code>.
	 * @throws IOException if an I/O error occurs loading the world.
	 * @throws ClassNotFoundException if a class the world loads was not found.
	 * @throws IllegalAccessException if a class loaded by the world was not accessible.
	 * @throws InstantiationException if a class loaded by the world was not created.
	 */
	public RS2Server() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
		World.getWorld().init();
		this.engineService = Server.getInjector().getInstance(EngineService.class);
		acceptor.setHandler(new ConnectionHandler());
		//acceptor.getFilterChain().addFirst("throttleFilter", new ConnectionThrottleFilter());
	}

	/**
	 * Binds the server to the specified port.
	 * @param port The port to bind to.
	 * @return The server instance, for chaining.
	 * @throws IOException
	 */
	public RS2Server bind(int port) throws IOException {
		logger.info("Binding to port : " + port + "...");
		SocketSessionConfig config = (SocketSessionConfig) acceptor.getSessionConfig();
		config.setReuseAddress(true);
		config.setTcpNoDelay(true);

		while (true) {
			try {
				acceptor.bind(new InetSocketAddress("0.0.0.0", port));
			} catch (Exception e) {
				logger.warn("Failed to bind to port " + port + " + (" + e.getMessage() + "). Retrying...");
				try {
					Thread.sleep(4000);
				} catch (InterruptedException e1) {
				}
				continue;
			}
			break;
		}
		return this;
	}

	/**
	 * Starts the <code>GameEngine</code>.
	 * @throws ExecutionException if an error occured during background loading.
	 */
	public void start() throws ExecutionException {
		ScriptManager.getScriptManager().loadScripts(Constants.SCRIPTS_DIRECTORY);
		if(World.getWorld().getBackgroundLoader().getPendingTaskAmount() > 0) {
			logger.info("Waiting for pending background loading tasks...");
			try {
				World.getWorld().getBackgroundLoader().waitForPendingTasks();
			} catch (ExecutionException e) {
				e.printStackTrace();
				System.exit(1);
			}
		}
		World.getWorld().getBackgroundLoader().shutdown();
		engineService.start();
		// This doesn't fit well here, but due to the way the World is initialised, deal with it.
		Server.getInjector().getInstance(HookService.class).post(new GameWorldLoadedEvent());

		logger.info("Ready");
		//Ready to intiailize

	}

}
