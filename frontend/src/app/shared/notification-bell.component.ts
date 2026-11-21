import {
  ChangeDetectionStrategy,
  ChangeDetectorRef,
  Component,
  effect,
  inject,
  OnDestroy,
} from "@angular/core";

import { CommonModule } from "@angular/common";

import { AuthService } from "../core/auth.service";
import { errorMessage } from "../core/api.service";
import { AppNotification } from "../core/models";
import { NotificationService } from "../core/notification.service";

@Component({
  selector: "app-notification-bell",
  standalone: true,
  changeDetection: ChangeDetectionStrategy.Eager,
  imports: [CommonModule],
  templateUrl: "./notification-bell.component.html",
  styleUrls: ["./notification-bell.component.css"],
})
export class NotificationBellComponent implements OnDestroy {
  private auth = inject(AuthService);
  private service = inject(NotificationService);
  private cdr = inject(ChangeDetectorRef);

  items: AppNotification[] = [];
  unreadCount = 0;
  page = 0;
  totalPages = 0;

  open = false;
  loading = false;
  markingId = "";
  error = "";

  private generation = 0;
  private pendingGeneration: number | null = null;
  private destroyed = false;

  constructor() {
    effect((onCleanup) => {
      const session = this.auth.session();
      const generation = ++this.generation;

      this.items = [];
      this.unreadCount = 0;
      this.page = 0;
      this.totalPages = 0;
      this.open = false;
      this.loading = false;
      this.markingId = "";
      this.error = "";
      this.pendingGeneration = null;

      if (!session) return;

      void this.refresh(generation);

      const timer = setInterval(() => {
        void this.refresh(generation);
      }, 15000);

      onCleanup(() => {
        clearInterval(timer);
        this.generation++;
      });
    });
  }

  toggle() {
    this.open = !this.open;

    if (this.open) {
      void this.refresh();
    }
  }

  async refresh(generation: number = this.generation) {
    if (
      this.destroyed ||
      generation !== this.generation ||
      !this.auth.session() ||
      this.pendingGeneration === generation ||
      this.markingId
    ) {
      return;
    }

    this.pendingGeneration = generation;
    this.loading = true;

    try {
      const [count, notifications] = await Promise.all([
        this.service.getUnreadCount(),
        this.service.getMine(this.page, 5),
      ]);

      if (this.destroyed || generation !== this.generation) {
        return;
      }

      this.unreadCount = count.count;
      this.items = notifications.items;
      this.page = notifications.page;
      this.totalPages = notifications.totalPages;
      this.error = "";
    } catch (error) {
      if (!this.destroyed && generation === this.generation) {
        this.error = errorMessage(error);
      }
    } finally {
      if (!this.destroyed && generation === this.generation) {
        this.pendingGeneration = null;
        this.loading = false;
        this.cdr.markForCheck();
      }
    }
  }

  changePage(page: number) {
    if (
      this.loading ||
      this.markingId ||
      page < 0 ||
      page >= this.totalPages
    ) {
      return;
    }

    this.page = page;
    void this.refresh();
  }

  async markAsRead(item: AppNotification) {
    if (item.readAt || this.loading || this.markingId) return;

    const generation = this.generation;
    this.markingId = item.id;
    this.error = "";

    try {
      const updated = await this.service.markAsRead(item.id);

      if (!this.destroyed && generation === this.generation) {
        this.items = this.items.map((notification) =>
          notification.id === updated.id ? updated : notification,
        );
      }
    } catch (error) {
      if (!this.destroyed && generation === this.generation) {
        this.error = errorMessage(error);
      }
    } finally {
      if (!this.destroyed && generation === this.generation) {
        this.markingId = "";
        this.cdr.markForCheck();
        void this.refresh();
      }
    }
  }

  ngOnDestroy() {
    this.destroyed = true;
    this.generation++;
  }
}