package com.samir.vortex.bhs.checkin.controller;

import com.samir.vortex.bhs.checkin.dto.BagCheckInRequest;
import com.samir.vortex.bhs.checkin.dto.BagCheckInResponse;
import com.samir.vortex.bhs.checkin.service.CheckInService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/bags")
@RequiredArgsConstructor
public class CheckInController {

    private final CheckInService checkInService;

    @PostMapping("/checkin")
    public ResponseEntity<BagCheckInResponse> checkInBag(@Valid @RequestBody BagCheckInRequest bagCheckInRequest) {
        BagCheckInResponse savedBag = checkInService.processCheckIn(bagCheckInRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBag);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BagCheckInResponse> getBagById(@PathVariable UUID id) {
        return ResponseEntity.ok(checkInService.getBagById(id));
    }

    @GetMapping
    public ResponseEntity<Page<BagCheckInResponse>> getAllBags(Pageable pageable) {
        return ResponseEntity.ok(checkInService.getAllBags(pageable));
    }
}