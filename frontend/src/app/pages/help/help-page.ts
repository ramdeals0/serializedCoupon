import { Component, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-help-page',
  imports: [RouterLink],
  templateUrl: './help-page.html',
  styleUrl: './help-page.css',
})
export class HelpPage {
  readonly auth = inject(AuthService);
  readonly couponCodePattern = 'FF[0-9]{4}[A-Z0-9]{8}';
}
