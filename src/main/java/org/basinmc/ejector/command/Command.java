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
package org.basinmc.ejector.command;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.List;
import java.util.Set;
import org.basinmc.ejector.command.error.CommandException;
import org.basinmc.ejector.command.error.CommandParameterException;
import org.basinmc.ejector.communication.Message;

/**
 * Provides a command implementation to which the bot will reply in a certain manner..
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public interface Command {

  /**
   * Executes the command's internal logic.
   *
   * @param context a command context.
   * @param commandName a command name.
   * @param arguments a list of arguments.
   * @throws CommandParameterException when the supplied parameters are outside of their expected
   * bounds.
   * @throws CommandException when the command execution itself fails.
   */
  void execute(@NonNull Context context, @NonNull String commandName,
      @NonNull List<String> arguments) throws CommandException;

  /**
   * Retrieves a list of command names which this command is accessible.
   *
   * @return one or more names.
   */
  @NonNull
  Set<String> getNames();

  /**
   * Provides contextual information to commands during their invocation.
   */
  interface Context {

    /**
     * Retrieves the name of the user which invoked the command.
     *
     * @return a name.
     */
    @NonNull
    String getUserName();

    /**
     * Retrieves a formatted user reference which may be prepended to a response in order to ping
     * the user which invoked the command.
     *
     * @return a reference.
     */
    @NonNull
    String getUserReference();

    /**
     * Sends a message to the source communication adapter.
     *
     * @param message a message.
     */
    void sendMessage(@NonNull Message message);
  }
}
