package com.nongthinh.brand_service.presentation.controller;

import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.nongthinh.brand_service.application.command.CompleteDocumentsReviewCommand;
import com.nongthinh.brand_service.application.command.CompleteFinalDecisionCommand;
import com.nongthinh.brand_service.application.command.CompletePhoneVerificationCommand;
import com.nongthinh.brand_service.application.port.in.ticket.ClaimTicketUseCase;
import com.nongthinh.brand_service.application.port.in.ticket.CompleteDocumentsReviewUseCase;
import com.nongthinh.brand_service.application.port.in.ticket.CompleteFinalDecisionUseCase;
import com.nongthinh.brand_service.application.port.in.ticket.CompletePhoneVerificationUseCase;
import com.nongthinh.brand_service.application.port.in.ticket.GetTicketUseCase;
import com.nongthinh.brand_service.application.port.in.ticket.ListTicketsUseCase;
import com.nongthinh.brand_service.application.port.in.ticket.UnclaimTicketUseCase;
import com.nongthinh.brand_service.application.view.TicketDetailView;
import com.nongthinh.brand_service.application.view.TicketView;
import com.nongthinh.brand_service.common.constant.PermissionConstant;
import com.nongthinh.brand_service.common.response.ApiResponse;
import com.nongthinh.brand_service.presentation.dto.request.CompleteDocumentsReviewRequest;
import com.nongthinh.brand_service.presentation.dto.request.CompleteFinalDecisionRequest;
import com.nongthinh.brand_service.presentation.dto.request.CompletePhoneVerificationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("admin/tickets")
@RequiredArgsConstructor
public class AdminTicketController {

        private final ListTicketsUseCase listTicketsUseCase;
        private final GetTicketUseCase getTicketUseCase;
        private final ClaimTicketUseCase claimTicketUseCase;
        private final UnclaimTicketUseCase unclaimTicketUseCase;
        private final CompletePhoneVerificationUseCase completePhoneVerificationUseCase;
        private final CompleteDocumentsReviewUseCase completeDocumentsReviewUseCase;
        private final CompleteFinalDecisionUseCase completeFinalDecisionUseCase;

        @GetMapping
        @PreAuthorize("hasAuthority('" + PermissionConstant.TICKET_MANAGE + "')")
        public ResponseEntity<ApiResponse<List<TicketView>>> list() {
                List<TicketView> tickets = listTicketsUseCase.execute();
                return ResponseEntity.ok(ApiResponse.<List<TicketView>>builder()
                                .message("Tickets retrieved successfully")
                                .result(tickets)
                                .build());
        }

        @GetMapping("/{taskId}")
        @PreAuthorize("hasAuthority('" + PermissionConstant.TICKET_MANAGE + "')")
        public ResponseEntity<ApiResponse<TicketDetailView>> get(@PathVariable String taskId) {
                TicketDetailView ticket = getTicketUseCase.execute(taskId);
                return ResponseEntity.ok(ApiResponse.<TicketDetailView>builder()
                                .message("Ticket retrieved successfully")
                                .result(ticket)
                                .build());
        }

        @PostMapping("/{taskId}/claim")
        @PreAuthorize("hasAuthority('" + PermissionConstant.TICKET_MANAGE + "')")
        public ResponseEntity<ApiResponse<Void>> claim(@PathVariable String taskId) {
                claimTicketUseCase.execute(taskId);
                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .message("Ticket claimed successfully")
                                .build());
        }

        @PostMapping("/{taskId}/unclaim")
        @PreAuthorize("hasAuthority('" + PermissionConstant.TICKET_MANAGE + "')")
        public ResponseEntity<ApiResponse<Void>> unclaim(@PathVariable String taskId) {
                unclaimTicketUseCase.execute(taskId);
                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .message("Ticket unclaimed successfully")
                                .build());
        }

        @PostMapping("/{taskId}/complete/phone-verification")
        @PreAuthorize("hasAuthority('" + PermissionConstant.TICKET_MANAGE + "')")
        public ResponseEntity<ApiResponse<Void>> completePhoneVerification(
                        @PathVariable String taskId,
                        @RequestBody @Valid CompletePhoneVerificationRequest request) {
                completePhoneVerificationUseCase.execute(
                                taskId,
                                new CompletePhoneVerificationCommand(
                                                request.phoneCalled(),
                                                request.result(),
                                                request.note()));
                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .message("Phone verification completed successfully")
                                .build());
        }

        @PostMapping("/{taskId}/complete/documents-review")
        @PreAuthorize("hasAuthority('" + PermissionConstant.TICKET_MANAGE + "')")
        public ResponseEntity<ApiResponse<Void>> completeDocumentsReview(
                        @PathVariable String taskId,
                        @RequestBody @Valid CompleteDocumentsReviewRequest request) {
                completeDocumentsReviewUseCase.execute(
                                taskId,
                                new CompleteDocumentsReviewCommand(
                                                request.documentsOk(),
                                                request.revisionReason()));
                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .message("Documents review completed successfully")
                                .build());
        }

        @PostMapping("/{taskId}/complete/final-decision")
        @PreAuthorize("hasAuthority('" + PermissionConstant.TICKET_MANAGE + "')")
        public ResponseEntity<ApiResponse<Void>> completeFinalDecision(
                        @PathVariable String taskId,
                        @RequestBody @Valid CompleteFinalDecisionRequest request) {
                completeFinalDecisionUseCase.execute(
                                taskId,
                                new CompleteFinalDecisionCommand(
                                                request.outcome(),
                                                request.rejectionReason()));
                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .message("Final decision completed successfully")
                                .build());
        }
}
