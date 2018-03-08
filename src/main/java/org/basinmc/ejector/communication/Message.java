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
package org.basinmc.ejector.communication;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represents a message
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class Message {

  private final Color color;
  private final Style style;
  private final String text;
  private final Message next;

  private Message(@NonNull Color color, @NonNull Style style, @NonNull String text,
      @Nullable Message next) {
    this.color = color;
    this.style = style;
    this.text = text;
    this.next = next;
  }

  /**
   * Traverses every single element within the message.
   *
   * @param consumer a consumer.
   */
  public void accept(@NonNull Consumer<Message> consumer) {
    consumer.accept(this);

    if (this.next != null) {
      this.next.accept(consumer);
    }
  }

  /**
   * Creates a new empty factory for message instances.
   *
   * @return a builder.
   */
  @NonNull
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Retrieves the selected color for this message.
   *
   * @return a color.
   */
  @NonNull
  public Color getColor() {
    return this.color;
  }

  /**
   * Retrieves the selected style for this message.
   *
   * @return a style.
   */
  @NonNull
  public Style getStyle() {
    return this.style;
  }

  /**
   * Retrieves the text content for this message.
   *
   * @return a message content.
   */
  @NonNull
  public String getText() {
    return this.text;
  }

  /**
   * Retrieves the upcoming message element.
   *
   * @return a message or, if none is specified, an empty optional.
   */
  @NonNull
  public Optional<Message> getNext() {
    return Optional.ofNullable(this.next);
  }

  /**
   * Converts the message into a string using a custom conversion method.
   *
   * @param converter a conversion method.
   * @return a string.
   */
  @NonNull
  public String toString(@NonNull Function<Message, String> converter) {
    StringBuilder builder = new StringBuilder();

    this.accept((m) -> {
      if (builder.length() != 0) {
        builder.append(" ");
      }

      builder.append(converter.apply(m));
    });

    return builder.toString();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Message)) {
      return false;
    }
    Message message = (Message) o;
    return this.color == message.color &&
        this.style == message.style &&
        Objects.equals(this.text, message.text) &&
        Objects.equals(this.next, message.next);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode() {
    return Objects.hash(this.color, this.style, this.text, this.next);
  }

  /**
   * Provides a factory for message instances.
   */
  public static final class Builder {

    private final Builder parent;

    private Color color = Color.NONE;
    private Style style = Style.NORMAL;
    private final StringBuilder text = new StringBuilder();

    private Builder() {
      this.parent = null;
    }

    private Builder(@Nullable Builder parent) {
      this.parent = parent;
    }

    /**
     * Builds the entire chain of messages and returns the first element within the chain.
     *
     * @return a message.
     */
    @NonNull
    public Message build() {
      return this.build(null);
    }

    /**
     * Builds the entire chain of messages and returns the topmost element.
     *
     * @param child a child message.
     * @return a message.
     */
    @NonNull
    private Message build(@Nullable Message child) {
      Message message = new Message(this.color, this.style, this.text.toString(), child);

      if (this.parent != null) {
        return this.parent.build(message);
      }

      return message;
    }

    /**
     * Selects a color for the upcoming text.
     *
     * @param color a color.
     * @return a builder (or a reference to this builder if no text has been specified yet).
     */
    @NonNull
    public Builder withColor(@NonNull Color color) {
      if (this.text.length() != 0) {
        return new Builder(this)
            .withColor(color);
      }

      this.color = color;
      return this;
    }

    /**
     * Selects a style for the upcoming text.
     *
     * @param style a style.
     * @return a builder (or a reference to this builder if no text has been specified yet).
     */
    @NonNull
    public Builder withStyle(@NonNull Style style) {
      if (this.text.length() != 0) {
        return new Builder(this)
            .withStyle(style);
      }

      this.style = style;
      return this;
    }

    /**
     * Appends a text element to this builder.
     *
     * @param text a text element.
     * @return a reference to this builder.
     */
    @NonNull
    public Builder withText(@NonNull String text) {
      this.text.append(text);
      return this;
    }
  }

  /**
   * Provides a list of valid message colors.
   */
  public enum Color {
    BLACK,
    BLUE,
    BROWN,
    CYAN,
    DARK_BLUE,
    DARK_GREEN,
    DARK_GRAY,
    GREEN,
    LIGHT_GRAY,
    MAGENTA,
    NONE,
    OLIVE,
    PURPLE,
    RED,
    TEAL,
    WHITE,
    YELLOW
  }

  /**
   * Provides a list of valid message styles.
   */
  public enum Style {
    BOLD,
    ITALICS,
    NORMAL,
    UNDERLINE
  }
}
