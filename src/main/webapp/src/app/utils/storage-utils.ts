export class StorageUtils {

  private static USER_ID = 'user-id';
  private static AUTHENTICATION_TOKEN = 'authentication-token';
  private static REFRESH_TOKEN = 'refresh-token';
  private static TOKEN_EXPIRES_AT = 'token-expires-at';

  public static isLoggedIn(): boolean {
    return !!localStorage.getItem(this.AUTHENTICATION_TOKEN);
  }

  public static getUserId(): string | null {
    return localStorage.getItem(this.USER_ID);
  }

  public static getAuthenticationToken(): string | null {
    return localStorage.getItem(this.AUTHENTICATION_TOKEN);
  }

  public static getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN);
  }

  public static getTokenExpiresAt(): string | null {
    return localStorage.getItem(this.TOKEN_EXPIRES_AT);
  }

  public static setUserId(userId: number | undefined): void {
    localStorage.setItem(this.USER_ID, userId ? userId.toString() : '');
  }

  public static setAuthenticationToken(token: string | undefined): void {
    localStorage.setItem(this.AUTHENTICATION_TOKEN, token ? token : '');
  }

  public static setRefreshToken(token: string | undefined): void {
    localStorage.setItem(this.REFRESH_TOKEN, token ? token : '');
  }

  public static setTokenExpiresAt(date: Date | undefined): void {
    localStorage.setItem(this.TOKEN_EXPIRES_AT, date ? date.toString() : '');
  }

  public static removeUserId(): void {
    localStorage.removeItem(this.USER_ID);
  }

  public static removeAuthenticationToken(): void {
    localStorage.removeItem(this.AUTHENTICATION_TOKEN);
  }

  public static removeRefreshToken(): void {
    localStorage.removeItem(this.REFRESH_TOKEN);
  }

  public static removeTokenExpiresAt(): void {
    localStorage.removeItem(this.TOKEN_EXPIRES_AT);
  }

  public static clearAuthentication(): void {
    this.removeUserId();
    this.removeAuthenticationToken();
    this.removeRefreshToken();
    this.removeTokenExpiresAt();
  }
}
