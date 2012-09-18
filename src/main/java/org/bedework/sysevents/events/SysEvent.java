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
package org.bedework.sysevents.events;

import org.bedework.sysevents.NotificationException;

import edu.rpi.cmt.access.AccessPrincipal;

import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.property.LastModified;

/**
 * A system event - like adding something, updating something, startup, shutdown
 * etc.
 * <p>
 * The Notifications interface uses these to carry information about system
 * events. Listeners can be registered for particular system event types.
 * <p>
 * sub-classes should define the compareTo() and hashCode methods. They should
 * also define fields and methods appropriate to the type of event.
 * <p>
 * For example, the ENTITY_UPDATE event should contain enough information to
 * identify the entity, e.g. the path for a calendar or a uid for the event. It
 * is probably NOT a good idea to have a reference to the actual entity.
 * <p>
 * Some of these events may be persisted to ensure their survival across system
 * restarts and their generation is considered part of the operation.
 * <p>
 * Note that we do not modify system events once they are persisted. We retrieve
 * them and delete them from the database.
 * 
 * @author Mike Douglass
 */
public class SysEvent implements SysEventBase, Comparable<SysEvent> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1725170962897624132L;

	private SysCode sysCode;

	private SysEvent related;

	private boolean indexable;

	/** UTC datetime */
	private String dtstamp;

	/**
	 * Ensure uniqueness - dtstamp only down to second.
	 */
	private int sequence;

	/**
	 * Constructor \
	 * 
	 * @param sysCode
	 */
	public SysEvent(final SysCode sysCode) {
		this.sysCode = sysCode;

		indexable = sysCode.getIndexable();
		updateDtstamp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.bedework.sysevents.events.SysEventBase#getSysCode()
	 */
	public SysCode getSysCode() {
		return sysCode;
	}

	/**
	 * @param val
	 */
	public void setDtstamp(final String val) {
		dtstamp = val;
	}

	/**
	 * @return String dtstamp
	 */
	public String getDtstamp() {
		return dtstamp;
	}

	/**
	 * Set the sequence
	 * 
	 * @param val
	 *            sequence number
	 */
	public void setSequence(final int val) {
		sequence = val;
	}

	/**
	 * Get the sequence
	 * 
	 * @return int the sequence
	 */
	public int getSequence() {
		return sequence;
	}

	/**
	 * @return true if this indicates an indexable change may have occurred
	 */
	public boolean getIndexable() {
		return indexable;
	}

	/**
	 * This allows for linking together related events. For example a calendar
	 * change event might be triggered by an event being added.
	 * 
	 * @param val
	 */
	public void setRelated(final SysEvent val) {
		related = val;
	}

	/**
	 * @return SysEvent
	 */
	public SysEvent getRelated() {
		return related;
	}

	/*
	 * ====================================================================
	 * Factory methods
	 * ====================================================================
	 */

	/**
	 * @param code
	 * @param val
	 * @return SysEvent
	 * @throws NotificationException
	 */
	public static SysEvent makePrincipalEvent(final SysCode code,
			final AccessPrincipal val) throws NotificationException {
		SysEvent sysev = new PrincipalEvent(code, val.getPrincipalRef());

		return sysev;
	}

	/**
	 * @param code
	 * @param path
	 * @return SysEvent
	 * @throws NotificationException
	 */
	public static SysEvent makeCollectionChangeEvent(final SysCode code,
			final String path) throws NotificationException {
		SysEvent sysev = new CollectionChangeEvent(code, path);

		return sysev;
	}

	/**
	 * @param code
	 * @param publick
	 * @param ownerHref
	 * @param path
	 * @return SysEvent
	 * @throws NotificationException
	 */
	public static SysEvent makeCollectionDeletionEvent(final SysCode code,
			final boolean publick, final String ownerHref, final String path)
			throws NotificationException {
		SysEvent sysev = new CollectionDeletionEvent(code, publick, ownerHref,
				path);

		return sysev;
	}

	/**
	 * @param code
	 * @param name
	 * @param oldColPath
	 *            old parent
	 * @param newColPath
	 *            new parent
	 * @return SysEvent
	 * @throws NotificationException
	 */
	public static SysEvent makeCollectionMoveEvent(final SysCode code,
			final String name, final String oldColPath, final String newColPath)
			throws NotificationException {
		SysEvent sysev = new CollectionMoveEvent(code, name, oldColPath,
				newColPath);

		return sysev;
	}

	/**
	 * @param code
	 * @param publick
	 * @param ownerHref
	 * @param name
	 * @param uid
	 * @param rid
	 * @param path
	 * @return SysEvent
	 * @throws NotificationException
	 */
	public static SysEvent makeEntityDeletionEvent(final SysCode code,
			final boolean publick, final String ownerHref, final String name,
			final String uid, final String rid, final String path)
			throws NotificationException {
		SysEvent sysev = new EntityDeletionEvent(code, publick, ownerHref,
				name, uid, rid, path);

		return sysev;
	}

	/**
	 * @param code
	 * @param publick
	 * @param ownerHref
	 * @param name
	 * @param uid
	 * @param rid
	 * @param path
	 * @return SysEvent
	 * @throws NotificationException
	 */
	public static SysEvent makeEntityChangeEvent(final SysCode code,
			final boolean publick, final String ownerHref, final String name,
			final String uid, final String rid, final String path)
			throws NotificationException {
		SysEvent sysev = new EntityChangeEvent(code, publick, ownerHref, name,
				uid, rid, path);

		return sysev;
	}

	/**
	 * @param name
	 * @param strValue
	 * @return SysEvent
	 * @throws NotificationException
	 */
	public static SysEvent makeStatsEvent(final String name,
			final String strValue) throws NotificationException {
		SysEvent sysev = new StatsEvent(name, strValue);

		return sysev;
	}

	/**
	 * @param name
	 * @param longValue
	 * @return SysEvent
	 * @throws NotificationException
	 */
	public static SysEvent makeStatsEvent(final String name,
			final Long longValue) throws NotificationException {
		SysEvent sysev = new StatsEvent(name, longValue);

		return sysev;
	}

	/**
	 * @param code
	 * @param name
	 * @param oldPath
	 * @param newPath
	 * @return SysEvent
	 * @throws NotificationException
	 */
	public static SysEvent makeEntityMoveEvent(final SysCode code,
			final String name, final String oldPath, final String newPath)
			throws NotificationException {
		SysEvent sysev = new EntityMoveEvent(code, name, oldPath, newPath);

		return sysev;
	}

	/**
	 * @param code
	 * @param ownerHref
	 * @param name
	 * @param rid
	 * @param inBox
	 * @return SysEvent
	 */
	public static SysEvent makeEntityQueuedEvent(final SysCode code,
			final String ownerHref, final String name, final String rid,
			final boolean inBox) {
		SysEvent sysev = new EntityQueuedEvent(code, ownerHref, name, rid,
				inBox);

		return sysev;
	}

	/**
	 * Update last mod fields
	 */
	private void updateDtstamp() {
		setDtstamp(new LastModified(new DateTime(true)).getValue());
		setSequence(getSequence() + 1);
	}

	/*
	 * ====================================================================
	 * Object methods
	 * ====================================================================
	 */

	public int compareTo(final SysEvent val) {
		return (sysCode.compareTo(val.sysCode));
	}

	@Override
	public int hashCode() {
		return sysCode.hashCode();
	}

	/**
	 * Add our stuff to the StringBuilder
	 * 
	 * @param sb
	 *            StringBuilder for result
	 */
	public void toStringSegment(final StringBuilder sb) {
		sb.append("sysCode=");
		sb.append(getSysCode());
		sb.append(", dtstamp=");
		sb.append(getDtstamp());
		sb.append(", sequence=");
		sb.append(getSequence());
		sb.append(", indexable=");
		sb.append(getIndexable());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("SysEvent{");

		toStringSegment(sb);

		sb.append("}");

		return sb.toString();
	}
}
