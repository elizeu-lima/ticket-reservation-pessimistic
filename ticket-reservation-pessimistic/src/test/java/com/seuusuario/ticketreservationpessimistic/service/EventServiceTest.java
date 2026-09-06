package com.seuusuario.ticketreservationpessimistic.service;

import com.seuusuario.ticketreservationpessimistic.dto.CreateEventRequest;
import com.seuusuario.ticketreservationpessimistic.dto.EventResponse;
import com.seuusuario.ticketreservationpessimistic.exception.EventNotFoundException;
import com.seuusuario.ticketreservationpessimistic.model.Event;
import com.seuusuario.ticketreservationpessimistic.repository.EventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    @DisplayName("Deve criar um evento com sucesso quando o request for válido")
    void createEvent_deveRetornarEventResponse_quandoRequestValido() {
        CreateEventRequest request = new CreateEventRequest();
        request.setName("Conferência Tech");
        request.setDate(OffsetDateTime.now().plusDays(10));

        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event eventEnviado = invocation.getArgument(0);
            eventEnviado.setId(1L);
            return eventEnviado;
        });

        EventResponse response = eventService.createEvent(request);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Conferência Tech", response.getName());
        assertEquals(request.getDate(), response.getDate());

        verify(eventRepository).save(any(Event.class));
    }

    @Test
    @DisplayName("Deve retornar a entidade Event quando o ID existir")
    void findEntityById_deveRetornarEvent_quandoIdExiste() {
        Long eventId = 1L;
        Event mockEvent = new Event();
        mockEvent.setId(eventId);
        mockEvent.setName("Festival de Verão");
        mockEvent.setDate(OffsetDateTime.now().plusDays(30));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(mockEvent));

        Event eventRetornado = eventService.findEntityById(eventId);

        assertNotNull(eventRetornado);
        assertEquals(eventId, eventRetornado.getId());
        assertEquals("Festival de Verão", eventRetornado.getName());

        verify(eventRepository).findById(eventId);
    }

    @Test
    @DisplayName("Deve lançar EventNotFoundException quando o ID do evento não existir no repositório")
    void findEntityById_deveLancarExcecao_quandoIdNaoExiste() {
        Long idInexistente = 999L;

        when(eventRepository.findById(idInexistente)).thenReturn(Optional.empty());

        assertThrows(EventNotFoundException.class, () -> {
            eventService.findEntityById(idInexistente);
        });

        verify(eventRepository).findById(idInexistente);
    }
}