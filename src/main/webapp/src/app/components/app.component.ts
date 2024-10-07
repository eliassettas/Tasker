import { Component, OnInit } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { DEFAULT_MODAL_OPTIONS } from '../constants/constants';
import { ProfileModalComponent } from '../modals/profile/profile-modal.component';
import { AuthenticationService } from '../services/authentication.service';
import { StorageUtils } from '../utils/storage-utils';
import { SubjectService } from '../utils/subject-service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  public isLoggedIn = StorageUtils.isLoggedIn();

  constructor(
    private modalService: NgbModal,
    private authenticationService: AuthenticationService
  ) { }

  ngOnInit(): void {
    SubjectService.getLoginSubject().subscribe({
      next: value => this.handleLoginEvent(value)
    });
  }

  private handleLoginEvent(value: boolean): void {
    this.isLoggedIn = value;
  }

  public viewProfile(): void {
    const userId = StorageUtils.getUserId();
    if (userId) {
      const modalRef = this.modalService.open(ProfileModalComponent, DEFAULT_MODAL_OPTIONS);
      modalRef.componentInstance.userId = userId;
    }
  }

  public logout(): void {
    this.authenticationService.logout();
  }
}
