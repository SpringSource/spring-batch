package org.springframework.batch.integration.chunk;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class TestItemWriter<T> implements ItemWriter<T> {

	private static final Log logger = LogFactory.getLog(TestItemWriter.class);

	/**
	 * Counts the number of chunks processed in the handler.
	 */
	public volatile static int count = 0;

	/**
	 * Item that causes failure in handler.
	 */
	public final static String FAIL_ON = "bad";

	/**
	 * Item that causes handler to wait to simulate delayed processing.
	 */
	public static final String WAIT_ON = "wait";

	public void write(List<? extends T> items) throws Exception {

		for (T item : items) {

			count++;

			logger.debug("Writing: " + item);

			if (item.equals(WAIT_ON)) {
				try {
					Thread.sleep(200);
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException("Unexpected interruption.", e);
				}
			}

			if (item.equals(FAIL_ON)) {
				throw new IllegalStateException("Planned failure on: " + FAIL_ON);
			}

		}

	}

}
