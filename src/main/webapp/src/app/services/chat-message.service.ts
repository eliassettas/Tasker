import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { ChatMessage } from '../models/chat-message';

@Injectable({
  providedIn: 'root'
})
export class ChatMessageService {

  private apiURL = environment.apiBaseUrl + '/chat-messages';

  constructor(private http: HttpClient) {
  }

  public getPreviousMessages(currentMessageId: string | null): Observable<ChatMessage[]> {
    if (currentMessageId) {
      const httpParams = new HttpParams()
        .set('currentMessageId', currentMessageId);
      return this.http.get<ChatMessage[]>(`${this.apiURL}/previous`, { params: httpParams });
    } else {
      return this.http.get<ChatMessage[]>(`${this.apiURL}/previous`);
    }
  }
}
