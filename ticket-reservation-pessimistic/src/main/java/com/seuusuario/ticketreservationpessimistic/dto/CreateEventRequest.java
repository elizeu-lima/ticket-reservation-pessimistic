package com.seuusuario.ticketreservationpessimistic.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public class CreateEventRequest {
    @NotBlank(message = "O nome do evento é obrigatório")
    private String name;

    @NotNull(message = "A data do evento é obrigatória")
    @Future(message = "A data do evento deve ser futura (RN-004)")
    private OffsetDateTime date;

    public CreateEventRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public void setDate(OffsetDateTime date) {
        this.date = date;
    }
}
