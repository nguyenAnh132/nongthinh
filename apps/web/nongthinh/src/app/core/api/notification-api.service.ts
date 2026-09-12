import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response';
import { NotificationPage, NotificationState } from '../realtime/realtime.models';

export interface EmailTemplatePurposeView {
  id: number;
  code: string;
  name: string;
  description: string;
  systemDefined: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface EmailTemplateView {
  id: number;
  purposeId: number;
  name: string;
  description: string;
  subject: string;
  htmlContent: string;
  textContent: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface EmailTemplateVariableView {
  variableName: string;
  description: string;
  exampleValue: string;
  required: boolean;
}

export interface PreviewEmailTemplateView {
  subject: string;
  htmlContent: string;
  textContent: string;
}

export interface CreateEmailTemplatePayload {
  name: string;
  description?: string;
  subject: string;
  htmlContent?: string;
  textContent?: string;
}

export interface UpdateEmailTemplatePayload {
  name: string;
  description?: string;
  subject: string;
  htmlContent?: string;
  textContent?: string;
}

export interface PreviewDraftEmailTemplatePayload {
  subject: string;
  htmlContent?: string;
  textContent?: string;
}

export interface EmailHistoryView {
  id: number;
  userId: string;
  templateId: number;
  purposeId: number;
  status: string;
  sendAt: string;
}

@Injectable({ providedIn: 'root' })
export class NotificationApiService {
  listNotifications(page = 0): Observable<ApiResponse<NotificationPage>> {
    return this.http.get<ApiResponse<NotificationPage>>('/api/v1/notification/notifications', { params: { page, size: 20 } });
  }
  getUnreadNotificationCount(): Observable<ApiResponse<NotificationState>> {
    return this.http.get<ApiResponse<NotificationState>>('/api/v1/notification/notifications/unread-count');
  }
  markNotificationRead(id: string): Observable<ApiResponse<NotificationState>> {
    return this.http.patch<ApiResponse<NotificationState>>(`/api/v1/notification/notifications/${id}/read`, null);
  }
  markAllNotificationsRead(): Observable<ApiResponse<NotificationState>> {
    return this.http.patch<ApiResponse<NotificationState>>('/api/v1/notification/notifications/read-all', null);
  }
  private readonly http = inject(HttpClient);
  private readonly purposesBaseUrl = '/api/v1/notification/email-template-purposes';
  private readonly templatesBaseUrl = '/api/v1/notification/email-templates';
  private readonly historyBaseUrl = '/api/v1/notification/email-history';

  // ── Email template purposes ──────────────────────────────────────────

  getEmailTemplatePurposes(): Observable<ApiResponse<EmailTemplatePurposeView[]>> {
    return this.http.get<ApiResponse<EmailTemplatePurposeView[]>>(this.purposesBaseUrl);
  }

  getEmailTemplatePurpose(id: number): Observable<ApiResponse<EmailTemplatePurposeView>> {
    return this.http.get<ApiResponse<EmailTemplatePurposeView>>(`${this.purposesBaseUrl}/${id}`);
  }

  getEmailTemplatesByPurposeId(
    purposeId: number,
  ): Observable<ApiResponse<EmailTemplateView[]>> {
    return this.http.get<ApiResponse<EmailTemplateView[]>>(
      `${this.purposesBaseUrl}/${purposeId}/email-templates`,
    );
  }

  createEmailTemplate(
    purposeId: number,
    payload: CreateEmailTemplatePayload,
  ): Observable<ApiResponse<EmailTemplateView>> {
    return this.http.post<ApiResponse<EmailTemplateView>>(
      `${this.purposesBaseUrl}/${purposeId}/email-templates`,
      payload,
    );
  }

  previewDraftEmailTemplate(
    purposeId: number,
    payload: PreviewDraftEmailTemplatePayload,
  ): Observable<ApiResponse<PreviewEmailTemplateView>> {
    return this.http.post<ApiResponse<PreviewEmailTemplateView>>(
      `${this.purposesBaseUrl}/${purposeId}/email-templates/preview`,
      payload,
    );
  }

  getEmailTemplateVariables(
    purposeId: number,
  ): Observable<ApiResponse<EmailTemplateVariableView[]>> {
    return this.http.get<ApiResponse<EmailTemplateVariableView[]>>(
      `${this.purposesBaseUrl}/${purposeId}/variables`,
    );
  }

  // ── Email templates ────────────────────────────────────────────────────

  getEmailTemplate(id: number): Observable<ApiResponse<EmailTemplateView>> {
    return this.http.get<ApiResponse<EmailTemplateView>>(`${this.templatesBaseUrl}/${id}`);
  }

  updateEmailTemplate(
    id: number,
    payload: UpdateEmailTemplatePayload,
  ): Observable<ApiResponse<EmailTemplateView>> {
    return this.http.put<ApiResponse<EmailTemplateView>>(
      `${this.templatesBaseUrl}/${id}`,
      payload,
    );
  }

  activateEmailTemplate(id: number): Observable<ApiResponse<void>> {
    return this.http.patch<ApiResponse<void>>(
      `${this.templatesBaseUrl}/${id}/activation`,
      null,
    );
  }

  previewEmailTemplate(id: number): Observable<ApiResponse<PreviewEmailTemplateView>> {
    return this.http.post<ApiResponse<PreviewEmailTemplateView>>(
      `${this.templatesBaseUrl}/${id}/preview`,
      null,
    );
  }

  // ── Email history (raw array, không bọc ApiResponse) ─────────────────

  getAllEmailHistory(): Observable<EmailHistoryView[]> {
    return this.http.get<EmailHistoryView[]>(this.historyBaseUrl);
  }

  getEmailHistoryByUserId(userId: string): Observable<EmailHistoryView[]> {
    return this.http.get<EmailHistoryView[]>(`${this.historyBaseUrl}/users/${userId}`);
  }

  getEmailHistoryByTemplateId(templateId: number): Observable<EmailHistoryView[]> {
    return this.http.get<EmailHistoryView[]>(
      `${this.historyBaseUrl}/templates/${templateId}`,
    );
  }

  getEmailHistoryByPurposeId(purposeId: number): Observable<EmailHistoryView[]> {
    return this.http.get<EmailHistoryView[]>(
      `${this.historyBaseUrl}/purposes/${purposeId}`,
    );
  }
}
