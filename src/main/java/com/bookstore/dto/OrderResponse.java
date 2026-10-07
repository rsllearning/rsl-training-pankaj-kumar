package com.bookstore.dto;

import com.bookstore.model.Order;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {

    private String orderId;
    private String orderNumber;
    private String customerId;
    private String orderStatus;
    private String paymentStatus;
    private List<OrderItemResponse> items;
    private PricingSummaryResponse pricingSummary;
    private ShippingDetailsResponse shippingDetails;
    private PaymentDetailsResponse paymentDetails;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Map<String, String> links = new HashMap<>();

    public static OrderResponse fromEntity(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getId());
        response.setOrderNumber(order.getOrderNumber());
        response.setCustomerId(order.getCustomerId());
        response.setOrderStatus(order.getOrderStatus());
        response.setPaymentStatus(order.getPaymentStatus());

        if (order.getItems() != null) {
            response.setItems(order.getItems().stream()
                .map(item -> new OrderItemResponse(
                    item.getBookId(),
                    item.getTitle(),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.getLineTotal()
                ))
                .collect(Collectors.toList()));
        }

        PricingSummaryResponse pricing = new PricingSummaryResponse(
            order.getSubtotal(),
            order.getDiscountAmount(),
            order.getShippingFee(),
            order.getTaxAmount(),
            order.getTotalAmount(),
            order.getCurrency()
        );
        response.setPricingSummary(pricing);

        if (order.getShippingAddress() != null) {
            ShippingDetailsResponse shipping = new ShippingDetailsResponse();
            shipping.setFullName(order.getShippingAddress().getFullName());
            shipping.setStreetAddress(order.getShippingAddress().getStreetAddress());
            shipping.setCity(order.getShippingAddress().getCity());
            shipping.setState(order.getShippingAddress().getState());
            shipping.setPostalCode(order.getShippingAddress().getPostalCode());
            shipping.setCountry(order.getShippingAddress().getCountry());
            shipping.setCarrier("Standard Express");
            shipping.setEstimatedDeliveryDate(order.getEstimatedDeliveryDate());
            response.setShippingDetails(shipping);
        }

        if (order.getPaymentMethod() != null) {
            PaymentDetailsResponse payment = new PaymentDetailsResponse(
                order.getPaymentTransactionId(),
                order.getPaymentMethod(),
                order.getCardLast4(),
                order.getCreatedAt()
            );
            response.setPaymentDetails(payment);
        }

        response.setNotes(order.getNotes());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        // HATEOAS Links
        Map<String, String> links = new HashMap<>();
        links.put("self", "/v1/orders/" + order.getId());
        links.put("invoice", "/v1/orders/" + order.getId() + "/invoice");
        links.put("tracking", "/v1/orders/" + order.getId() + "/tracking");
        links.put("cancel", "/v1/orders/" + order.getId() + "/cancel");
        response.setLinks(links);

        return response;
    }
}
