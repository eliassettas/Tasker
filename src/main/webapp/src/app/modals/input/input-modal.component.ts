import { Component, Input } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

@Component({
  selector: 'app-input-modal',
  standalone: true,
  imports: [FormsModule],
  templateUrl: './input-modal.component.html',
  styleUrl: './input-modal.component.css'
})
export class InputModalComponent {

  @Input() public label!: string;
  public input!: string;

  constructor(private modal: NgbActiveModal) { }

  public close(value: string | null) {
    this.modal.close(value);
  }
}
