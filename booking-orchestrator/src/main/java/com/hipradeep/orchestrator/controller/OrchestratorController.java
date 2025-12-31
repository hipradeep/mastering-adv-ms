package com.hipradeep.orchestrator.controller;

import com.hipradeep.orchestrator.service.BookingOrchestratorService;
import com.hipradeep.saga.commons.dto.BookingRequest;
import com.hipradeep.saga.commons.dto.BookingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orchestrator")
@RequiredArgsConstructor
public class OrchestratorController {

    private final BookingOrchestratorService orchestratorService;

    @PostMapping("/booking")
    public BookingResponse createBooking(@RequestBody BookingRequest request) {
        return orchestratorService.createBooking(request);
    }
}
