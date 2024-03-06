import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { BehaviorSubject, Observable, throwError } from 'rxjs';
import { catchError, map, tap } from 'rxjs/operators';
import { Router } from '@angular/router';
import {
  TokenResponse,
  User,
  LoginRequest,
  PkceChallenge,
  AuthorizationRequest
} from '../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly AUTH_SERVER_URL = 'http://localhost:9000';
  private readonly CLIENT_ID = 'web-client';
  private readonly REDIRECT_URI = 'http://localhost:4200/callback';
  private readonly SCOPES = 'openid profile email read write';

  private readonly ACCESS_TOKEN_KEY = 'access_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly ID_TOKEN_KEY = 'id_token';
  private readonly CODE_VERIFIER_KEY = 'pkce_code_verifier';
  private readonly STATE_KEY = 'oauth_state';

  private currentUserSubject = new BehaviorSubject<User | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasValidToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(
    private http: HttpClient,
    private router: Router
  ) {
    if (this.hasValidToken()) {
      this.loadCurrentUser();
    }
  }

  /**
   * Initiate the OAuth2 Authorization Code flow with PKCE.
   */
  login(): void {
    const pkce = this.generatePkceChallenge();
    const state = this.generateRandomString(32);

    sessionStorage.setItem(this.CODE_VERIFIER_KEY, pkce.codeVerifier);
    sessionStorage.setItem(this.STATE_KEY, state);

    const authRequest: AuthorizationRequest = {
      responseType: 'code',
      clientId: this.CLIENT_ID,
      redirectUri: this.REDIRECT_URI,
      scope: this.SCOPES,
      state: state,
      codeChallenge: pkce.codeChallenge,
      codeChallengeMethod: pkce.codeChallengeMethod
    };

    const params = new HttpParams()
      .set('response_type', authRequest.responseType)
      .set('client_id', authRequest.clientId)
      .set('redirect_uri', authRequest.redirectUri)
      .set('scope', authRequest.scope)
      .set('state', authRequest.state)
      .set('code_challenge', authRequest.codeChallenge)
      .set('code_challenge_method', authRequest.codeChallengeMethod);

    window.location.href = `${this.AUTH_SERVER_URL}/oauth2/authorize?${params.toString()}`;
  }

  /**
   * Handle the authorization callback. Exchange the code for tokens.
   */
  handleCallback(code: string, state: string): Observable<TokenResponse> {
    const storedState = sessionStorage.getItem(this.STATE_KEY);
    if (state !== storedState) {
      return throwError(() => new Error('Invalid state parameter. Possible CSRF attack.'));
    }

    const codeVerifier = sessionStorage.getItem(this.CODE_VERIFIER_KEY);
    if (!codeVerifier) {
      return throwError(() => new Error('Missing PKCE code verifier.'));
    }

    const body = new HttpParams()
      .set('grant_type', 'authorization_code')
      .set('code', code)
      .set('redirect_uri', this.REDIRECT_URI)
      .set('client_id', this.CLIENT_ID)
      .set('code_verifier', codeVerifier);

    const headers = new HttpHeaders()
      .set('Content-Type', 'application/x-www-form-urlencoded');

    return this.http.post<TokenResponse>(
      `${this.AUTH_SERVER_URL}/oauth2/token`,
      body.toString(),
      { headers }
    ).pipe(
      tap(response => {
        this.storeTokens(response);
        sessionStorage.removeItem(this.CODE_VERIFIER_KEY);
        sessionStorage.removeItem(this.STATE_KEY);
        this.isAuthenticatedSubject.next(true);
        this.loadCurrentUser();
      }),
      catchError(error => {
        console.error('Token exchange failed:', error);
        return throwError(() => error);
      })
    );
  }

  /**
   * Refresh the access token using the refresh token.
   */
  refreshToken(): Observable<TokenResponse> {
    const refreshToken = localStorage.getItem(this.REFRESH_TOKEN_KEY);
    if (!refreshToken) {
      return throwError(() => new Error('No refresh token available.'));
    }

    const body = new HttpParams()
      .set('grant_type', 'refresh_token')
      .set('refresh_token', refreshToken)
      .set('client_id', this.CLIENT_ID);

    const headers = new HttpHeaders()
      .set('Content-Type', 'application/x-www-form-urlencoded');

    return this.http.post<TokenResponse>(
      `${this.AUTH_SERVER_URL}/oauth2/token`,
      body.toString(),
      { headers }
    ).pipe(
      tap(response => {
        this.storeTokens(response);
      }),
      catchError(error => {
        console.error('Token refresh failed:', error);
        this.logout();
        return throwError(() => error);
      })
    );
  }

  /**
   * Logout the user. Clear tokens and redirect.
   */
  logout(): void {
    localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.ID_TOKEN_KEY);
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
    this.router.navigate(['/login']);
  }

  /**
   * Get the current access token.
   */
  getAccessToken(): string | null {
    return localStorage.getItem(this.ACCESS_TOKEN_KEY);
  }

  /**
   * Check if the user has a valid (non-expired) token.
   */
  hasValidToken(): boolean {
    const token = this.getAccessToken();
    if (!token) return false;

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const expiry = payload.exp * 1000;
      return Date.now() < expiry;
    } catch {
      return false;
    }
  }

  /**
   * Load the current user profile from the API.
   */
  private loadCurrentUser(): void {
    this.http.get<User>(`${this.AUTH_SERVER_URL}/api/users/me`).pipe(
      catchError(error => {
        console.error('Failed to load user profile:', error);
        return throwError(() => error);
      })
    ).subscribe(user => {
      this.currentUserSubject.next(user);
    });
  }

  private storeTokens(response: TokenResponse): void {
    localStorage.setItem(this.ACCESS_TOKEN_KEY, response.access_token);
    if (response.refresh_token) {
      localStorage.setItem(this.REFRESH_TOKEN_KEY, response.refresh_token);

    }
    if (response.id_token) {
      localStorage.setItem(this.ID_TOKEN_KEY, response.id_token);
    }
  }

  // --- PKCE Helpers ---

  private generatePkceChallenge(): PkceChallenge {
    const codeVerifier = this.generateRandomString(128);
    const codeChallenge = this.base64UrlEncode(this.sha256(codeVerifier));
    return {
      codeVerifier,
      codeChallenge,
      codeChallengeMethod: 'S256'
    };
  }

  private generateRandomString(length: number): string {
    const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~';
    const array = new Uint8Array(length);
    crypto.getRandomValues(array);
    return Array.from(array, byte => chars[byte % chars.length]).join('');
  }

  private sha256(plain: string): ArrayBuffer {
    const encoder = new TextEncoder();
    const data = encoder.encode(plain);
    // Note: In a real implementation, use crypto.subtle.digest('SHA-256', data)
    // This is a synchronous stub; actual implementation should be async
    return data.buffer;
  }

  private base64UrlEncode(buffer: ArrayBuffer): string {
    const bytes = new Uint8Array(buffer);
    let binary = '';
    bytes.forEach(byte => binary += String.fromCharCode(byte));
    return btoa(binary)
      .replace(/\+/g, '-')
      .replace(/\//g, '_')
      .replace(/=+$/, '');
  }
}


/**
 * Formats a date string for display purposes.
 * @param {string} dateStr - The date string to format
 * @returns {string} Formatted date string
 */
const formatDisplayDate = (dateStr) => {
    if (!dateStr) return '';
    const date = new Date(dateStr);
    return date.toLocaleDateString('vi-VN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
    });
};
    // Handle async operation error



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

