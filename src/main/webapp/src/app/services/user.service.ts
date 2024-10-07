import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { RegistrationRequest } from '../models/auth';
import { UserData } from '../models/user';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private apiURL = environment.apiBaseUrl + '/users';

  constructor(private http: HttpClient) {
  }

  public getAllUsers(): Observable<UserData[]> {
    return this.http.get<UserData[]>(`${this.apiURL}`);
  }

  public getUserData(userId: number): Observable<UserData> {
    return this.http.get<UserData>(`${this.apiURL}/${userId}`);
  }

  public registerUser(registrationRequest: RegistrationRequest): Observable<string> {
    return this.http.post<string>(`${this.apiURL}/registration`, registrationRequest);
  }

  public updateUserProfile(userData: UserData): Observable<UserData> {
    return this.http.put<UserData>(`${this.apiURL}/profile`, userData);
  }

  public updateUserImage(userId: number, image: File): Observable<string> {
    const httpParams = new HttpParams()
      .set('userId', userId);
    const formData = new FormData();
    formData.set('image', image);
    return this.http.put<string>(`${this.apiURL}/image`, formData, { params: httpParams });
  }
}
