package tn.smartbus.trip.mapper;

import java.time.LocalTime;
import org.springframework.stereotype.Component;
import tn.smartbus.trip.dto.*;
import tn.smartbus.trip.entity.Schedule;

@Component
public class ScheduleMapper {
  public Schedule toEntity(String routeId, ScheduleRequest r) {
    int first = LocalTime.parse(r.firstDeparture()).toSecondOfDay() / 60,
        last = LocalTime.parse(r.lastDeparture()).toSecondOfDay() / 60;
    if (last < first)
      throw new IllegalArgumentException("Last departure must follow first departure");
    return new Schedule(routeId, first, last, r.intervalMinutes());
  }

  public ScheduleResponse toResponse(Schedule s) {
    return new ScheduleResponse(
        s.routeId(), time(s.firstMinute()), time(s.lastMinute()), s.intervalMinutes());
  }

  private String time(int m) {
    return "%02d:%02d".formatted(m / 60, m % 60);
  }
}
