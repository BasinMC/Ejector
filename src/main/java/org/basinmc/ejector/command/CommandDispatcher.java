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
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.basinmc.ejector.command.Command.Context;
import org.basinmc.ejector.command.error.CommandException;
import org.basinmc.ejector.command.error.CommandParameterException;
import org.basinmc.ejector.command.error.NoSuchCommandException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Manages the registration and execution of commands.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
@Component
public class CommandDispatcher {

  private static final Logger logger = LogManager.getFormatterLogger(CommandDispatcher.class);

  private final Map<String, Command> commandMap;

  @Autowired
  public CommandDispatcher(@Nullable @Autowired(required = false) Collection<Command> commands) {
    if (commands == null) {
      this.commandMap = Collections.emptyMap();
      logger.warn("No registered commands");
      return;
    }

    this.commandMap = commands.stream()
        .flatMap((c) -> c.getNames().stream().map((n) -> new SimpleImmutableEntry<>(n, c)))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }

  /**
   * Executes an event (if exists) within the specified context.
   *
   * @param context a context.
   * @param name a command name.
   * @param arguments a list of arguments.
   * @throws CommandException when the command execution fails.
   * @throws CommandParameterException when the supplied command arguments are invalid.
   * @throws NoSuchCommandException when no command with the specified name has been defined.
   */
  public void dispatch(@NonNull Context context, @NonNull String name,
      @NonNull List<String> arguments)
      throws CommandException {
    Command command = this.commandMap.get(name);

    if (command == null) {
      throw new NoSuchCommandException("No command with alias \"" + name + "\" registered");
    }

    command.execute(context, name, arguments);
  }
}
