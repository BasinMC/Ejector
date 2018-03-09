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
package org.basinmc.ejector.controller;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdviceAdapter;

/**
 * Provides a controller advice which automatically verifies the payload signature when a secret has
 * been given to the context.
 *
 * @author <a href="mailto:johannesd@torchmind.com">Johannes Donath</a>
 */
@ConditionalOnProperty("ejector.github.secret")
@ControllerAdvice(assignableTypes = GitHubController.class)
public class GitHubAuthenticationAdvice extends RequestBodyAdviceAdapter {

  private final String secret;

  public GitHubAuthenticationAdvice(@NonNull @Value("${ejector.github.secret}") String secret) {
    this.secret = secret;
  }

  /**
   * {@inheritDoc}
   */
  @NonNull
  @Override
  public Object afterBodyRead(@NonNull Object body, @NonNull HttpInputMessage inputMessage,
      @NonNull MethodParameter parameter, @NonNull Type targetType,
      @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
    // before we're verifying anything, we'll evaluate whether a signature was placed at all
    // and if not we'll simply deny access in its entirety
    String signature = inputMessage.getHeaders().getFirst("X-Hub-Signature");

    if (signature == null) {
      throw new AuthenticationException("Lacking WebHook signature");
    }

    // next up, we'll figure out which message digest algorithm is being used to sign the request
    // (typically GitHub will use sha1, however, we're future proofing a little bit here)
    int separatorIndex = signature.indexOf('=');

    if (separatorIndex == -1) {
      throw new AuthenticationException("Malformed WebHook signature: No algorithm prefix");
    }

    String algorithm = signature.substring(0, separatorIndex).toLowerCase();
    signature = signature.substring(separatorIndex + 1);

    switch (algorithm) {
      case "sha1":
        algorithm = "HmacSHA1";
        break;
      case "sha256":
        algorithm = "HmacSHA256";
        break;
      default:
        throw new AuthenticationException(
            "Malformed WebHook signature: Unsupported signature algorithm \"" + algorithm + "\"");
    }

    // since we have now acquired the signature value and algorithm, we can now construct a new
    // Mac instance, initialize it with the secret key and verify the signature
    try {
      Mac mac = Mac.getInstance(algorithm);
      mac.init(new SecretKeySpec(this.secret.getBytes(StandardCharsets.UTF_8), algorithm));

      byte[] expectedSignature = mac.doFinal(((String) body).getBytes(StandardCharsets.UTF_8));
      byte[] actualSignature = Hex.decodeHex(signature);

      if (!Arrays.equals(expectedSignature, actualSignature)) {
        throw new AuthenticationException("Malformed WebHook signature: Mismatch");
      }
    } catch (NoSuchAlgorithmException ex) {
      throw new AuthenticationException("Authentication is unavailable: " + ex.getMessage(), ex);
    } catch (InvalidKeyException ex) {
      throw new AuthenticationException("Illegal secret: " + ex.getMessage(), ex);
    } catch (DecoderException ex) {
      throw new AuthenticationException("Failed to read request body: " + ex.getMessage(), ex);
    }

    return body;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean supports(@NonNull MethodParameter methodParameter, @NonNull Type targetType,
      @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
    return true;
  }

  /**
   * Provides an exception which notifies the caller of the lack of valid authentication details
   * within the request.
   */
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public static class AuthenticationException extends RuntimeException {

    public AuthenticationException() {
    }

    public AuthenticationException(String message) {
      super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
      super(message, cause);
    }

    public AuthenticationException(Throwable cause) {
      super(cause);
    }
  }
}
