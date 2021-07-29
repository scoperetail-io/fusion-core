package com.scoperetail.fusion.core.adapter.out.messaging.mail;

/*-
 * *****
 * fusion-core
 * -----
 * Copyright (C) 2018 - 2021 Scope Retail Systems Inc.
 * -----
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * =====
 */

import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.scoperetail.fusion.core.application.port.out.mail.MailDetailsDto;
import com.scoperetail.fusion.core.application.port.out.mail.PosterOutboundMailPort;
import com.scoperetail.fusion.messaging.config.MailHost;
import com.scoperetail.fusion.messaging.config.Smtp;
import com.scoperetail.fusion.shared.kernel.common.annotation.MessagingAdapter;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@MessagingAdapter
@AllArgsConstructor
@Slf4j
public class PosterOutboundMailAdapter implements PosterOutboundMailPort {

  @Override
  public void post(final MailDetailsDto mailDetailsDto) {
    final JavaMailSender mailSender = getJavaMailSender(mailDetailsDto.getMailHost());
    try {
      final MimeMessage mimeMessage = mailSender.createMimeMessage();
      createMimeMessage(
          mailDetailsDto.getFrom(),
          mailDetailsDto.getTo(),
          mailDetailsDto.getReplyTo(),
          mailDetailsDto.getSubject(),
          mailDetailsDto.getBody(),
          mimeMessage);
      mailSender.send(mimeMessage);
    } catch (final MessagingException e) {
      log.error("Message occured while sending mail: {}", e);
    }
  }

  private JavaMailSender getJavaMailSender(final MailHost mailHost) {
    final JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
    mailSender.setHost(mailHost.getHostUrl());
    mailSender.setPort(mailHost.getPort());

    mailSender.setUsername(mailHost.getUsername());
    mailSender.setPassword(mailHost.getPassword());

    final Properties props = mailSender.getJavaMailProperties();
    final Smtp smtp = mailHost.getSmtp();
    props.put("mail.transport.protocol", mailHost.getTransportProtocol());
    props.put("mail.smtp.auth", smtp.isAuth());
    props.put("mail.smtp.starttls.enable", smtp.isStartTtlsEnabled());
    props.put("mail.debug", mailHost.isDebugEnabled());

    return mailSender;
  }

  private void createMimeMessage(
      final String from,
      final String to,
      final String replyTo,
      final String subject,
      final String text,
      final MimeMessage mimeMessage)
      throws MessagingException {
    final MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
    helper.setFrom(from);
    helper.setTo(to);
    helper.setReplyTo(replyTo);
    helper.setSubject(subject);
    final MimeBodyPart textPart = new MimeBodyPart();
    textPart.setContent(text, "text/html");

    final Multipart mp = helper.getMimeMultipart();
    mp.addBodyPart(textPart);
  }
}
