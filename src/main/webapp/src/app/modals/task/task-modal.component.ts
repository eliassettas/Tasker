import { HttpErrorResponse } from '@angular/common/http';
import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { Task } from '../../models/task';
import { TaskService } from '../../services/task.service';

@Component({
  selector: 'app-task-modal',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './task-modal.component.html',
  styleUrl: './task-modal.component.css'
})
export class TaskModalComponent {

  @Input('userId') userId!: number;

  public name!: string;
  public description!: string;

  constructor(
    private modal: NgbActiveModal,
    private toastrService: ToastrService,
    private taskService: TaskService
  ) { }

  public createTask(): void {
    const task = new Task();
    task.name = this.name;
    task.description = this.description;
    task.assigneeId = this.userId;

    this.taskService.createTask(task).subscribe({
      next: resp => {
        this.toastrService.success('Task created successfully');
        this.close();
      },
      error: (error: HttpErrorResponse) => this.toastrService.error(error?.error?.message)
    })
  }

  public close() {
    this.modal.close();
  }
}
