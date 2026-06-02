package com.hipradeep.gatewayservice.repository;

import com.hipradeep.gatewayservice.model.GatewayLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GatewayLogRepository extends JpaRepository<GatewayLog, Long> {
}
