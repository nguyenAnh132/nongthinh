package com.nongthinh.profile_service.configuration;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import com.nongthinh.profile_service.application.port.in.brand.GetMyBrandAccessUseCase;
import com.nongthinh.profile_service.application.view.BrandAccessView;
import com.nongthinh.profile_service.presentation.advice.GlobalExceptionHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BrandAccessMvcTest {
    @AfterEach
    void clearContext() { SecurityContextHolder.clearContext(); }

    @Test
    void forbiddenResponseStopsControllerAndPreservesOwnProfileRead() throws Exception {
        var access = mock(GetMyBrandAccessUseCase.class);
        when(access.execute()).thenReturn(new BrandAccessView(false, false, false));
        var controller = new TestController();
        var mvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(new BrandAccessConfig(access))
                .setControllerAdvice(new GlobalExceptionHandler(Optional.empty())).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "old-token", null, List.of(new SimpleGrantedAuthority("ROLE_BRAND"))));
        mvc.perform(post("/profile/feature").contextPath("/profile").servletPath("/feature"))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.code").value("BRAND_ACCESS_DENIED"));
        assertEquals(0, controller.mutations.get());
        mvc.perform(get("/profile/brand-profiles/me").contextPath("/profile").servletPath("/brand-profiles/me"))
                .andExpect(status().isOk());
        mvc.perform(patch("/profile/brand-profiles/me").contextPath("/profile").servletPath("/brand-profiles/me"))
                .andExpect(status().isForbidden());
        assertEquals(0, controller.mutations.get());
        when(access.execute()).thenReturn(new BrandAccessView(true, true, false));
        mvc.perform(post("/profile/feature").contextPath("/profile").servletPath("/feature"))
                .andExpect(status().isOk());
        assertEquals(1, controller.mutations.get());
    }

    @RestController
    static class TestController {
        final AtomicInteger mutations = new AtomicInteger();
        @PostMapping("/feature")
        String feature() { mutations.incrementAndGet(); return "ok"; }
        @GetMapping("/brand-profiles/me")
        String profile() { return "profile"; }
        @PatchMapping("/brand-profiles/me")
        String update() { mutations.incrementAndGet(); return "ok"; }
    }
}
