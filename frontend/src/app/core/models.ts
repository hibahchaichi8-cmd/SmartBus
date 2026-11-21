export type Role = "ADMIN" | "CITIZEN" | "DRIVER";
export interface User {
  id: string;
  fullName: string;
  email: string;
  role: Role;
}
export interface Session {
  accessToken: string;
  tokenType: string;
  expiresAt: string;
  user: User;
}
export interface Bus {
  id: string;
  registrationNumber: string;
  capacity: number;
}
export interface Station {
  id: string;
  name: string;
  latitude: number | null;
  longitude: number | null;
}
export interface RouteStop {
  stationId: string;
  offsetMinutes: number;
}
export interface BusRoute {
  id: string;
  code: string;
  name: string;
  stops: RouteStop[];
}
export interface Fare {
  id: string;
  routeId: string;
  amountMillimes: number;
}
export interface Schedule {
  routeId: string;
  firstDeparture: string;
  lastDeparture: string;
  intervalMinutes: number;
}
export interface Journey {
  routeId: string;
  routeName: string;
  origin: string;
  destination: string;
  departure: string;
  estimatedArrival: string;
  durationMinutes: number;
  amountMillimes: number;
  currency: string;
  source: string;
}
export interface TripStop {
  stationId: string;
  name: string;
  offsetMinutes: number;
  latitude: number | null;
  longitude: number | null;
}
export interface Trip {
  id: string;
  routeId: string;
  routeName: string;
  busId: string;
  registrationNumber: string;
  capacity: number;
  driverId: string;
  driverName: string;
  plannedDeparture: string;
  actualStart: string | null;
  actualEnd: string | null;
  status: "PLANNED" | "IN_PROGRESS" | "COMPLETED" | "CANCELLED";
  stops: TripStop[];
  checkpoints: { stopIndex: number; arrivedAt: string }[];
  estimatedArrival: string;
  overdue: boolean;
}
export interface Tracking {
  tripId: string;
  routeName: string;
  registrationNumber: string;
  capacity: number;
  status: string;
  stops: TripStop[];
  estimatedArrival: string;
  overdue: boolean;
  latitude: number | null;
  longitude: number | null;
  source: string | null;
  positionReceivedAt: string | null;
  stale: boolean;
  passengerCount: number | null;
  seatsAvailable: number | null;
  occupancyReceivedAt: string | null;
}
export type TicketStatus = "VALID" | "USED" | "CANCELLED";

export interface AvailableTrip {
  id: string;
  routeId: string;
  routeName: string;
  registrationNumber: string;
  plannedDeparture: string;
  estimatedArrival: string;
  amountMillimes: number;
  currency: string;
}

export interface Ticket {
  id: string;
  tripId: string;
  routeName: string;
  registrationNumber: string;
  plannedDeparture: string;
  amountMillimes: number;
  currency: string;
  status: TicketStatus;
  purchasedAt: string;
  validatedAt: string | null;
}

export interface TicketPurchaseRequest {
  tripId: string;
  purchaseRequestId: string;
}

export interface TicketPageResponse {
  items: Ticket[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface TicketQrResponse {
  ticketId: string;
  qrToken: string;
}
export type AppNotificationType =
  | "TRIP_ASSIGNED"
  | "TRIP_STARTED"
  | "TRIP_COMPLETED"
  | "BUS_DELAYED"
  | "BUS_BREAKDOWN";

export interface AppNotification {
  id: string;
  type: AppNotificationType;
  tripId: string;
  message: string;
  createdAt: string;
  readAt: string | null;
}

export interface NotificationPageResponse {
  items: AppNotification[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface NotificationCountResponse {
  count: number;
}