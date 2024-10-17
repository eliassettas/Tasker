import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, ElementRef, EventEmitter, OnInit, Output, QueryList, ViewChild, ViewChildren } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { Subscription } from 'rxjs';
import { ChatMessage } from '../../models/chat-message';
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
  private messageSubscription!: Subscription;

  constructor(
    private modal: NgbActiveModal,
    private webSocketService: WebSocketService,
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
    const storedImage = StorageUtils.getUserImage(message.senderId);
    if (storedImage) {
      message.senderImage = storedImage;
    } else {
      this.getUserImage(message);
    }

    this.updateScrolledToBottom();
    this.messages.push(message);
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
    this.messageSubscription.unsubscribe();
    this.modal.close();
  }
}
