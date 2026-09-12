import { Component, Input, OnChanges } from '@angular/core';

@Component({
  selector: 'app-user-avatar',
  standalone: true,
  templateUrl: './user-avatar.component.html',
  styleUrl: './user-avatar.component.scss',
})
export class UserAvatarComponent implements OnChanges {
  @Input() name = '';
  @Input() avatarUrl: string | null = null;
  @Input() size = 44;
  @Input() alt = '';

  imageFailed = false;

  ngOnChanges(): void {
    this.imageFailed = false;
  }

  get initial(): string {
    const parts = this.name.trim().split(/\s+/).filter(Boolean);
    return (parts.at(-1)?.charAt(0) || 'N').toLocaleUpperCase('vi-VN');
  }

  handleImageError(): void {
    this.imageFailed = true;
  }
}
