/**
 * @(#)JMSConnectionHandler.java    1.0.0 3:08:13 PM
 *
 * Idega Software hf. Source Code Licence Agreement x
 *
 * This agreement, made this 10th of February 2006 by and between 
 * Idega Software hf., a business formed and operating under laws 
 * of Iceland, having its principal place of business in Reykjavik, 
 * Iceland, hereinafter after referred to as "Manufacturer" and Agura 
 * IT hereinafter referred to as "Licensee".
 * 1.  License Grant: Upon completion of this agreement, the source 
 *     code that may be made available according to the documentation for 
 *     a particular software product (Software) from Manufacturer 
 *     (Source Code) shall be provided to Licensee, provided that 
 *     (1) funds have been received for payment of the License for Software and 
 *     (2) the appropriate License has been purchased as stated in the 
 *     documentation for Software. As used in this License Agreement, 
 *     Licensee shall also mean the individual using or installing 
 *     the source code together with any individual or entity, including 
 *     but not limited to your employer, on whose behalf you are acting 
 *     in using or installing the Source Code. By completing this agreement, 
 *     Licensee agrees to be bound by the terms and conditions of this Source 
 *     Code License Agreement. This Source Code License Agreement shall 
 *     be an extension of the Software License Agreement for the associated 
 *     product. No additional amendment or modification shall be made 
 *     to this Agreement except in writing signed by Licensee and 
 *     Manufacturer. This Agreement is effective indefinitely and once
 *     completed, cannot be terminated. Manufacturer hereby grants to 
 *     Licensee a non-transferable, worldwide license during the term of 
 *     this Agreement to use the Source Code for the associated product 
 *     purchased. In the event the Software License Agreement to the 
 *     associated product is terminated; (1) Licensee's rights to use 
 *     the Source Code are revoked and (2) Licensee shall destroy all 
 *     copies of the Source Code including any Source Code used in 
 *     Licensee's applications.
 * 2.  License Limitations
 *     2.1 Licensee may not resell, rent, lease or distribute the 
 *         Source Code alone, it shall only be distributed as a 
 *         compiled component of an application.
 *     2.2 Licensee shall protect and keep secure all Source Code 
 *         provided by this this Source Code License Agreement. 
 *         All Source Code provided by this Agreement that is used 
 *         with an application that is distributed or accessible outside
 *         Licensee's organization (including use from the Internet), 
 *         must be protected to the extent that it cannot be easily 
 *         extracted or decompiled.
 *     2.3 The Licensee shall not resell, rent, lease or distribute 
 *         the products created from the Source Code in any way that 
 *         would compete with Idega Software.
 *     2.4 Manufacturer's copyright notices may not be removed from 
 *         the Source Code.
 *     2.5 All modifications on the source code by Licencee must 
 *         be submitted to or provided to Manufacturer.
 * 3.  Copyright: Manufacturer's source code is copyrighted and contains 
 *     proprietary information. Licensee shall not distribute or 
 *     reveal the Source Code to anyone other than the software 
 *     developers of Licensee's organization. Licensee may be held 
 *     legally responsible for any infringement of intellectual property 
 *     rights that is caused or encouraged by Licensee's failure to abide 
 *     by the terms of this Agreement. Licensee may make copies of the 
 *     Source Code provided the copyright and trademark notices are 
 *     reproduced in their entirety on the copy. Manufacturer reserves 
 *     all rights not specifically granted to Licensee.
 *
 * 4.  Warranty & Risks: Although efforts have been made to assure that the 
 *     Source Code is correct, reliable, date compliant, and technically 
 *     accurate, the Source Code is licensed to Licensee as is and without 
 *     warranties as to performance of merchantability, fitness for a 
 *     particular purpose or use, or any other warranties whether 
 *     expressed or implied. Licensee's organization and all users 
 *     of the source code assume all risks when using it. The manufacturers, 
 *     distributors and resellers of the Source Code shall not be liable 
 *     for any consequential, incidental, punitive or special damages 
 *     arising out of the use of or inability to use the source code or 
 *     the provision of or failure to provide support services, even if we 
 *     have been advised of the possibility of such damages. In any case, 
 *     the entire liability under any provision of this agreement shall be 
 *     limited to the greater of the amount actually paid by Licensee for the 
 *     Software or 5.00 USD. No returns will be provided for the associated 
 *     License that was purchased to become eligible to receive the Source 
 *     Code after Licensee receives the source code. 
 */
package com.idega.bedework.events;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.jms.InvalidSelectorException;

