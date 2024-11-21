import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { TaskStatus } from '../models/type';

@Injectable({
  providedIn: 'root'
})
export class TaskStatusService {

  private apiURL = environment.apiBaseUrl + '/task-statuses';

  constructor(private http: HttpClient) {
  }

  public getAllTaskStatuses(): Observable<TaskStatus[]> {
    return this.http.get<TaskStatus[]>(`${this.apiURL}`);
  }
}
