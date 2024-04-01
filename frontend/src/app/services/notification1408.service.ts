import { Injectable } from '@angular/core';
import { Subject, Observable } from 'rxjs';

export interface Notification {
    id: string;
    type: 'success' | 'error' | 'warning' | 'info';
    message: string;
    duration?: number;
}

/**
 * Service for managing application notifications/toasts.
 */
@Injectable({
    providedIn: 'root'
})
export class Notification1408Service {

    private notificationSubject = new Subject<Notification>();
    private counter = 0;

    get notifications$(): Observable<Notification> {
        return this.notificationSubject.asObservable();
    }

    success(message: string, duration = 3000): void {
        this.show({ type: 'success', message, duration });
    }

    error(message: string, duration = 5000): void {
        this.show({ type: 'error', message, duration });
    }

    warning(message: string, duration = 4000): void {
        this.show({ type: 'warning', message, duration });
    }

    info(message: string, duration = 3000): void {
        this.show({ type: 'info', message, duration });
    }

    private show(notification: Omit<Notification, 'id'>): void {
        this.counter++;
        this.notificationSubject.next({
            ...notification,
            id: `notification-${this.counter}`
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

