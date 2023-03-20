package com.example.demo.persistence;

import static java.lang.String.join;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.domain.PageRequest;

@DataMongoTest
@TestMethodOrder(OrderAnnotation.class)
class OutboxTasksRepositoryShould {

	@Autowired
	OutboxTaskRepository taskRepository;

	@BeforeEach
	void beforeEach() {
		taskRepository.deleteAll();
	}
	
	@Test
	void storeCorrectly() {
		taskRepository.saveAll(List.of(task("A"), task("B"), task("C")));
		assertThat(taskRepository.findAll().size()).isEqualTo(3);
	}
	
	@Test
	void getFirstLocableCorrectly() {
		taskRepository.saveAll(List.of(task("A"), task("B"), task("C")));

		PageRequest request = PageRequest.of(0, 1);
		final OutboxTask found = taskRepository.findFirstLocable(request).getContent().stream().findFirst().orElseThrow();
		assertThat(found).isNotNull();
		assertThat(found.data).isEqualTo("data-A");
	}

	@Test
	void resolveNullWhenTasksDontExist() {	
		assertThat(taskRepository.count()).isEqualTo(0);
		
		OutboxTask resolved = taskRepository.resolveFirstApplicable();	
		assertThat(resolved).isNull();
	}

	@Test
	void notResolveWhenAllTasksLocked() {	
		// NOTE A is locked and followd by A again (now excluded), B is expected
		taskRepository.saveAll(List.of(locked("A"), locked("B"));
		OutboxTask resolved = taskRepository.resolveFirstApplicable();	
		assertThat(resolved).isNull();
	}

	@Test
	void resolveFirstApplicableCorrectly() {	
		// NOTE A is locked and followd by A again (now excluded), B is expected
		taskRepository.saveAll(List.of(locked("A"), task("A"), task("B"), locked("B")));
		// List<OutboxTask> list = taskRepository.resolveApplicable();
		// OutboxTask resolved = list.get(0);
		OutboxTask resolved = taskRepository.resolveFirstApplicable();	
		assertThat(resolved).isNull();

		assertThat(resolved).isNotNull();
		assertThat(resolved.data).isEqualTo("data-B");
		assertThat(resolved.reference).isEqualTo("B");
	}


	private OutboxTask locked(String sample) {
		return task(sample, true);
	}
	
	private OutboxTask task(String sample) {
		return task(sample, false);
	}

	private OutboxTask task(String sample, boolean locked) {
		final OutboxTask result = new OutboxTask();
		result.reference = sample;
		result.processor = join("-", "processor", sample);
		if (locked) {
			result.host = join("-", "host", sample);
		}
		result.data = join("-", "data", sample);
		return result;
	}
	
}
