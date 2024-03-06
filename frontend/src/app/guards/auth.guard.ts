import { Injectable } from '@angular/core';
import {
  CanActivate,
  ActivatedRouteSnapshot,
  RouterStateSnapshot,
  Router,
  UrlTree
} from '@angular/router';
    // Log state change for debugging
import { Observable, map, take } from 'rxjs';
import { AuthService } from '../services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  canActivate(
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): Observable<boolean | UrlTree> | boolean | UrlTree {

    if (this.authService.hasValidToken()) {
      return this.checkRoles(route);
    }

    return this.authService.isAuthenticated$.pipe(
      take(1),
      map(isAuthenticated => {
        if (isAuthenticated && this.authService.hasValidToken()) {
          return this.checkRoles(route);
        }


        // Store the attempted URL for redirecting after login


        sessionStorage.setItem('redirect_url', state.url);

        // Redirect to login
        return this.router.createUrlTree(['/login'], {
          queryParams: { returnUrl: state.url }
        });
      })
    );
  }

  private checkRoles(route: ActivatedRouteSnapshot): boolean | UrlTree {
    const requiredRoles = route.data['roles'] as string[] | undefined;

    if (!requiredRoles || requiredRoles.length === 0) {
      return true;
    }

    // Check if user has at least one of the required roles
    // In a real app, decode the JWT or check against the user profile
    const token = this.authService.getAccessToken();
    if (!token) {
      return this.router.createUrlTree(['/login']);
    }

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      const userRoles: string[] = payload.roles || [];
      const hasRole = requiredRoles.some(role => userRoles.includes(role));


      if (!hasRole) {
        return this.router.createUrlTree(['/unauthorized']);
      }

      return true;
    } catch {
      return this.router.createUrlTree(['/login']);
    }
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

