package com.ipcms.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ReserveUpdateRequest {

    @NotNull
    @DecimalMin(value = "0.00", message = "Gross reserve must be non-negative")
    private BigDecimal newReserve;

    private String justificationNotes;

    public ReserveUpdateRequest() {}

    public ReserveUpdateRequest(BigDecimal newReserve, String justificationNotes) {
        this.newReserve = newReserve;
        this.justificationNotes = justificationNotes;
    }

    public BigDecimal getNewReserve() { return newReserve; }
    public void setNewReserve(BigDecimal newReserve) { this.newReserve = newReserve; }

    public String getJustificationNotes() { return justificationNotes; }
    public void setJustificationNotes(String justificationNotes) { this.justificationNotes = justificationNotes; }
}
