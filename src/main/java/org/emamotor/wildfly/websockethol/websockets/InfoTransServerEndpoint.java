package org.emamotor.wildfly.websockethol.websockets;

import org.emamotor.wildfly.websockethol.ejbs.ClientManageSinglEJB;

import javax.ejb.EJB;
import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Yoshimasa Tanabe
 */
@ServerEndpoint("/infotrans")
public class InfoTransServerEndpoint {

    private static final Logger logger =
            Logger.getLogger(InfoTransServerEndpoint.class.getPackage().getName());

    @EJB
    ClientManageSinglEJB clManager;

    @OnOpen
    public void initOpen(Session session) {
        logger.log(Level.INFO, "connect: " + session.getId());
        clManager.addClient(session);
    }

    @OnClose
    public void closeWebSocket(Session session) {
        logger.log(Level.INFO, "close: " + session.getId());
        clManager.removeClient(session);
    }

}
