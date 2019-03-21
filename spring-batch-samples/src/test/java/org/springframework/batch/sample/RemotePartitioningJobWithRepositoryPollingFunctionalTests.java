/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.batch.sample;

import org.springframework.batch.sample.config.JobRunnerConfiguration;
import org.springframework.batch.sample.remotepartitioning.polling.MasterConfiguration;
import org.springframework.batch.sample.remotepartitioning.polling.WorkerConfiguration;
import org.springframework.test.context.ContextConfiguration;

/**
 * The master step of the job under test will create 3 partitions for workers
 * to process.
 *
 * @author Mahmoud Ben Hassine
 */
@ContextConfiguration(classes = {JobRunnerConfiguration.class, MasterConfiguration.class})
public class RemotePartitioningJobWithRepositoryPollingFunctionalTests extends RemotePartitioningJobFunctionalTests {

	@Override
	protected Class<WorkerConfiguration> getWorkerConfigurationClass() {
		return WorkerConfiguration.class;
	}

}
