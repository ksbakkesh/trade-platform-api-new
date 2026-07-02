package com.tradingplatform.repository;

import com.tradingplatform.domain.DailyOpenPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DailyOpenPriceRepository extends JpaRepository<DailyOpenPrice, Long> {
    Optional<DailyOpenPrice> findByBrokerAccountIdAndIndexNameAndTradeDate(
            Long brokerAccountId, String indexName, LocalDate tradeDate);
}