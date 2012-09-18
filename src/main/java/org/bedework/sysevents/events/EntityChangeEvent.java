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

/** Signal a change to an entity
 * @author douglm
 *
 */
public class EntityChangeEvent extends EntityEvent {
  private static final long serialVersionUID = 1L;

  private boolean publick;

  /**
   * @param code
   * @param publick
   * @param ownerHref
   * @param name
   * @param uid
   * @param rid
   * @param colPath path to parent
   */
  public EntityChangeEvent(final SysCode code,
                           final boolean publick,
                           final String ownerHref,
                           final String name,
                           final String uid,
                           final String rid,
                           final String colPath) {
    super(code, ownerHref, name, uid, rid, colPath);

    this.publick = publick;
  }

  /** Get the publick flag
   *
   * @return boolean
   */
  public boolean getPublick() {
    return publick;
  }

  /** Add our stuff to the StringBuilder
   *
   * @param sb    StringBuilder for result
   */
  @Override
  public void toStringSegment(final StringBuilder sb) {
    super.toStringSegment(sb);

    sb.append(", publick=");
    sb.append(getPublick());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("EntityChangeEvent{");

    toStringSegment(sb);


    sb.append("}");

    return sb.toString();
  }
}
