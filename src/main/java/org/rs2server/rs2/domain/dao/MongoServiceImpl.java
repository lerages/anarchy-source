package org.rs2server.rs2.domain.dao;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.WriteConcern;

/**
 * MongoService implementation.
 */
public class MongoServiceImpl implements MongoService {

	private DB database;

	@Inject
	public MongoServiceImpl() {
		final MongoClient mongoClient = new MongoClient(ImmutableList.of(new ServerAddress("127.0.0.1", 27017)));
		mongoClient.setWriteConcern(WriteConcern.ACKNOWLEDGED);
		database = mongoClient.getDB("lostisle");
	}

	@Override
	public DB getDatabase() {
		return database;
	}
}
