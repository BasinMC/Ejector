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
package org.basinmc.ejector.communication;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.basinmc.stormdrain.Payload;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Provides a method of notifying all communication adapters within the application of events.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
@Component
public class CommunicationManager {

  private final Set<CommunicationAdapter> adapters;

  @Autowired
  public CommunicationManager(@NonNull Collection<? extends CommunicationAdapter> adapters) {
    this.adapters = new HashSet<>(adapters);
  }

  /**
   * Sends a GitHub event payload to all communication adapters within the application.
   *
   * @param payload a payload.
   */
  public void handlePayload(@NonNull Payload<?> payload) {
    this.adapters.forEach((a) -> a.handlePayload(payload));
  }

  /**
   * Sends a message to all communication adapters within the application.
   *
   * @param message a message.
   */
  public void sendMessage(@NonNull Message message) {
    this.adapters.forEach((a) -> a.sendMessage(message));
  }
}
