import { inject, Injectable, signal } from "@angular/core";
import { Router } from "@angular/router";
import { ApiService } from "./api.service";
import { Session, Role } from "./models";
@Injectable({ providedIn: "root" })
export class AuthService {
  private api = inject(ApiService);
  private router = inject(Router);
  private timer?: ReturnType<typeof setTimeout>;
  session = signal<Session | null>(null);
  has(role: Role) {
    return this.session()?.user.role === role;
  }
  token() {
    return this.session()?.accessToken;
  }
  async signIn(path: "login" | "register", body: unknown) {
    const session = await this.api.post<Session>("auth/" + path, body);
    this.session.set(session);
    clearTimeout(this.timer);
    this.timer = setTimeout(
      () => this.logout(),
      Math.max(0, new Date(session.expiresAt).getTime() - Date.now()),
    );
    await this.router.navigateByUrl(
      session.user.role === "ADMIN"
        ? "/admin"
        : session.user.role === "DRIVER"
          ? "/driver"
          : "/search",
    );
  }
  logout() {
    clearTimeout(this.timer);
    this.session.set(null);
    void this.router.navigateByUrl("/login");
  }
}
