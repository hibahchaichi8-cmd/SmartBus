import { inject, Injectable } from "@angular/core";
import { ApiService } from "./api.service";
import {
  AvailableTrip,
  Ticket,
  TicketPageResponse,
  TicketPurchaseRequest,
  TicketQrResponse,
} from "./models";

@Injectable({
  providedIn: "root",
})
export class TicketService {

  private api = inject(ApiService);

  getAvailableTrips(routeId?: string): Promise<AvailableTrip[]> {
    let path = "trips/available";

    if (routeId) {
      path += "?routeId=" + encodeURIComponent(routeId);
    }

    return this.api.get<AvailableTrip[]>(path);
  }
  purchase(request: TicketPurchaseRequest): Promise<Ticket> {
    return this.api.post<Ticket>(
      "tickets/purchase",
      request,
    );
  }

  getMyTickets(
    page: number = 0,
    size: number = 20,
  ): Promise<TicketPageResponse> {

    const path = `tickets/mine?page=${page}&size=${size}`;

    return this.api.get<TicketPageResponse>(path);
  }

  getTicket(ticketId: string): Promise<Ticket> {
    const id = encodeURIComponent(ticketId);

    return this.api.get<Ticket>(
      `tickets/${id}`,
    );
  }

  getQr(ticketId: string): Promise<TicketQrResponse> {
    const id = encodeURIComponent(ticketId);

    return this.api.get<TicketQrResponse>(
      `tickets/${id}/qr`,
    );
  }

  validate(qrToken: string): Promise<Ticket> {
    return this.api.post<Ticket>(
      "tickets/validate",
      { qrToken },
    );
  }
}