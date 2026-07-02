package com.tradingplatform.api;

import com.tradingplatform.angelone.AngelOneAuthClient;
import com.tradingplatform.common.ErrorMessages;
import com.tradingplatform.domain.BrokerAccount;
import com.tradingplatform.domain.User;
import com.tradingplatform.repository.BrokerAccountRepository;
import com.tradingplatform.repository.UserRepository;
import com.tradingplatform.repository.StrategySettingsRepository;
import com.tradingplatform.repository.RiskSettingsRepository;
import com.tradingplatform.domain.StrategySettings;
import com.tradingplatform.domain.RiskSettings;
import com.tradingplatform.domain.enums.IndexName;
import com.tradingplatform.domain.enums.OpenPriceMode;
import com.tradingplatform.domain.enums.ExitStrategyMode;
import com.tradingplatform.domain.enums.QuantityMode;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Admin-only endpoint for managing broker accounts on behalf of users.
 * Used by ADMIN to connect/disconnect broker accounts for clients.
 */
@RestController
@RequestMapping("/api/admin/broker")
public class AdminBrokerController {

    private final BrokerAccountRepository brokerAccountRepository;
    private final UserRepository userRepository;
    private final AngelOneAuthClient angelOneAuthClient;
    private final StrategySettingsRepository strategySettingsRepository;
    private final RiskSettingsRepository riskSettingsRepository;

    public AdminBrokerController(BrokerAccountRepository brokerAccountRepository,
                                  UserRepository userRepository,
                                  AngelOneAuthClient angelOneAuthClient,
                                  StrategySettingsRepository strategySettingsRepository,
                                  RiskSettingsRepository riskSettingsRepository) {
        this.brokerAccountRepository = brokerAccountRepository;
        this.userRepository = userRepository;
        this.angelOneAuthClient = angelOneAuthClient;
        this.strategySettingsRepository = strategySettingsRepository;
        this.riskSettingsRepository = riskSettingsRepository;
    }

    /** Get broker account for a specific user */
    @GetMapping("/{userId}")
    public ResponseEntity<?> getBrokerAccount(@PathVariable Long userId) {
        return brokerAccountRepository.findByUserIdAndBrokerName(userId, "ANGEL_ONE")
                .map(a -> ResponseEntity.ok(Map.of(
                        "id", a.getId(),
                        "clientCode", a.getClientCode(),
                        "brokerName", a.getBrokerName(),
                        "isActive", a.isActive(),
                        "createdAt", a.getCreatedAt().toString()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Connect Angel One on behalf of a user */
    @PostMapping("/{userId}/connect")
    public ResponseEntity<?> connectBroker(@PathVariable Long userId,
                                            @RequestBody ConnectRequest req) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();

        try {
            var existing = brokerAccountRepository.findByUserIdAndBrokerName(userId, "ANGEL_ONE");
            boolean isNew = existing.isEmpty();

            BrokerAccount account = existing.orElse(new BrokerAccount());
            account.setUser(user);
            account.setBrokerName("ANGEL_ONE");
            account.setClientCode(req.clientCode());
            account.setApiKey(req.apiKey());
            account.setPassword(req.password());
            account.setTotpSecret(req.totpSecret());
            account.setActive(true);
            brokerAccountRepository.save(account);

            // Auto-create default settings for new accounts
            if (isNew) createDefaultSettings(account);

            return ResponseEntity.ok(Map.of(
                    "id", account.getId(),
                    "clientCode", account.getClientCode(),
                    "brokerName", account.getBrokerName(),
                    "isActive", account.isActive(),
                    "createdAt", account.getCreatedAt().toString(),
                    "message", "Angel One connected successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /** Disconnect broker account for a user */
    @DeleteMapping("/{userId}/disconnect")
    public ResponseEntity<?> disconnectBroker(@PathVariable Long userId) {
        brokerAccountRepository.findByUserIdAndBrokerName(userId, "ANGEL_ONE")
                .ifPresent(brokerAccountRepository::delete);
        return ResponseEntity.ok(Map.of("message", "Broker disconnected"));
    }

    /** Test Angel One connection for a user */
    @PostMapping("/{userId}/test")
    public ResponseEntity<?> testConnection(@PathVariable Long userId) {
        try {
            BrokerAccount account = brokerAccountRepository
                    .findByUserIdAndBrokerName(userId, "ANGEL_ONE")
                    .orElseThrow(() -> new RuntimeException("No broker account found"));
            angelOneAuthClient.loginForAccount(account.getId());
            return ResponseEntity.ok(Map.of(
                    "message", "Connection successful! Angel One is working for " + account.getClientCode()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private void createDefaultSettings(BrokerAccount account) {
        for (IndexName index : List.of(IndexName.NIFTY, IndexName.SENSEX)) {
            if (strategySettingsRepository.findByBrokerAccountIdAndIndexName(account.getId(), index).isEmpty()) {
                StrategySettings s = new StrategySettings();
                s.setBrokerAccount(account);
                s.setIndexName(index);
                s.setOpenPriceMode(OpenPriceMode.AUTO);
                s.setPremiumThreshold(new BigDecimal("125"));
                s.setCandleTimeframeMinutes(15);
                s.setRsiThreshold(new BigDecimal("60"));
                s.setVolumeMultiplier(new BigDecimal("2"));
                s.setDeltaMin(new BigDecimal("0.45"));
                s.setDeltaMax(new BigDecimal("0.65"));
                s.setStopLossPoints(new BigDecimal("100"));
                s.setTarget1Points(new BigDecimal("160"));
                s.setTarget2Points(new BigDecimal("200"));
                s.setExitStrategyMode(ExitStrategyMode.OPTION1);
                s.setReEntryEnabled(true);
                s.setQuantityMode(QuantityMode.CAPITAL_BASED);
                s.setCapitalAllocationPercent(new BigDecimal("20"));
                s.setAutoTradingEnabled(true);
                strategySettingsRepository.save(s);
            }
        }
        if (riskSettingsRepository.findByBrokerAccountId(account.getId()).isEmpty()) {
            RiskSettings r = new RiskSettings();
            r.setBrokerAccount(account);
            r.setMaxTradesPerDay(2);
            r.setDailyLossLimit(new BigDecimal("4500"));
            riskSettingsRepository.save(r);
        }
    }

    public record ConnectRequest(
            @NotBlank String clientCode,
            @NotBlank String apiKey,
            @NotBlank String password,
            @NotBlank String totpSecret
    ) {}
}