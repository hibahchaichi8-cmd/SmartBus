import {
  ChangeDetectionStrategy,
  Component,
  inject,
} from "@angular/core";

import {
  RouterLink,
  RouterLinkActive,
  RouterOutlet,
} from "@angular/router";

import { AuthService } from "./core/auth.service";
import { NotificationBellComponent } from "./shared/notification-bell.component";

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  selector: "app-root",
  standalone: true,
  imports: [
    RouterLink,
    RouterLinkActive,
    RouterOutlet,
    NotificationBellComponent,
  ],
  templateUrl: "./app.component.html",
})
export class AppComponent {
  auth = inject(AuthService);
}