import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ApiResponse } from '../models/api-response';
import { AdminBrandProfileDetailView } from './brand-profile-api.service';

export interface TicketView {
  taskId: string;
  taskDefinitionKey: string;
  taskName: string;
  processInstanceId: string;
  businessKey: string | null;
  assignee: string | null;
  createdAt: string | null;
  variables: Record<string, unknown> | null;
}

export interface BrandApprovalProcessView {
  id: string;
  brandProfileId: string;
  camundaProcessInstanceId: string | null;
  camundaBusinessKey: string | null;
  status: string;
  assignedReviewerId: string | null;
  startedAt: string | null;
  completedAt: string | null;
  activeTaskDefinitionKeys: string[] | null;
}

export interface TicketDetailView {
  ticket: TicketView;
  profileDetail: AdminBrandProfileDetailView | null;
  approvalProcess: BrandApprovalProcessView | null;
}

export interface CompletePhoneVerificationRequest {
  phoneCalled: string;
  result: 'VERIFIED' | 'UNREACHABLE' | 'NEED_MORE_INFO';
  note?: string | null;
}

export interface CompleteDocumentsReviewRequest {
  documentsOk: boolean;
  revisionReason?: string | null;
}

export interface CompleteFinalDecisionRequest {
  outcome: 'APPROVED' | 'REJECTED';
  rejectionReason?: string | null;
}

@Injectable({ providedIn: 'root' })
export class BrandTicketApiService {
  private readonly http = inject(HttpClient);
  private readonly ticketsUrl = '/api/v1/brand/admin/tickets';
  private readonly processUrl = '/api/v1/brand/admin/brand-profiles';

  listTickets(): Observable<ApiResponse<TicketView[]>> {
    return this.http.get<ApiResponse<TicketView[]>>(this.ticketsUrl);
  }

  getTicket(taskId: string): Observable<ApiResponse<TicketDetailView>> {
    return this.http.get<ApiResponse<TicketDetailView>>(`${this.ticketsUrl}/${taskId}`);
  }

  claim(taskId: string): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.ticketsUrl}/${taskId}/claim`, {});
  }

  unclaim(taskId: string): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(`${this.ticketsUrl}/${taskId}/unclaim`, {});
  }

  completePhoneVerification(
    taskId: string,
    body: CompletePhoneVerificationRequest
  ): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(
      `${this.ticketsUrl}/${taskId}/complete/phone-verification`,
      body
    );
  }

  completeDocumentsReview(
    taskId: string,
    body: CompleteDocumentsReviewRequest
  ): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(
      `${this.ticketsUrl}/${taskId}/complete/documents-review`,
      body
    );
  }

  completeFinalDecision(
    taskId: string,
    body: CompleteFinalDecisionRequest
  ): Observable<ApiResponse<void>> {
    return this.http.post<ApiResponse<void>>(
      `${this.ticketsUrl}/${taskId}/complete/final-decision`,
      body
    );
  }

  getProcess(brandProfileId: string): Observable<ApiResponse<BrandApprovalProcessView>> {
    return this.http.get<ApiResponse<BrandApprovalProcessView>>(
      `${this.processUrl}/${brandProfileId}/process`
    );
  }
}
