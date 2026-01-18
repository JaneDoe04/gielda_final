package com.stockmarket.logic;

import com.stockmarket.domain.Order;
import com.stockmarket.domain.OrderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PortfolioPriorityQueueTest {
    private Portfolio portfolio;
    private static final double MARKET_PRICE = 110.0;

    @BeforeEach
    void setUp() {
        portfolio = new Portfolio(100000.0);
    }

    @Test
    void testBuyOrderPrioritization() {
        Order orderA = new Order("AAPL", OrderType.BUY, 10, 100.0, MARKET_PRICE);
        Order orderB = new Order("AAPL", OrderType.BUY, 10, 105.0, MARKET_PRICE);
        
        portfolio.addOrder(orderA);
        portfolio.addOrder(orderB);
        
        Order next = portfolio.pollNextOrder();
        assertNotNull(next);
        assertEquals(105.0, next.getLimitPrice(), 0.01);
    }

    @Test
    void testSellOrderPrioritization() {
        Order orderA = new Order("AAPL", OrderType.SELL, 10, 115.0, MARKET_PRICE);
        Order orderB = new Order("AAPL", OrderType.SELL, 10, 110.0, MARKET_PRICE);
        
        portfolio.addOrder(orderA);
        portfolio.addOrder(orderB);
        
        Order next = portfolio.pollNextOrder();
        assertNotNull(next);
        assertEquals(110.0, next.getLimitPrice(), 0.01);
    }

    @Test
    void testOrderQueueEmpty() {
        assertNull(portfolio.peekNextOrder());
        assertNull(portfolio.pollNextOrder());
    }

    @Test
    void testMultipleOrdersCorrectOrdering() {
        Order order1 = new Order("AAPL", OrderType.BUY, 10, 100.0, MARKET_PRICE);
        Order order2 = new Order("AAPL", OrderType.BUY, 10, 105.0, MARKET_PRICE);
        Order order3 = new Order("AAPL", OrderType.BUY, 10, 108.0, MARKET_PRICE);
        
        portfolio.addOrder(order1);
        portfolio.addOrder(order3);
        portfolio.addOrder(order2);
        
        Order next1 = portfolio.pollNextOrder();
        assertEquals(108.0, next1.getLimitPrice(), 0.01);
        
        Order next2 = portfolio.pollNextOrder();
        assertEquals(105.0, next2.getLimitPrice(), 0.01);
        
        Order next3 = portfolio.pollNextOrder();
        assertEquals(100.0, next3.getLimitPrice(), 0.01);
    }
}
