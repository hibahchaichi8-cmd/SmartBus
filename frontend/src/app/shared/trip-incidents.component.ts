import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  Input,
  OnDestroy,
} from "@angular/core";

import { CommonModule } from "@angular/common";
import { FormsModule } from "@angular/forms";

import { ApiService, errorMessage } from "../core/api.service";
import { Trip } from "../core/models";

@Component({
  selector: "app-trip-incidents",
  standalone: true,
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [CommonModule, FormsModule],
  templateUrl: "./trip-incidents.component.html",
})
export class TripIncidentsComponent implements OnDestroy {
  @Input({ required: true }) trip!: Trip;

  private api = inject(ApiService);
  private cdr = inject(ChangeDetectorRef);

  delayMinutes = 15;
  busy = false;
  error = "";
  message = "";

  private delayRequestId = "";
  private requestedMinutes: number | null = null;
  private breakdownRequestId = "";
  private destroyed = false;

  async reportDelay() {
    if (!this.canReport()) return;

    const minutes = Number(this.delayMinutes);

    if (
      !Number.isInteger(minutes) ||
      minutes < 1 ||
      minutes > 1440
    ) {
      this.error = "Enter a delay between 1 and 1440 minutes.";
      return;
    }

    if (
      !confirm(
        `Report a delay of ${minutes} minutes for bus ${this.trip.registrationNumber}?`,
      )
    ) {
      return;
    }

    if (
      !this.delayRequestId ||
      this.requestedMinutes !== minutes
    ) {
      this.delayRequestId = crypto.randomUUID();
      this.requestedMinutes = minutes;
    }

    const success = await this.send("delay", {
      requestId: this.delayRequestId,
      minutes,
    });

    if (success) {
      this.delayRequestId = "";
      this.requestedMinutes = null;
    }
  }

  async reportBreakdown() {
    if (!this.canReport()) return;

    if (
      !confirm(
        `Report a breakdown for bus ${this.trip.registrationNumber}?`,
      )
    ) {
      return;
    }

    if (!this.breakdownRequestId) {
      this.breakdownRequestId = crypto.randomUUID();
    }

    const success = await this.send("breakdown", {
      requestId: this.breakdownRequestId,
    });

    if (success) {
      this.breakdownRequestId = "";
    }
  }

  private canReport(): boolean {
    if (this.busy || this.destroyed) return false;

    this.error = "";
    this.message = "";

    if (
      this.trip.status !== "PLANNED" &&
      this.trip.status !== "IN_PROGRESS"
    ) {
      this.error = "This trip can no longer receive incident reports.";
      return false;
    }

    if (!globalThis.crypto?.randomUUID) {
      this.error = "Please open the application on localhost or HTTPS.";
      return false;
    }

    return true;
  }

  private async send(
    type: "delay" | "breakdown",
    body: { requestId: string; minutes?: number },
  ): Promise<boolean> {
    this.busy = true;

    try {
      await this.api.post(
        `trips/${encodeURIComponent(this.trip.id)}/incidents/${type}`,
        body,
      );

      if (!this.destroyed) {
        this.message =
          type === "delay"
            ? "Delay reported successfully."
            : "Breakdown reported successfully.";
      }

      return true;
    } catch (error) {
      if (!this.destroyed) {
        this.error = errorMessage(error);
      }

      return false;
    } finally {
      this.busy = false;

      if (!this.destroyed) {
        this.cdr.markForCheck();
      }
    }
  }

  ngOnDestroy() {
    this.destroyed = true;
  }
}