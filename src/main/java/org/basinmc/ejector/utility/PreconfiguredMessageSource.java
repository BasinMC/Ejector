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
package org.basinmc.ejector.utility;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.util.Locale;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.DelegatingMessageSource;

/**
 * Provides an extension to the standard message source which uses a statically configured locale
 * instead of relying on a passed value.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
public class PreconfiguredMessageSource extends DelegatingMessageSource {

  private final Locale locale;

  public PreconfiguredMessageSource() {
    this(Locale.getDefault());
  }

  public PreconfiguredMessageSource(@NonNull Locale locale) {
    this.locale = locale;
  }

  @NonNull
  public String getMessage(@NonNull String code, @NonNull Object... args)
      throws NoSuchMessageException {
    return this.getMessage(code, args, "??_" + code + "_??", this.locale);
  }
}
