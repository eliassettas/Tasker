package com.example.tasker.repository;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.tasker.model.message.ChatMessage;

@Repository
public interface ChatMessageRepository extends MongoRepository<ChatMessage, String> {

    List<ChatMessage> findTop20ByOrderByIdDesc();

    List<ChatMessage> findTop20ByIdIsLessThanOrderByIdDesc(ObjectId id);
}
