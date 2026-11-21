package tn.smartbus.trip.mapper;

import org.springframework.stereotype.Component;

import tn.smartbus.trip.dto.TripIncidentResponse;
import tn.smartbus.trip.entity.TripIncident;

@Component
public class TripIncidentMapper {

    public TripIncidentResponse toResponse(TripIncident incident) {
        return new TripIncidentResponse(
                incident.id(),
                incident.type(),
                incident.delayMinutes(),
                incident.reportedAt()
        );
    }
}