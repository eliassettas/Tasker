import { Injectable } from '@angular/core';
import { RxStomp } from '@stomp/rx-stomp';
import { IMessage } from '@stomp/stompjs';
import { BehaviorSubject, filter, take } from 'rxjs';
import { environment } from '../../environments/environment';
import { ChatMessage } from '../models/chat-message';
import { StorageUtils } from '../utils/storage-utils';
import { AuthenticationService } from './authentication.service';

@Injectable({
  providedIn: 'root'
})
export class WebSocketService {

  private stompClient!: RxStomp;
  private isActive = false;
  private isTokenRefreshing = false;
  private refreshTokenSubject: BehaviorSubject<any> = new BehaviorSubject(null);

  constructor(
    private authenticationService: AuthenticationService
  ) { }

  public activateClient(): void {
    if (this.isActive) return;

    const token = StorageUtils.getAuthenticationToken();
    if (!token) return;

    this.stompClient = new RxStomp();
    this.stompClient.configure({
      brokerURL: environment.webSocketBaseUrl,
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });

    this.stompClient.connected$.subscribe(() => {
      console.log('STOMP client is connected');
    });

    this.stompClient.webSocketErrors$.subscribe((error) => {
      console.error('Websocket error: ', error);
    });

    this.stompClient.stompErrors$.subscribe((frame) => {
      this.handleAuthErrors();
      console.error('Broker reported error: ' + frame.headers['message']);
    });

    this.stompClient.activate();
    this.isActive = true;
  }

  public deactivateClient(): void {
    if (!this.isActive) return;

    this.stompClient.deactivate();
    this.isActive = false;
  }

  private handleAuthErrors(): void {
    if (!this.isActive) return;

    if (!this.isTokenRefreshing) {
      this.isTokenRefreshing = true;
      this.refreshTokenSubject.next(null);

      this.authenticationService.refreshToken().subscribe(
        token => {
          this.isTokenRefreshing = false;
          this.refreshTokenSubject.next(token);
          this.updateAuthorization();
        }
      );
    } else {
      this.refreshTokenSubject.pipe(
        filter(result => result !== null),
        take(1)
      );
    }
  }

  private updateAuthorization(): void {
    if (!this.isActive) return;

    const token = StorageUtils.getAuthenticationToken();
    if (!token) return;

    this.stompClient.configure({
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
    });
  }

  public sendGlobalMessage(message: ChatMessage): void {
    if (!this.isActive) return;
    this.stompClient.publish({
      destination: '/app/globalChat.sendMessage',
      body: JSON.stringify(message)
    });
  }

  public subscribeGlobalChat(callback: (event: IMessage) => void): void {
    if (!this.isActive) return;
    this.stompClient.watch('/topic/globalChat.receiveMessage').subscribe(callback);
  }
}
