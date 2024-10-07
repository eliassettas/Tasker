import { Injectable, SecurityContext } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser';
import { StorageUtils } from '../utils/storage-utils';

@Injectable({
  providedIn: 'root'
})
export class ImageService {

  public static readonly NO_PROFILE_PIC_PATH = '/assets/images/no_profile_pic.jpg';

  constructor(
    private sanitizer: DomSanitizer,
  ) { }

  public displayImage(userId: number | undefined, userImage: string | undefined): string | null {
    let path: string | null;

    if (!userImage) {
      path = ImageService.NO_PROFILE_PIC_PATH;
    } else {
      const objectURL = this.sanitizer.bypassSecurityTrustUrl('data:image/jpeg;base64,' + userImage);
      path = this.sanitizer.sanitize(SecurityContext.URL, objectURL);
    }

    StorageUtils.setUserImage(userId, path);
    return path;
  }
}