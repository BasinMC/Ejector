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
package org.basinmc.ejector.command.error;

/**
 * Provides an exception for error cases where the supplied command parameters are invalid.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class CommandParameterException extends CommandException {

  public CommandParameterException() {
  }

  public CommandParameterException(String message) {
    super(message);
  }

  public CommandParameterException(String message, Throwable cause) {
    super(message, cause);
  }

  public CommandParameterException(Throwable cause) {
    super(cause);
  }
}
