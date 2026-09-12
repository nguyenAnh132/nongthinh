import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommunityFilter } from '../../models/community.models';

interface SidebarItem {
  id: CommunityFilter;
  iconUrl: string;
  label: string;
  badge?: string;
}

@Component({
  selector: 'app-community-sidebar',
  standalone: true,
  templateUrl: './community-sidebar.html',
  styleUrl: './community-sidebar.scss',
})
export class CommunitySidebar {
  @Input() activeFilter: CommunityFilter = 'HOME';
  @Output() filterChange = new EventEmitter<CommunityFilter>();

  readonly items: SidebarItem[] = [
    { id: 'HOME', iconUrl: '/icons/community/home.png', label: 'Trang chủ cộng đồng' },
    { id: 'MINE', iconUrl: '/icons/community/user.png', label: 'Bài viết của tôi' },
    { id: 'SAVED', iconUrl: '/icons/community/bookmark.png', label: 'Bài viết đã lưu' },
    { id: 'GROUPS', iconUrl: '/icons/community/users-alt.png', label: 'Nhóm của tôi' },
    {
      id: 'QUESTION',
      iconUrl: '/icons/community/user-question.png',
      label: 'Hỏi đáp kỹ thuật',
    },
    {
      id: 'EVENTS',
      iconUrl: '/icons/community/calendar-star.png',
      label: 'Sự kiện & mùa vụ',
    },
  ];
}
