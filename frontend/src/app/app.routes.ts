import { Routes } from "@angular/router";

import { authGuard } from "./core/auth.guard";
import { ValidateTicketComponent } from "./pages/validate-ticket/validate-ticket.component";
import { SearchComponent } from "./pages/search/search.component";
import { LoginComponent } from "./pages/login/login.component";
import { AdminComponent } from "./pages/admin/admin.component";
import { DriverComponent } from "./pages/driver/driver.component";
import { LiveComponent } from "./pages/live/live.component";
import { BuyTicketComponent } from "./pages/buy-ticket/buy-ticket.component";
import { MyTicketsComponent } from "./pages/my-tickets/my-tickets.component";
export const routes: Routes = [
  {
    path: "search",
    component: SearchComponent,
  },
  {
    path: "live",
    component: LiveComponent,
  },
  {
    path: "login",
    component: LoginComponent,
  },
  {
    path: "buy-ticket",
    component: BuyTicketComponent,
    canActivate: [authGuard],
    data: { role: "CITIZEN" },
  },
  {
    path: "admin",
    component: AdminComponent,
    canActivate: [authGuard],
    data: { role: "ADMIN" },
  },
  {
    path: "driver",
    component: DriverComponent,
    canActivate: [authGuard],
    data: { role: "DRIVER" },
  },
  {
  path: "my-tickets",
  component: MyTicketsComponent,
  canActivate: [authGuard],
  data: { role: "CITIZEN" },
},

{
  path: "validate-ticket",
  component: ValidateTicketComponent,
  canActivate: [authGuard],
  data: { role: "DRIVER" },
},
  {
    path: "",
    redirectTo: "search",
    pathMatch: "full",
  },
  {
    path: "**",
    redirectTo: "search",
  },
];