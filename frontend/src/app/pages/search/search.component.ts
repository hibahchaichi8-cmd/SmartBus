import { ChangeDetectionStrategy } from "@angular/core";
import { ChangeDetectorRef } from "@angular/core";
import { Component, inject, OnInit } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { CommonModule } from "@angular/common";
import { ApiService, errorMessage, tunisInput } from "../../core/api.service";
import { Station, Journey } from "../../core/models";
@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: "./search.component.html",
})
export class SearchComponent implements OnInit {
  private cdr = inject(ChangeDetectorRef);
  api = inject(ApiService);
  stations: Station[] = [];
  journeys: Journey[] = [];
  origin = "";
  destination = "";
  departure = tunisInput();
  busy = false;
  searched = false;
  error = "";
  async ngOnInit() {
    try {
      this.stations = await this.api.get<Station[]>("stations");
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.cdr.markForCheck();
    }
  }
  async search() {
    this.busy = true;
    this.error = "";
    this.searched = false;
    try {
      this.journeys = await this.api.get<Journey[]>(
        "journeys?" +
          new URLSearchParams({
            originId: this.origin,
            destinationId: this.destination,
            departure: this.departure,
          }),
      );
      this.searched = true;
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.busy = false;
      this.cdr.markForCheck();
    }
  }
}
