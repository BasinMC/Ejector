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
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.HashSet;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
@ConfigurationProperties("ejector.discord")
public class DiscordConfiguration {

  private boolean enabled;
  private String token;
  private final Set<Channel> channels = new HashSet<>();

  public boolean isEnabled() {
    return this.enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @Nullable
  public String getToken() {
    return this.token;
  }

  public void setToken(@Nullable String token) {
    this.token = token;
  }

  @NonNull
  @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType") // required for configuration
  public Set<Channel> getChannels() {
    return this.channels;
  }

  /**
   * Represents a single channel.
   */
  public static class Channel extends AbstractChannelEntry {

    private long guildId;
    private long channelId;

    public long getGuildId() {
      return this.guildId;
    }

    public void setGuildId(long guildId) {
      this.guildId = guildId;
    }

    public long getChannelId() {
      return this.channelId;
    }

    public void setChannelId(long channelId) {
      this.channelId = channelId;
    }
  }
}
