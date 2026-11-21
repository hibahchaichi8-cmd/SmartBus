package tn.smartbus.trip.entity;

import java.time.Instant;

public record Checkpoint(int stopIndex, Instant arrivedAt) {}
