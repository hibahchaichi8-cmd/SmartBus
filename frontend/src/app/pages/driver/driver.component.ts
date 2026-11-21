import { ChangeDetectionStrategy } from "@angular/core";
import { ChangeDetectorRef } from "@angular/core";
import { Component, inject, OnInit, OnDestroy } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import { ApiService, errorMessage } from "../../core/api.service";
import { Trip, Tracking } from "../../core/models";
import { TripIncidentsComponent } from "../../shared/trip-incidents.component";
@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: true,
  imports: [
  CommonModule,
  FormsModule,
  TripIncidentsComponent,
],
  templateUrl: "./driver.component.html",
})
export class DriverComponent implements OnInit, OnDestroy {
  private cdr = inject(ChangeDetectorRef);
  private api = inject(ApiService);
  trips: Trip[] = [];
  error = "";
  message = "";
  busy = false;
  counts: Record<string, number> = {};
  latitudes: Record<string, number> = {};
  longitudes: Record<string, number> = {};
  gpsTrip = "";
  private watchId?: number;
  private timer?: ReturnType<typeof setInterval>;
  private loading = false;
  private positionPending = false;
  private destroyed = false;
  ngOnInit() {
    void this.load();
    this.timer = setInterval(() => void this.load(), 10000);
  }
  async load() {
    if (this.loading) return;
    this.loading = true;
    try {
      const trips = await this.api.get<Trip[]>("trips/mine");
      if (!this.destroyed) {
        this.trips = trips;
        if (
          this.gpsTrip &&
          !trips.some(
            (t) => t.id === this.gpsTrip && t.status === "IN_PROGRESS",
          )
        )
          this.stopGps();
      }
    } catch (e) {
      if (!this.destroyed) this.error = errorMessage(e);
    } finally {
      this.loading = false;
      this.cdr.markForCheck();
    }
  }
  async action(trip: Trip, action: "start" | "checkpoints" | "finish") {
    this.busy = true;
    this.error = "";
    try {
      await this.api.post(
        "trips/" + trip.id + "/" + action,
        action === "checkpoints" ? { stopIndex: trip.checkpoints.length } : {},
      );
      if (action === "finish") this.stopGps();
      this.message = "Trip updated.";
      await this.load();
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.busy = false;
      this.cdr.markForCheck();
    }
  }
  async count(trip: Trip) {
    this.busy = true;
    this.error = "";
    try {
      const r = await this.api.put<Tracking>(
        "tracking/" + trip.id + "/occupancy",
        { passengerCount: this.counts[trip.id] },
      );
      this.message =
        "Passenger count saved: " + r.passengerCount + " / " + r.capacity;
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.busy = false;
      this.cdr.markForCheck();
    }
  }
  async position(
    tripId: string,
    latitude: number,
    longitude: number,
    source: "MANUAL" | "SIMULATED" | "GPS",
  ) {
    if (this.positionPending || this.destroyed) return;
    this.positionPending = true;
    this.error = "";
    try {
      await this.api.put("tracking/" + tripId + "/position", {
        latitude,
        longitude,
        source,
        observedAt: new Date().toISOString(),
      });
      if (!this.destroyed) this.message = source + " location sent.";
    } catch (e) {
      if (!this.destroyed) this.error = errorMessage(e);
    } finally {
      this.positionPending = false;
      this.cdr.markForCheck();
    }
  }

  startGps(trip: Trip) {
    this.stopGps();
    if (!navigator.geolocation) {
      this.error = "Geolocation is unavailable in this browser.";
      return;
    }
    this.gpsTrip = trip.id;
    let last = 0;
    this.watchId = navigator.geolocation.watchPosition(
      (p) => {
        if (
          this.destroyed ||
          this.gpsTrip !== trip.id ||
          Date.now() - last < 5000
        )
          return;
        last = Date.now();
        void this.position(
          trip.id,
          p.coords.latitude,
          p.coords.longitude,
          "GPS",
        );
      },
      () => {
        this.error =
          "Location access failed. Allow browser location permission, .";
        this.stopGps();
      },
      { enableHighAccuracy: true, maximumAge: 5000, timeout: 15000 },
    );
  }
  stopGps() {
    if (this.watchId !== undefined)
      navigator.geolocation.clearWatch(this.watchId);
    this.watchId = undefined;
    this.gpsTrip = "";
  }
  ngOnDestroy() {
    this.destroyed = true;
    this.stopGps();
    clearInterval(this.timer);
  }
}
