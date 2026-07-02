-- Store auto-fetched open price per index per day
CREATE TABLE daily_open_prices (
    id              BIGSERIAL PRIMARY KEY,
    broker_account_id BIGINT NOT NULL REFERENCES broker_accounts(id) ON DELETE CASCADE,
    index_name      VARCHAR(20) NOT NULL,
    open_price      NUMERIC(12,4) NOT NULL,
    trade_date      DATE NOT NULL DEFAULT CURRENT_DATE,
    fetched_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    source          VARCHAR(20) NOT NULL DEFAULT 'AUTO',
    CONSTRAINT uq_daily_open_price UNIQUE (broker_account_id, index_name, trade_date)
);

CREATE INDEX idx_daily_open_prices_account ON daily_open_prices(broker_account_id, trade_date);