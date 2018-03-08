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
package org.basinmc.ejector;import edu.umd.cs.findbugs.annotations.NonNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * <p>Provides an entry point configuration which introduces the application to Spring and scans for
 * further components.</p>
 *
 * <p>Note that this class also contains a JVM entry point for the purposes of debugging. This entry
 * point is, however, replaced with a loader implementation by the Spring Boot maven plugin at
 * runtime.</p>
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
@SpringBootApplication
public class EjectorApplication {

  /**
   * JVM Entry Point
   *
   * @param arguments an array of command line arguments.
   */
  public static void main(@NonNull String[] arguments) {
    SpringApplication.run(EjectorApplication.class);
  }
}
