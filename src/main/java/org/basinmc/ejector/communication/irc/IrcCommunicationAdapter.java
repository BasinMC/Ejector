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
import java.io.IOException;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.basinmc.ejector.communication.CommunicationAdapter;
import org.basinmc.ejector.communication.Message;
import org.basinmc.ejector.communication.Message.Color;
import org.basinmc.ejector.configuration.irc.IrcChannel;
import org.basinmc.ejector.configuration.irc.IrcConfiguration;
import org.basinmc.ejector.configuration.irc.IrcServer;
import org.basinmc.ejector.utility.PreconfiguredMessageSource;
import org.basinmc.stormdrain.Payload;
import org.basinmc.stormdrain.PayloadType;
import org.basinmc.stormdrain.event.CommitCommentEvent;
import org.basinmc.stormdrain.event.CreateEvent;
import org.basinmc.stormdrain.event.DeleteEvent;
import org.basinmc.stormdrain.event.DeploymentEvent;
import org.basinmc.stormdrain.event.DeploymentStatusEvent;
import org.basinmc.stormdrain.event.Event;
import org.basinmc.stormdrain.event.ForkEvent;
import org.basinmc.stormdrain.event.GollumEvent;
import org.basinmc.stormdrain.event.IssueCommentEvent;
import org.basinmc.stormdrain.event.IssuesEvent;
import org.basinmc.stormdrain.event.LabelEvent;
import org.basinmc.stormdrain.event.MemberEvent;
import org.basinmc.stormdrain.event.MembershipEvent;
import org.basinmc.stormdrain.event.MilestoneEvent;
import org.basinmc.stormdrain.event.OrganizationBlockEvent;
import org.basinmc.stormdrain.event.OrganizationEvent;
import org.basinmc.stormdrain.event.PageBuildEvent;
import org.basinmc.stormdrain.event.PublicEvent;
import org.basinmc.stormdrain.event.PullRequestEvent;
import org.basinmc.stormdrain.event.PullRequestReviewCommentEvent;
import org.basinmc.stormdrain.event.PullRequestReviewEvent;
import org.basinmc.stormdrain.event.PushEvent;
import org.basinmc.stormdrain.event.ReleaseEvent;
import org.basinmc.stormdrain.event.RepositoryEvent;
import org.basinmc.stormdrain.event.TeamAddEvent;
import org.basinmc.stormdrain.event.TeamEvent;
import org.basinmc.stormdrain.event.WatchEvent;
import org.basinmc.stormdrain.resource.Membership;
import org.pircbotx.Colors;
import org.pircbotx.Configuration;
import org.pircbotx.PircBotX;
import org.pircbotx.exception.IrcException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySourcesPropertyResolver;
import org.springframework.stereotype.Component;

