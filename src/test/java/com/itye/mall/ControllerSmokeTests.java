package com.itye.mall;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itye.mall.common.response.PageResult;
import com.itye.mall.controller.AdminAuthController;
import com.itye.mall.controller.AdminBannerController;
import com.itye.mall.controller.AdminBrandController;
import com.itye.mall.controller.AdminCategoryController;
import com.itye.mall.controller.AdminCouponController;
import com.itye.mall.controller.AdminInventoryController;
import com.itye.mall.controller.AdminOrderController;
import com.itye.mall.controller.AdminProductController;
import com.itye.mall.controller.AdminRefundController;
import com.itye.mall.controller.AdminSkuController;
import com.itye.mall.controller.AdminOperationLogController;
import com.itye.mall.controller.AuthController;
import com.itye.mall.controller.BannerController;
import com.itye.mall.controller.CartController;
import com.itye.mall.controller.CouponController;
import com.itye.mall.controller.FavoriteController;
import com.itye.mall.controller.OrderController;
import com.itye.mall.controller.PaymentController;
import com.itye.mall.controller.ProductCategoryController;
import com.itye.mall.controller.ProductController;
import com.itye.mall.controller.ProductReviewController;
import com.itye.mall.controller.RefundController;
import com.itye.mall.controller.ShipmentController;
import com.itye.mall.controller.UserAddressController;
import com.itye.mall.entity.AdminUser;
import com.itye.mall.entity.Banner;
import com.itye.mall.entity.Brand;
import com.itye.mall.entity.Coupon;
import com.itye.mall.entity.InventoryLog;
import com.itye.mall.entity.OperationLog;
import com.itye.mall.entity.ProductCategory;
import com.itye.mall.entity.ProductSku;
import com.itye.mall.entity.User;
import com.itye.mall.entity.UserAddress;
import com.itye.mall.exception.GlobalExceptionHandler;
import com.itye.mall.mapper.AdminUserMapper;
import com.itye.mall.mapper.UserMapper;
import com.itye.mall.security.AuthenticatedUser;
import com.itye.mall.service.AdminAuthService;
import com.itye.mall.service.BannerService;
import com.itye.mall.service.AdminBrandService;
import com.itye.mall.service.AdminCategoryService;
import com.itye.mall.service.CouponService;
import com.itye.mall.service.AdminOrderService;
import com.itye.mall.service.AdminProductService;
import com.itye.mall.service.AuthService;
import com.itye.mall.service.CartService;
import com.itye.mall.service.FavoriteService;
import com.itye.mall.service.InventoryService;
import com.itye.mall.service.OrderService;
import com.itye.mall.service.OperationLogService;
import com.itye.mall.service.PaymentService;
import com.itye.mall.service.ProductCategoryService;
import com.itye.mall.service.ProductReviewService;
import com.itye.mall.service.ProductService;
import com.itye.mall.service.RefundService;
import com.itye.mall.service.ShipmentService;
import com.itye.mall.service.UserAddressService;
import com.itye.mall.vo.admin.AdminProductListItemVO;
import com.itye.mall.vo.auth.AdminAuthVO;
import com.itye.mall.vo.auth.AdminProfileVO;
import com.itye.mall.vo.auth.AuthVO;
import com.itye.mall.vo.auth.UserProfileVO;
import com.itye.mall.vo.cart.CartVO;
import com.itye.mall.vo.coupon.UserCouponVO;
import com.itye.mall.vo.favorite.FavoriteVO;
import com.itye.mall.vo.order.OrderVO;
import com.itye.mall.vo.payment.PaymentVO;
import com.itye.mall.vo.product.CategoryTreeNodeVO;
import com.itye.mall.vo.product.ProductDetailVO;
import com.itye.mall.vo.product.ProductListItemVO;
import com.itye.mall.vo.product.ProductSkuVO;
import com.itye.mall.vo.review.ProductReviewVO;
import com.itye.mall.vo.refund.RefundVO;
import com.itye.mall.vo.shipment.ShipmentVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ControllerSmokeTests {
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private AuthenticatedUser currentPrincipal;

    @Mock
    private AdminAuthService adminAuthService;
    @Mock
    private BannerService bannerService;
    @Mock
    private AdminBrandService adminBrandService;
    @Mock
    private AdminCategoryService adminCategoryService;
    @Mock
    private CouponService couponService;
    @Mock
    private AdminOrderService adminOrderService;
    @Mock
    private AdminProductService adminProductService;
    @Mock
    private AuthService authService;
    @Mock
    private CartService cartService;
    @Mock
    private FavoriteService favoriteService;
    @Mock
    private InventoryService inventoryService;
    @Mock
    private OrderService orderService;
    @Mock
    private OperationLogService operationLogService;
    @Mock
    private PaymentService paymentService;
    @Mock
    private ProductCategoryService productCategoryService;
    @Mock
    private ProductReviewService productReviewService;
    @Mock
    private ProductService productService;
    @Mock
    private RefundService refundService;
    @Mock
    private ShipmentService shipmentService;
    @Mock
    private UserAddressService userAddressService;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AdminUserMapper adminUserMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new AdminAuthController(adminAuthService, adminUserMapper),
                        new AdminBannerController(bannerService),
                        new AdminBrandController(adminBrandService),
                        new AdminCategoryController(adminCategoryService),
                        new AdminCouponController(couponService),
                        new AdminInventoryController(inventoryService),
                        new AdminOperationLogController(operationLogService),
                        new AdminOrderController(adminOrderService, shipmentService),
                        new AdminProductController(adminProductService),
                        new AdminRefundController(refundService),
                        new AdminSkuController(adminProductService),
                        new AuthController(authService, userMapper),
                        new BannerController(bannerService),
                        new CartController(cartService),
                        new CouponController(couponService),
                        new FavoriteController(favoriteService),
                        new OrderController(orderService),
                        new PaymentController(paymentService),
                        new ProductCategoryController(productCategoryService),
                        new ProductController(productService),
                        new ProductReviewController(productReviewService),
                        new RefundController(refundService),
                        new ShipmentController(shipmentService),
                        new UserAddressController(userAddressService)
                )
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(new TestAuthenticationPrincipalResolver())
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .build();
    }

    @Test
    void publicEndpointsWork() throws Exception {
        when(authService.register(any())).thenReturn(authVO());
        when(authService.login(any())).thenReturn(authVO());
        when(productCategoryService.tree()).thenReturn(List.of(categoryTreeNode()));
        when(productService.list(isNull(), isNull(), isNull(), isNull())).thenReturn(page(productListItem()));
        when(productService.list(isNull(), eq("phone"), isNull(), isNull())).thenReturn(page(productListItem()));
        when(productService.detail(1L)).thenReturn(productDetail());
        when(productService.skus(1L)).thenReturn(List.of(productSkuVO()));
        when(productReviewService.listByProduct(eq(1L), isNull(), isNull())).thenReturn(page(productReviewVO()));
        when(bannerService.listVisible(isNull())).thenReturn(List.of(banner()));
        when(couponService.available()).thenReturn(List.of(coupon()));

        ok(post("/api/auth/register"), body("username", "user", "password", "123456"));
        ok(post("/api/auth/login"), body("account", "user", "password", "123456"));
        ok(get("/api/categories/tree"));
        ok(get("/api/products"));
        ok(get("/api/products/search").param("keyword", "phone"));
        ok(get("/api/products/1"));
        ok(get("/api/products/1/skus"));
        ok(get("/api/products/1/reviews"));
        ok(get("/api/banners"));
        ok(get("/api/coupons/available"));
    }

    @Test
    void userEndpointsWork() throws Exception {
        AuthenticatedUser user = userPrincipal();
        when(userMapper.selectById(1L)).thenReturn(user());
        when(userAddressService.list(1L)).thenReturn(List.of(userAddress()));
        when(userAddressService.detail(1L, 1L)).thenReturn(userAddress());
        when(userAddressService.create(eq(1L), any())).thenReturn(userAddress());
        when(userAddressService.update(eq(1L), eq(1L), any())).thenReturn(userAddress());
        when(userAddressService.setDefault(1L, 1L)).thenReturn(userAddress());
        doNothing().when(userAddressService).delete(1L, 1L);
        when(cartService.getCart(1L)).thenReturn(CartVO.builder().items(List.of()).build());
        when(cartService.addItem(eq(1L), any())).thenReturn(CartVO.builder().items(List.of()).build());
        when(cartService.updateItem(eq(1L), eq(1L), any())).thenReturn(CartVO.builder().items(List.of()).build());
        doNothing().when(cartService).deleteItem(1L, 1L);
        when(orderService.create(eq(1L), any())).thenReturn(orderVO());
        when(orderService.list(eq(1L), isNull(), isNull(), isNull())).thenReturn(page(orderVO()));
        when(orderService.detail(1L, 1L)).thenReturn(orderVO());
        when(orderService.cancel(1L, 1L)).thenReturn(orderVO());
        when(paymentService.mockPay(1L, 1L)).thenReturn(paymentVO());
        when(paymentService.latestByOrder(1L, 1L)).thenReturn(paymentVO());
        when(shipmentService.getByUserOrder(1L, 1L)).thenReturn(shipmentVO());
        when(shipmentService.receive(1L, 1L)).thenReturn(shipmentVO());
        when(productReviewService.create(eq(1L), any())).thenReturn(productReviewVO());
        when(productReviewService.listByOrder(1L, 1L)).thenReturn(List.of(productReviewVO()));
        when(couponService.claim(1L, 1L)).thenReturn(userCouponVO());
        when(couponService.userCoupons(1L, null)).thenReturn(List.of(userCouponVO()));
        when(favoriteService.list(1L, null, null)).thenReturn(page(favoriteVO()));
        when(favoriteService.add(1L, 1L)).thenReturn(favoriteVO());
        doNothing().when(favoriteService).delete(1L, 1L);
        when(refundService.create(eq(1L), any())).thenReturn(refundVO());
        when(refundService.listByUser(1L, null, null, null)).thenReturn(page(refundVO()));
        when(refundService.detailByUser(1L, 1L)).thenReturn(refundVO());

        okWithUser(get("/api/auth/me"), user);
        okWithUser(get("/api/user/addresses"), user);
        okWithUser(get("/api/user/addresses/1"), user);
        okWithUser(post("/api/user/addresses"), user, addressBody());
        okWithUser(put("/api/user/addresses/1"), user, addressBody());
        okWithUser(delete("/api/user/addresses/1"), user);
        okWithUser(put("/api/user/addresses/1/default"), user);
        okWithUser(get("/api/cart"), user);
        okWithUser(post("/api/cart/items"), user, body("skuId", 1, "quantity", 1));
        okWithUser(put("/api/cart/items/1"), user, body("quantity", 2, "selected", 1));
        okWithUser(delete("/api/cart/items/1"), user);
        okWithUser(post("/api/orders"), user, body("addressId", 1));
        okWithUser(get("/api/orders"), user);
        okWithUser(get("/api/orders/1"), user);
        okWithUser(put("/api/orders/1/cancel"), user);
        okWithUser(post("/api/payments/mock-pay/1"), user);
        okWithUser(get("/api/payments/order/1"), user);
        okWithUser(get("/api/orders/1/shipment"), user);
        okWithUser(put("/api/orders/1/receive"), user);
        okWithUser(post("/api/reviews"), user, body("orderItemId", 1, "rating", 5));
        okWithUser(get("/api/orders/1/reviews"), user);
        okWithUser(post("/api/coupons/1/claim"), user);
        okWithUser(get("/api/user/coupons"), user);
        okWithUser(get("/api/favorites"), user);
        okWithUser(post("/api/favorites/1"), user);
        okWithUser(delete("/api/favorites/1"), user);
        okWithUser(post("/api/refunds"), user, refundBody());
        okWithUser(get("/api/refunds"), user);
        okWithUser(get("/api/refunds/1"), user);
    }

    @Test
    void adminEndpointsWork() throws Exception {
        AuthenticatedUser admin = adminPrincipal();
        when(adminAuthService.login(any())).thenReturn(adminAuthVO());
        when(adminUserMapper.selectById(1L)).thenReturn(adminUser());
        when(adminCategoryService.list(isNull(), isNull(), isNull(), isNull())).thenReturn(page(productCategory()));
        when(adminCategoryService.detail(1L)).thenReturn(productCategory());
        when(adminCategoryService.create(any())).thenReturn(productCategory());
        when(adminCategoryService.update(eq(1L), any())).thenReturn(productCategory());
        doNothing().when(adminCategoryService).delete(1L);
        when(adminBrandService.list(isNull(), isNull(), isNull(), isNull())).thenReturn(page(brand()));
        when(adminBrandService.detail(1L)).thenReturn(brand());
        when(adminBrandService.create(any())).thenReturn(brand());
        when(adminBrandService.update(eq(1L), any())).thenReturn(brand());
        doNothing().when(adminBrandService).delete(1L);
        when(adminProductService.list(isNull(), isNull(), isNull(), isNull(), isNull(), isNull())).thenReturn(page(adminProduct()));
        when(adminProductService.detail(1L)).thenReturn(productDetail());
        when(adminProductService.create(any())).thenReturn(productDetail());
        when(adminProductService.update(eq(1L), any())).thenReturn(productDetail());
        when(adminProductService.onSale(1L)).thenReturn(productDetail());
        when(adminProductService.offSale(1L)).thenReturn(productDetail());
        doNothing().when(adminProductService).delete(1L);
        when(adminProductService.listSkus(1L)).thenReturn(List.of(productSkuVO()));
        when(adminProductService.createSku(eq(1L), any())).thenReturn(productSkuVO());
        when(adminProductService.updateSku(eq(1L), any())).thenReturn(productSkuVO());
        doNothing().when(adminProductService).deleteSku(1L);
        when(adminOrderService.list(isNull(), isNull(), isNull(), isNull())).thenReturn(page(orderVO()));
        when(adminOrderService.detail(1L)).thenReturn(orderVO());
        when(adminOrderService.close(eq(1L), eq(1L), any())).thenReturn(orderVO());
        when(shipmentService.ship(eq(1L), eq(1L), any())).thenReturn(shipmentVO());
        when(inventoryService.listLogs(1L, null, null)).thenReturn(page(inventoryLog()));
        when(inventoryService.increaseStock(eq(1L), eq(10), any())).thenReturn(productSku());
        when(inventoryService.decreaseStock(eq(1L), eq(1), any())).thenReturn(productSku());
        when(couponService.adminList(isNull(), isNull(), isNull(), isNull())).thenReturn(page(coupon()));
        when(couponService.detail(1L)).thenReturn(coupon());
        when(couponService.adminCreate(eq(1L), any())).thenReturn(coupon());
        when(couponService.adminUpdate(eq(1L), eq(1L), any())).thenReturn(coupon());
        doNothing().when(couponService).adminDelete(1L, 1L);
        when(bannerService.adminList(isNull(), isNull(), isNull(), isNull())).thenReturn(page(banner()));
        when(bannerService.detail(1L)).thenReturn(banner());
        when(bannerService.adminCreate(eq(1L), any())).thenReturn(banner());
        when(bannerService.adminUpdate(eq(1L), eq(1L), any())).thenReturn(banner());
        doNothing().when(bannerService).adminDelete(1L, 1L);
        when(refundService.adminList(isNull(), isNull(), isNull(), isNull())).thenReturn(page(refundVO()));
        when(refundService.adminDetail(1L)).thenReturn(refundVO());
        when(refundService.approve(eq(1L), eq(1L), any())).thenReturn(refundVO());
        when(refundService.reject(eq(1L), eq(1L), any())).thenReturn(refundVO());
        when(operationLogService.list(isNull(), isNull(), isNull(), isNull())).thenReturn(page(operationLog()));

        ok(post("/api/admin/auth/login"), body("account", "admin", "password", "123456"));
        okWithUser(get("/api/admin/auth/me"), admin);
        ok(get("/api/admin/categories"));
        ok(get("/api/admin/categories/1"));
        ok(post("/api/admin/categories"), body("name", "手机"));
        ok(put("/api/admin/categories/1"), body("name", "数码"));
        ok(delete("/api/admin/categories/1"));
        ok(get("/api/admin/brands"));
        ok(get("/api/admin/brands/1"));
        ok(post("/api/admin/brands"), body("name", "品牌"));
        ok(put("/api/admin/brands/1"), body("name", "品牌2"));
        ok(delete("/api/admin/brands/1"));
        ok(get("/api/admin/products"));
        ok(get("/api/admin/products/1"));
        ok(post("/api/admin/products"), productBody());
        ok(put("/api/admin/products/1"), productBody());
        ok(put("/api/admin/products/1/on-sale"));
        ok(put("/api/admin/products/1/off-sale"));
        ok(delete("/api/admin/products/1"));
        ok(get("/api/admin/products/1/skus"));
        ok(post("/api/admin/products/1/skus"), skuBody());
        ok(put("/api/admin/skus/1"), skuBody());
        ok(delete("/api/admin/skus/1"));
        ok(get("/api/admin/orders"));
        ok(get("/api/admin/orders/1"));
        okWithUser(put("/api/admin/orders/1/close"), admin, body("reason", "异常关闭"));
        okWithUser(post("/api/admin/orders/1/ship"), admin, body("logisticsCompany", "顺丰", "logisticsNo", "SF1"));
        ok(get("/api/admin/skus/1/inventory-logs"));
        ok(post("/api/admin/skus/1/stock/increase"), body("quantity", 10));
        ok(post("/api/admin/skus/1/stock/decrease"), body("quantity", 1));
        ok(get("/api/admin/coupons"));
        ok(get("/api/admin/coupons/1"));
        okWithUser(post("/api/admin/coupons"), admin, couponBody());
        okWithUser(put("/api/admin/coupons/1"), admin, couponBody());
        okWithUser(delete("/api/admin/coupons/1"), admin);
        ok(get("/api/admin/banners"));
        ok(get("/api/admin/banners/1"));
        okWithUser(post("/api/admin/banners"), admin, bannerBody());
        okWithUser(put("/api/admin/banners/1"), admin, bannerBody());
        okWithUser(delete("/api/admin/banners/1"), admin);
        ok(get("/api/admin/refunds"));
        ok(get("/api/admin/refunds/1"));
        okWithUser(put("/api/admin/refunds/1/approve"), admin, reviewBody());
        okWithUser(put("/api/admin/refunds/1/reject"), admin, reviewBody());
        ok(get("/api/admin/operation-logs"));
    }

    private void ok(MockHttpServletRequestBuilder request) throws Exception {
        mockMvc.perform(request.contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));
    }

    private void ok(MockHttpServletRequestBuilder request, String content) throws Exception {
        ok(request.content(content));
    }

    private void okWithUser(MockHttpServletRequestBuilder request, AuthenticatedUser user) throws Exception {
        currentPrincipal = user;
        try {
            ok(request);
        } finally {
            currentPrincipal = null;
        }
    }

    private void okWithUser(MockHttpServletRequestBuilder request, AuthenticatedUser user, String content) throws Exception {
        okWithUser(request.content(content), user);
    }

    private String body(Object... pairs) throws Exception {
        Map<String, Object> values = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            values.put(String.valueOf(pairs[i]), pairs[i + 1]);
        }
        return objectMapper.writeValueAsString(values);
    }

    private String addressBody() throws Exception {
        return body("receiverName", "张三", "receiverPhone", "13800000000", "province", "浙江", "city", "杭州", "district", "西湖", "detailAddress", "1号");
    }

    private String productBody() throws Exception {
        return body("categoryId", 1, "spuCode", "SPU1", "name", "商品", "status", 1);
    }

    private String skuBody() throws Exception {
        return body("skuCode", "SKU1", "name", "规格", "salePrice", 99.00, "stock", 10, "status", 1);
    }

    private String couponBody() throws Exception {
        return body(
                "name", "满100减10",
                "type", 1,
                "faceValue", 10.00,
                "minOrderAmount", 100.00,
                "totalQuantity", 100,
                "perUserLimit", 1,
                "startsAt", "2026-06-01T00:00:00",
                "endsAt", "2026-12-31T23:59:59",
                "status", 1
        );
    }

    private String bannerBody() throws Exception {
        return body(
                "title", "首页活动",
                "imageUrl", "https://example.com/banner.jpg",
                "linkUrl", "https://example.com",
                "position", "home",
                "sortOrder", 1,
                "status", 1
        );
    }

    private String refundBody() throws Exception {
        return body(
                "orderId", 1,
                "orderItemId", 1,
                "amount", 10.00,
                "reason", "不想要了"
        );
    }

    private String reviewBody() throws Exception {
        return body("note", "审核备注");
    }

    private AuthenticatedUser userPrincipal() {
        return new AuthenticatedUser(user());
    }

    private AuthenticatedUser adminPrincipal() {
        return new AuthenticatedUser(adminUser());
    }

    private User user() {
        return User.builder().id(1L).username("user").passwordHash("x").nickname("用户").status(1).build();
    }

    private AdminUser adminUser() {
        return AdminUser.builder().id(1L).username("admin").passwordHash("x").realName("管理员").role("admin").status(1).build();
    }

    private AuthVO authVO() {
        return AuthVO.builder().tokenType("Bearer").token("token").expiresIn(1L).user(UserProfileVO.from(user())).build();
    }

    private AdminAuthVO adminAuthVO() {
        return AdminAuthVO.builder().tokenType("Bearer").token("token").expiresIn(1L).admin(AdminProfileVO.from(adminUser())).build();
    }

    private UserAddress userAddress() {
        return UserAddress.builder().id(1L).userId(1L).receiverName("张三").receiverPhone("13800000000").build();
    }

    private ProductCategory productCategory() {
        return ProductCategory.builder().id(1L).name("手机").status(1).level(1).build();
    }

    private Brand brand() {
        return Brand.builder().id(1L).name("品牌").status(1).build();
    }

    private Banner banner() {
        return Banner.builder().id(1L).title("首页活动").position("home").status(1).build();
    }

    private Coupon coupon() {
        return Coupon.builder().id(1L).name("满100减10").type(1).status(1).build();
    }

    private ProductSku productSku() {
        return ProductSku.builder().id(1L).productId(1L).skuCode("SKU1").name("规格").salePrice(BigDecimal.TEN).stock(10).lockedStock(0).status(1).build();
    }

    private ProductSkuVO productSkuVO() {
        return ProductSkuVO.from(productSku());
    }

    private ProductDetailVO productDetail() {
        return ProductDetailVO.builder().id(1L).name("商品").skus(List.of(productSkuVO())).images(List.of()).attributes(List.of()).build();
    }

    private ProductListItemVO productListItem() {
        return ProductListItemVO.builder().id(1L).name("商品").build();
    }

    private AdminProductListItemVO adminProduct() {
        return AdminProductListItemVO.builder().id(1L).spuCode("SPU1").name("商品").status(1).build();
    }

    private CategoryTreeNodeVO categoryTreeNode() {
        return CategoryTreeNodeVO.builder().id(1L).name("手机").children(List.of()).build();
    }

    private OrderVO orderVO() {
        return OrderVO.builder().id(1L).orderNo("O1").status(10).items(List.of()).build();
    }

    private PaymentVO paymentVO() {
        return PaymentVO.builder().id(1L).orderId(1L).paymentNo("P1").build();
    }

    private ShipmentVO shipmentVO() {
        return ShipmentVO.builder().id(1L).orderId(1L).logisticsCompany("顺丰").logisticsNo("SF1").build();
    }

    private UserCouponVO userCouponVO() {
        return UserCouponVO.builder().id(1L).couponId(1L).name("满100减10").status(10).build();
    }

    private FavoriteVO favoriteVO() {
        return FavoriteVO.builder().id(1L).productId(1L).productName("商品").build();
    }

    private ProductReviewVO productReviewVO() {
        return ProductReviewVO.builder().id(1L).productId(1L).rating(5).build();
    }

    private InventoryLog inventoryLog() {
        return InventoryLog.builder().id(1L).skuId(1L).quantityChange(1).createdAt(LocalDateTime.now()).build();
    }

    private RefundVO refundVO() {
        return RefundVO.builder().id(1L).refundNo("R1").orderId(1L).orderItemId(1L).status(10).amount(BigDecimal.TEN).reason("不想要了").build();
    }

    private OperationLog operationLog() {
        return OperationLog.builder().id(1L).adminId(1L).action("refund.approve").targetType("refund").targetId("1").createdAt(LocalDateTime.now()).build();
    }

    private <T> PageResult<T> page(T record) {
        return PageResult.<T>builder().total(1L).pageNum(1).pageSize(10).records(List.of(record)).build();
    }

    private class TestAuthenticationPrincipalResolver implements HandlerMethodArgumentResolver {
        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(AuthenticationPrincipal.class)
                    && AuthenticatedUser.class.isAssignableFrom(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(MethodParameter parameter,
                                      ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest,
                                      WebDataBinderFactory binderFactory) {
            return currentPrincipal;
        }
    }
}
