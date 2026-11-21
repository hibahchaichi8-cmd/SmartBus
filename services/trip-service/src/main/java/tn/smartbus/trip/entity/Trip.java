package tn.smartbus.trip.entity;

import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("trips")
public class Trip {
  @Id private String id;
  @Version private Long version;
  private String routeId;
  private String routeName;
  private String busId;
  private String registrationNumber;
  private int capacity;
  private String driverId;
  private String driverName;
  private java.time.Instant plannedDeparture;
  private java.time.Instant actualStart;
  private java.time.Instant actualEnd;
  private TripStatus status;
  private java.util.List<TripStop> stops;
  private java.util.List<Checkpoint> checkpoints;

  public String getId() {
    return id;
  }

  public void setId(String value) {
    id = value;
  }

  public Long getVersion() {
    return version;
  }

  public void setVersion(Long value) {
    version = value;
  }

  public String getRouteId() {
    return routeId;
  }

  public void setRouteId(String value) {
    routeId = value;
  }

  public String getRouteName() {
    return routeName;
  }

  public void setRouteName(String value) {
    routeName = value;
  }

  public String getBusId() {
    return busId;
  }

  public void setBusId(String value) {
    busId = value;
  }

  public String getRegistrationNumber() {
    return registrationNumber;
  }

  public void setRegistrationNumber(String value) {
    registrationNumber = value;
  }

  public int getCapacity() {
    return capacity;
  }

  public void setCapacity(int value) {
    capacity = value;
  }

  public String getDriverId() {
    return driverId;
  }

  public void setDriverId(String value) {
    driverId = value;
  }

  public String getDriverName() {
    return driverName;
  }

  public void setDriverName(String value) {
    driverName = value;
  }

  public java.time.Instant getPlannedDeparture() {
    return plannedDeparture;
  }

  public void setPlannedDeparture(java.time.Instant value) {
    plannedDeparture = value;
  }

  public java.time.Instant getActualStart() {
    return actualStart;
  }

  public void setActualStart(java.time.Instant value) {
    actualStart = value;
  }

  public java.time.Instant getActualEnd() {
    return actualEnd;
  }

  public void setActualEnd(java.time.Instant value) {
    actualEnd = value;
  }

  public TripStatus getStatus() {
    return status;
  }

  public void setStatus(TripStatus value) {
    status = value;
  }

  public java.util.List<TripStop> getStops() {
    return stops;
  }

  public void setStops(java.util.List<TripStop> value) {
    stops = value;
  }

  public java.util.List<Checkpoint> getCheckpoints() {
    return checkpoints;
  }

  public void setCheckpoints(java.util.List<Checkpoint> value) {
    checkpoints = value;
  }
}
