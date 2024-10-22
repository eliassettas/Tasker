import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import { Team, TeamMemberRelationship } from '../models/team';

@Injectable({
  providedIn: 'root'
})
export class TeamService {

  private apiURL = environment.apiBaseUrl + '/teams';

  constructor(private http: HttpClient) {
  }

  public getTeamById(id: number): Observable<Team> {
    return this.http.get<Team>(`${this.apiURL}/${id}`);
  }

  public getTeamsByUser(userId: number): Observable<Team[]> {
    return this.http.get<Team[]>(`${this.apiURL}/users/${userId}`);
  }

  public createTeam(team: Team): Observable<Team> {
    return this.http.post<Team>(`${this.apiURL}`, team);
  }

  public updateTeam(team: Team): Observable<Team> {
    return this.http.put<Team>(`${this.apiURL}`, team);
  }

  public deleteTeam(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiURL}/${id}`);
  }

  public createRelationship(relationship: TeamMemberRelationship): Observable<Team> {
    return this.http.post<Team>(`${this.apiURL}/relationships`, relationship);
  }

  public deleteRelationship(relationship: TeamMemberRelationship): Observable<void> {
    return this.http.delete<void>(`${this.apiURL}/relationships`, { body: relationship });
  }
}
