package org.emamotor.wildfly.websockethol.ejbs;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Yoshimasa Tanabe
 */
@MessageDriven(activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationType",        propertyValue = "javax.jms.Topic"),
    @ActivationConfigProperty(propertyName = "destinationLookup",      propertyValue = "jms/inforegtopic"),
    @ActivationConfigProperty(propertyName = "subscriptionDurability", propertyValue = "Durable"),
//    @ActivationConfigProperty(propertyName = "clientId",               propertyValue = "jms/inforegtopic"),
    @ActivationConfigProperty(propertyName = "subscriptionName",       propertyValue = "jms/inforegtopic")
})
public class MessageListenerMDBImpl implements MessageListener {

    private static final Logger logger =
            Logger.getLogger(MessageListenerMDBImpl.class.getPackage().getName());

    @EJB
    ClientManageSinglEJB clManager;

    public MessageListenerMDBImpl() {}

    @Override
    public void onMessage(Message message) {

        TextMessage textMessage = (TextMessage) message;
        try {
            String text = textMessage.getText();
            clManager.sendMessage(text);
        } catch (JMSException ex) {
            logger.log(Level.SEVERE, "recieve message failed :", ex);
        }

    }

}
