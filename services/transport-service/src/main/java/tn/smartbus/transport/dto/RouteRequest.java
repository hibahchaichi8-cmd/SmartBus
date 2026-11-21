package tn.smartbus.transport.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import tn.smartbus.transport.entity.RouteStop;

public record RouteRequest(
    @NotBlank @Size(max = 20) String code,
    @NotBlank @Size(max = 100) String name,
    @NotNull @Size(min = 2, max = 50) java.util.List<@Valid RouteStop> stops) {}
