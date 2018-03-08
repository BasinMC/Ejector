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
package org.basinmc.ejector.communication.discord;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.OnlineStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.MessageEmbed;
import org.basinmc.ejector.communication.CommunicationAdapter;
import org.basinmc.ejector.communication.Message;
import org.basinmc.ejector.configuration.DiscordConfiguration;
import org.basinmc.ejector.utility.PreconfiguredMessageSource;
import org.basinmc.stormdrain.Payload;
import org.basinmc.stormdrain.PayloadType;
import org.basinmc.stormdrain.event.AbstractUserTriggeredEvent;
import org.basinmc.stormdrain.event.CommitCommentEvent;
import org.basinmc.stormdrain.event.CreateEvent;
import org.basinmc.stormdrain.event.DeleteEvent;
import org.basinmc.stormdrain.event.DeploymentEvent;
import org.basinmc.stormdrain.event.DeploymentStatusEvent;
import org.basinmc.stormdrain.event.Event;
import org.basinmc.stormdrain.event.ForkEvent;
import org.basinmc.stormdrain.event.IssueCommentEvent;
import org.basinmc.stormdrain.event.IssuesEvent;
import org.basinmc.stormdrain.event.LabelEvent;
import org.basinmc.stormdrain.event.MemberEvent;
import org.basinmc.stormdrain.event.MembershipEvent;
import org.basinmc.stormdrain.event.MilestoneEvent;
import org.basinmc.stormdrain.event.OrganizationBlockEvent;
import org.basinmc.stormdrain.event.OrganizationEvent;
import org.basinmc.stormdrain.event.OrganizationEvent.Action;
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
import org.basinmc.stormdrain.resource.CommitComment;
import org.basinmc.stormdrain.resource.Issue.Label;
import org.basinmc.stormdrain.resource.Membership;
import org.basinmc.stormdrain.resource.Repository;
import org.basinmc.stormdrain.resource.User;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.stereotype.Component;

