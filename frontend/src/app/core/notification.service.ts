import { inject, Injectable } from "@angular/core";

import { ApiService } from "./api.service";

import {
  AppNotification,
  NotificationCountResponse,
  NotificationPageResponse,
} from "./models";

@Injectable({
  providedIn: "root",
})
export class NotificationService {
  private api = inject(ApiService);

  getMine(
    page: number = 0,
    size: number = 5,
  ): Promise<NotificationPageResponse> {
    return this.api.get<NotificationPageResponse>(
      `notifications?page=${page}&size=${size}`,
    );
  }

  getUnreadCount(): Promise<NotificationCountResponse> {
    return this.api.get<NotificationCountResponse>(
      "notifications/unread-count",
    );
  }

  markAsRead(id: string): Promise<AppNotification> {
    return this.api.patch<AppNotification>(
      `notifications/${encodeURIComponent(id)}/read`,
    );
  }
}