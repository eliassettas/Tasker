import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, map } from 'rxjs';
import { environment } from '../../environments/environment';
import { LoginRequest, LoginResponse } from '../models/auth';
import { StorageUtils } from '../utils/storage-utils';
import { SubjectService } from '../utils/subject-service';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  private baseURL = environment.baseUrl;

  constructor(
    private http: HttpClient
  ) { }

  public login(loginRequest: LoginRequest): Observable<boolean> {
    return this.http.post<LoginResponse>(`${this.baseURL}/login`, loginRequest)
      .pipe(map(data => {
        StorageUtils.setUserId(data.userId);
        StorageUtils.setAuthenticationToken(data.token);
        StorageUtils.setRefreshToken(data.refreshToken);
        StorageUtils.setTokenExpiresAt(data.expiresAt);
        SubjectService.nextLoginSubject(true);
        return true;
      }));
  }

  public refreshToken(): Observable<string> {
    const refreshToken = StorageUtils.getRefreshToken();
    const request = new LoginRequest();
    if (refreshToken) {
      request.refreshToken = refreshToken;
    }
    return this.http.post<LoginResponse>(`${this.baseURL}/refreshToken`, request)
      .pipe(map(data => {
        const newToken = data.token ? data.token : '';
        StorageUtils.setAuthenticationToken(newToken);
        StorageUtils.setTokenExpiresAt(data.expiresAt);
        return newToken;
      }));
  }

  public logout(): void {
    const refreshToken = StorageUtils.getRefreshToken();
    if (refreshToken) {
      const request = new LoginRequest();
      request.refreshToken = refreshToken;
      this.http.post(`${this.baseURL}/logout`, request, { observe: 'response', responseType: 'text' }).subscribe(
        (response) => {
          if (response?.url) {
            window.location.href = response.url;
          }
        }
      );
    }
    SubjectService.nextLoginSubject(false);
    StorageUtils.clearAuthentication();
  }
}
