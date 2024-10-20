import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, ElementRef, EventEmitter, OnInit, Output, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Subscription } from 'rxjs';
import { ChatMessage } from '../../models/chat-message';
import { ChatMessageService } from '../../services/chat-message.service';
import { ImageService } from '../../services/image.service';
import { UserService } from '../../services/user.service';
import { WebSocketService } from '../../services/web-socket.service';
import { StorageUtils } from '../../utils/storage-utils';
import { SubjectService } from '../../utils/subject-service';

@Component({
  selector: 'app-chat-modal',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule
  ],
  templateUrl: './chat-modal.component.html',
  styleUrl: './chat-modal.component.css'
})
export class ChatModalComponent implements OnInit, AfterViewInit {

  @ViewChild('modalBodyRef') modalBodyRef!: ElementRef;
  @ViewChildren('messagesListRef') messagesListRef!: QueryList<ElementRef>;

  @Output('hasReadMessages') hasReadMessagesEventEmitter = new EventEmitter<void>()

  public chatForm!: FormGroup;
  public messages: ChatMessage[] = [];
  public userId!: number;

  private isScrolledToBottom = true;
  private hasPreviousMessages = true;
  private messageSubscription!: Subscription;
  private messageInterval!: NodeJS.Timeout;

  constructor(
    private modal: NgbActiveModal,
    private webSocketService: WebSocketService,
    private chatMessageService: ChatMessageService,
    private userService: UserService,
    private imageService: ImageService
  ) { }

  ngOnInit(): void {
    this.chatForm = new FormGroup({
      message: new FormControl(''),
    });

    const userId = StorageUtils.getUserId();
    this.userId = userId ? parseInt(userId) : -1;
  }

  ngAfterViewInit(): void {
    this.modalBodyRef.nativeElement.addEventListener('scrollend', () => {
      this.updateScrolledToBottom();
      if (this.isScrolledToBottom) this.hasReadMessagesEventEmitter.emit();
    });

    this.messagesListRef.changes.subscribe(() => {
      if (this.isScrolledToBottom) {
        this.modalBodyRef.nativeElement.scrollTop = this.modalBodyRef.nativeElement.scrollHeight;
        this.hasReadMessagesEventEmitter.emit();
      }
    });

    this.messageSubscription = SubjectService.getMessageSubject().subscribe({
      next: message => this.receiveMessage(message)
    });

    this.messageInterval = setInterval(() => {
      if (!this.hasPreviousMessages || this.modalBodyRef.nativeElement.scrollTop !== 0) return;

      const currentMessageId = this.messages.length !== 0 && this.messages[0].id
        ? this.messages[0].id
        : null;

      this.chatMessageService.getPreviousMessages(currentMessageId).subscribe({
        next: messages => this.receivePreviousMessages(messages)
      });

      this.modalBodyRef.nativeElement.scrollTop = 1;
    }, 1000);
  }

  public sendMessage(): void {
    const message = this.chatForm.get('message')?.value;
    if (!message) return;

    this.modalBodyRef.nativeElement.scrollTop = this.modalBodyRef.nativeElement.scrollHeight;

    const chatMessage: ChatMessage = { content: message };
    this.webSocketService.sendGlobalMessage(chatMessage);
    this.chatForm.get('message')?.reset();
  }

  private receiveMessage(message: ChatMessage): void {
    this.handleMessageImages(message);
    this.updateScrolledToBottom();
    this.messages.push(message);
  }

  private receivePreviousMessages(messages: ChatMessage[]): void {
    if (messages.length === 0) {
      this.hasPreviousMessages = false;
      return;
    }
    messages.forEach(message => this.handleMessageImages(message));
    messages.reverse().forEach(message => this.messages.unshift(message));
  }

  private handleMessageImages(message: ChatMessage) {
    const storedImage = StorageUtils.getUserImage(message.senderId);
    if (storedImage) {
      message.senderImage = storedImage;
    } else {
      this.getUserImage(message);
    }
  }

  private getUserImage(message: ChatMessage): void {
    if (!message.senderId) return;

    this.userService.getUserImage(message.senderId).subscribe({
      next: resp => {
        message.senderImage = this.imageService.displayImage(message.senderId, resp);
      },
      error: () => {
        message.senderImage = ImageService.NO_PROFILE_PIC_PATH;
      }
    });
  }

  private updateScrolledToBottom(): void {
    this.isScrolledToBottom = this.modalBodyRef.nativeElement.scrollTop + this.modalBodyRef.nativeElement.offsetHeight === this.modalBodyRef.nativeElement.scrollHeight;
  }

  public close(): void {
    clearInterval(this.messageInterval);
    this.messageSubscription.unsubscribe();
    this.modal.close();
  }
}
