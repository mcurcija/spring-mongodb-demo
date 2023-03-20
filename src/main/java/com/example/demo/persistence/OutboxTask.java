package com.example.demo.persistence;

import java.time.Instant;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;

public class OutboxTask {

	@Id
	private ObjectId id;

	@CreatedDate
	public Instant createdOn;

	@Indexed
	public String reference;

	public String host;

	public String processor;

	public String data;

	
}