import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.region.DestinationInterceptor;
import org.apache.activemq.broker.region.virtual.CompositeQueue;
import org.apache.activemq.broker.region.virtual.FilteredDestination;
import org.apache.activemq.broker.region.virtual.VirtualDestination;
import org.apache.activemq.broker.region.virtual.VirtualDestinationInterceptor;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQQueue;

import com.idega.util.StringUtil;


/**
 * Class description goes here.
 * <p>You can report about problems to: 
 * <a href="mailto:martynas@idega.com">Martynas Stakė</a></p>
 * <p>You can expect to find some test cases notice in the end of the file.</p>
 *
 * @version 1.0.0 Sep 6, 2012
 * @author martynasstake
 */
public class JMSConnectionHandler {
	
	private static final Logger LOGGER = Logger.getLogger(JMSConnectionHandler.class.getName());
	
	private static ArrayList<ActiveMQQueue> destinations = new ArrayList<ActiveMQQueue>();
	
	protected ArrayList<ActiveMQQueue> getActiveMQDestinations() {
		return destinations;
	}
	
	/**
	 * <p>Searches for created {@link ActiveMQQueue}s, adds new if not 
	 * exists.</p>
	 * @param name {@link ActiveMQQueue#getQualifiedName()} or <code>null</code>
	 * @param physicalName {@link ActiveMQQueue#getPhysicalName()} or 
	 * <code>null</code>.
	 * @return {@link ActiveMQQueue} or <code>null</code> on failure.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	public ActiveMQQueue getActiveMQQueue(String name, String physicalName) {
		if (StringUtil.isEmpty(name) && StringUtil.isEmpty(physicalName)) {
			LOGGER.info("Nothing is given.");
			return null;
		}
		
		if (destinations == null) {
			LOGGER.log(Level.SEVERE, "This is deadly strange... " +
					"destinations are not initialized");
			return null;
		}
		
		for (ActiveMQQueue destination : destinations) {
			if (!StringUtil.isEmpty(physicalName) 
					&& physicalName.equals(destination.getPhysicalName())) {
				return destination;
			}
			
			if (!StringUtil.isEmpty(name) 
					&& name.equals(destination.getQualifiedName())) {
				return destination;
			}
		}
		
		
		ActiveMQQueue newDestination = new ActiveMQQueue(name);
		newDestination.setPhysicalName(physicalName);
		destinations.add(newDestination);
		
		return newDestination;
	}
	
	private static BrokerService broker = null;

	/**
	 * <p>Starts ActiveMQ {@link BrokerService}. Creates only one instance 
	 * if not created or gives existing one. FIXME Hardcoded values.</p>
	 * @return
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	public boolean startBroker() {
		if (broker != null) {
			LOGGER.log(Level.INFO, "Broker already started!");
			return Boolean.FALSE;
		}
		
		/* Starts ActiveMQ */
		broker = new BrokerService();
		broker.setBrokerName(BedeworkEventsConstants.ACTIVE_MQ_BROKER_NAME);
		broker.setDestinations(
				new ActiveMQDestination[] {
						getActiveMQQueue(
								BedeworkEventsConstants.JMS_QUEUE_CRAWLER_NAME, 
								BedeworkEventsConstants.JMS_QUEUE_CRAWLER_PHYSICAL_NAME), 
						getActiveMQQueue(
								BedeworkEventsConstants.JMS_QUEUE_SYSEVENTS_MONITOR_NAME, 
								BedeworkEventsConstants.JMS_QUEUE_SYSEVENTS_MONITOR_PHYSICAL_NAME), 
						getActiveMQQueue(
								BedeworkEventsConstants.JMS_QUEUE_SYSEVENTS_LOGGER_NAME, 
								BedeworkEventsConstants.JMS_QUEUE_SYSEVENTS_LOGGER_PHYSICAL_NAME),
						getActiveMQQueue(
								BedeworkEventsConstants.JMS_QUEUE_SCHEDULE_IN_NAME,
								BedeworkEventsConstants.JMS_QUEUE_SCHEDULE_IN_PHYSICAL_NAME), 
						getActiveMQQueue(
								BedeworkEventsConstants.JMS_QUEUE_SCHEDULE_OUT_NAME,
								BedeworkEventsConstants.JMS_QUEUE_SCHEDULE_OUT_PHYSICAL_NAME)
						}
				);
		
