import { ChangeDetectionStrategy } from "@angular/core";
import {
  AfterViewInit,
  Component,
  ElementRef,
  Input,
  OnChanges,
  OnDestroy,
  ViewChild,
} from "@angular/core";
import * as L from "leaflet";
import { Tracking } from "../core/models";
@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  selector: "app-bus-map",
  standalone: true,
  template:
    '<div #canvas class="map" role="img" aria-label="Map showing reported bus positions and route stops"></div>',
})
export class BusMapComponent implements AfterViewInit, OnChanges, OnDestroy {
  @Input() reports: Tracking[] = [];
  @ViewChild("canvas") canvas!: ElementRef<HTMLDivElement>;
  private map?: L.Map;
  private layers?: L.LayerGroup;
  ngAfterViewInit() {
    this.map = L.map(this.canvas.nativeElement).setView([36.82, 10.16], 12);
    L.tileLayer("https://tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 19,
      attribution:
        '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    }).addTo(this.map);
    this.layers = L.layerGroup().addTo(this.map);
    this.draw();
  }
  ngOnChanges() {
    this.draw();
  }
  private draw() {
    if (!this.map || !this.layers) return;
    this.layers.clearLayers();
    const points: L.LatLngTuple[] = [];
    for (const report of this.reports) {
      const stops = report.stops
        .filter((s) => s.latitude !== null && s.longitude !== null)
        .map((s) => [s.latitude!, s.longitude!] as L.LatLngTuple);
      if (stops.length > 1)
        L.polyline(stops, {
          color: "#91a7b7",
          weight: 2,
          dashArray: "5 8",
        }).addTo(this.layers);
      if (report.latitude !== null && report.longitude !== null) {
        const location: L.LatLngTuple = [report.latitude, report.longitude];
        points.push(location);
        const text = document.createElement("span");
        text.textContent =
          report.registrationNumber +
          " · " +
          (report.stale ? "stale" : report.source);
        L.circleMarker(location, {
          radius: 9,
          color: "#fff",
          weight: 2,
          fillColor: report.stale ? "#8b959e" : "#22618c",
          fillOpacity: 1,
        })
          .bindPopup(text)
          .addTo(this.layers);
      }
    }
    if (points.length)
      this.map.fitBounds(L.latLngBounds(points), {
        padding: [40, 40],
        maxZoom: 14,
      });
  }
  ngOnDestroy() {
    this.map?.remove();
  }
}
