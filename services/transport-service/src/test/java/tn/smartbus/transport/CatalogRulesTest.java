package tn.smartbus.transport;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.Test;
import tn.smartbus.transport.dto.*;
import tn.smartbus.transport.entity.RouteStop;
import tn.smartbus.transport.repository.*;
import tn.smartbus.transport.service.*;

class CatalogRulesTest {
  @Test
  void rejectsRepeatedStationAndMissingCoordinate() {
    var stations = mock(StationRepository.class);
    when(stations.existsById(anyString())).thenReturn(true);
    var rules =
        new CatalogRules(
            stations,
            mock(RouteRepository.class),
            mock(FareRepository.class),
            mock(ServiceClient.class),
            "http://trip");
    assertThrows(
        RuntimeException.class,
        () ->
            rules.validate(
                new RouteRequest(
                    "20", "Route", List.of(new RouteStop("s", 0), new RouteStop("s", 10)))));
    assertThrows(
        RuntimeException.class, () -> rules.validate(new StationRequest("Station", 36.8, null)));
  }
}
