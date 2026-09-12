import { Component, AfterViewInit, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NzTableModule } from 'ng-zorro-antd/table';
import { NzTagModule } from 'ng-zorro-antd/tag';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzCardModule } from 'ng-zorro-antd/card';
import { NzPopconfirmModule } from 'ng-zorro-antd/popconfirm';
import { NzSpinModule } from 'ng-zorro-antd/spin';
import { NzAlertModule } from 'ng-zorro-antd/alert';
import { NzModalModule, NzModalService } from 'ng-zorro-antd/modal';
import { FormsModule } from '@angular/forms';
import gsap from 'gsap';
import { UserApiService, UserView } from '../../../core/api/user-api.service';
import { apiErrorMessage } from '../../../core/models/api-response';
import { ToastService } from '../../../shared/toast/toast.service';

interface UserRow {
  id: string;
  email: string;
  roles: string[];
  rolesLabel: string;
  status: string;
  createdAt: string;
}

const ALL_ROLES = ['FARMER', 'BRAND', 'ADMIN'];

@Component({
  selector: 'app-users',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    NzTableModule,
    NzTagModule,
    NzButtonModule,
    NzInputModule,
    NzSelectModule,
    NzCardModule,
    NzPopconfirmModule,
    NzSpinModule,
    NzAlertModule,
    NzModalModule,
  ],
  templateUrl: './users.html',
  styleUrl: './users.scss',
})
export class Users implements OnInit, AfterViewInit {
  private readonly userApi = inject(UserApiService);
  private readonly modal = inject(NzModalService);
  private readonly toast = inject(ToastService);

  users: UserRow[] = [];
  loading = true;
  actionLoadingId: string | null = null;

  searchText = '';
  roleFilter = '';

  readonly availableRoles = ALL_ROLES;

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.loading = true;
    this.userApi.getUsers().subscribe({
      next: (res) => {
        this.users = (res.result ?? []).map((u) => this.toRow(u));
        this.loading = false;
        setTimeout(() => this.runAnimations(), 50);
      },
      error: (err) => {
        this.toast.error(apiErrorMessage(err, 'Không thể tải danh sách người dùng.'));
        this.loading = false;
      },
    });
  }

  private toRow(u: UserView): UserRow {
    const roles = (u.roles ?? []).map((r) => r.replace(/^ROLE_/, ''));
    return {
      id: u.id,
      email: u.email,
      roles,
      rolesLabel: roles.join(', ') || '—',
      status: u.status,
      createdAt: u.createdAt ? new Date(u.createdAt).toLocaleString('vi-VN') : '—',
    };
  }

  get filteredUsers() {
    return this.users.filter((u) => {
      const matchSearch =
        !this.searchText ||
        u.email.toLowerCase().includes(this.searchText.toLowerCase()) ||
        u.id.toLowerCase().includes(this.searchText.toLowerCase());
      const matchRole = !this.roleFilter || u.roles.includes(this.roleFilter);
      return matchSearch && matchRole;
    });
  }

  approveBrand(user: UserRow) {
    this.updateStatus(user.id, 'ACTIVE');
  }

  disableUser(user: UserRow) {
    this.updateStatus(user.id, 'DISABLED');
  }

  enableUser(user: UserRow) {
    this.updateStatus(user.id, 'ACTIVE');
  }

  rejectBrand(user: UserRow) {
    this.updateStatus(user.id, 'REJECTED');
  }

  editRoles(user: UserRow) {
    const selected = new Set<string>(user.roles);
    const ref = this.modal.create({
      nzTitle: `Cập nhật vai trò cho ${user.email}`,
      nzContent: this.buildRoleEditorContent(user, selected),
      nzOkText: 'Lưu',
      nzCancelText: 'Hủy',
      nzOnOk: () => this.applyRoles(user, Array.from(selected)),
    });
    ref.afterClose.subscribe();
  }

  private buildRoleEditorContent(user: UserRow, selected: Set<string>): string {
    const checkboxes = this.availableRoles
      .map((role) => {
        const checked = selected.has(role) ? 'checked' : '';
        return `<label style="display:flex;align-items:center;gap:8px;margin-bottom:8px;">
          <input type="checkbox" value="${role}" ${checked}
            onchange="this.checked ? window.__roleSet?.add('${role}') : window.__roleSet?.delete('${role}')" />
          <span>${role}</span>
        </label>`;
      })
      .join('');
    (window as unknown as { __roleSet?: Set<string> }).__roleSet = selected;
    return `<div>
      <p style="color:#fa8c16;margin-bottom:12px;">
        <strong>Lưu ý:</strong> Khi thêm vai trò, hồ sơ tương ứng sẽ được tạo tự động.
        Khi xóa vai trò, hồ sơ tương ứng sẽ bị xóa vĩnh viễn.
      </p>
      <p>Vai trò hiện tại: <strong>${user.rolesLabel}</strong></p>
      <div style="margin-top:12px;">${checkboxes}</div>
    </div>`;
  }

  private applyRoles(user: UserRow, roles: string[]): Promise<boolean> {
    if (roles.length === 0) {
      this.toast.error('Người dùng phải có ít nhất một vai trò.');
      return Promise.resolve(false);
    }
    this.actionLoadingId = user.id;
    return new Promise((resolve) => {
      this.userApi.updateUserRoles(user.id, roles).subscribe({
        next: () => {
          this.actionLoadingId = null;
          this.loadUsers();
          resolve(true);
        },
        error: (err) => {
          this.actionLoadingId = null;
          this.toast.error(apiErrorMessage(err, 'Không thể cập nhật vai trò.'));
          resolve(false);
        },
      });
    });
  }

  private updateStatus(id: string, status: string) {
    this.actionLoadingId = id;
    this.userApi.updateUserStatus(id, status).subscribe({
      next: () => {
        this.actionLoadingId = null;
        this.loadUsers();
      },
      error: (err) => {
        this.actionLoadingId = null;
        this.toast.error(apiErrorMessage(err, 'Không thể cập nhật trạng thái.'));
      },
    });
  }

  statusColor(status: string): string {
    switch (status) {
      case 'ACTIVE':
        return 'green';
      case 'PENDING':
        return 'gold';
      case 'DISABLED':
      case 'REJECTED':
        return 'red';
      default:
        return 'default';
    }
  }

  roleColor(roles: string[]): string {
    if (roles.includes('BRAND')) return 'gold';
    if (roles.includes('ADMIN')) return 'purple';
    return 'blue';
  }

  ngAfterViewInit() {
    if (!this.loading) this.runAnimations();
  }

  private runAnimations() {
    const tl = gsap.timeline({ defaults: { ease: 'power3.out' } });
    tl.from('.admin-title', { y: -20, autoAlpha: 0, duration: 0.4 })
      .from('.users-toolbar', { y: 10, autoAlpha: 0, duration: 0.3 }, '-=0.2')
      .from('.ant-table-wrapper', { y: 20, autoAlpha: 0, duration: 0.5 }, '-=0.1');
  }
}
