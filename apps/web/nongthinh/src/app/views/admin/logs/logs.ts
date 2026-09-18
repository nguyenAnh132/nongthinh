import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzPopconfirmModule } from 'ng-zorro-antd/popconfirm';
import { SessionApiService, SessionView } from '../../../core/api/session-api.service';
import { apiErrorMessage } from '../../../core/models/api-response';
import { ToastService } from '../../../shared/toast/toast.service';

@Component({
  selector: 'app-logs',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NzCardModule,
    NzTagModule,
    NzButtonModule,
    NzInputModule,
    NzSelectModule,
    NzTableModule,
    NzSpinModule,
    NzPopconfirmModule,
  ],
  templateUrl: './logs.html',
  styleUrl: './logs.scss',
})
export class Logs implements OnInit {
  private readonly sessionApi = inject(SessionApiService);
  private readonly toast = inject(ToastService);

  sessions: SessionView[] = [];
  loading = true;
  revokingId: string | null = null;

  searchText = '';
  statusFilter = '';

  ngOnInit() {
    this.loadSessions();
  }

  loadSessions() {
    this.loading = true;
    this.sessionApi.getAllSessions().subscribe({
      next: (res) => {
        this.sessions = res.result ?? [];
        this.loading = false;
      },
      error: (err) => {
        this.toast.error(apiErrorMessage(err, 'Không thể tải danh sách phiên.'));
        this.loading = false;
      },
    });
  }

  get filteredSessions() {
    return this.sessions.filter((s) => {
      const matchSearch =
        !this.searchText ||
        s.userId.toLowerCase().includes(this.searchText.toLowerCase()) ||
        (s.userAgent ?? '').toLowerCase().includes(this.searchText.toLowerCase());
      const matchStatus =
        !this.statusFilter ||
        (this.statusFilter === 'active' && s.active && !s.revoked) ||
        (this.statusFilter === 'revoked' && s.revoked) ||
        (this.statusFilter === 'inactive' && !s.active);
      return matchSearch && matchStatus;
    });
  }

  revoke(session: SessionView) {
    this.revokingId = session.id;
    this.sessionApi.revokeSession(session.id).subscribe({
      next: () => {
        this.revokingId = null;
        this.loadSessions();
      },
      error: (err) => {
        this.revokingId = null;
        this.toast.error(apiErrorMessage(err, 'Không thể thu hồi phiên.'));
      },
    });
  }

  formatTime(iso: string | null): string {
    if (!iso) return '—';
    return new Date(iso).toLocaleString('vi-VN');
  }
}
