package com.sellerpro.parser;

import com.sellerpro.entity.Order;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ParseResult {

    private String platform;
    private String fileName;
    private String batchId;

    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @Builder.Default
    private List<String> errors = new ArrayList<>();

    @Builder.Default
    private int totalRows = 0;

    @Builder.Default
    private int successCount = 0;

    @Builder.Default
    private int skipCount = 0;

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public void addError(String error) {
        this.errors.add(error);
    }

    public void addOrder(Order order) {
        this.orders.add(order);
        this.successCount++;
    }
}
