package com.seuusuario.ticketreservationpessimistic.controller;

import com.seuusuario.ticketreservationpessimistic.dto.CreateTicketTypeRequest;
import com.seuusuario.ticketreservationpessimistic.dto.TicketTypeResponse;
import com.seuusuario.ticketreservationpessimistic.service.TicketTypeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/events/{eventId}/ticket-types")
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;

    public TicketTypeController(TicketTypeService ticketTypeService) {
        this.ticketTypeService = ticketTypeService;
    }

    @PostMapping
    public ResponseEntity<TicketTypeResponse> createTicketType(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateTicketTypeRequest request) {

        TicketTypeResponse response = ticketTypeService.createTicketType(eventId, request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TicketTypeResponse>> findByEventId(@PathVariable Long eventId) {
        List<TicketTypeResponse> responses = ticketTypeService.findByEventId(eventId);
        return ResponseEntity.ok(responses);
    }
}