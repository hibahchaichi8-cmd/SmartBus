import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from "@angular/core";

import { CommonModule } from "@angular/common";

import { errorMessage } from "../../core/api.service";
import { AvailableTrip, Ticket } from "../../core/models";
import { TicketService } from "../../core/ticket.service";

@Component({
  standalone: true,
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [CommonModule],
  templateUrl: "./buy-ticket.component.html",
})
export class BuyTicketComponent implements OnInit, OnDestroy {
  private service = inject(TicketService);
  private cdr = inject(ChangeDetectorRef);

  trips: AvailableTrip[] = [];
  selectedTrip: AvailableTrip | null = null;
  purchasedTicket: Ticket | null = null;

  loading = false;
  busy = false;
  error = "";

  private purchaseRequestId = "";
  private destroyed = false;

  ngOnInit() {
    void this.load();
  }

  async load() {
    if (this.loading || this.busy || this.destroyed) return;

    this.loading = true;
    this.error = "";

    try {
      const trips = await this.service.getAvailableTrips();

      if (!this.destroyed) {
        this.trips = trips;
      }
    } catch (error) {
      if (!this.destroyed) {
        this.error = errorMessage(error);
      }
    } finally {
      this.loading = false;

      if (!this.destroyed) {
        this.cdr.markForCheck();
      }
    }
  }

  selectTrip(trip: AvailableTrip) {
    if (this.busy) return;

    if (!globalThis.crypto?.randomUUID) {
      this.error = "Please open the application on localhost or HTTPS.";
      return;
    }

    this.selectedTrip = trip;
    this.purchasedTicket = null;
    this.purchaseRequestId = crypto.randomUUID();
    this.error = "";
  }

  async purchase() {
    if (this.busy || !this.selectedTrip || !this.purchaseRequestId) {
      return;
    }

    this.busy = true;
    this.error = "";

    try {
      const ticket = await this.service.purchase({
        tripId: this.selectedTrip.id,
        purchaseRequestId: this.purchaseRequestId,
      });

      if (!this.destroyed) {
        this.purchasedTicket = ticket;
        this.selectedTrip = null;
        this.purchaseRequestId = "";
      }
    } catch (error) {
      if (!this.destroyed) {
        this.error = errorMessage(error);
      }

      // Keep the same request ID when retrying this purchase.
    } finally {
      this.busy = false;

      if (!this.destroyed) {
        this.cdr.markForCheck();
      }
    }
  }

  resetSelection() {
    if (this.busy) return;

    this.selectedTrip = null;
    this.purchasedTicket = null;
    this.purchaseRequestId = "";
    this.error = "";

    void this.load();
  }

  ngOnDestroy() {
    this.destroyed = true;
  }
}