package kitchenpos.application;

import kitchenpos.dao.OrderDao;
import kitchenpos.dao.OrderTableDao;
import kitchenpos.domain.OrderStatus;
import kitchenpos.domain.OrderTable;
import kitchenpos.table.domain.Empty;
import kitchenpos.table.domain.NumberOfGuests;
import kitchenpos.table.dto.ChangeEmptyRequest;
import kitchenpos.table.dto.ChangeNumberOfGuestsRequest;
import kitchenpos.table.dto.OrderTableRequest;
import kitchenpos.table.dto.OrderTableResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Transactional(readOnly = true)
@Service
public class TableService {

    private final OrderDao orderDao;
    private final OrderTableDao orderTableDao;

    public TableService(
            OrderDao orderDao
            , OrderTableDao orderTableDao) {
        this.orderDao = orderDao;
        this.orderTableDao = orderTableDao;
    }

    @Transactional
    public OrderTableResponse create(final OrderTableRequest request) {
        NumberOfGuests numberOfGuests = NumberOfGuests.of(request.getNumberOfGuests());
        Empty empty = Empty.of(request.isEmpty());
        OrderTable orderTable = OrderTable.of(null, numberOfGuests, empty);

        return OrderTableResponse.of(orderTableDao.save(orderTable));
    }

    public List<OrderTableResponse> list() {
        final List<OrderTable> orderTables = orderTableDao.findAll();

        return orderTables.stream()
                .map(OrderTableResponse::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderTableResponse changeEmpty(final Long orderTableId, final ChangeEmptyRequest request) {
        OrderTable persistOrderTable = findById(orderTableId);
        if (isCookingOrMealExists(persistOrderTable.getId())) {
            throw new IllegalArgumentException();
        }

        Empty empty = Empty.of(request.isEmpty());
        persistOrderTable.changeEmpty(empty);
        return OrderTableResponse.of(persistOrderTable);
    }

    @Transactional
    public OrderTableResponse changeNumberOfGuests(final Long orderTableId, final ChangeNumberOfGuestsRequest request) {
        NumberOfGuests numberOfGuests = NumberOfGuests.of(request.getNumberOfGuests());
        OrderTable persistOrderTable = findById(orderTableId);

        persistOrderTable.changeNumberOfGuests(numberOfGuests);
        return OrderTableResponse.of(persistOrderTable);
    }

    public OrderTable findById(Long orderTableId) {
        return orderTableDao.findById(orderTableId)
                .orElseThrow(NoSuchElementException::new);
    }

    public boolean isCookingOrMealExists(Long orderTableId) {
        List<String> orderStatuses = Arrays.asList(OrderStatus.COOKING.name(), OrderStatus.MEAL.name());
        return orderDao.existsByOrderTableIdAndOrderStatusIn(orderTableId, orderStatuses);
    }
}
