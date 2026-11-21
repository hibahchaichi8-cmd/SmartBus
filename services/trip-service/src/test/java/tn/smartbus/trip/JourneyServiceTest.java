package tn.smartbus.trip;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import tn.smartbus.trip.dto.CatalogDtos.*;
import tn.smartbus.trip.entity.Schedule;
import tn.smartbus.trip.repository.ScheduleRepository;
import tn.smartbus.trip.service.*;

class JourneyServiceTest {
  @Test
  void boardingOffsetAndLastDepartureAreApplied() {
    var repo = mock(ScheduleRepository.class);
    var catalog = mock(CatalogClient.class);
    when(repo.findAll()).thenReturn(List.of(new Schedule("r", 360, 1320, 20)));
    when(catalog.route("r"))
        .thenReturn(
            new Route(
                "r",
                "20",
                "Route",
                List.of(new Stop("a", 0), new Stop("b", 15), new Stop("c", 35))));
    when(catalog.station("b")).thenReturn(new Station("b", "B", null, null));
    when(catalog.station("c")).thenReturn(new Station("c", "C", null, null));
    when(catalog.fare("r")).thenReturn(new Fare("f", "r", 1200));
    var service = new JourneyService(repo, catalog);
    var found = service.search("b", "c", LocalDateTime.parse("2026-11-10T06:16:01"));
    assertEquals(1, found.size());
    assertEquals(Instant.parse("2026-11-10T05:35:00Z"), found.get(0).departure());
    assertEquals(20, found.get(0).durationMinutes());
    when(catalog.fare("r"))
        .thenThrow(new tn.smartbus.trip.exception.ApiException(404, "Fare missing"));
    assertTrue(service.search("b", "c", LocalDateTime.parse("2026-11-10T06:00:00")).isEmpty());
    doThrow(new tn.smartbus.trip.exception.ApiException(503, "Unavailable")).when(catalog).fare("r");
    assertThrows(
        tn.smartbus.trip.exception.ApiException.class,
        () -> service.search("b", "c", LocalDateTime.parse("2026-11-10T06:00:00")));

    assertTrue(service.search("b", "c", LocalDateTime.parse("2026-11-10T22:16:00")).isEmpty());
    assertTrue(service.search("c", "b", LocalDateTime.parse("2026-11-10T06:00:00")).isEmpty());
  }
}
