package org.emamotor.wildfly.websockethol.cdis;

import javax.annotation.Resource;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.Topic;

/**
 * @author Yoshimasa Tanabe
 */
@Named(value = "indexManage")
@RequestScoped
public class IndexPageMgdBean {

    @Inject
    @JMSConnectionFactory("ConnectionFactory")
    JMSContext context;

    @Resource(mappedName = "jms/inforegtopic")
    Topic topic;

    private String message;

    public IndexPageMgdBean() {}

    public String pushSendButton() {
        context.createProducer().send(topic, getMessage());
        return "";
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
