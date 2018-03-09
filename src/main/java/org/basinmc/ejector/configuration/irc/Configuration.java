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
import java.util.Optional;

/**
 * Provides a configuration which provides information on how a bot is expected to behave on a given
 * server.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public interface Configuration {

  /**
   * Retrieves a list of valid channel prefixes (typically # and & for global and local channels
   * respectively).
   *
   * @return a list of channel prefix characters.
   */
  @NonNull
  String getChannelPrefixes();

  /**
   * Retrieves the charset with which messages are n- and decoded.
   *
   * @return a charset.
   */
  @NonNull
  Charset getCharset();

  /**
   * <p>Retrieves the response template for CTCP FINGER requests.</p>
   *
   * <p>This value will be passed to {@link String#format(String, Object...)} with two
   * parameters:</p>
   *
   * <ol>
   *
   * <li>Bot Version Number</li>
   *
   * <li>Bot Project URL</li>
   *
   * </ol>
   *
   * @return a finger response template.
   */
  @NonNull
  String getCtcpFingerResponseTemplate();

  /**
   * <p>Retrieves teh response template for CTCP VERSION requests.</p>
   *
   * <p>This value will be passed to {@link String#format(String, Object...)} with two
   * parameters:</p>
   *
   * <ol>
   *
   * <li>Bot Version Number</li>
   *
   * <li>Bot Project URL</li>
   *
   * </ol>
   *
   * @return a version response template.
   */
  @NonNull
  String getCtcpVersionResponseTemplate();

  /**
   * Retrieves the ident (or login) name for this bot.
   *
   * @return an ident.
   */
  @NonNull
  String getIdent();

  /**
   * Retrieves the selected local address (e.g. network interface) with which the bot will connect
   * to the server.
   *
   * @return an address.
   */
  @NonNull
  Optional<InetAddress> getLocalAddress();

  /**
   * Retrieves the delay between sending messages (in milliseconds).
   *
   * @return a delay.
   */
  long getMessageDelay();

  /**
   * Retrieves the maximum amount of times the bot is permitted to attempt to reconnect to a server
   * before giving up.
   *
   * @return an amount.
   */
  int getMaximumReconnectAttempts();

  /**
   * Retrieves the full name of the NickServ implementation on this server.
   *
   * @return a nickname.
   */
  @NonNull
  String getNickServNick();

  /**
   * Retrieves the password which is passed to NickServ upon connecting in order to authenticate.
   *
   * @return a password or, if no authentication via NickServ is desired, an empty optional.
   */
  @NonNull
  Optional<String> getNickServPassword();

  /**
   * Retrieves the nickname for the bot.
   *
   * @return a nickname.
   */
  @NonNull
  String getName();

  /**
   * Retrieves the amount of time (in milliseconds) which is required to pass before another
   * reconnect attempt is made.
   *
   * @return a delay.
   */
  int getReconnectDelay();

  /**
   * <p>Retrieves the real name (e.g. the long identity description) for this bot.</p>
   *
   * <p>This value will be passed to {@link String#format(String, Object...)} with two
   * parameters:</p>
   *
   * <ol>
   *
   * <li>Bot Version Number</li>
   *
   * <li>Bot Project URL</li>
   *
   * </ol>
   *
   * @return a real name template.
   */
  @NonNull
  String getRealNameTemplate();

  /**
   * Retrieves the total amount of time (in milliseconds) which may pass since the last message
   * until a ping request is sent to verify whether the socket is still alive.
   *
   * @return a timeout.
   */
  int getSocketTimeout();

  /**
   * Retrieves a list of valid user prefixes (e.g. channel permission prefixes).
   *
   * @return a set of prefix characters.
   */
  @NonNull
  String getUserPrefixes();

  /**
   * Evaluates whether any operations (including joining of channels) is delayed until the bot has
   * received authentication confirmation.
   *
   * @return true if delay is enabled, false otherwise.
   */
  boolean isAuthenticationDelayEnabled();

  /**
   * Evaluates whether the bot shall try to automatically regain its configured nickname.
   *
   * @return true if automatic changing is desired, false otherwise.
   */
  boolean isAutomaticNicknameChangeEnabled();
}
