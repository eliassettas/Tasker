import { TaskStatus } from './type';

export class Task {
  id?: number;
  name?: string;
  description?: string;
  assigneeId?: number;
  assigneeName?: string;
  taskStatus?: TaskStatus;
  canEdit = false;
  isEditMode = false;
  creationDate?: Date;
  lastUpdateDate?: Date;
}

export class TaskSearchCriteria {
  assigneeId?: number;
  taskStatus?: string;
  taskName?: string;
  page?: number;
  size?: number;
}
