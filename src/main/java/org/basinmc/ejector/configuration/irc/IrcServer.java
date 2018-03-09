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
package org.basinmc.ejector.configuration.irc;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;

/**
 * Represents an IRC server configuration.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class IrcServer implements Configuration {

  private IrcConfiguration parent;

  private String hostname;
  private int port = 6667;
  private boolean secure;
  private final Set<IrcChannel> channels = new HashSet<>();

  private String channelPrefixes;
  private Charset charset;
  private String ctcpFingerResponseTemplate;
  private String ctcpVersionResponseTemplate;
  private String ident;
  private InetAddress localAddress;
  private long messageDelay = -1;
  private int maximumReconnectAttempts = -1;
  private String nickServNick;
  private String nickServPassword;
  private String name;
  private int reconnectDelay = -1;
  private String realNameTemplate;
  private int socketTimeout = -1;
  private String userPrefixes;
  private Boolean authenticationDelayEnabled;
  private Boolean automaticNicknameChangeEnabled;

  @Value("${ejector.irc}")
  public void setParent(IrcConfiguration parent) {
    this.parent = parent;
  }

  public String getHostname() {
    return this.hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public int getPort() {
    return this.port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public boolean isSecure() {
    return this.secure;
  }

  public void setSecure(boolean secure) {
    this.secure = secure;
  }

  @NonNull
  @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
  public Set<IrcChannel> getChannels() {
    return this.channels;
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public String getChannelPrefixes() {
    if (this.channelPrefixes != null) {
      return this.channelPrefixes;
    }

    return this.parent.getChannelPrefixes();
  }

  public void setChannelPrefixes(String channelPrefixes) {
    this.channelPrefixes = channelPrefixes;
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public Charset getCharset() {
    if (this.charset != null) {
      return this.charset;
    }

    return this.parent.getCharset();
  }

  public void setCharset(Charset charset) {
    this.charset = charset;
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public String getCtcpFingerResponseTemplate() {
    if (this.ctcpFingerResponseTemplate != null) {
      return this.ctcpFingerResponseTemplate;
    }

    return this.parent.getCtcpFingerResponseTemplate();
  }

  public void setCtcpFingerResponseTemplate(String ctcpFingerResponseTemplate) {
    this.ctcpFingerResponseTemplate = ctcpFingerResponseTemplate;
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public String getCtcpVersionResponseTemplate() {
    if (this.ctcpVersionResponseTemplate != null) {
      return this.ctcpVersionResponseTemplate;
    }

    return this.parent.getCtcpVersionResponseTemplate();
  }

  public void setCtcpVersionResponseTemplate(String ctcpVersionResponseTemplate) {
    this.ctcpVersionResponseTemplate = ctcpVersionResponseTemplate;
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public String getIdent() {
    if (this.ident != null) {
      return this.ident;
    }

    return this.parent.getIdent();
  }

  public void setIdent(String ident) {
    this.ident = ident;
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public Optional<InetAddress> getLocalAddress() {
    if (this.localAddress != null) {
      return Optional.of(this.localAddress);
    }

    return this.parent.getLocalAddress();
  }

  public void setLocalAddress(InetAddress localAddress) {
    this.localAddress = localAddress;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getMessageDelay() {
    if (this.messageDelay >= 0) {
      return this.messageDelay;
    }

    return this.parent.getMessageDelay();
  }

  public void setMessageDelay(long messageDelay) {
    this.messageDelay = messageDelay;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getMaximumReconnectAttempts() {
    if (this.maximumReconnectAttempts >= 0) {
      return this.maximumReconnectAttempts;
    }

    return this.parent.getMaximumReconnectAttempts();
  }

  public void setMaximumReconnectAttempts(int maximumReconnectAttempts) {
    this.maximumReconnectAttempts = maximumReconnectAttempts;
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public String getNickServNick() {
    if (this.nickServNick != null) {
      return this.nickServNick;
    }

    return this.parent.getNickServNick();
  }

  public void setNickServNick(String nickServNick) {
    this.nickServNick = nickServNick;
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public Optional<String> getNickServPassword() {
    if (this.nickServPassword != null) {
      return Optional.of(this.nickServPassword);
    }

    return this.parent.getNickServPassword();
  }

  public void setNickServPassword(String nickServPassword) {
    this.nickServPassword = nickServPassword;
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public String getName() {
    if (this.name != null) {
      return this.name;
    }

    return this.parent.getName();
  }

  public void setName(String name) {
    this.name = name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getReconnectDelay() {
    if (this.reconnectDelay >= 0) {
      return this.reconnectDelay;
    }

    return this.parent.getReconnectDelay();
  }

  public void setReconnectDelay(int reconnectDelay) {
    this.reconnectDelay = reconnectDelay;
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public String getRealNameTemplate() {
    if (this.realNameTemplate != null) {
      return this.realNameTemplate;
    }

    return this.parent.getRealNameTemplate();
  }

  public void setRealNameTemplate(String realNameTemplate) {
    this.realNameTemplate = realNameTemplate;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getSocketTimeout() {
    if (this.socketTimeout >= 0) {
      return this.socketTimeout;
    }

    return this.parent.getSocketTimeout();
  }

  public void setSocketTimeout(int socketTimeout) {
    this.socketTimeout = socketTimeout;
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public String getUserPrefixes() {
    if (this.userPrefixes != null) {
      return this.userPrefixes;
    }

    return this.parent.getUserPrefixes();
  }

  public void setUserPrefixes(String userPrefixes) {
    this.userPrefixes = userPrefixes;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAuthenticationDelayEnabled() {
    if (this.authenticationDelayEnabled != null) {
      return this.authenticationDelayEnabled;
    }

    return this.parent.isAuthenticationDelayEnabled();
  }

  public void setAuthenticationDelayEnabled(Boolean authenticationDelayEnabled) {
    this.authenticationDelayEnabled = authenticationDelayEnabled;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAutomaticNicknameChangeEnabled() {
    if (this.automaticNicknameChangeEnabled != null) {
      return this.automaticNicknameChangeEnabled;
    }

    return this.parent.isAutomaticNicknameChangeEnabled();
  }

  public void setAutomaticNicknameChangeEnabled(Boolean automaticNicknameChangeEnabled) {
    this.automaticNicknameChangeEnabled = automaticNicknameChangeEnabled;
  }
}
