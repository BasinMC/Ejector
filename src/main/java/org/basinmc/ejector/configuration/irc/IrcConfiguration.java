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
import edu.umd.cs.findbugs.annotations.Nullable;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Represents the root IRC configuration.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
@ConfigurationProperties("ejector.irc")
public class IrcConfiguration implements Configuration {

  private boolean enabled;
  @NestedConfigurationProperty
  private final Set<IrcServer> servers = new HashSet<>();

  private String channelPrefixes = "#&+!";
  private Charset charset = StandardCharsets.UTF_8;
  private String ctcpFingerResponseTemplate = "( ͡° ͜ʖ ͡°)";
  private String ctcpVersionResponseTemplate = "Ejector v%s (+%s)";
  private String ident = "ejector";
  private InetAddress localAddress;
  private long messageDelay = 500;
  private int maximumReconnectAttempts = Integer.MAX_VALUE;
  private String nickServNick = "NickServ";
  private String nickServPassword;
  private String name = "Ejector";
  private int reconnectDelay = 5000;
  private String realNameTemplate = "Ejector v%s (+%s)";
  private int socketTimeout = 120000;
  private String userPrefixes = "+@%&~!";
  private boolean authenticationDelayEnabled = true;
  private boolean automaticNicknameChangeEnabled = true;

  public boolean isEnabled() {
    return this.enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  @NonNull
  @SuppressWarnings("AssignmentOrReturnOfFieldWithMutableType")
  public Set<IrcServer> getServers() {
    return this.servers;
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public String getChannelPrefixes() {
    return this.channelPrefixes;
  }

  public void setChannelPrefixes(@NonNull String channelPrefixes) {
    this.channelPrefixes = channelPrefixes;
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public Charset getCharset() {
    return this.charset;
  }

  public void setCharset(@NonNull Charset charset) {
    this.charset = charset;
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public String getCtcpFingerResponseTemplate() {
    return this.ctcpFingerResponseTemplate;
  }

  public void setCtcpFingerResponseTemplate(@NonNull String ctcpFingerResponseTemplate) {
    this.ctcpFingerResponseTemplate = ctcpFingerResponseTemplate;
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public String getCtcpVersionResponseTemplate() {
    return this.ctcpVersionResponseTemplate;
  }

  public void setCtcpVersionResponseTemplate(@NonNull String ctcpVersionResponseTemplate) {
    this.ctcpVersionResponseTemplate = ctcpVersionResponseTemplate;
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public String getIdent() {
    return this.ident;
  }

  public void setIdent(@NonNull String ident) {
    this.ident = ident;
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public Optional<InetAddress> getLocalAddress() {
    return Optional.ofNullable(this.localAddress);
  }

  public void setLocalAddress(@Nullable InetAddress localAddress) {
    this.localAddress = localAddress;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long getMessageDelay() {
    return this.messageDelay;
  }

  public void setMessageDelay(int messageDelay) {
    this.messageDelay = messageDelay;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getMaximumReconnectAttempts() {
    return this.maximumReconnectAttempts;
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
    return this.nickServNick;
  }

  public void setNickServNick(@NonNull String nickServNick) {
    this.nickServNick = nickServNick;
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public Optional<String> getNickServPassword() {
    return Optional.ofNullable(this.nickServPassword);
  }

  public void setNickServPassword(@Nullable String nickServPassword) {
    this.nickServPassword = nickServPassword;
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public String getName() {
    return this.name;
  }

  public void setName(@NonNull String name) {
    this.name = name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getReconnectDelay() {
    return this.reconnectDelay;
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
    return this.realNameTemplate;
  }

  public void setRealNameTemplate(String realNameTemplate) {
    this.realNameTemplate = realNameTemplate;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getSocketTimeout() {
    return this.socketTimeout;
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
    return this.userPrefixes;
  }

  public void setUserPrefixes(@NonNull String userPrefixes) {
    this.userPrefixes = userPrefixes;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAuthenticationDelayEnabled() {
    return this.authenticationDelayEnabled;
  }

  public void setAuthenticationDelayEnabled(boolean authenticationDelayEnabled) {
    this.authenticationDelayEnabled = authenticationDelayEnabled;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean isAutomaticNicknameChangeEnabled() {
    return this.automaticNicknameChangeEnabled;
  }

  public void setAutomaticNicknameChangeEnabled(boolean automaticNicknameChangeEnabled) {
    this.automaticNicknameChangeEnabled = automaticNicknameChangeEnabled;
  }
}
