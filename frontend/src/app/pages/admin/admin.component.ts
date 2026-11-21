import { ChangeDetectionStrategy } from "@angular/core";
import { ChangeDetectorRef } from "@angular/core";
import { Component, inject, OnInit } from "@angular/core";
import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";
import {
  ApiService,
  errorMessage,
  tunisInput,
  tunisIso,
} from "../../core/api.service";
import {
  Bus,
  Station,
  BusRoute,
  Fare,
  Schedule,
  User,
  Trip,
  RouteStop,
} from "../../core/models";
type Tab =
  | "buses"
  | "stations"
  | "routes"
  | "fares"
  | "schedules"
  | "drivers"
  | "trips";
@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./admin.component.html",
})
export class AdminComponent implements OnInit {
  private cdr = inject(ChangeDetectorRef);
  private api = inject(ApiService);
  tabs: Tab[] = [
    "buses",
    "stations",
    "routes",
    "fares",
    "schedules",
    "drivers",
    "trips",
  ];
  tab: Tab = "buses";
  buses: Bus[] = [];
  stations: Station[] = [];
  routes: BusRoute[] = [];
  fares: Fare[] = [];
  schedules: Schedule[] = [];
  drivers: User[] = [];
  trips: Trip[] = [];
  error = "";
  success = "";
  busy = false;
  formOpen = false;
  editing = "";
  form = this.blank();
  blank() {
    return {
      registrationNumber: "",
      capacity: 50,
      name: "",
      latitude: null as number | null,
      longitude: null as number | null,
      code: "",
      stops: [] as RouteStop[],
      routeId: "",
      amountMillimes: 1000,
      firstDeparture: "06:00",
      lastDeparture: "22:00",
      intervalMinutes: 20,
      fullName: "",
      email: "",
      password: "",
      busId: "",
      driverId: "",
      plannedDeparture: tunisInput(new Date(Date.now() + 3600000)),
    };
  }
  ngOnInit() {
    void this.load();
  }
  async load() {
    this.busy = true;
    try {
      [
        this.buses,
        this.stations,
        this.routes,
        this.fares,
        this.schedules,
        this.drivers,
        this.trips,
      ] = await Promise.all([
        this.api.get<Bus[]>("buses"),
        this.api.get<Station[]>("stations"),
        this.api.get<BusRoute[]>("routes"),
        this.api.get<Fare[]>("fares"),
        this.api.get<Schedule[]>("schedules"),
        this.api.get<User[]>("drivers"),
        this.api.get<Trip[]>("trips"),
      ]);
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.busy = false;
      this.cdr.markForCheck();
    }
  }
  choose(tab: Tab) {
    this.tab = tab;
    this.formOpen = false;
    this.error = "";
    this.success = "";
  }
  open(value?: Bus | Station | BusRoute | Fare | Schedule) {
    this.form = this.blank();
    this.editing = "";
    if (value) {
      Object.assign(this.form, JSON.parse(JSON.stringify(value)));
      this.editing = "id" in value ? value.id : value.routeId;
    } else if (this.tab === "routes")
      this.form.stops = [
        { stationId: "", offsetMinutes: 0 },
        { stationId: "", offsetMinutes: 20 },
      ];
    this.error = "";
    this.success = "";
    this.formOpen = true;
    this.cdr.markForCheck();
  }
  stationName(id: string) {
    return (
      this.stations.find((s) => s.id === id)?.name || "Unavailable station"
    );
  }
  routeName(id: string) {
    const r = this.routes.find((r) => r.id === id);
    return r ? r.code + " · " + r.name : "Unavailable route";
  }
  async save() {
    this.busy = true;
    this.error = "";
    try {
      const f = this.form;
      let body: unknown;
      switch (this.tab) {
        case "buses":
          body = {
            registrationNumber: f.registrationNumber,
            capacity: f.capacity,
          };
          break;
        case "stations":
          body = { name: f.name, latitude: f.latitude, longitude: f.longitude };
          break;
        case "routes":
          body = { code: f.code, name: f.name, stops: f.stops };
          break;
        case "fares":
          body = { routeId: f.routeId, amountMillimes: f.amountMillimes };
          break;
        case "schedules":
          body = {
            firstDeparture: f.firstDeparture,
            lastDeparture: f.lastDeparture,
            intervalMinutes: f.intervalMinutes,
          };
          break;
        case "drivers":
          body = { fullName: f.fullName, email: f.email, password: f.password };
          break;
        case "trips":
          body = {
            routeId: f.routeId,
            busId: f.busId,
            driverId: f.driverId,
            plannedDeparture: tunisIso(f.plannedDeparture),
          };
          break;
      }
      if (this.tab === "schedules")
        await this.api.put("schedules/" + f.routeId, body);
      else if (this.editing)
        await this.api.put(this.tab + "/" + this.editing, body);
      else await this.api.post(this.tab, body);
      this.form.password = "";
      this.formOpen = false;
      this.success = "Saved successfully.";
      await this.load();
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.busy = false;
      this.cdr.markForCheck();
    }
  }
  async remove(id: string) {
    if (
      !confirm("Delete this record? Referenced records must be removed first.")
    )
      return;
    this.busy = true;
    this.error = "";
    try {
      await this.api.delete(this.tab + "/" + id);
      this.success = "Record deleted.";
      await this.load();
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.busy = false;
      this.cdr.markForCheck();
    }
  }
  async cancel(id: string) {
    if (!confirm("Cancel this planned trip?")) return;
    this.busy = true;
    this.error = "";
    try {
      await this.api.post("trips/" + id + "/cancel");
      this.success = "Trip cancelled.";
      await this.load();
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.busy = false;
      this.cdr.markForCheck();
    }
  }
}