/**
 * Provides a Discord based communication adapter.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
@Component
@EnableConfigurationProperties(DiscordConfiguration.class)
@ConditionalOnProperty(prefix = "ejector.discord", name = "enabled")
public class DiscordCommunicationAdapter implements CommunicationAdapter, InitializingBean {

  private static final Map<PayloadType, Integer> colorMap = new EnumMap<>(PayloadType.class);
  private final Map<Class<? extends Event>, PayloadMessageBuilder> handlerMap = new HashMap<>();

  private final DiscordConfiguration configuration;
  private final PreconfiguredMessageSource messageSource;

  private final JDA client;
  private final Guild guild;

  static {
    // Payload Type Colors
    final int COLOR_BLUE = 0x2d82cc;
    final int COLOR_GREEN = 0x2ecc8b;
    final int COLOR_RED = 0xcc2d2d;

    colorMap.put(PayloadType.COMMIT_COMMENT, COLOR_BLUE);
    colorMap.put(PayloadType.CREATE, COLOR_GREEN);
    colorMap.put(PayloadType.DELETE, COLOR_RED);
    colorMap.put(PayloadType.DEPLOYMENT, COLOR_GREEN);
    colorMap.put(PayloadType.DEPLOYMENT_STATUS, COLOR_BLUE);
    colorMap.put(PayloadType.FORK, COLOR_GREEN);
    colorMap.put(PayloadType.GOLLUM, COLOR_BLUE);
    colorMap.put(PayloadType.ISSUE_COMMENT, COLOR_BLUE);
    colorMap.put(PayloadType.ISSUES, COLOR_RED);
    colorMap.put(PayloadType.LABEL, COLOR_BLUE);
    colorMap.put(PayloadType.MEMBER, COLOR_BLUE);
    colorMap.put(PayloadType.MEMBERSHIP, COLOR_BLUE);
    colorMap.put(PayloadType.MILESTONE, COLOR_GREEN);
    colorMap.put(PayloadType.ORGANIZATION, COLOR_BLUE);
    colorMap.put(PayloadType.ORG_BLOCK, COLOR_RED);
    colorMap.put(PayloadType.PAGE_BUILD, COLOR_GREEN);
    colorMap.put(PayloadType.PUBLIC, COLOR_GREEN);
    colorMap.put(PayloadType.PULL_REQUEST_REVIEW_COMMENT, COLOR_BLUE);
    colorMap.put(PayloadType.PULL_REQUEST_REVIEW, COLOR_BLUE);
    colorMap.put(PayloadType.PULL_REQUEST, COLOR_GREEN);
    colorMap.put(PayloadType.PUSH, COLOR_BLUE);
    colorMap.put(PayloadType.REPOSITORY, COLOR_BLUE);
    colorMap.put(PayloadType.RELEASE, COLOR_GREEN);
    colorMap.put(PayloadType.TEAM, COLOR_BLUE);
    colorMap.put(PayloadType.TEAM_ADD, COLOR_GREEN);
  }

  @Autowired
  public DiscordCommunicationAdapter(@NonNull DiscordConfiguration configuration)
      throws LoginException, InterruptedException {
    this.configuration = configuration;

    ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
    messageSource.setDefaultEncoding("UTF-8");
    messageSource.setBasename("classpath:/localization/discord");

    if (Boolean.getBoolean("ejector.cache.disabled")) {
      messageSource.setCacheMillis(0);
    }

    this.messageSource = new PreconfiguredMessageSource(Locale.getDefault());
    this.messageSource.setParentMessageSource(messageSource);

    this.client = new JDABuilder(AccountType.BOT)
        .setToken(configuration.getToken())
        .buildBlocking();
    this.guild = this.client.getGuildById(configuration.getGuildId());

    // Payload Handlers
    for (PayloadType type : PayloadType.values()) {
      MethodHandles.Lookup caller = MethodHandles.lookup();

      MethodType factoryType = MethodType
          .methodType(PayloadMessageBuilder.class, DiscordCommunicationAdapter.class);
      MethodType implementationType = MethodType.methodType(void.class, MessageBuilder.class,
          EmbedBuilder.class, type.getType());

      try {
        MethodHandle handle = caller
            .findVirtual(DiscordCommunicationAdapter.class, "buildMessage", implementationType);
        MethodType invocationType = handle.type().dropParameterTypes(0, 1);

        CallSite site = LambdaMetafactory.metafactory(
            caller,
            "build",
            factoryType,
            invocationType.changeParameterType(2, Event.class),
            handle,
            invocationType
        );

        try {
          this.handlerMap.put(
              type.getType(),
              (PayloadMessageBuilder) site.getTarget().invoke(this)
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
    this.client.getPresence().setPresence(OnlineStatus.ONLINE, Game.playing("with a potato"));
  }

  private void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull CommitCommentEvent event) {

    message.setContent(this.messageSource.getMessage(
        "github.commit.comment." + event.getAction().name().toLowerCase(),
        event.getRepository().getFullName()
    ));

    CommitComment comment = event.getComment();
    embed.setTitle(
        comment.getLocation()
            .map((l) -> this.messageSource
                .getMessage("github.commit.comment.title.path", l.getPath(), comment.getCommitId()))
            .orElseGet(() -> this.messageSource
                .getMessage("github.commit.comment.title", comment.getCommitId())),
        comment.getBrowserUrl().toExternalForm()
    );
    comment.getBody().ifPresent(embed::setDescription);
  }

  private void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull CreateEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.create." + event.getReferenceType().name().toLowerCase(),
        event.getRepository().getFullName()
    ));
    embed.setTitle(event.getReference());
  }

  private void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed, @NonNull
      DeleteEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.delete." + event.getReferenceType().name().toLowerCase(),
        event.getRepository().getFullName()
    ));
    embed.setTitle(event.getReference());
  }

  private void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull DeploymentEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.deployment",
        event.getRepository().getFullName()
    ));
    embed.setTitle(this.messageSource.getMessage(
        "github.deployment.title",
        event.getDeployment().getEnvironment()
    ));
    event.getDeployment().getDescription().ifPresent(embed::setDescription);
  }

  private void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull DeploymentStatusEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.deployment.status",
        event.getRepository().getFullName()
    ));
    embed.setTitle(this.messageSource.getMessage(
        "github.deployment.title",
        event.getDeployment().getEnvironment()
    ), event.getDeploymentStatus().getTargetUrl()
        .map(URL::toExternalForm)
        .orElse(null));
    event.getDeployment().getDescription().ifPresent(embed::setDescription);
    embed.addField(
        this.messageSource.getMessage("github.deployment.status.state"),
        event.getDeploymentStatus().getState().name().toLowerCase(),
        false
    );
  }

  private void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull ForkEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.fork",
        event.getRepository().getFullName()
    ));

    embed.setTitle(event.getForkee().getFullName(),
        event.getForkee().getBrowserUrl().toExternalForm());
    event.getForkee().getDescription().ifPresent(embed::setDescription);
  }

  public void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull IssueCommentEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.issue.comment." + event.getAction().name().toLowerCase(),
        event.getRepository().getFullName()
    ));

    embed.setTitle(this.messageSource.getMessage(
        "github.issues.title",
        event.getIssue().getNumber(),
        event.getIssue().getTitle()
    ), event.getIssue().getBrowserUrl().toExternalForm());
    event.getComment().getBody().ifPresent(embed::setDescription);
  }

  private void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull IssuesEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.issues." + event.getAction().name().toLowerCase(),
        event.getRepository().getFullName()
    ));

    embed.setTitle(this.messageSource.getMessage(
        "github.issues.title",
        event.getIssue().getNumber(),
        event.getIssue().getTitle()
    ), event.getIssue().getBrowserUrl().toExternalForm());
    event.getIssue().getBody().ifPresent(embed::setDescription);

    event.getIssue().getMilestone().ifPresent((m) -> {
      embed.addField(
          this.messageSource.getMessage("github.issues.milestone"),
          "[" + m.getTitle() + "]" + "(" + m.getBrowserUrl().toExternalForm()
              + ")",
          false
      );
    });

    User reporter = event.getIssue().getUser();
    embed.addField(
        this.messageSource.getMessage("github.issues.reporter"),
        "[" + reporter.getLogin() + "](" + reporter.getBrowserUrl().toExternalForm() + ")",
        true
    );

    event.getIssue().getAssignee().ifPresent((a) -> {
      embed.addField(
          this.messageSource.getMessage("github.issues.assignee"),
          "[" + a.getLogin() + "](" + a.getBrowserUrl().toExternalForm() + ")",
          true
      );
    });

    if (!event.getIssue().getLabels().isEmpty()) {
      embed.setFooter(
          "Labels: " + event.getIssue().getLabels().stream()
              .map(Label::getName)
              .collect(Collectors.joining(", ")),
          "https://github.com/fluidicon.png"
      );
    }
  }

  public void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull LabelEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.label." + event.getAction().name().toLowerCase(),
        event.getRepository().getFullName()
    ));

    embed.setTitle(event.getLabel().getName());
    embed.setColor(event.getLabel().getColor());
  }

  public void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull MemberEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.member." + event.getAction().name().toLowerCase(),
        event.getRepository().getFullName()
    ));

    embed
        .setTitle(event.getMember().getLogin(), event.getMember().getBrowserUrl().toExternalForm());
    embed.setImage(event.getMember().getAvatarUrl().toExternalForm());
  }

  public void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull MembershipEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.membership." + event.getAction().name().toLowerCase(),
        event.getOrganization().getLogin()
    ));

    embed
        .setTitle(event.getMember().getLogin(), event.getMember().getBrowserUrl().toExternalForm());
    embed.setImage(event.getMember().getAvatarUrl().toExternalForm());
    embed.addField(
        this.messageSource.getMessage("github.membership.team"),
        event.getTeam().getName(),
        false
    );
  }

  public void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull MilestoneEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.milestone." + event.getAction().name().toLowerCase(),
        event.getRepository().getFullName()
    ));

    embed.setTitle(event.getMilestone().getTitle(),
        event.getMilestone().getBrowserUrl().toExternalForm());
    event.getMilestone().getDescription().ifPresent(embed::setDescription);

    if (event.getAction() == MilestoneEvent.Action.CLOSED
        || event.getAction() == MilestoneEvent.Action.EDITED) {
      embed.addField(
          this.messageSource.getMessage("github.milestone.stats.open"),
          Long.toString(event.getMilestone().getOpenIssues()),
          true
      );
      embed.addField(
          this.messageSource.getMessage("github.milestone.stats.closed"),
          Long.toString(event.getMilestone().getClosedIssues()),
          true
      );
    }
  }

  public void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull OrganizationEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.organization." + event.getAction().name().toLowerCase(),
        event.getOrganization().getLogin()
    ));

    if (event.getAction() == Action.MEMBER_INVITED) {
      embed.setTitle(
          event.getInvitation().getEmail()
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
                  .orElseThrow(
                      () -> new IllegalStateException("Must set either email or login (or both)")))
      );
      embed.addField(
          this.messageSource.getMessage("github.organization.role"),
          event.getInvitation().getRole().name().toLowerCase(),
          false
      );
    } else {
      Membership membership = event.getMembership().orElseThrow(
          () -> new IllegalStateException("Must set membership (or use member_invited instead)"));

      embed.setTitle(membership.getUser().getLogin(),
          membership.getUser().getBrowserUrl().toExternalForm());
      embed.setImage(membership.getUser().getAvatarUrl().toExternalForm());
    }

  }

  private void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull OrganizationBlockEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.organization.block." + event.getAction().name().toLowerCase(),
        event.getOrganization().getLogin()
    ));

    embed.setTitle(event.getBlockedUser().getLogin(),
        event.getBlockedUser().getBrowserUrl().toExternalForm());
    embed.setImage(event.getBlockedUser().getAvatarUrl().toExternalForm());
  }

  private void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull PageBuildEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.page.build." + event.getBuild().getStatus().name().toLowerCase(),
        event.getRepository().getFullName()
    ));

    embed.setTitle(event.getRepository().getFullName(),
        event.getRepository().getBrowserUrl().toExternalForm());
    event.getBuild().getErrorMessage().ifPresent(embed::setDescription);

    embed.addField(
        this.messageSource.getMessage("github.page.build.commitId"),
        event.getBuild().getCommitId(),
        false
    );

    event.getBuild().getDuration().ifPresent((d) -> {
      embed.addField(
          this.messageSource.getMessage("github.page.build.duration"),
          String.format("%02d:%02d", d.toMinutesPart(), d.toSecondsPart()),
          false
      );
    });
  }

  private void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull PublicEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.public",
        event.getRepository().getFullName()
    ));

    embed.setTitle(event.getRepository().getFullName(),
        event.getRepository().getBrowserUrl().toExternalForm());
    event.getRepository().getDescription().ifPresent(embed::setDescription);
  }

  private void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull PullRequestEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.pull." + event.getAction().name().toLowerCase(),
        event.getRepository().getFullName()
    ));

    embed.setTitle(this.messageSource.getMessage(
        "github.pull.title",
        event.getPullRequest().getNumber(),
        event.getPullRequest().getTitle()
    ), event.getPullRequest().getBrowserUrl().toExternalForm());
    event.getPullRequest().getBody().ifPresent(embed::setDescription);

    event.getPullRequest().getMergedAt().ifPresent((d) -> {
      embed.addField(
          this.messageSource.getMessage("github.pull.mergedAt"),
          DateTimeFormatter
              .ofLocalizedDateTime(FormatStyle.SHORT)
              .withZone(ZoneId.systemDefault())
              .format(d),
          false
      );
    });
    event.getPullRequest().getMilestone().ifPresent((m) -> {
      embed.addField(
          this.messageSource.getMessage("github.issues.milestone"),
          "[" + m.getTitle() + "]" + "(" + m.getBrowserUrl().toExternalForm()
              + ")",
          false
      );
    });
    User author = event.getPullRequest().getUser();
    embed.addField(
        this.messageSource.getMessage("github.pull.author"),
        "[" + author.getLogin() + "](" + author.getBrowserUrl().toExternalForm() + ")",
        true
    );
    event.getPullRequest().getAssignee().ifPresent((a) -> {
      embed.addField(
          this.messageSource.getMessage("github.issues.assignee"),
          "[" + a.getLogin() + "](" + a.getBrowserUrl().toExternalForm() + ")",
          true
      );
    });
  }

  public void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull PullRequestReviewEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.pull.review." + event.getAction().name().toLowerCase(),
        event.getRepository().getFullName()
    ));

    embed.setTitle(this.messageSource.getMessage(
        "github.pull.title",
        event.getPullRequest().getNumber(),
        event.getPullRequest().getTitle()
    ), event.getReview().getBrowserUrl().toExternalForm());
    embed.setDescription(event.getReview().getBody());

    embed.addField(
        this.messageSource.getMessage("github.pull.review.state"),
        event.getReview().getState().name().toLowerCase(),
        false
    );
  }

  public void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull PullRequestReviewCommentEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.pull.review.comment." + event.getAction().name().toLowerCase(),
        event.getRepository().getFullName()
    ));

    embed.setTitle(this.messageSource.getMessage(
        "github.pull.title",
        event.getPullRequest().getNumber(),
        event.getPullRequest().getTitle()
    ), event.getComment().getBrowserUrl().toExternalForm());
    event.getComment().getBody().ifPresent(embed::setDescription);

    event.getPullRequest().getMergedAt().ifPresent((d) -> {
      embed.addField(
          this.messageSource.getMessage("github.pull.mergedAt"),
          DateTimeFormatter
              .ofLocalizedDateTime(FormatStyle.SHORT)
              .withZone(ZoneId.systemDefault())
              .format(d),
          false
      );
    });
    event.getPullRequest().getMilestone().ifPresent((m) -> {
      embed.addField(
          this.messageSource.getMessage("github.issues.milestone"),
          "[" + m.getTitle() + "]" + "(" + m.getBrowserUrl().toExternalForm()
              + ")",
          false
      );
    });
    User author = event.getPullRequest().getUser();
    embed.addField(
        this.messageSource.getMessage("github.pull.author"),
        "[" + author.getLogin() + "](" + author.getBrowserUrl().toExternalForm() + ")",
        true
    );
    event.getPullRequest().getAssignee().ifPresent((a) -> {
      embed.addField(
          this.messageSource.getMessage("github.issues.assignee"),
          "[" + a.getLogin() + "](" + a.getBrowserUrl().toExternalForm() + ")",
          true
      );
    });
  }

  public void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull PushEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.push",
        event.getRepository().getFullName()
    ));

    if (event.getCommits().size() > 1) {
      StringBuilder builder = new StringBuilder();
      event.getCommits()
          .forEach((c) -> builder.append(" - ").append(c.getMessage()).append("\r\n"));
      embed.setDescription(builder.toString());
    } else {
      embed.setDescription(event.getCommits().iterator().next().getMessage());
    }

    embed.addField(
        this.messageSource.getMessage("github.push.added"),
        Long.toString(
            event.getCommits().stream()
                .mapToInt((c) -> c.getAddedFiles().size())
                .sum()
        ),
        true
    );

    embed.addField(
        this.messageSource.getMessage("github.push.modified"),
        Long.toString(
            event.getCommits().stream()
                .mapToInt((c) -> c.getModifiedFiles().size())
                .sum()
        ),
        true
    );

    embed.addField(
        this.messageSource.getMessage("github.push.deleted"),
        Long.toString(
            event.getCommits().stream()
                .mapToInt((c) -> c.getRemovedFiles().size())
                .sum()
        ),
        true
    );
  }

  public void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull ReleaseEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.release",
        event.getRepository().getFullName()
    ));

    embed.setTitle(
        event.getRelease().getName()
            .map((t) -> this.messageSource
                .getMessage("github.release.title.set", event.getRelease().getTagName(), t))
            .orElseGet(() -> this.messageSource
                .getMessage("github.release.title", event.getRelease().getTagName())),
        event.getRelease().getBrowserUrl().toExternalForm()
    );

    event.getRelease().getAssets().forEach((a) -> {
      embed.addField(
          a.getName(),
          this.messageSource
              .getMessage("github.release.download", a.getBrowserUrl().toExternalForm()),
          true
      );
    });

    embed.addField(
        this.messageSource.getMessage("github.release.tar"),
        this.messageSource.getMessage("github.release.download",
            event.getRelease().getTarballUrl().toExternalForm()),
        true
    );
    embed.addField(
        this.messageSource.getMessage("github.release.tar"),
        this.messageSource.getMessage("github.release.download",
            event.getRelease().getZipballUrl().toExternalForm()),
        true
    );
  }

  public void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull RepositoryEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.repository." + event.getAction().name().toLowerCase(),
        event.getRepository().getFullName()
    ));

    embed.setTitle(
        event.getRepository().getFullName(),
        event.getRepository().getBrowserUrl().toExternalForm()
    );
    event.getRepository().getDescription().ifPresent(embed::setDescription);

    embed.addField(
        this.messageSource.getMessage("github.repository.gitUrl"),
        event.getRepository().getGitUrl(),
        false
    );
    embed.addField(
        this.messageSource.getMessage("github.repository.cloneUrl"),
        event.getRepository().getCloneUrl(),
        false
    );
  }

  public void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull TeamEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.team." + event.getAction().name().toLowerCase(),
        event.getRepository()
            .map(Repository::getFullName)
            .orElse(null)
    ));

    embed.setTitle(event.getTeam().getName());

    embed.addField(
        this.messageSource.getMessage("github.team.permission"),
        event.getTeam().getPermission().name().toLowerCase(),
        true
    );
    event.getRepository().ifPresent((r) -> {
      embed.addField(
          this.messageSource.getMessage("github.team.repository"),
          "[" + r.getFullName() + "](" + r.getBrowserUrl().toExternalForm() + ")",
          true
      );
    });
  }

  public void buildMessage(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed,
      @NonNull TeamAddEvent event) {
    message.setContent(this.messageSource.getMessage(
        "github.team.added_to_repository",
        event.getRepository().getFullName()
    ));

    embed.setTitle(event.getTeam().getName());

    embed.addField(
        this.messageSource.getMessage("github.team.permission"),
        event.getTeam().getPermission().name().toLowerCase(),
        true
    );
    embed.addField(
        this.messageSource.getMessage("github.team.repository"),
        "[" + event.getRepository().getFullName() + "](" + event.getRepository().getBrowserUrl()
            .toExternalForm() + ")",
        true
    );
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void handlePayload(@NonNull Payload<?> payload) {
    MessageBuilder message = new MessageBuilder();
    EmbedBuilder embed = new EmbedBuilder();
    Event event = payload.getEvent();

    // if a color has been specified for this particular payload type, we'll add it as well (all
    // supported events currently have their own color value)
    Integer color = colorMap.get(payload.getType());
    if (color != null) {
      embed.setColor(color);
    }

    // if the event is user based, we'll add an author to the embed ion order to link them to their
    // respective action
    if (event instanceof AbstractUserTriggeredEvent) {
      User sender = ((AbstractUserTriggeredEvent) event).getSender();
      embed.setAuthor(sender.getLogin(), sender.getBrowserUrl().toExternalForm(),
          sender.getAvatarUrl().toExternalForm());
    }

    PayloadMessageBuilder builder = this.handlerMap.get(event.getClass());

    if (builder == null) {
      return;
    }

    builder.build(message, embed, event);
    message.setEmbed(embed.build());
    this.sendMessage(message.build());
  }

  /**
   * Sends a simple string message to all configured channels.
   *
   * @param message a message.
   */
  public void sendMessage(@NonNull String message) {
    this.configuration.getChannels()
        .forEach((c) -> this.guild.getTextChannelById(c).sendMessage(message).queue());
  }

  /**
   * Sends a message to all configured channels.
   *
   * @param message a message.
   */
  public void sendMessage(@NonNull net.dv8tion.jda.core.entities.Message message) {
    this.configuration.getChannels()
        .forEach((c) -> this.guild.getTextChannelById(c).sendMessage(message).queue());
  }

  /**
   * Sends an embed message to all configured channels.
   *
   * @param embed a message.
   */
  public void sendMessage(@NonNull MessageEmbed embed) {
    this.configuration.getChannels()
        .forEach((c) -> this.guild.getTextChannelById(c).sendMessage(embed).queue());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void sendMessage(@NonNull Message message) {
    this.sendMessage(message.toString((m) -> {
      String formatCharacter = "";

      switch (m.getStyle()) {
        case BOLD:
          formatCharacter = "**";
          break;
        case ITALICS:
          formatCharacter = "*";
          break;
        case UNDERLINE:
          formatCharacter = "__";
          break;
      }

      return formatCharacter + m.getText() + formatCharacter;
    }));
  }

  /**
   * Handles the construction of messages based on GitHub events.
   */
  @FunctionalInterface
  interface PayloadMessageBuilder {

    void build(@NonNull MessageBuilder message, @NonNull EmbedBuilder embed, @NonNull Event event);
  }
}
