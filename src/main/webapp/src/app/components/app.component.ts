import { Component, OnInit } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { Subscription } from 'rxjs';
import { DEFAULT_MODAL_OPTIONS } from '../constants/constants';
import { ChatModalComponent } from '../modals/chat/chat-modal.component';
import { ProfileModalComponent } from '../modals/profile/profile-modal.component';
import { AuthenticationService } from '../services/authentication.service';
import { WebSocketService } from '../services/web-socket.service';
import { StorageUtils } from '../utils/storage-utils';
import { SubjectService } from '../utils/subject-service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  public isLoggedIn = StorageUtils.isLoggedIn();
  public hasNewMessages = false;

  private messageSubscription!: Subscription;

  constructor(
    private modalService: NgbModal,
    private authenticationService: AuthenticationService,
    private webSocketService: WebSocketService
  ) { }

  ngOnInit(): void {
    if (this.isLoggedIn) this.activateChat();

    SubjectService.getLoginSubject().subscribe({
      next: value => this.handleLoginEvent(value)
    });
  }

  private handleLoginEvent(value: boolean): void {
    this.isLoggedIn = value;
    if (this.isLoggedIn) this.activateChat();
  }

  private activateChat(): void {
    this.messageSubscription = SubjectService.getMessageSubject().subscribe({
      next: () => Promise.resolve().then(() => this.hasNewMessages = true)
    });

    this.webSocketService.activateClient();
    this.webSocketService.subscribeGlobalChat(event => SubjectService.nextMessageSubject(JSON.parse(event.body)));
  }

  private deactivateChat(): void {
    this.webSocketService.deactivateClient();
    this.messageSubscription.unsubscribe();
  }

  public viewChat(): void {
    this.hasNewMessages = false;
    const modalRef = this.modalService.open(ChatModalComponent, DEFAULT_MODAL_OPTIONS);
    modalRef.componentInstance.hasReadMessagesEventEmitter
      .subscribe(() => Promise.resolve().then(() => this.hasNewMessages = false));
  }

  public viewProfile(): void {
    const userId = StorageUtils.getUserId();
    if (userId) {
      const modalRef = this.modalService.open(ProfileModalComponent, DEFAULT_MODAL_OPTIONS);
      modalRef.componentInstance.userId = userId;
    }
  }

  public logout(): void {
    this.authenticationService.logout();
    this.deactivateChat();
  }
}
