/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.item.mail;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.AdditionalMatchers.aryEq;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicReference;

import javax.mail.MessagingException;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mail.MailException;
import org.springframework.mail.MailMessage;
import org.springframework.mail.MailSendException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

/**
 * @author Dave Syer
 * @author Will Schipp
 * 
 * @since 2.1
 * 
 */
public class SimpleMailMessageItemWriterTests {

	private SimpleMailMessageItemWriter writer = new SimpleMailMessageItemWriter();

	private MailSender mailSender = mock(MailSender.class);

	@Before
	public void setUp() {
		writer.setMailSender(mailSender);
	}

	@Test
	public void testSend() throws Exception {

		SimpleMailMessage foo = new SimpleMailMessage();
		SimpleMailMessage bar = new SimpleMailMessage();
		SimpleMailMessage[] items = new SimpleMailMessage[] { foo, bar };

		mailSender.send(aryEq(items));

		writer.write(Arrays.asList(items));


	}

	@Test(expected = MailSendException.class)
	public void testDefaultErrorHandler() throws Exception {

		SimpleMailMessage foo = new SimpleMailMessage();
		SimpleMailMessage bar = new SimpleMailMessage();
		SimpleMailMessage[] items = new SimpleMailMessage[] { foo, bar };

		mailSender.send(aryEq(items));
		when(mailSender).thenThrow(new MailSendException(Collections.singletonMap((Object)foo, (Exception)new MessagingException("FOO"))));

		writer.write(Arrays.asList(items));

	}

	@Test
	public void testCustomErrorHandler() throws Exception {

		final AtomicReference<String> content = new AtomicReference<String>();
		writer.setMailErrorHandler(new MailErrorHandler() {
            @Override
			public void handle(MailMessage message, Exception exception) throws MailException {
				content.set(exception.getMessage());
			}
		});

		SimpleMailMessage foo = new SimpleMailMessage();
		SimpleMailMessage bar = new SimpleMailMessage();
		SimpleMailMessage[] items = new SimpleMailMessage[] { foo, bar };

		mailSender.send(aryEq(items));
		when(mailSender).thenThrow(new MailSendException(Collections.singletonMap((Object)foo, (Exception)new MessagingException("FOO"))));

		writer.write(Arrays.asList(items));

		assertEquals("FOO", content.get());


	}

}
