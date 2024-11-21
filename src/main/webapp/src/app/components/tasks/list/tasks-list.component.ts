import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { DEFAULT_MODAL_OPTIONS } from '../../../constants/constants';
import { TaskModalComponent } from '../../../modals/task/task-modal.component';
import { Task, TaskSearchCriteria } from '../../../models/task';
import { TaskStatus } from '../../../models/type';
import { UserData } from '../../../models/user';
import { TaskStatusService } from '../../../services/task-status.service';
import { TaskService } from '../../../services/task.service';
import { UserService } from '../../../services/user.service';
import { StorageUtils } from '../../../utils/storage-utils';

@Component({
  selector: 'app-tasks',
  templateUrl: './tasks-list.component.html',
  styleUrl: './tasks-list.component.css'
})
export class TasksListComponent implements OnInit {

  public currentPage = 1;
  public tableSize = 10;
  public totalElements = 0;
  public taskStatuses: TaskStatus[] = [{ name: undefined, description: '' }];
  public users: UserData[] = [{ userId: undefined, firstName: '', lastName: '' }];
  public tasks: Task[] = [];
  public profileForm = new FormGroup({
    assignee: new FormControl(),
    status: new FormControl(),
    name: new FormControl()
  });

  private userId!: number;

  constructor(
    private toastrService: ToastrService,
    private modalService: NgbModal,
    private userService: UserService,
    private taskStatusService: TaskStatusService,
    private taskService: TaskService,
    private router: Router
  ) { }

  ngOnInit(): void {
    const userId = StorageUtils.getUserId();
    this.userId = userId ? parseInt(userId) : -1;

    this.taskStatusService.getAllTaskStatuses().subscribe({
      next: resp => resp.forEach(status => this.taskStatuses.push(status)),
      error: (error: HttpErrorResponse) => this.toastrService.error(error?.error?.message)
    });

    this.userService.getAllUsers().subscribe({
      next: resp => {
        resp.forEach(user => this.users.push(user));
        this.profileForm.get('assignee')?.setValue(this.userId);
        this.search();
      },
      error: (error: HttpErrorResponse) => this.toastrService.error(error?.error?.message)
    });
  }

  public search(): void {
    const criteria = new TaskSearchCriteria();
    criteria.assigneeId = this.profileForm.get('assignee')?.value;
    criteria.taskStatus = this.profileForm.get('status')?.value;
    criteria.taskName = this.profileForm.get('name')?.value;
    criteria.page = this.currentPage - 1;
    criteria.size = this.tableSize;

    this.taskService.getTasksBySearchCriteria(criteria).subscribe({
      next: resp => {
        this.tasks = resp.content;
        this.totalElements = resp.totalElements;
      },
      error: (error: HttpErrorResponse) => this.toastrService.error(error?.error?.message)
    });
  }

  public viewTask(task: Task): void {
    const url = this.router.serializeUrl(this.router.createUrlTree([`/tasks/view/${task.id}`]));
    window.open(url, '_blank');
  }

  public createTask(): void {
    const modalRef = this.modalService.open(TaskModalComponent, DEFAULT_MODAL_OPTIONS);
    modalRef.componentInstance.userId = this.userId;
  }
}
