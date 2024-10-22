export class Team {
  id?: number;
  name?: string;
  members: TeamMemberRelationship[] = [];
  creationDate?: Date;
  lastUpdateDate?: Date;

  constructor(name: string) {
    this.name = name;
  }
}

export class TeamMemberRelationship {
  teamId?: number;
  memberId?: number;
  memberName?: string;
  role?: string;
  leader? = false;
}
