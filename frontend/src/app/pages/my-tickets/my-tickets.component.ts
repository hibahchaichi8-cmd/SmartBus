import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
  OnInit,
} from "@angular/core";

import { CommonModule } from "@angular/common";
import * as QRCode from "qrcode";

import { errorMessage } from "../../core/api.service";
import { Ticket } from "../../core/models";
import { TicketService } from "../../core/ticket.service";

@Component({
  standalone: true,
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [CommonModule],
  templateUrl: "./my-tickets.component.html",
})
export class MyTicketsComponent implements OnInit, OnDestroy {
  private service = inject(TicketService);
  private cdr = inject(ChangeDetectorRef);

  tickets: Ticket[] = [];

  selectedTicket: Ticket | null = null;
  qrImage = "";

  page = 0;
  totalPages = 0;
  totalElements = 0;

  loading = false;
  qrLoading = false;
  error = "";

  private destroyed = false;

  ngOnInit() {
    void this.load();
  }

  async load(page: number = 0) {
    if (this.loading || this.qrLoading || this.destroyed) return;

    this.loading = true;
    this.error = "";
    this.selectedTicket = null;
    this.qrImage = "";

    try {
      const response = await this.service.getMyTickets(page, 20);

      if (!this.destroyed) {
        this.tickets = response.items;
        this.page = response.page;
        this.totalPages = response.totalPages;
        this.totalElements = response.totalElements;
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

  async showQr(ticket: Ticket) {
    if (
      this.loading ||
      this.qrLoading ||
      this.destroyed ||
      ticket.status !== "VALID"
    ) {
      return;
    }

    this.selectedTicket = ticket;
    this.qrImage = "";
    this.qrLoading = true;
    this.error = "";

    try {
      const response = await this.service.getQr(ticket.id);

      if (this.destroyed) return;

      const image = await QRCode.toDataURL(response.qrToken, {
        width: 240,
        margin: 4,
        errorCorrectionLevel: "M",
      });

      if (!this.destroyed) {
        this.qrImage = image;
      }
    } catch (error) {
      if (!this.destroyed) {
        this.error = errorMessage(error);
      }
    } finally {
      this.qrLoading = false;

      if (!this.destroyed) {
        this.cdr.markForCheck();
      }
    }
  }

  closeQr() {
    if (this.qrLoading) return;

    this.selectedTicket = null;
    this.qrImage = "";
    this.error = "";
  }

  ngOnDestroy() {
    this.destroyed = true;
    this.qrImage = "";
  }
}