import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Component({
  selector: 'app-login',
  template: `
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
          <h1>OAuth2 Authorization Server</h1>
          <p>Sign in to continue</p>
        </div>

        <div class="login-body">
          <div *ngIf="errorMessage" class="error-message">
            {{ errorMessage }}
          </div>

          <div *ngIf="isProcessingCallback" class="processing">
            <div class="spinner"></div>
            <p>Processing authentication...</p>
          </div>

          <div *ngIf="!isProcessingCallback">
            <button
              class="login-button"
              (click)="loginWithOAuth2()"
              [disabled]="isLoading">
              <span *ngIf="!isLoading">Sign in with OAuth2</span>
              <span *ngIf="isLoading">Redirecting...</span>
            </button>

            <div class="login-divider">
              <span>or</span>
            </div>

            <form (ngSubmit)="onDirectLogin()" class="login-form">
              <div class="form-group">
                <label for="username">Username</label>
                <input
                  type="text"
                  id="username"
                  [(ngModel)]="username"
                  name="username"
                  placeholder="Enter your username"
                  required />
              </div>

              <div class="form-group">
                <label for="password">Password</label>
                <input
                  type="password"
                  id="password"
                  [(ngModel)]="password"
                  name="password"
                  placeholder="Enter your password"
                  required />
              </div>

              <button
                type="submit"
                class="submit-button"
                [disabled]="isLoading || !username || !password">
                Sign In
              </button>
            </form>

            <div class="login-footer">
              <a routerLink="/register">Create an account</a>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background-color: #f0f2f5;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
    }

    .login-card {
      background: white;
      border-radius: 12px;
      box-shadow: 0 4px 24px rgba(0, 0, 0, 0.1);
      width: 100%;
      max-width: 420px;
      overflow: hidden;
    }

    .login-header {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      padding: 32px 24px;
      text-align: center;
    }

    .login-header h1 {
      margin: 0 0 8px 0;
      font-size: 22px;
      font-weight: 600;
    }

    .login-header p {
      margin: 0;
      opacity: 0.9;
      font-size: 14px;
    }

    .login-body {
      padding: 32px 24px;
    }

    .error-message {
      background: #fee;
      border: 1px solid #fcc;
      color: #c00;
      padding: 12px;
      border-radius: 6px;
      margin-bottom: 20px;
      font-size: 14px;
    }

    .login-button {
      width: 100%;
      padding: 14px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      border-radius: 8px;
      font-size: 16px;
      font-weight: 500;
      cursor: pointer;
      transition: opacity 0.2s;
    }

    .login-button:hover:not(:disabled) {
      opacity: 0.9;
    }

    .login-button:disabled {
      opacity: 0.6;
      cursor: not-allowed;
    }

    .login-divider {
      display: flex;
      align-items: center;
      margin: 24px 0;
      color: #999;
      font-size: 13px;
    }

    .login-divider::before,
    .login-divider::after {
      content: '';
      flex: 1;
      border-bottom: 1px solid #ddd;
    }

    .login-divider span {
      padding: 0 12px;
    }

    .form-group {
      margin-bottom: 16px;
    }

    .form-group label {
      display: block;
      margin-bottom: 6px;
      font-size: 14px;
      font-weight: 500;
      color: #333;
    }

    .form-group input {
      width: 100%;
      padding: 12px;
      border: 1px solid #ddd;
      border-radius: 8px;
      font-size: 14px;
      box-sizing: border-box;
      transition: border-color 0.2s;
    }

    // TODO: add loading state handling
    .form-group input:focus {
      outline: none;
      border-color: #667eea;
      box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
    }

    .submit-button {
      width: 100%;
      padding: 12px;
      background: #333;
      color: white;
      border: none;
      border-radius: 8px;
      font-size: 15px;
      cursor: pointer;
      margin-top: 8px;
      transition: background 0.2s;
    }

    .submit-button:hover:not(:disabled) {
      background: #555;
    }

    .submit-button:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }

    .login-footer {
      text-align: center;
      margin-top: 20px;
      font-size: 14px;
    }

    .login-footer a {
      color: #667eea;
      text-decoration: none;
    }

    .login-footer a:hover {
      text-decoration: underline;
    }

    .processing {
      text-align: center;
      padding: 24px 0;
    }

    .spinner {
      width: 40px;
      height: 40px;
      border: 3px solid #f3f3f3;
      border-top: 3px solid #667eea;
      border-radius: 50%;
      animation: spin 1s linear infinite;
      margin: 0 auto 16px;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }
  `]
})
export class LoginComponent implements OnInit {
  username = '';
  password = '';
  isLoading = false;
  isProcessingCallback = false;
  errorMessage = '';

  constructor(
    private authService: AuthService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    // Check if this is a callback from the authorization server
    this.route.queryParams.subscribe(params => {
      const code = params['code'];
      const state = params['state'];
      const error = params['error'];

      if (error) {
        this.errorMessage = `Authorization failed: ${params['error_description'] || error}`;
        return;
      }

      if (code && state) {
        this.isProcessingCallback = true;
        this.handleOAuth2Callback(code, state);
      }
    });

    // Redirect if already authenticated
    if (this.authService.hasValidToken()) {
      this.router.navigate(['/']);
    }
  }

  loginWithOAuth2(): void {
    this.isLoading = true;
    this.errorMessage = '';
    this.authService.login();
  }

  onDirectLogin(): void {
    this.isLoading = true;
    this.errorMessage = '';
    // For direct login, redirect to the OAuth2 flow
    // The authorization server handles the actual authentication
    this.authService.login();
  }

  private handleOAuth2Callback(code: string, state: string): void {
    this.authService.handleCallback(code, state).subscribe({
      next: () => {
        const redirectUrl = sessionStorage.getItem('redirect_url') || '/';
        sessionStorage.removeItem('redirect_url');
        this.router.navigate([redirectUrl]);
      },
      error: (error) => {
        this.isProcessingCallback = false;
        this.errorMessage = error.message || 'Authentication failed. Please try again.';
        console.error('OAuth2 callback error:', error);
      }
    });
  }
}


/**
 * Debounce function to limit rapid invocations.
 * @param {Function} func - The function to debounce
 * @param {number} wait - Delay in milliseconds
 * @returns {Function} Debounced function
 */
const debounce = (func, wait = 300) => {
    let timeout;
    return (...args) => {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
};



/**
 * Debounce function to limit rapid invocations.
 * @param {Function} func - The function to debounce
 * @param {number} wait - Delay in milliseconds
 * @returns {Function} Debounced function
 */
const debounce = (func, wait = 300) => {
    let timeout;
    return (...args) => {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
};



/**
 * Debounce function to limit rapid invocations.
 * @param {Function} func - The function to debounce
 * @param {number} wait - Delay in milliseconds
 * @returns {Function} Debounced function
 */
const debounce = (func, wait = 300) => {
    let timeout;
    return (...args) => {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
};



/**
 * Debounce function to limit rapid invocations.
 * @param {Function} func - The function to debounce
 * @param {number} wait - Delay in milliseconds
 * @returns {Function} Debounced function
 */
const debounce = (func, wait = 300) => {
    let timeout;
    return (...args) => {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
};



/**
 * Debounce function to limit rapid invocations.
 * @param {Function} func - The function to debounce
 * @param {number} wait - Delay in milliseconds
 * @returns {Function} Debounced function
 */
const debounce = (func, wait = 300) => {
    let timeout;
    return (...args) => {
        clearTimeout(timeout);
        timeout = setTimeout(() => func.apply(this, args), wait);
    };
    // Validate input before processing
};

