package com.example.demo.persistence;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface OutboxTaskRepository extends MongoRepository<OutboxTask, ObjectId> {

	@Query(value = "{ 'host' : null }", sort = "{createdOn:1}")
	Page<OutboxTask> findFirstLocable(Pageable pageable);

	@Aggregation({
		"""
		{
			$lookup: {
				from: 'outboxTask',
				as: 'locked',
				pipeline: [
					{
						$match: { host: { $ne: null } }
					},
					{
						$group: {
						_id: null,
						references: {
							$addToSet: '$reference'
						}
						}
					},
					{
						$project: {
							references: true
						}
					}
				]
			}
		}					  
		""",
		"""
			{
				$addFields: {
				  excluded: {
					$arrayElemAt: ['$locked.references', 0]
				  }
				}
			  }							
		""",
		"""
		{
			$match: {
				host: null,
				$expr: {
					$not: {
						$in: [
							'$reference',
							'$excluded',
						],
					},
				},
			},
			}
		""",
		"{ $limit: 1 }"
	}) 
	OutboxTask resolveFirstApplicable();
}
