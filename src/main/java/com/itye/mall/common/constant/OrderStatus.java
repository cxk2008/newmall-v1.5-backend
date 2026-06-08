package com.itye.mall.common.constant;

public final class OrderStatus {
    public static final int PENDING_PAY = 10;
    public static final int PAID = 20;
    public static final int SHIPPED = 30;
    public static final int COMPLETED = 40;
    public static final int CLOSED = 50;
    public static final int REFUNDED = 60;

    private OrderStatus() {
    }
}
