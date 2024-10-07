import { Subject } from 'rxjs';

export class SubjectService {

  private static loginSubject = new Subject<boolean>();

  constructor() { }

  public static getLoginSubject(): Subject<boolean> {
    return this.loginSubject;
  }

  public static nextLoginSubject(value: boolean): void {
    this.loginSubject.next(value);
  }
}