/**
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
@Component
@EnableConfigurationProperties(IrcConfiguration.class)
@ConditionalOnProperty(prefix = "ejector.irc", name = "enabled")
public class IrcCommunicationAdapter implements CommunicationAdapter, InitializingBean {

  private static final String PROJECT_URL = "https://github.com/BasinMC/Ejector";
  private static final Logger logger = LogManager.getFormatterLogger(IrcCommunicationAdapter.class);

  private static final Map<Color, String> colorCodes = new HashMap<>();

  private final IrcConfiguration configuration;
  private final Set<BotInstance> botMap;
  private final Map<Class<? extends Event>, Function<Event, String>> handlerMap = new HashMap<>();

  private final PreconfiguredMessageSource messageSource;
  private final PropertySourcesPropertyResolver propertyResolver;

  static {
    colorCodes.put(Color.BLACK, Colors.BLACK);
    colorCodes.put(Color.BLUE, Colors.BLUE);
    colorCodes.put(Color.BROWN, Colors.BROWN);
    colorCodes.put(Color.CYAN, Colors.CYAN);
    colorCodes.put(Color.DARK_BLUE, Colors.DARK_BLUE);
    colorCodes.put(Color.DARK_GREEN, Colors.DARK_GREEN);
    colorCodes.put(Color.DARK_GRAY, Colors.DARK_GRAY);
    colorCodes.put(Color.GREEN, Colors.GREEN);
    colorCodes.put(Color.LIGHT_GRAY, Colors.LIGHT_GRAY);
    colorCodes.put(Color.MAGENTA, Colors.MAGENTA);
    colorCodes.put(Color.NONE, Colors.NORMAL);
    colorCodes.put(Color.OLIVE, Colors.OLIVE);
    colorCodes.put(Color.PURPLE, Colors.PURPLE);
    colorCodes.put(Color.RED, Colors.RED);
    colorCodes.put(Color.TEAL, Colors.TEAL);
    colorCodes.put(Color.WHITE, Colors.WHITE);
    colorCodes.put(Color.YELLOW, Colors.YELLOW);
  }

  public IrcCommunicationAdapter(@NonNull IrcConfiguration configuration) {
    this.configuration = configuration;

    ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setBasename("classpath:/localization/irc");

    if (Boolean.getBoolean("ejector.cache.disabled")) {
      messageSource.setCacheMillis(0);
    }

    this.messageSource = new PreconfiguredMessageSource();
    this.messageSource.setParentMessageSource(messageSource);

    MutablePropertySources sources = new MutablePropertySources();
    sources.addLast(new IrcColorPropertySource());
    this.propertyResolver = new PropertySourcesPropertyResolver(sources);
    this.propertyResolver.setPlaceholderPrefix("$(");
    this.propertyResolver.setPlaceholderSuffix(")");

    this.botMap = configuration.getServers().stream()
        .map((s) -> {
          // FIXME: This sucks but Spring doesn't construct it correctly otherwise :(
          s.setParent(configuration);

          Configuration cnf = toConfiguration(s);
          return new BotInstance(new PircBotX(cnf), s);
        })
        .collect(Collectors.toSet());

    // Payload Handlers
    for (PayloadType type : PayloadType.values()) {
      MethodHandles.Lookup caller = MethodHandles.lookup();

      MethodType factoryType = MethodType
          .methodType(Function.class, IrcCommunicationAdapter.class);
      MethodType implementationType = MethodType.methodType(String.class, type.getType());

      try {
        MethodHandle handle = caller
            .findVirtual(IrcCommunicationAdapter.class, "buildMessage", implementationType);
        MethodType invocationType = handle.type().dropParameterTypes(0, 1);

        CallSite site = LambdaMetafactory.metafactory(
            caller,
            "apply",
            factoryType,
            invocationType.changeReturnType(Object.class).changeParameterType(0, Object.class),
            handle,
            invocationType
        );

        try {
          this.handlerMap.put(
              type.getType(),
              (Function<Event, String>) site.getTarget().invoke(this)
          );
        } catch (Throwable ex) {
          throw new RuntimeException("Lambda factory invocation failed: " + ex.getMessage(), ex);
        }
      } catch (NoSuchMethodException ignore) {
      } catch (IllegalAccessException ex) {
        throw new RuntimeException(
            "Failed to access builder method for payload type " + type + ": " + ex.getMessage(),
            ex);
      } catch (LambdaConversionException ex) {
        throw new RuntimeException(
            "Failed to create lambda method for payload type " + type + ": " + ex.getMessage(), ex);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    this.botMap.forEach((b) -> {
      Thread thread = new Thread(b::start);
      thread.setName("irc-adapter");
      thread.start();
    });
  }

  @NonNull
  private String buildMessage(@NonNull CommitCommentEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.commit_comment." + event.getAction().name().toLowerCase(),
        event.getRepository().getFullName(),
        event.getSender().getLogin(),
        event.getComment().getCommitId(),
        event.getComment().getBrowserUrl().toExternalForm()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull CreateEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.create",
        event.getRepository().getFullName(),
        event.getSender().getLogin(),
        event.getReferenceType().name().toLowerCase(),
        event.getReference()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull DeleteEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.delete",
        event.getRepository().getFullName(),
        event.getSender().getLogin(),
        event.getReferenceType().name().toLowerCase(),
        event.getReference()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull DeploymentEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.deployment",
        event.getRepository().getFullName(),
        event.getSender().getLogin(),
        event.getDeployment().getEnvironment()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull DeploymentStatusEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.deployment.status" + (event.getDeploymentStatus().getTargetUrl().isPresent()
            ? ".target" : ""),
        event.getRepository().getFullName(),
        event.getDeployment().getEnvironment(),
        event.getDeploymentStatus().getState().name().toLowerCase(),
        event.getDeploymentStatus().getTargetUrl().map(URL::toExternalForm).orElse(null)
    ));
  }

  @NonNull
  private String buildMessage(@NonNull ForkEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.fork",
        event.getRepository().getFullName(),
        event.getSender().getLogin(),
        event.getForkee().getFullName(),
        event.getForkee().getBrowserUrl()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull GollumEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.gollum",
        event.getRepository().getFullName(),
        event.getSender().getLogin(),
        event.getPages().size()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull IssueCommentEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.issues.comment." + event.getAction().name().toLowerCase(),
        event.getRepository().getFullName(),
        event.getSender().getLogin(),
        event.getIssue().getNumber(),
        event.getIssue().getTitle(),
        event.getComment().getBrowserUrl().toExternalForm()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull IssuesEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.issues." + event.getAction().name().toLowerCase(),
        event.getRepository().getFullName(),
        event.getSender().getLogin(),
        event.getIssue().getNumber(),
        event.getIssue().getTitle(),
        event.getIssue().getBrowserUrl().toExternalForm()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull LabelEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.label." + event.getAction().name().toLowerCase(),
        event.getRepository().getFullName(),
        event.getSender().getLogin(),
        event.getLabel().getName()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull MemberEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.member." + event.getAction().name().toLowerCase(),
        event.getRepository().getFullName(),
        event.getSender().getLogin(),
        event.getMember().getLogin()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull MembershipEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.membership." + event.getAction().name().toLowerCase(),
        event.getOrganization().getLogin(),
        event.getSender().getLogin(),
        event.getMember().getLogin(),
        event.getTeam().getName(),
        event.getTeam().getPermission().name().toLowerCase()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull MilestoneEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.milestone." + event.getAction().name().toLowerCase(),
        event.getRepository().getFullName(),
        event.getSender().getLogin(),
        event.getMilestone().getNumber(),
        event.getMilestone().getTitle(),
        event.getMilestone().getBrowserUrl().toExternalForm()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull OrganizationEvent event) {
    if (event.getAction() != OrganizationEvent.Action.MEMBER_INVITED) {
      Membership membership = event.getMembership()
          .orElseThrow(() -> new IllegalStateException("Expected membership"));

      return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
          "github.organization." + event.getAction().name().toLowerCase(),
          event.getOrganization().getLogin(),
          event.getSender().getLogin(),
          membership.getUser().getLogin(),
          membership.getRole().name().toLowerCase()
      ));
    }

    String invite = event.getInvitation().getEmail()
        .map((e) -> {
          String[] elements = e.split("@");

          if (elements.length != 2) {
            return "**@**.**";
          } else {
            String account = elements[0];
            String domain = elements[1];

            if (account.length() <= 4) {
              account = "**";
            } else {
              account = account.substring(0, 1) + "**" + account
                  .substring(account.length() - 1, account.length());
            }

            return account + "@" + domain;
          }
        })
        .orElseGet(() -> event.getInvitation().getLogin()
            .orElseThrow(() -> new IllegalStateException("Either login or email required")));

    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.organization." + event.getAction().name().toLowerCase(),
        event.getOrganization().getLogin(),
        event.getSender().getLogin(),
        invite,
        event.getInvitation().getRole().name().toLowerCase()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull OrganizationBlockEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.organization.block." + event.getAction().name().toLowerCase(),
        event.getOrganization().getLogin(),
        event.getSender().getLogin(),
        event.getBlockedUser().getLogin()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull PageBuildEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.page.build." + event.getBuild().getStatus().name().toLowerCase(),
        event.getRepository().getFullName(),
        event.getSender().getLogin(),
        event.getBuild().getCommitId()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull PublicEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.public",
        event.getRepository().getFullName(),
        event.getSender().getLogin()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull PullRequestEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.pull." + event.getAction().name().toLowerCase(),
        event.getRepository().getFullName(),
        event.getSender().getLogin(),
        event.getPullRequest().getNumber(),
        event.getPullRequest().getTitle(),
        event.getPullRequest().getBrowserUrl().toExternalForm()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull PullRequestReviewEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.review." + event.getAction().name().toLowerCase(),
        event.getRepository().getFullName(),
        event.getSender().getLogin(),
        event.getPullRequest().getNumber(),
        event.getPullRequest().getTitle(),
        event.getReview().getBrowserUrl().toExternalForm()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull PullRequestReviewCommentEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.review.comment." + event.getAction().name().toLowerCase(),
        event.getRepository().getFullName(),
        event.getSender().getLogin(),
        event.getPullRequest().getNumber(),
        event.getPullRequest().getTitle(),
        event.getComment().getBrowserUrl().toExternalForm()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull PushEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.push",
        event.getRepository().getFullName(),
        event.getSender().getLogin(),
        event.getReference(),
        event.getCommits().size(),
        event.getCommits().stream()
            .mapToInt((c) -> c.getAddedFiles().size())
            .sum(),
        event.getCommits().stream()
            .mapToInt((c) -> c.getModifiedFiles().size())
            .sum(),
        event.getCommits().stream()
            .mapToInt((c) -> c.getRemovedFiles().size())
            .sum(),
        event.getCompareUrl().toExternalForm()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull ReleaseEvent event) {
    String title = event.getRelease().getName()
        .map((n) -> event.getRelease().getTagName() + " - " + n)
        .orElseGet(event.getRelease()::getTagName);

    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.release",
        event.getRepository().getFullName(),
        event.getSender().getLogin(),
        title,
        event.getRelease().getBrowserUrl().toExternalForm()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull RepositoryEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.repository." + event.getAction().name().toLowerCase(),
        event.getRepository().getFullName(),
        event.getSender().getLogin(),
        event.getRepository().getBrowserUrl()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull TeamEvent event) {
    return event.getRepository()
        .filter((r) -> event.getAction() == TeamEvent.Action.ADDED_TO_REPOSITORY ||
            event.getAction() == TeamEvent.Action.REMOVED_FROM_REPOSITORY) // for debugging mostly
        .map((r) -> this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
            "github.team." + event.getAction().name().toLowerCase(),
            event.getOrganization().getLogin(),
            event.getSender().getLogin(),
            event.getTeam().getName(),
            r.getFullName()
        )))
        .orElseGet(() -> this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
            "github.team." + event.getAction().name().toLowerCase(),
            event.getOrganization().getLogin(),
            event.getSender().getLogin(),
            event.getTeam().getName()
        )));
  }

  @NonNull
  private String buildMessage(@NonNull TeamAddEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.team.added",
        event.getRepository().getFullName(),
        event.getSender().getLogin(),
        event.getTeam().getName()
    ));
  }

  @NonNull
  private String buildMessage(@NonNull WatchEvent event) {
    return this.propertyResolver.resolvePlaceholders(this.messageSource.getMessage(
        "github.watch",
        event.getRepository().getFullName(),
        event.getSender().getLogin()
    ));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void handlePayload(@NonNull Payload<?> payload) {
    Function<Event, String> handler = this.handlerMap.get(payload.getType().getType());

    if (handler == null) {
      return;
    }

    String message = handler.apply(payload.getEvent());

    this.botMap.forEach((i) -> {
      i.server.getChannels().forEach((ch) -> {
        if (!ch.isReceivingEvent(payload.getType())) {
          return;
        }

        i.bot.send().message(ch.getName(), message);
      });
    });
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void sendMessage(@NonNull Message message) {
    String msg = message.toString((m) -> {
      StringBuilder builder = new StringBuilder();
      String prefix = "";

      switch (m.getStyle()) {
        case BOLD:
          prefix = Colors.BOLD;
          break;
        case ITALICS:
          prefix = Colors.ITALICS;
          break;
        case UNDERLINE:
          prefix = Colors.UNDERLINE;
          break;
      }

      builder.append(prefix);
      builder.append(colorCodes.getOrDefault(m.getColor(), Colors.NORMAL));
      builder.append(Colors.removeFormattingAndColors(m.getText()));
      builder.append(prefix);

      return builder.toString();
    });

    this.botMap.forEach(
        (i) -> i.server.getChannels().forEach((ch) -> i.bot.send().message(ch.getName(), msg)));
  }

  /**
   * Converts an IRC server configuration into its respective PircBotX representation.
   *
   * @param server a server.
   * @return a configuration.
   */
  @NonNull
  private static Configuration toConfiguration(@NonNull IrcServer server) {
    String version = detectVersion();

    return new Configuration.Builder()
        .addServer(server.getHostname(), server.getPort())
        .setServerPassword(server.getPassword().orElse(null))
        .setSocketFactory(
            server.isSecure() ? SSLSocketFactory.getDefault() : SocketFactory.getDefault())
        .addAutoJoinChannels(
            server.getChannels().stream()
                .map(IrcChannel::getName)
                .collect(Collectors.toSet())
        )
        .setChannelPrefixes(server.getChannelPrefixes())
        .setFinger(String.format(server.getCtcpFingerResponseTemplate(), version, PROJECT_URL))
        .setVersion(String.format(server.getCtcpVersionResponseTemplate(), version, PROJECT_URL))
        .setLogin(server.getIdent())
        .setLocalAddress(server.getLocalAddress().orElse(null))
        .setMessageDelay(server.getMessageDelay())
        .setAutoReconnectAttempts(server.getMaximumReconnectAttempts())
        .setNickservNick(server.getNickServNick())
        .setNickservPassword(server.getNickServPassword().orElse(null))
        .setName(server.getName())
        .setAutoReconnectDelay(server.getReconnectDelay())
        .setRealName(String.format(server.getRealNameTemplate(), version, PROJECT_URL))
        .setSocketTimeout(server.getSocketTimeout())
        .setUserLevelPrefixes(server.getUserPrefixes())
        .setNickservDelayJoin(
            server.getNickServPassword().isPresent() && server.isAuthenticationDelayEnabled())
        .setAutoNickChange(server.isAutomaticNicknameChangeEnabled())
        .buildConfiguration();
  }

  /**
   * Detects the current bot version.
   *
   * @return a version number or "0.0.0" if none is set (in development environments, for instance).
   */
  @NonNull
  private static String detectVersion() {
    Package p = IrcCommunicationAdapter.class.getPackage();
    return Optional.ofNullable(p.getImplementationVersion())
        .orElse("0.0.0");
  }

  /**
   * Provides an internal representation for bot instances.
   */
  private static final class BotInstance {

    private final PircBotX bot;
    private final IrcServer server;

    private BotInstance(@NonNull PircBotX bot, @NonNull IrcServer server) {
      this.bot = bot;
      this.server = server;
    }

    @NonNull
    public PircBotX getBot() {
      return this.bot;
    }

    @NonNull
    public IrcServer getServer() {
      return this.server;
    }

    public void start() {
      try {
        this.bot.startBot();
      } catch (IrcException | IOException ex) {
        logger.error("Failed to initialize bot for server " + this.server.getHostname() + ": " + ex
            .getMessage(), ex);
      }
    }
  }
}
