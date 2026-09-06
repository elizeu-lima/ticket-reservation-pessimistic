package com.seuusuario.ticketreservationpessimistic.service;

import com.seuusuario.ticketreservationpessimistic.dto.CreateTicketTypeRequest;
import com.seuusuario.ticketreservationpessimistic.dto.TicketTypeResponse;
import com.seuusuario.ticketreservationpessimistic.model.Event;
import com.seuusuario.ticketreservationpessimistic.model.TicketType;
import com.seuusuario.ticketreservationpessimistic.repository.TicketTypeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TicketTypeService {

    private final TicketTypeRepository ticketTypeRepository;
    private final EventService eventService;

    public TicketTypeService(TicketTypeRepository ticketTypeRepository, EventService eventService) {
        this.ticketTypeRepository = ticketTypeRepository;
        this.eventService = eventService;
    }

    @Transactional
    public TicketTypeResponse createTicketType(Long eventId, CreateTicketTypeRequest request) {
        // 1. Busca o evento associado pelo ID vindo da URL (lança EventNotFoundException caso não exista)
        Event event = eventService.findEntityById(eventId);

        // 2. Mapeia DTO para Entidade
        TicketType ticketType = new TicketType();
        ticketType.setName(request.getName());
        ticketType.setPrice(request.getPrice());
        ticketType.setTotalQuantity(request.getTotalQuantity());

        // 3. RN-005: quantidade_disponivel inicia igual a quantidade_total
        ticketType.setAvailableQuantity(request.getTotalQuantity());

        // 4. Associa ao evento e salva
        ticketType.setEvent(event);
        TicketType savedTicketType = ticketTypeRepository.save(ticketType);

        return TicketTypeResponse.fromEntity(savedTicketType);
    }

    @Transactional(readOnly = true)
    public List<TicketTypeResponse> findByEventId(Long eventId) {
        // Valida se o evento existe antes de buscar os ingressos
        eventService.findEntityById(eventId);

        return ticketTypeRepository.findByEventId(eventId)
                .stream()
                .map(TicketTypeResponse::fromEntity)
                .toList();
    }
}