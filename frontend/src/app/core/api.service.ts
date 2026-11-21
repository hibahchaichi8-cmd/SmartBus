import { inject, Injectable } from "@angular/core";
import { HttpClient, HttpErrorResponse } from "@angular/common/http";
import { firstValueFrom } from "rxjs";

@Injectable({ providedIn: "root" })
export class ApiService {
  private http = inject(HttpClient);

  get<T>(path: string) {
    return firstValueFrom(
      this.http.get<T>("/api/v1/" + path),
    );
  }

  post<T>(path: string, body: unknown = {}) {
    return firstValueFrom(
      this.http.post<T>("/api/v1/" + path, body),
    );
  }

  put<T>(path: string, body: unknown) {
    return firstValueFrom(
      this.http.put<T>("/api/v1/" + path, body),
    );
  }

  patch<T>(path: string, body: unknown = {}) {
    return firstValueFrom(
      this.http.patch<T>("/api/v1/" + path, body),
    );
  }

  delete(path: string) {
    return firstValueFrom(
      this.http.delete<void>("/api/v1/" + path),
    );
  }
}

export function errorMessage(error: unknown): string {
  if (error instanceof HttpErrorResponse) {
    const body = error.error;

    const fields = body?.errors
      ? Object.entries(body.errors)
          .map(([key, value]) => key + ": " + value)
          .join("; ")
      : "";

    return (
      fields ||
      body?.message ||
      (error.status === 0
        ? "Cannot reach the server. Check that the containers are running."
        : "Request failed (" + error.status + ")")
    );
  }

  return error instanceof Error ? error.message : "Request failed";
}

export function tunisInput(date = new Date()): string {
  const parts = new Intl.DateTimeFormat("sv-SE", {
    timeZone: "Africa/Tunis",
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    hourCycle: "h23",
  }).format(date);

  return parts.replace(" ", "T");
}

export function tunisIso(local: string): string {
  return local + ":00+01:00";
}