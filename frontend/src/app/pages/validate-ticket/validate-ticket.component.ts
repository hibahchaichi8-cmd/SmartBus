import {
  AfterViewInit,
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  inject,
  OnDestroy,
} from "@angular/core";

import { CommonModule } from "@angular/common";

import {
  Html5QrcodeScanner,
  Html5QrcodeScanType,
  Html5QrcodeSupportedFormats,
} from "html5-qrcode";

import { errorMessage } from "../../core/api.service";
import { Ticket } from "../../core/models";
import { TicketService } from "../../core/ticket.service";

@Component({
  standalone: true,
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [CommonModule],
  templateUrl: "./validate-ticket.component.html",
})
export class ValidateTicketComponent
  implements AfterViewInit, OnDestroy {

  private service = inject(TicketService);
  private cdr = inject(ChangeDetectorRef);

  private scanner?: Html5QrcodeScanner;
  private stopping?: Promise<void>;

  private handled = false;
  private destroyed = false;

  ticket: Ticket | null = null;
  error = "";
  busy = false;
  scanFinished = false;

  ngAfterViewInit() {
    this.renderScanner();
  }

  private renderScanner() {
    this.handled = false;
    this.stopping = undefined;

    this.scanner = new Html5QrcodeScanner(
      "ticket-reader",
      {
        fps: 5,
        qrbox: {
          width: 220,
          height: 220,
        },
        rememberLastUsedCamera: false,
        formatsToSupport: [
          Html5QrcodeSupportedFormats.QR_CODE,
        ],
        supportedScanTypes: [
          Html5QrcodeScanType.SCAN_TYPE_CAMERA,
          Html5QrcodeScanType.SCAN_TYPE_FILE,
        ],
      },
      false,
    );

    this.scanner.render(
      (text) => {
        void this.validateToken(text);
      },
      () => {
        // A frame without a readable QR code is normal.
      },
    );
  }

  private stopScanner(): Promise<void> {
    if (!this.scanner) {
      return Promise.resolve();
    }

    if (!this.stopping) {
      this.stopping = this.scanner.clear();
    }

    return this.stopping;
  }

  private async validateToken(token: string) {
    if (this.handled || this.destroyed) return;

    // Process only one detection until the next scan.
    this.handled = true;
    this.busy = true;
    this.error = "";
    this.ticket = null;
    this.cdr.markForCheck();

    try {
      await this.stopScanner();

      if (this.destroyed) return;

      const ticket = await this.service.validate(token);

      if (!this.destroyed) {
        this.ticket = ticket;
      }
    } catch (error) {
      if (!this.destroyed) {
        this.error = errorMessage(error);
      }
    } finally {
      this.busy = false;
      this.scanFinished = true;

      if (!this.destroyed) {
        this.cdr.markForCheck();
      }
    }
  }

  async scanAgain() {
    if (this.busy || this.destroyed) return;

    this.busy = true;

    try {
      await this.stopScanner();

      if (this.destroyed) return;

      this.ticket = null;
      this.error = "";
      this.scanFinished = false;

      this.renderScanner();
    } catch (error) {
      if (!this.destroyed) {
        this.error = errorMessage(error);
      }
    } finally {
      this.busy = false;

      if (!this.destroyed) {
        this.cdr.markForCheck();
      }
    }
  }

  ngOnDestroy() {
    this.destroyed = true;

    void this.stopScanner().catch(() => {
      // Ignore cleanup errors after leaving the page.
    });
  }
}