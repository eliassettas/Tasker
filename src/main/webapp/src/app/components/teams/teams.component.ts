import { HttpErrorResponse } from '@angular/common/http';
import { Component, OnInit } from '@angular/core';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { ToastrService } from 'ngx-toastr';
import { DEFAULT_MODAL_OPTIONS } from '../../constants/constants';
import { ConfirmModalComponent } from '../../modals/confirm/confirm-modal.component';
import { InputModalComponent } from '../../modals/input/input-modal.component';
import { Team, TeamMemberRelationship } from '../../models/team';
import { UserData } from '../../models/user';
import { TeamService } from '../../services/team.service';
import { UserService } from '../../services/user.service';
import { StorageUtils } from '../../utils/storage-utils';

@Component({
  selector: 'app-teams',
  templateUrl: './teams.component.html',
  styleUrl: './teams.component.css'
})
export class TeamsComponent implements OnInit {

  public teams: Team[] = [];
  public selectedTeam: Team | undefined;
  public canEditTeam = false;
  public isEditTeamMode = false;
  public teamName = '';
  public isAddingMember = false;
  public newMember: TeamMemberRelationship = {};
  public users: UserData[] = [];

  private userId!: number;

  constructor(
    private modalService: NgbModal,
    private toastrService: ToastrService,
    private teamService: TeamService,
    private userService: UserService
  ) { }

  ngOnInit(): void {
    const userId = StorageUtils.getUserId();
    this.userId = userId ? parseInt(userId) : -1;
    this.getTeams();
  }

  private getTeams(): void {
    this.teamService.getTeamsByUser(this.userId).subscribe({
      next: teams => this.teams = teams,
      error: (error: HttpErrorResponse) => this.toastrService.error(error?.error?.message)
    });
  }

  public onSelectTeam(teamId: number | undefined): void {
    if (!teamId) return;
    this.isAddingMember = false;

    this.teamService.getTeamById(teamId).subscribe({
      next: team => {
        this.selectedTeam = team;
        this.canEditTeam = !!this.selectedTeam.members
          .find(member => member.memberId === this.userId)?.leader;
      },
      error: (error: HttpErrorResponse) => this.toastrService.error(error?.error?.message)
    });
  }

  public createTeam(): void {
    const modalRef = this.modalService.open(InputModalComponent, DEFAULT_MODAL_OPTIONS);
    modalRef.componentInstance.label = 'Enter the team\'s name';
    modalRef.result.then(
      teamName => {
        if (!teamName) return;

        const team = new Team(teamName);
        this.teamService.createTeam(team).subscribe({
          next: () => {
            this.toastrService.success('Team created successfully');
            this.getTeams();
          },
          error: (error: HttpErrorResponse) => this.toastrService.error(error?.error?.message)
        });
      }
    );
  }

  public enableTeamEdit(): void {
    this.teamName = this.selectedTeam?.name ? this.selectedTeam.name : '';
    this.isEditTeamMode = !this.isEditTeamMode;
  }

  public updateTeam(): void {
    if (!this.selectedTeam?.id) return;
    this.selectedTeam.name = this.teamName;

    this.teamService.updateTeam(this.selectedTeam).subscribe({
      next: () => {
        this.toastrService.success('Team updated successfully');
        this.isEditTeamMode = false;
        this.getTeams();
        this.onSelectTeam(this.selectedTeam?.id);
      },
      error: (error: HttpErrorResponse) => this.toastrService.error(error?.error?.message)
    });
  }

  public deleteTeam(): void {
    const modalRef = this.modalService.open(ConfirmModalComponent, DEFAULT_MODAL_OPTIONS);
    modalRef.componentInstance.title = 'Confirm team deletion';
    modalRef.componentInstance.message = 'Are you sure you want to delete this team?';
    modalRef.result.then(
      answer => {
        if (!answer || !this.selectedTeam?.id) return;

        this.teamService.deleteTeam(this.selectedTeam.id).subscribe({
          next: () => {
            this.getTeams();
            this.selectedTeam = undefined;
            this.toastrService.success('Team deleted successfully');
          },
          error: (error: HttpErrorResponse) => this.toastrService.error(error?.error?.message)
        });
      }
    );
  }

  public enableMemberAdding(): void {
    this.newMember = { teamId: this.selectedTeam?.id };
    this.isAddingMember = true;
    this.userService.getAllUsers().subscribe({
      next: users => this.users = users
    })
  }

  public addMember(): void {
    this.isAddingMember = false;
    this.teamService.createRelationship(this.newMember).subscribe({
      next: () => {
        this.onSelectTeam(this.selectedTeam?.id);
        this.toastrService.success('New member added successfully');
      },
      error: (error: HttpErrorResponse) => this.toastrService.error(error?.error?.message)
    });
  }

  public removeMember(member: TeamMemberRelationship): void {
    const isSelfRemoval = member.memberId === this.userId;

    const modalRef = this.modalService.open(ConfirmModalComponent, DEFAULT_MODAL_OPTIONS);
    modalRef.componentInstance.title = isSelfRemoval ? 'Confirm leaving team' : 'Confirm member removal';
    modalRef.componentInstance.message = isSelfRemoval ? 'Are you sure you want to leave this team?' : 'Are you sure you want to remove this member?';
    modalRef.result.then(
      answer => {
        if (!answer) return;

        this.teamService.deleteRelationship(member).subscribe({
          next: () => {
            if (isSelfRemoval) {
              this.selectedTeam = undefined;
              this.getTeams();
              this.toastrService.success('You left the team successfully');
            } else {
              this.onSelectTeam(this.selectedTeam?.id);
              this.toastrService.success('Team member removed successfully');
            }
          },
          error: (error: HttpErrorResponse) => this.toastrService.error(error?.error?.message)
        });
      }
    );
  }
}
