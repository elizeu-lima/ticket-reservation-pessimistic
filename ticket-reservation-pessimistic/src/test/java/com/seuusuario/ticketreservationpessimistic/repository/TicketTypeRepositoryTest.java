package com.seuusuario.ticketreservationpessimistic.repository;

import com.seuusuario.ticketreservationpessimistic.model.Event;
import com.seuusuario.ticketreservationpessimistic.model.TicketType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TicketTypeRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TicketTypeRepository ticketTypeRepository;

    @Autowired
    private EventRepository eventRepository;

    @Test
    @DisplayName("Deve violar a CHECK constraint do Postgres ao tentar salvar availableQuantity maior que totalQuantity")
    void deveLancarExcecao_quandoAvailableQuantityMaiorQueTotalQuantity() {
        // 1. ARRANGE
        Event event = new Event();
        event.setName("Evento Teste Constraint");
        event.setDate(OffsetDateTime.now().plusDays(5));
        Event savedEvent = eventRepository.saveAndFlush(event);

        TicketType invalidTicketType = new TicketType();
        invalidTicketType.setName("Pista Invalida");
        invalidTicketType.setPrice(new BigDecimal("100.00"));
        invalidTicketType.setTotalQuantity(10);
        invalidTicketType.setAvailableQuantity(20); // VIOLAÇÃO PROPOSITAL: 20 > 10
        invalidTicketType.setEvent(savedEvent);

        // 2. ACT & 3. ASSERT
        assertThrows(DataIntegrityViolationException.class, () -> {
            ticketTypeRepository.saveAndFlush(invalidTicketType);
        });
    }
}