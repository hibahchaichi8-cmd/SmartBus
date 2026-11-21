package tn.smartbus.transport.dto;

import tn.smartbus.transport.entity.RouteStop;

public record RouteResponse(String id, String code, String name, java.util.List<RouteStop> stops) {}
