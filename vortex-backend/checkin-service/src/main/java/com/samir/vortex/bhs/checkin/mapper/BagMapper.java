package com.samir.vortex.bhs.checkin.mapper;

import com.samir.vortex.bhs.checkin.dto.BagCheckInRequest;
import com.samir.vortex.bhs.checkin.dto.BagCheckInResponse;
import com.samir.vortex.bhs.checkin.model.Bag;
import com.samir.vortex.bhs.checkin.model.BagStatus;
import org.springframework.stereotype.Component;

@Component
public class BagMapper {

    public Bag toEntity(BagCheckInRequest request) {
        if (request == null) {
            return null;
        }
        return Bag.builder()
                .bagTag(request.bagTag())
                .flightNumber(request.flightNumber())
                .status(BagStatus.RECEIVED)
                .build();
    }

    public BagCheckInResponse toResponse(Bag bag) {
        if (bag == null) {
            return null;
        }
        return BagCheckInResponse.builder()
                .id(bag.getId())
                .bagTag(bag.getBagTag())
                .flightNumber(bag.getFlightNumber())
                .status(bag.getStatus())
                .createdAt(bag.getCreatedAt())
                .build();
    }
}