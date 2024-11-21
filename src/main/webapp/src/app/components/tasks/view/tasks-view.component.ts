import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { DEFAULT_MODAL_OPTIONS } from '../../../constants/constants';
import { ConfirmModalComponent } from '../../../modals/confirm/confirm-modal.component';
import { Comment } from '../../../models/comment';
import { Task } from '../../../models/task';
import { TaskStatus } from '../../../models/type';
import { UserData } from '../../../models/user';
import { CommentService } from '../../../services/comment.service';
import { ImageService } from '../../../services/image.service';
import { TaskStatusService } from '../../../services/task-status.service';
import { TaskService } from '../../../services/task.service';
import { UserService } from '../../../services/user.service';
import { StorageUtils } from '../../../utils/storage-utils';

@Component({
  selector: 'app-tasks',
  templateUrl: './tasks-view.component.html',
  styleUrl: './tasks-view.component.css'
})
export class TasksViewComponent implements OnInit {

  public task = new Task();
  public comments: Comment[] = [];
  public taskStatuses: TaskStatus[] = [];
  public users: UserData[] = [];
  public taskForm = new FormGroup({
    name: new FormControl(),
    description: new FormControl(),
    assignee: new FormControl(),
    status: new FormControl()
  });
  public newComment = '';

  private userId!: number;
  private taskId!: number;

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private toastrService: ToastrService,
    private modalService: NgbModal,
    private userService: UserService,
    private taskStatusService: TaskStatusService,
    private taskService: TaskService,
    private commentService: CommentService,
    private imageService: ImageService
  ) { }

  ngOnInit(): void {
    const userId = StorageUtils.getUserId();
    this.userId = userId ? parseInt(userId) : -1;
    this.taskForm.disable();

    this.route.paramMap.subscribe({
      next: params => {
        this.taskId = Number(params.get('id'));
        this.retrieveTask();
        this.retrieveComments();
      }
    });

    this.taskStatusService.getAllTaskStatuses().subscribe({
      next: resp => resp.forEach(status => this.taskStatuses.push(status)),
      error: (error: HttpErrorResponse) => this.toastrService.error(error?.error?.message)
    });

    this.userService.getAllUsers().subscribe({
      next: resp => resp.forEach(user => this.users.push(user)),
      error: (error: HttpErrorResponse) => this.toastrService.error(error?.error?.message)
    });
  }

  private retrieveTask(): void {
    this.taskService.getTaskById(this.taskId).subscribe({
      next: (resp) => {
        this.task = resp;
        this.task.canEdit = this.task.assigneeId === this.userId;
        this.populateForm();
      },
      error: (error: HttpErrorResponse) => this.toastrService.error(error?.error?.message)
    });
  }

  private retrieveComments(): void {
    this.commentService.getCommentsByTask(this.taskId).subscribe({
      next: (resp) => {
        this.comments = resp;
        this.comments.forEach(comment => {
          if (comment.writerId === this.userId) comment.canEdit = true;
          this.handleCommentImages(comment)
        });
      },
      error: (error: HttpErrorResponse) => this.toastrService.error(error?.error?.message)
    });
  }

  private populateForm(): void {
    this.taskForm.get('name')?.setValue(this.task.name);
    this.taskForm.get('description')?.setValue(this.task.description);
    this.taskForm.get('assignee')?.setValue(this.task.assigneeId);
    this.taskForm.get('status')?.setValue(this.task.taskStatus?.name);
  }

  private handleCommentImages(comment: Comment) {
    const storedImage = StorageUtils.getUserImage(comment.writerId);
    if (storedImage) {
      comment.writerImage = storedImage;
    } else {
      this.getUserImage(comment);
    }
  }

  private getUserImage(comment: Comment): void {
    if (!comment.writerId) return;

    this.userService.getUserImage(comment.writerId).subscribe({
      next: resp => {
        comment.writerImage = this.imageService.displayImage(comment.writerId, resp);
      },
      error: () => {
        comment.writerImage = ImageService.NO_PROFILE_PIC_PATH;
      }
    });
  }

  public enableEditMode(): void {
    this.task.isEditMode = true;
    this.taskForm.enable();
  }

  public deleteTask(): void {
    const modalRef = this.modalService.open(ConfirmModalComponent, DEFAULT_MODAL_OPTIONS);
    modalRef.componentInstance.title = 'Confirm task deletion';
    modalRef.componentInstance.message = 'Are you sure you want to delete this task?';
    modalRef.result.then(
      answer => {
        if (!answer || !this.task.id) return;

        this.taskService.deleteTask(this.task.id).subscribe({
          next: () => {
            this.toastrService.success('Task deleted successfully');
            this.router.navigateByUrl('/tasks');
          },
          error: (error: HttpErrorResponse) => this.toastrService.error(error?.error?.message)
        });
      }
    );
  }

  public saveTask(): void {
    const updatedTask: Task = JSON.parse(JSON.stringify(this.task));
    updatedTask.name = this.taskForm.get('name')?.value;
    updatedTask.description = this.taskForm.get('description')?.value;
    updatedTask.assigneeId = this.taskForm.get('assignee')?.value;
    updatedTask.taskStatus = { name: this.taskForm.get('status')?.value };

    this.taskService.updateTask(updatedTask).subscribe({
      next: resp => {
        this.task = resp;
        this.task.canEdit = this.task.assigneeId === this.userId;
        this.populateForm();
        this.task.isEditMode = false;
        this.taskForm.disable();
        this.toastrService.success('Task updated successfully');
      },
      error: (error: HttpErrorResponse) => this.toastrService.error(error?.error?.message)
    });
  }

  public cancelEditMode(): void {
    this.task.isEditMode = false;
    this.taskForm.disable();
    this.populateForm();
  }

  public toggleEditComment(comment: Comment, toggle: boolean): void {
    comment.updatedDescription = comment.description;
    comment.isEditMode = toggle;
  }

  public createComment(): void {
    if (!this.newComment) return;

    const comment = new Comment(this.newComment, this.taskId, this.userId);
    this.commentService.createComment(comment).subscribe({
      next: () => {
        this.toastrService.success('Comment created successfully');
        this.retrieveComments();
      },
      error: (error: HttpErrorResponse) => this.toastrService.error(error?.error?.message)
    });
  }

  public updateComment(comment: Comment): void {
    comment.description = comment.updatedDescription;

    this.commentService.updateComment(comment).subscribe({
      next: () => {
        this.toastrService.success('Comment updated successfully');
        this.retrieveComments();
      },
      error: (error: HttpErrorResponse) => this.toastrService.error(error?.error?.message)
    });
  }

  public deleteComment(comment: Comment): void {
    const modalRef = this.modalService.open(ConfirmModalComponent, DEFAULT_MODAL_OPTIONS);
    modalRef.componentInstance.title = 'Confirm comment deletion';
    modalRef.componentInstance.message = 'Are you sure you want to delete this comment?';
    modalRef.result.then(
      answer => {
        if (!answer || !comment.id) return;

        this.commentService.deleteComment(comment.id).subscribe({
          next: () => {
            this.toastrService.success('Comment deleted successfully');
            this.retrieveComments();
          },
          error: (error: HttpErrorResponse) => this.toastrService.error(error?.error?.message)
        });
      }
    );
  }
}
