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
package org.basinmc.ejector.communication.irc;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.pircbotx.Colors;
import org.springframework.core.env.PropertySource;

/**
 * Provides IRC color codes as resolvable properties.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class IrcColorPropertySource extends PropertySource<String> {

  public IrcColorPropertySource() {
    super("color");
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getProperty(@NonNull String name) {
    if (name.startsWith("color.")) {
      name = name.substring(6);
      return Colors.lookup(name.toUpperCase());
    }

    return null;
  }
}
