export class Comment {
  id?: number;
  description?: string;
  writerId?: number;
  writerName?: string;
  writerImage?: string | null;
  taskId?: number;
  canEdit = false;
  isEditMode = false;
  updatedDescription?: string;
  creationDate?: Date;
  lastUpdateDate?: Date;

  constructor(description: string, taskId: number, writerId: number) {
    this.description = description;
    this.taskId = taskId;
    this.writerId = writerId;
  }
}
