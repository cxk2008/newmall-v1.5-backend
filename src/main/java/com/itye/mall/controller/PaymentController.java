package com.itye.mall.controller;

import com.itye.mall.common.response.ApiResponse;
import com.itye.mall.security.AuthenticatedUser;
import com.itye.mall.service.PaymentService;
import com.itye.mall.vo.payment.PaymentVO;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/mock-pay/{orderId}")
    public ApiResponse<PaymentVO> mockPay(@AuthenticationPrincipal AuthenticatedUser user,
                                                @PathVariable Long orderId) {
        return ApiResponse.ok(paymentService.mockPay(user.getId(), orderId));
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<PaymentVO> latestByOrder(@AuthenticationPrincipal AuthenticatedUser user,
                                                      @PathVariable Long orderId) {
        return ApiResponse.ok(paymentService.latestByOrder(user.getId(), orderId));
    }
}
