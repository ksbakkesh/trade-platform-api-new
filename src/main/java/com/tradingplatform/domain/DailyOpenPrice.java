package com.tradingplatform.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "daily_open_prices")
public class DailyOpenPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "broker_account_id", nullable = false)
    private BrokerAccount brokerAccount;

    @Column(name = "index_name", nullable = false, length = 20)
    private String indexName;

    @Column(name = "open_price", nullable = false, precision = 12, scale = 4)
    private BigDecimal openPrice;

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate = LocalDate.now();

    @Column(name = "fetched_at", nullable = false)
    private Instant fetchedAt = Instant.now();

    @Column(name = "source", nullable = false, length = 20)
    private String source = "AUTO";

    public Long getId() { return id; }
    public BrokerAccount getBrokerAccount() { return brokerAccount; }
    public void setBrokerAccount(BrokerAccount brokerAccount) { this.brokerAccount = brokerAccount; }
    public String getIndexName() { return indexName; }
    public void setIndexName(String indexName) { this.indexName = indexName; }
    public BigDecimal getOpenPrice() { return openPrice; }
    public void setOpenPrice(BigDecimal openPrice) { this.openPrice = openPrice; }
    public LocalDate getTradeDate() { return tradeDate; }
    public void setTradeDate(LocalDate tradeDate) { this.tradeDate = tradeDate; }
    public Instant getFetchedAt() { return fetchedAt; }
    public void setFetchedAt(Instant fetchedAt) { this.fetchedAt = fetchedAt; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}