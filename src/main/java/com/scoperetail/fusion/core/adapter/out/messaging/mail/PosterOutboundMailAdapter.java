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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Properties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import com.scoperetail.fusion.core.application.port.out.mail.PosterOutboundMailPort;
import com.scoperetail.fusion.messaging.config.MailHost;
import com.scoperetail.fusion.messaging.config.Smtp;
import com.scoperetail.fusion.shared.kernel.common.annotation.MessagingAdapter;
import lombok.AllArgsConstructor;

@MessagingAdapter
@AllArgsConstructor
public class PosterOutboundMailAdapter implements PosterOutboundMailPort {


  @Override
  public void post(final MailHost mailHost, final String from, final String to, final String cc,
      final String bcc, final String replyTo, final String subject, final String text,
      final String sentDate) {
    final JavaMailSender mailSender = getJavaMailSender(mailHost);
    final SimpleMailMessage simpleMailMessage =
        getSimpleMessage(from, to, cc, bcc, replyTo, subject, text, sentDate);
    mailSender.send(simpleMailMessage);
  }

  private SimpleMailMessage getSimpleMessage(final String from, final String to, final String cc,
      final String bcc, final String replyTo, final String subject, final String text,
      final String sendDate) {
    final SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
    simpleMailMessage.setFrom(from);
    simpleMailMessage.setTo(to);
    simpleMailMessage.setCc(cc);
    simpleMailMessage.setBcc(bcc);
    simpleMailMessage.setReplyTo(replyTo);
    simpleMailMessage.setSubject(subject);
    simpleMailMessage.setText(text);
    Date sDate;
    try {
      sDate = new SimpleDateFormat("dd/MM/yyyy").parse(sendDate);
    } catch (final ParseException e) {
      sDate = Date.from(Instant.now());
    }
    simpleMailMessage.setSentDate(sDate);
    return simpleMailMessage;
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
}
