package com._labor.fakecord.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com._labor.fakecord.domain.entity.Server;

public interface ServerRepository extends JpaRepository<Server, Long>{}