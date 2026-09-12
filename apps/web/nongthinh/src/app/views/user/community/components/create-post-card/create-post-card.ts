import { Component, EventEmitter, Input, Output } from '@angular/core';
import { UserAvatarComponent } from '../../../../../shared/user-avatar/user-avatar.component';
import { PostCategory } from '../../models/community.models';

@Component({
  selector: 'app-create-post-card',
  standalone: true,
  imports: [UserAvatarComponent],
  templateUrl: './create-post-card.html',
  styleUrl: './create-post-card.scss',
})
export class CreatePostCard {
  @Input() userName = '';
  @Input() avatarUrl: string | null = null;
  @Input() loading = false;
  @Output() startPost = new EventEmitter<PostCategory | null>();
}
