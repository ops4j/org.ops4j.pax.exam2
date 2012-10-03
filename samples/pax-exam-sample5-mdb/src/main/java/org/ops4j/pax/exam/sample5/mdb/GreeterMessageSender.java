/*
 * Copyright 2012 Harald Wellmann
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.ops4j.pax.exam.sample5.mdb;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

@Singleton
@Startup
public class GreeterMessageSender {
    
    @Resource(mappedName= "jms/cf")
    private ConnectionFactory cf;
    
    @Resource(mappedName = "jms/greeter")
    private Queue queue;
    
    @PostConstruct
    public void init() {
        try {
            Connection connection = cf.createConnection();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(queue);
            for (int i = 1; i <= 100; i++) {
                TextMessage msg = session.createTextMessage(String.format("Hello JMS! [%d]", i));
                producer.send(msg);
            }
            producer.close();
            session.close();
            connection.close();
        }
        catch (JMSException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

}
