package com.seuusuario.ticketreservationpessimistic.dto;

import com.seuusuario.ticketreservationpessimistic.model.TicketType;
import java.math.BigDecimal;

public class TicketTypeResponse {
    private Long id;
    private Long eventId;
    private String name;
    private BigDecimal price;
    private Integer totalQuantity;
    private Integer availableQuantity;

    public TicketTypeResponse() {
    }

    public TicketTypeResponse(Long id, Long eventId, String name, BigDecimal price, Integer totalQuantity, Integer availableQuantity) {
        this.id = id;
        this.eventId = eventId;
        this.name = name;
        this.price = price;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = availableQuantity;
    }


    public static TicketTypeResponse fromEntity(TicketType ticketType) {
        return new TicketTypeResponse(
                ticketType.getId(),
                ticketType.getEvent().getId(),
                ticketType.getName(),
                ticketType.getPrice(),
                ticketType.getTotalQuantity(),
                ticketType.getAvailableQuantity()
        );
    }

    public Long getId() {
        return id;
    }

    public Long getEventId() {
        return eventId;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }
}
