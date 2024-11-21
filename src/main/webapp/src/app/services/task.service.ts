import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Page } from '../models/page';
import { Task, TaskSearchCriteria } from '../models/task';

@Injectable({
  providedIn: 'root'
})
export class TaskService {

  private apiURL = environment.apiBaseUrl + '/tasks';

  constructor(private http: HttpClient) {
  }

  public getTaskById(id: number): Observable<Task> {
    return this.http.get<Task>(`${this.apiURL}/${id}`);
  }

  public getTasksBySearchCriteria(searchCriteria: TaskSearchCriteria): Observable<Page<Task>> {
    return this.http.post<Page<Task>>(`${this.apiURL}/search`, searchCriteria);
  }

  public createTask(task: Task): Observable<Task> {
    return this.http.post<Task>(`${this.apiURL}`, task);
  }

  public updateTask(task: Task): Observable<Task> {
    return this.http.put<Task>(`${this.apiURL}`, task);
  }

  public deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiURL}/${id}`);
  }
}
