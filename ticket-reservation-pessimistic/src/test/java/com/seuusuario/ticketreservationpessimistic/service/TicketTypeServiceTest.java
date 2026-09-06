package com.seuusuario.ticketreservationpessimistic.service;

import com.seuusuario.ticketreservationpessimistic.dto.CreateTicketTypeRequest;
import com.seuusuario.ticketreservationpessimistic.dto.TicketTypeResponse;
import com.seuusuario.ticketreservationpessimistic.exception.EventNotFoundException;
import com.seuusuario.ticketreservationpessimistic.model.Event;
import com.seuusuario.ticketreservationpessimistic.model.TicketType;
import com.seuusuario.ticketreservationpessimistic.repository.TicketTypeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketTypeServiceTest {

    @Mock
    private TicketTypeRepository ticketTypeRepository;

    @Mock
    private EventService eventService;

    @InjectMocks
    private TicketTypeService ticketTypeService;

    @Test
    @DisplayName("Deve definir availableQuantity igual a totalQuantity ao criar TicketType (RN-005)")
    void createTicketType_deveDefinirAvailableQuantityIgualATotalQuantity() {
        // 1. ARRANGE
        Long eventId = 1L;

        Event mockEvent = new Event();
        mockEvent.setId(eventId);
        mockEvent.setName("Show de Rock");

        CreateTicketTypeRequest request = new CreateTicketTypeRequest();
        request.setName("VIP");
        request.setPrice(new BigDecimal("150.00"));
        request.setTotalQuantity(100);

        when(eventService.findEntityById(eventId)).thenReturn(mockEvent);

        when(ticketTypeRepository.save(any(TicketType.class))).thenAnswer(invocation -> {
            TicketType ticketTypeParaSalvar = invocation.getArgument(0);
            ticketTypeParaSalvar.setId(10L);
            return ticketTypeParaSalvar;
        });

        // 2. ACT
        TicketTypeResponse response = ticketTypeService.createTicketType(eventId, request);

        // 3. ASSERT
        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals("VIP", response.getName());
        assertEquals(100, response.getTotalQuantity());
        assertEquals(100, response.getAvailableQuantity(), "A quantidade disponível deve iniciar exatamente igual à quantidade total");

        verify(eventService).findEntityById(eventId);
        verify(ticketTypeRepository).save(any(TicketType.class));
    }

    @Test
    @DisplayName("Deve lançar EventNotFoundException e não chamar save() quando evento não existir")
    void createTicketType_deveLancarExcecao_quandoEventoNaoExiste() {
        // 1. ARRANGE
        Long eventIdInexistente = 999L;

        CreateTicketTypeRequest request = new CreateTicketTypeRequest();
        request.setName("VIP");
        request.setPrice(new BigDecimal("150.00"));
        request.setTotalQuantity(100);

        when(eventService.findEntityById(eventIdInexistente))
                .thenThrow(new EventNotFoundException(eventIdInexistente));

        // 2. ACT & 3. ASSERT
        assertThrows(EventNotFoundException.class, () -> {
            ticketTypeService.createTicketType(eventIdInexistente, request);
        });

        verify(eventService).findEntityById(eventIdInexistente);
        verify(ticketTypeRepository, never()).save(any(TicketType.class));
    }
}