/*
 * Copyright 2018 Johannes Donath <johannesd@torchmind.com>
 * and other copyright owners as documented in the project's IP log.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.basinmc.ejector.configuration;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.EnumSet;
import java.util.Set;
import org.basinmc.stormdrain.PayloadType;

/**
 * Provides a base for channel entries within adapter configurations which permits the customization
 * of pushed events.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public abstract class AbstractChannelEntry {

  private final Set<PayloadType> events = EnumSet.noneOf(PayloadType.class);

  /**
   * Retrieves the events which are to be redirected to this channel.
   *
   * @return a set of permitted event types.
   */
  @NonNull
  @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
  public Set<PayloadType> getEvents() {
    return this.events;
  }

  /**
   * Evaluates whether a given payload type should be received by this channel.
   *
   * @param type a payload type.
   * @return true if receiving is enabled, false otherwise.
   */
  public boolean isReceivingEvent(@NonNull PayloadType type) {
    return this.events.isEmpty() || this.events.contains(type);
  }
}
