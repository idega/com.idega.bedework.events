/* ********************************************************************
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:
        
    http://www.apache.org/licenses/LICENSE-2.0
        
    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
 */
package org.bedework.sysevents;

import java.io.InputStream;
import java.util.Properties;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.log4j.Logger;

import com.idega.bedework.events.JMSConnectionHandler;
import com.idega.util.CoreConstants;
import com.idega.util.IOUtil;

/**
 * This is a class to ease setting up of JMS connections..
 * 
 * @author Mike Douglass douglm - rpi.edu
 */
public class JmsConnectionHandler implements JmsDefs {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1459821310199535793L;

	private boolean debug;

	private static volatile Properties pr;

	private static volatile Object lockit = new Object();

	private transient Logger log;

	// private boolean debug;

	private Connection connection;

	private Queue ourQueue;

	private Session session;

	private MessageProducer sender;

	private MessageConsumer consumer;

	/**
   */
	public JmsConnectionHandler() {
		debug = getLogger().isDebugEnabled();
	}

	/**
	 * Open a connection to the named queue ready to create a producer or
	 * consumer.
	 * 
	 * @param queueName
	 * @throws NotificationException
	 */
	public void open(final String queueName) throws NotificationException {
		try {

			JMSConnectionHandler jmsch = new JMSConnectionHandler();
			jmsch.startBroker();

			ConnectionFactory connFactory;

			Properties pr = getPr();
			Context ctx = new InitialContext(pr);

			try {
				warn("##############################################################");
				String connectionFactProp = pr
						.getProperty("org.bedework.connection.factory.name");
				Object connectionFactory = ctx.lookup(connectionFactProp);
				if (connectionFactory instanceof javax.jms.ConnectionFactory)
					connection = ((javax.jms.ConnectionFactory) connectionFactory)
							.createConnection();
				else if (connectionFactory instanceof ActiveMQConnectionFactory)
					connection = ((ActiveMQConnectionFactory) connectionFactory)
							.createConnection();
				else {
					ActiveMQConnectionFactory factory = new org.apache.activemq.ActiveMQConnectionFactory();
					info("Created factory manually: " + factory + ". It is initialized: " + (factory != null));
					connection = factory.createConnection();
					info("Created connection manually: " + connection + ". It is initialized: " + (connection != null));
					
					warn("Expected connection type of javax.jms.ConnectionFactory by property '"
							+ connectionFactProp
							+ "', got: "
							+ connectionFactory
							+ (connectionFactory == null ? " (property not set?)"
									: " " + connectionFactory.getClass()));
					if (connectionFactory != null) {
						Class<?>[] interfaces = connectionFactory.getClass()
								.getInterfaces();
						if (interfaces == null)
							warn("There are no interfaces");
						else {
							for (Class<?> inter : interfaces)
								info("Interface " + inter.getName());
						}
						Class<?> theClass = connectionFactory.getClass();
						while (theClass != null
								&& theClass.getName().equals(
										Object.class.getName())) {
							theClass = theClass.getSuperclass();
							info("Superclass: " + theClass.getName());
						}
					}
				}

				warn("##############################################################");
			} catch (Throwable t) {
				if (debug) {
					error(t);
				}
				throw new NotificationException(t);
			}

			try {
				/*
				 * Session is not transacted, uses AUTO_ACKNOWLEDGE for message
				 * acknowledgement
				 */
				session = connection.createSession(useTransactions, ackMode);

				ourQueue = jmsch.getActiveMQQueue(queueName, "bedework."
						+ queueName);
				// try {
				// ourQueue = (Queue)new
				// InitialContext().lookup(pr.getProperty("org.bedework.jms.queue.prefix")
				// +
				// queueName);
				// } catch (NamingException ne) {
				// // Try again with our own properties
				// ourQueue =
				// (Queue)ctx.lookup(pr.getProperty("org.bedework.jms.queue.prefix")
				// +
				// queueName);
				// // org.apache.activemq.advisory.DestinationSource
				// destinationSource =
				// //
				// ((org.apache.activemq.ActiveMQConnection)connection).getDestinationSource();
				// }
			} catch (Throwable t) {
				if (debug) {
					error(t);
				}

				if (ourQueue == null) {
					throw new NotificationException(t);
				} else {
					getLogger()
							.warn("New queue is created with name: "
									+ ourQueue.getQueueName()
									+ " but this means you should check your jndi configuration.");
				}
			}
		} catch (NotificationException ne) {
			throw ne;
		} catch (Throwable t) {
			if (debug) {
				error(t);
			}
			throw new NotificationException(t);
		}
	}

	/**
   *
   */
	public void close() {
		try {
			if (session != null) {
				session.close();
			}
		} catch (Throwable t) {
			warn(t.getMessage());
		}
	}

	/**
	 * @return jms session
	 */
	public Session getSession() {
		return session;
	}

	/**
	 * @return a message producer
	 * @throws NotificationException
	 */
	public MessageProducer getProducer() throws NotificationException {
		try {
			sender = session.createProducer(ourQueue);

			connection.start();

			return sender;
		} catch (JMSException je) {
			throw new NotificationException(je);
		}
	}

	/**
	 * @return a message consumer
	 * @throws NotificationException
	 */
	public MessageConsumer getConsumer() throws NotificationException {
		try {
			consumer = session.createConsumer(ourQueue);

			connection.start();

			return consumer;
		} catch (JMSException je) {
			throw new NotificationException(je);
		}
	}

	/**
	 * @return next message
	 * @throws NotificationException
	 */
	public Message receive() throws NotificationException {
		try {
			return consumer.receive();
		} catch (JMSException je) {
			throw new NotificationException(je);
		}
	}

	/*
	 * ====================================================================
	 * Protected methods
	 * ====================================================================
	 */

	protected void info(final String msg) {
		getLogger().info(msg);
	}

	protected void trace(final String msg) {
		getLogger().debug(msg);
	}

	protected void warn(final String msg) {
		getLogger().warn(msg);
	}

	protected void error(final Throwable t) {
		getLogger().error(this, t);
	}

	/*
	 * Get a logger for messages
	 */
	protected Logger getLogger() {
		if (log == null) {
			log = Logger.getLogger(this.getClass());
		}

		return log;
	}

	/*
	 * ====================================================================
	 * Protected methods
	 * ====================================================================
	 */

	private static Properties getPr() throws NotificationException {
		synchronized (lockit) {
			if (pr != null) {
				return pr;
			}

			/** Load properties file */

			pr = new Properties();
			InputStream is = null;

			try {
				is = IOUtil
						.getStreamFromJar(
								BedeworkSyseventsConstants.IW_BUNDLE_IDENTIFIER,
								CoreConstants.PROPERTIES_FOLDER_NAME_IN_JAR
										+ CoreConstants.SLASH
										+ BedeworkSyseventsConstants.FILE_PATH_SYSEVENTS_PROPERTIES);

				if (is == null) {
					throw new NotificationException(
							"Unable to load properties file");
				}

				pr.load(is);

				return pr;
			} catch (NotificationException cee) {
				throw cee;
			} catch (Throwable t) {
				Logger.getLogger(JmsConnectionHandler.class).error(
						"getEnv error", t);
				throw new NotificationException(t.getMessage());
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (Throwable t1) {
					}
				}
			}
		}
	}
}