		try {
			broker.addConnector(
					getTransportConnector(
							BedeworkEventsConstants.ACTIVE_MQ_TRANSPORT_CONNECTOR_NAME,
							BedeworkEventsConstants.ACTIVE_MQ_TRANSPORT_CONNECTOR_URI
							));
		} catch (Exception e2) {
			LOGGER.log(Level.WARNING, 
					"Unable to add connector: " + getTransportConnector(
							BedeworkEventsConstants.ACTIVE_MQ_TRANSPORT_CONNECTOR_NAME,
							BedeworkEventsConstants.ACTIVE_MQ_TRANSPORT_CONNECTOR_URI), 
							e2);
			return Boolean.FALSE;
		}
		
		broker.setDestinationInterceptors(new DestinationInterceptor[] {
				getVirtualDestinationInterceptor(
						getCompositeQueue(
								BedeworkEventsConstants.JMS_QUEUE_SYSEVENTS))
		});
		broker.setUseJmx(Boolean.TRUE);
		broker.setUseShutdownHook(Boolean.TRUE);
		
		try {
			broker.start();
		} catch (Exception e1) {
			LOGGER.log(Level.WARNING, "Unable to start ActiveMQ broker: ", e1);
			return Boolean.FALSE;
		}
		
		return Boolean.TRUE;
	}
	
	private static ArrayList<CompositeQueue> compositeQueues = new ArrayList<CompositeQueue>();
	
	/**
	 * <p>Returns existing {@link CompositeQueue}s or creates new one if does 
	 * not exist.</p>
	 * FIXME Method with hardcoded values
	 * @param name {@link CompositeQueue#getName()}, not <code>null</code>.
	 * @return {@link CompositeQueue} or <code>null</code> if something stupid
	 * is done.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	public CompositeQueue getCompositeQueue(String name) {
		if (StringUtil.isEmpty(name)) {
			LOGGER.log(Level.WARNING, "Name not given.");
			return null;
		}
		
		if (compositeQueues == null) {
			LOGGER.log(Level.SEVERE, "'compositeQueues' field not initialized");
			return null;
		}
		
		for (CompositeQueue cq : compositeQueues) {
			if (name.equals(cq.getName())) {
				return cq;
			}
		}
		
		/* Add bedework.sysevents queue. */
		CompositeQueue bedeworkSysevents = new CompositeQueue();
		bedeworkSysevents.setName(name);
		
		/* Add forwarded bedework.sysevents.logger */
		bedeworkSysevents.setForwardTo(Arrays.asList(
				getFilteredDestination(getActiveMQQueue(
						BedeworkEventsConstants.JMS_QUEUE_SCHEDULE_OUT_NAME,
						BedeworkEventsConstants.JMS_QUEUE_SCHEDULE_OUT_PHYSICAL_NAME),
						"outbox = 'true'"), 
				getFilteredDestination(getActiveMQQueue(
						BedeworkEventsConstants.JMS_QUEUE_SCHEDULE_IN_NAME,
						BedeworkEventsConstants.JMS_QUEUE_SCHEDULE_IN_PHYSICAL_NAME),
						"inbox = 'true' or scheduleEvent = 'true'"),
				getActiveMQQueue(
						BedeworkEventsConstants.JMS_QUEUE_CRAWLER_NAME, 
						BedeworkEventsConstants.JMS_QUEUE_CRAWLER_PHYSICAL_NAME), 
				getActiveMQQueue(
						BedeworkEventsConstants.JMS_QUEUE_SYSEVENTS_MONITOR_NAME, 
						BedeworkEventsConstants.JMS_QUEUE_SYSEVENTS_MONITOR_PHYSICAL_NAME), 
				getActiveMQQueue(
						BedeworkEventsConstants.JMS_QUEUE_SYSEVENTS_LOGGER_NAME, 
						BedeworkEventsConstants.JMS_QUEUE_SYSEVENTS_LOGGER_PHYSICAL_NAME)
				));
		
		compositeQueues.add(bedeworkSysevents);
		return bedeworkSysevents;
	}
	
	private static ArrayList<FilteredDestination> filteredDestinations = new ArrayList<FilteredDestination>();
	
	/**
	 * <p>Searches for existing {@link FilteredDestination}s, if nothing found
	 * creates new one.</p>
	 * @param destination {@link ActiveMQDestination}, not <code>null</code>.
	 * @param selector something like "inbox = 'true' or scheduleEvent = 'true'",
	 * could be <code>null</code>.
	 * @return {@link FilteredDestination} or <code>null</code> if something 
	 * stupid is done.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	public FilteredDestination getFilteredDestination(
			ActiveMQDestination destination, String selector) {
		
		if (destination == null) {
			LOGGER.warning("No destination given.");
			return null;
		}
		
		if (filteredDestinations == null) {
			LOGGER.log(Level.SEVERE, "'filteredDestinations' field not initialized.");
			return null;
		}
		
		for (FilteredDestination fd : filteredDestinations) {
			if (destination.equals(fd.getDestination())) {
				return fd;
			}
		}
		
		/* 
		 * Add FilteredDestination for queue. 
		 */
		FilteredDestination scheduleOutFilteredDestination = new FilteredDestination();
		scheduleOutFilteredDestination.setDestination(destination);
		try {
			scheduleOutFilteredDestination.setSelector(selector);
		} catch (InvalidSelectorException e3) {
			LOGGER.log(Level.WARNING, "Unable to set selector: " + 
					" for FilteredDestination: " + 
					destination.getPhysicalName(), e3);
		}
		
		filteredDestinations.add(scheduleOutFilteredDestination);
		return scheduleOutFilteredDestination;
	}
	
	private static ArrayList<VirtualDestinationInterceptor> virtualDestinationInterceptors = new ArrayList<VirtualDestinationInterceptor>();
	
	/**
	 * <p>Searches first {@link VirtualDestinationInterceptor}, which contains 
	 * {@link org.apache.activemq.broker.region.virtual.CompositeQueue}. If
	 * not found creates new one and adds to {@link java.util.List}.</p>
	 * @param queue {@link org.apache.activemq.broker.region.virtual.CompositeQueue},
	 * not <code>null</code>.
	 * @return {@link VirtualDestinationInterceptor} or <code>null</code>, if 
	 * something stupid is done.
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	public VirtualDestinationInterceptor getVirtualDestinationInterceptor(
			org.apache.activemq.broker.region.virtual.CompositeQueue queue) {
		if (queue == null) {
			LOGGER.log(Level.WARNING, "No queue given.");
			return null;
		}
		
		if (virtualDestinationInterceptors == null) {
			LOGGER.log(Level.SEVERE, "Ok, something really stupid is done: " +
					"'virtualDestinationInterceptors' not initialized");
			return null;
		}
		
		for (VirtualDestinationInterceptor vdi : virtualDestinationInterceptors) {			
			VirtualDestination[] virtualDestinations = vdi.getVirtualDestinations();
			if (virtualDestinations == null) {
				continue;
			}
			
			for (VirtualDestination vd : virtualDestinations) {
				if (queue.equals(vd)) {
					return vdi;
				}
			}
		}
		
		/* Add DestinationInterceptor to broker. */
		VirtualDestinationInterceptor di = new VirtualDestinationInterceptor();
		di.setVirtualDestinations(new VirtualDestination[] {queue});
		virtualDestinationInterceptors.add(di);
		return di;
	}
	
	
	private static ArrayList<TransportConnector> connectors = new ArrayList<TransportConnector>();
	
	/**
	 * <p>Searches for existing {@link TransportConnector} with given name
	 * or {@link java.net.URI}, if {@link TransportConnector} not found, then
	 * creates new one and adds to {@link List}.</p>
	 * @param name {@link TransportConnector#getName()}, not <code>null</code>.
	 * @param connectorURL {@link TransportConnector#getConnectUri()}, not
	 * <code>null</code>.
	 * @return {@link TransportConnector} with given name, or <code>null</code>
	 * if you done something stupid!
	 * @author <a href="mailto:martynas@idega.com">Martynas Stakė</a>
	 */
	protected TransportConnector getTransportConnector(String name, 
			String connectorURL) {
		if (StringUtil.isEmpty(name)) {
			LOGGER.log(Level.WARNING, "No transport connector name given.");
			return null;
		}
		
		if (StringUtil.isEmpty(connectorURL)) {
			LOGGER.log(Level.WARNING, "No transport connector URL given.");
			return null;
		}
		
		if (connectors == null) {
			LOGGER.log(Level.SEVERE, "What was that? 'connectors' field should initialized");
			return null;
		}
		
		for (TransportConnector tp : connectors) {
			if (name.equals(tp.getName())) {
				return tp;
			}
			
			java.net.URI uri = null;
			try {
				uri = new java.net.URI(connectorURL);
			} catch (URISyntaxException e) {
				LOGGER.log(Level.WARNING, "Unable to create URI: " + 
						connectorURL, e);
			}
			
			if (connectorURL.equals(uri)) {
				return tp;
			}
		}
		
		/* Creates TransportConnector for ActiveMQ broker. */
		TransportConnector connector = new TransportConnector();
		connector.setName(name);
		
		try {
			connector.setUri(new java.net.URI(connectorURL));
		} catch (URISyntaxException e2) {
			LOGGER.log(Level.WARNING, "Unable to create connector to: " + 
					connectorURL, e2);
		}
		
		connectors.add(connector);
		return connector;
	}
}
