package tn.smartbus.trip;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import tn.smartbus.trip.dto.ScheduleRequest;
import tn.smartbus.trip.mapper.ScheduleMapper;

class ScheduleMapperTest {
  @Test
  void rejectsOvernightAndInvalidClock() {
    var mapper = new ScheduleMapper();
    assertThrows(
        IllegalArgumentException.class,
        () -> mapper.toEntity("r", new ScheduleRequest("22:00", "06:00", 20)));
    assertThrows(
        java.time.DateTimeException.class,
        () -> mapper.toEntity("r", new ScheduleRequest("29:00", "30:00", 20)));
  }
}
