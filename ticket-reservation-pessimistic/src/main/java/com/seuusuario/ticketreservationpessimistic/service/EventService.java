package com.seuusuario.ticketreservationpessimistic.service;

import com.seuusuario.ticketreservationpessimistic.dto.CreateEventRequest;
import com.seuusuario.ticketreservationpessimistic.dto.EventResponse;
import com.seuusuario.ticketreservationpessimistic.exception.EventNotFoundException;
import com.seuusuario.ticketreservationpessimistic.model.Event;
import com.seuusuario.ticketreservationpessimistic.repository.EventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional
    public EventResponse createEvent(CreateEventRequest request) {
        Event event = new Event();
        event.setName(request.getName());
        event.setDate(request.getDate());

        Event savedEvent = eventRepository.save(event);
        return EventResponse.fromEntity(savedEvent);
    }

    @Transactional(readOnly = true)
    public Event findEntityById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
    }
}