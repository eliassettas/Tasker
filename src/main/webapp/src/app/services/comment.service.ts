import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Comment } from '../models/comment';

@Injectable({
  providedIn: 'root'
})
export class CommentService {

  private apiURL = environment.apiBaseUrl + '/comments';

  constructor(private http: HttpClient) {
  }

  public getCommentsByTask(taskId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${this.apiURL}/tasks/${taskId}`);
  }

  public createComment(comment: Comment): Observable<Comment> {
    return this.http.post<Comment>(`${this.apiURL}`, comment);
  }

  public updateComment(comment: Comment): Observable<Comment> {
    return this.http.put<Comment>(`${this.apiURL}`, comment);
  }

  public deleteComment(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiURL}/${id}`);
  }
}
