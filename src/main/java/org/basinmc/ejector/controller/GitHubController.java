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
package org.basinmc.ejector.controller;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.io.IOException;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.basinmc.ejector.communication.CommunicationManager;
import org.basinmc.stormdrain.Payload;
import org.basinmc.stormdrain.PayloadType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Provides an endpoint to which GitHub may submit organization or repository events which are
 * forwarded directly to all available communication adapters.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
@RestController
@RequestMapping(path = "/hook/github", method = RequestMethod.POST, headers = {
    "X-Github-Delivery",
    "X-GitHub-Event"
})
public class GitHubController {

  private static final Logger logger = LogManager.getFormatterLogger(GitHubController.class);

  private final CommunicationManager communicationManager;

  @Autowired
  public GitHubController(@NonNull CommunicationManager communicationManager) {
    this.communicationManager = communicationManager;
  }

  /**
   * Handles a single GitHub event.
   *
   * @param deliveryId a delivery identifier.
   * @param type an event type.
   * @param encodedPayload a payload.
   * @throws IOException when decoding the payload fails.
   */
  @RequestMapping
  public void handle(
      @NonNull @RequestHeader("X-Github-Delivery") UUID deliveryId,
      @NonNull @RequestHeader("X-GitHub-Event") String type,
      @NonNull @RequestBody String encodedPayload) throws IOException {
    PayloadType payloadType;

    try {
      payloadType = PayloadType.valueOf(type.toUpperCase());
    } catch (IllegalArgumentException ex) {
      logger.warn("Received unsupported payload of type \"" + type + "\"");
      return;
    }

    Payload<?> payload = new Payload<>(deliveryId, payloadType,
        payloadType.read(encodedPayload));
    this.communicationManager.handlePayload(payload);
  }
}
