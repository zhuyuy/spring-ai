/*
 * Copyright 2024-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ai.chat.client.advisor;

import java.util.List;

import org.springframework.ai.chat.client.advisor.api.AdvisedRequest;
import org.springframework.ai.chat.client.advisor.api.AdvisedResponse;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.CallAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAroundAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.util.CollectionUtils;

import reactor.core.publisher.Flux;

/**
 * A {@link CallAroundAdvisor} and {@link StreamAroundAdvisor} that filters out the
 * response if the user input contains any of the sensitive words.
 *
 * @author Christian Tzolov
 * @since 1.0.0
 */
public class SafeGuardAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

	private final List<String> sensitiveWords;

	public SafeGuardAdvisor(List<String> sensitiveWords) {
		this.sensitiveWords = sensitiveWords;
	}

	public String getName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {

		if (!CollectionUtils.isEmpty(this.sensitiveWords)
				&& sensitiveWords.stream().anyMatch(w -> advisedRequest.userText().contains(w))) {
			return new AdvisedResponse(ChatResponse.builder().withGenerations(List.of()).build(),
					advisedRequest.adviseContext());
		}

		return chain.nextAroundCall(advisedRequest);
	}

	@Override
	public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {

		if (!CollectionUtils.isEmpty(this.sensitiveWords)
				&& sensitiveWords.stream().anyMatch(w -> advisedRequest.userText().contains(w))) {
			return Flux.empty();
		}

		return chain.nextAroundStream(advisedRequest);

	}

	@Override
	public int getOrder() {
		return 0;
	}

}