import { ChangeDetectionStrategy } from "@angular/core";
import { ChangeDetectorRef } from "@angular/core";
import { Component, inject } from "@angular/core";
import { FormsModule } from "@angular/forms";
import { AuthService } from "../../core/auth.service";
import { errorMessage } from "../../core/api.service";
@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: true,
  imports: [FormsModule],
  templateUrl: "./login.component.html",
})
export class LoginComponent {
  private cdr = inject(ChangeDetectorRef);
  auth = inject(AuthService);
  register = false;
  fullName = "";
  email = "";
  password = "";
  busy = false;
  error = "";
  async submit() {
    this.busy = true;
    this.error = "";
    try {
      await this.auth.signIn(this.register ? "register" : "login", {
        fullName: this.fullName,
        email: this.email,
        password: this.password,
      });
      this.password = "";
    } catch (e) {
      this.error = errorMessage(e);
    } finally {
      this.busy = false;
      this.cdr.markForCheck();
    }
  }
}
