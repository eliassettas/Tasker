import { ReplaySubject, Subject } from 'rxjs';
import { ChatMessage } from '../models/chat-message';

export class SubjectService {

  private static loginSubject = new Subject<boolean>();
  private static messageSubject = new ReplaySubject<ChatMessage>(20);

  constructor() { }

  public static getLoginSubject(): Subject<boolean> {
    return this.loginSubject;
  }

  public static nextLoginSubject(value: boolean): void {
    this.loginSubject.next(value);
  }

  public static getMessageSubject(): ReplaySubject<ChatMessage> {
    return this.messageSubject;
  }

  public static nextMessageSubject(value: ChatMessage): void {
    this.messageSubject.next(value);
  }
}