package com.stk123.repository;

import com.stk123.entity.StkDictionaryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StkDictionaryRepository extends JpaRepository<StkDictionaryEntity, StkDictionaryEntity.CompositeKey> {
}