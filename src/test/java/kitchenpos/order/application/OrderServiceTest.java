package kitchenpos.order.application;

import kitchenpos.menu.domain.Menu;
import kitchenpos.menu.domain.MenuProduct;
import kitchenpos.menu.fixture.MenuFixture;
import kitchenpos.menu.fixture.MenuProductFixture;
import kitchenpos.order.domain.Order;
import kitchenpos.order.domain.OrderLineItem;
import kitchenpos.order.domain.OrderRepository;
import kitchenpos.order.domain.OrderStatus;
import kitchenpos.order.dto.ChangeOrderStatusRequest;
import kitchenpos.order.dto.OrderLineItemRequest;
import kitchenpos.order.dto.OrderRequest;
import kitchenpos.order.dto.OrderResponse;
import kitchenpos.order.fixture.OrderFixture;
import kitchenpos.order.fixture.OrderLineItemFixture;
import kitchenpos.table.domain.OrderTable;
import kitchenpos.table.fixture.TableFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @InjectMocks
    private OrderService orderService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderValidator orderValidator;

    private Long productId;
    private Long menuGroupId;
    private MenuProduct 강정치킨_두마리;
    private Menu 강정치킨_두마리_세트_메뉴;
    private OrderTable 일반_테이블;
    private OrderLineItem 주문_항목;
    private Order 신규_주문;
    private Order 완료_주문;

    @BeforeEach
    public void setUp() {
        // given
        productId = 1L;
        menuGroupId = 1L;

        강정치킨_두마리 = MenuProductFixture.create(1L, productId, 2);
        강정치킨_두마리_세트_메뉴 = MenuFixture.create(1L, "강정치킨_두마리_세트_메뉴", BigDecimal.valueOf(30_000), menuGroupId, Arrays.asList(강정치킨_두마리));
        일반_테이블 = TableFixture.create(1L, null, 4, false);
        주문_항목 = OrderLineItemFixture.create(null, null, 강정치킨_두마리_세트_메뉴.getId(), 1L);
        신규_주문 = OrderFixture.create(1L, 일반_테이블.getId(), OrderStatus.COOKING, LocalDateTime.now(), Arrays.asList(주문_항목));
        완료_주문 = OrderFixture.create(1L, 일반_테이블.getId(), OrderStatus.COMPLETION, LocalDateTime.now(), Arrays.asList(주문_항목));
    }

    @DisplayName("주문 생성 성공 테스트")
    @Test
    void create_success() {
        // given
        OrderRequest orderRequest = OrderRequest.of(
                일반_테이블.getId(), Arrays.asList(OrderLineItemRequest.of(주문_항목.getMenuId(), 주문_항목.getQuantity().getQuantity())));

        given(orderRepository.save(any(Order.class))).willReturn(신규_주문);

        // when
        OrderResponse 생성된_주문 = orderService.create(orderRequest);

        // then
        assertThat(생성된_주문).isEqualTo(OrderResponse.of(신규_주문));
    }

    @DisplayName("주문 목록 조회 테스트")
    @Test
    void list() {
        // given
        given(orderRepository.findAll()).willReturn(Arrays.asList(신규_주문));

        // when
        List<OrderResponse> 조회된_주문_목록 = orderService.list();

        // then
        assertThat(조회된_주문_목록).containsExactly(OrderResponse.of(신규_주문));
    }

    @DisplayName("주문 상태 수정 성공 테스트")
    @Test
    void changeOrderStatus_success() {
        // given
        ChangeOrderStatusRequest changeOrderStatusRequest = ChangeOrderStatusRequest.of(OrderStatus.MEAL.name());

        given(orderRepository.findById(any(Long.class))).willReturn(Optional.of(신규_주문));

        // when
        OrderResponse 수정된_주문 = orderService.changeOrderStatus(신규_주문.getId(), changeOrderStatusRequest);

        // then
        assertThat(수정된_주문.getOrderStatus()).isEqualTo(OrderStatus.MEAL.name());
    }

    @DisplayName("주문 상태 수정 실패 테스트 - 수정 전 주문 상태가 COMPLETION")
    @Test
    void changeOrderStatus_failure_orderStatus() {
        // given
        ChangeOrderStatusRequest changeOrderStatusRequest = ChangeOrderStatusRequest.of(OrderStatus.MEAL.name());

        given(orderRepository.findById(any(Long.class))).willReturn(Optional.of(완료_주문));

        // when & then
        assertThatIllegalArgumentException()
                .isThrownBy(() -> orderService.changeOrderStatus(완료_주문.getId(), changeOrderStatusRequest));
    }
}
