package com.tradingplatform.api;

import com.tradingplatform.domain.BrokerAccount;
import com.tradingplatform.domain.DailyOpenPrice;
import com.tradingplatform.repository.BrokerAccountRepository;
import com.tradingplatform.repository.DailyOpenPriceRepository;
import com.tradingplatform.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/open-price")
public class OpenPriceController {

    private final DailyOpenPriceRepository openPriceRepository;
    private final UserRepository userRepository;
    private final BrokerAccountRepository brokerAccountRepository;

    public OpenPriceController(DailyOpenPriceRepository openPriceRepository,
                                UserRepository userRepository,
                                BrokerAccountRepository brokerAccountRepository) {
        this.openPriceRepository = openPriceRepository;
        this.userRepository = userRepository;
        this.brokerAccountRepository = brokerAccountRepository;
    }

    /** Get today's open prices for current user */
    @GetMapping("/today")
    public ResponseEntity<?> getTodayOpenPrices(Authentication auth) {
        Long accountId = getAccountId(auth);
        if (accountId == null) return ResponseEntity.notFound().build();

        LocalDate today = LocalDate.now();
        var nifty = openPriceRepository.findByBrokerAccountIdAndIndexNameAndTradeDate(accountId, "NIFTY", today);
        var sensex = openPriceRepository.findByBrokerAccountIdAndIndexNameAndTradeDate(accountId, "SENSEX", today);

        return ResponseEntity.ok(Map.of(
                "nifty", nifty.map(p -> Map.of(
                        "openPrice", p.getOpenPrice(),
                        "source", p.getSource(),
                        "fetchedAt", p.getFetchedAt().toString()
                )).orElse(null),
                "sensex", sensex.map(p -> Map.of(
                        "openPrice", p.getOpenPrice(),
                        "source", p.getSource(),
                        "fetchedAt", p.getFetchedAt().toString()
                )).orElse(null)
        ));
    }

    /** Manually save open price (when user enters it on dashboard) */
    @PostMapping("/manual")
    public ResponseEntity<?> saveManualOpenPrice(@RequestBody ManualOpenPriceRequest req,
                                                  Authentication auth) {
        Long accountId = getAccountId(auth);
        if (accountId == null) return ResponseEntity.notFound().build();

        BrokerAccount account = brokerAccountRepository.findById(accountId).orElse(null);
        if (account == null) return ResponseEntity.notFound().build();

        LocalDate today = LocalDate.now();
        DailyOpenPrice price = openPriceRepository
                .findByBrokerAccountIdAndIndexNameAndTradeDate(accountId, req.indexName(), today)
                .orElse(new DailyOpenPrice());

        price.setBrokerAccount(account);
        price.setIndexName(req.indexName());
        price.setOpenPrice(req.openPrice());
        price.setTradeDate(today);
        price.setSource("MANUAL");
        openPriceRepository.save(price);

        return ResponseEntity.ok(Map.of("message", "Open price saved", "openPrice", req.openPrice()));
    }

    private Long getAccountId(Authentication auth) {
        if (auth == null) return null;
        return userRepository.findByEmail(auth.getName())
                .flatMap(u -> brokerAccountRepository.findByUserIdAndBrokerName(u.getId(), "ANGEL_ONE"))
                .map(BrokerAccount::getId)
                .orElse(null);
    }

    public record ManualOpenPriceRequest(String indexName, BigDecimal openPrice) {}
}