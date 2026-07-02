-- User permissions table — controls which tabs each user can access
CREATE TABLE user_permissions (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    -- Navigation tabs
    perm_dashboard        BOOLEAN NOT NULL DEFAULT true,
    perm_market_overview  BOOLEAN NOT NULL DEFAULT false,
    perm_live_signals     BOOLEAN NOT NULL DEFAULT true,
    perm_positions        BOOLEAN NOT NULL DEFAULT true,
    perm_trade_history    BOOLEAN NOT NULL DEFAULT true,
    perm_orders           BOOLEAN NOT NULL DEFAULT false,
    perm_risk_management  BOOLEAN NOT NULL DEFAULT true,
    perm_funds_margin     BOOLEAN NOT NULL DEFAULT false,
    perm_broker_setup     BOOLEAN NOT NULL DEFAULT false,
    perm_strategy_settings BOOLEAN NOT NULL DEFAULT false,
    perm_configuration    BOOLEAN NOT NULL DEFAULT false,
    perm_logs             BOOLEAN NOT NULL DEFAULT false,
    perm_reports          BOOLEAN NOT NULL DEFAULT false,
    perm_user_management  BOOLEAN NOT NULL DEFAULT false,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_user_permissions UNIQUE (user_id)
);

-- Insert default permissions for existing users (ADMIN gets everything)
INSERT INTO user_permissions (user_id,
    perm_dashboard, perm_market_overview, perm_live_signals, perm_positions,
    perm_trade_history, perm_orders, perm_risk_management, perm_funds_margin,
    perm_broker_setup, perm_strategy_settings, perm_configuration, perm_logs,
    perm_reports, perm_user_management)
SELECT id,
    true, true, true, true, true, true, true, true, true, true, true, true, true, true
FROM users WHERE role = 'ADMIN';