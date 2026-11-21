import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from "@angular/core";
import { CommonModule } from "@angular/common";
import { ApiService, errorMessage } from "../../core/api.service";
import { Tracking } from "../../core/models";
import { BusMapComponent } from "../../shared/bus-map.component";

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: true,
  imports: [CommonModule, BusMapComponent],
  templateUrl: "./live.component.html",
})
export class LiveComponent implements OnInit, OnDestroy {
  private api = inject(ApiService);
  private cdr = inject(ChangeDetectorRef);

  reports: Tracking[] = [];
  error = "";
  updated?: Date;

  private timer?: ReturnType<typeof setInterval>;
  private pending = false;
  private destroyed = false;

  ngOnInit() {
    void this.refresh();

    this.timer = setInterval(() => {
      void this.refresh();
    }, 5000);
  }

  async refresh() {
    if (this.pending || this.destroyed) return;

    this.pending = true;

    try {
      const reports = await this.api.get<Tracking[]>("tracking");

      if (this.destroyed) return;

      this.reports = reports;
      this.updated = new Date();
      this.error = "";
    } catch (error) {
      if (!this.destroyed) {
        this.error = errorMessage(error);
      }
    } finally {
      this.pending = false;

      if (!this.destroyed) {
        this.cdr.markForCheck();
      }
    }
  }

  ngOnDestroy() {
    this.destroyed = true;
    clearInterval(this.timer);
  }
}