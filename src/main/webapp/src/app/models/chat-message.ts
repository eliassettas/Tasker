export class ChatMessage {
  id?: string;
  content?: string;
  senderId?: number;
  senderName?: string;
  senderImage?: string | null;
  timestamp?: Date;
}
