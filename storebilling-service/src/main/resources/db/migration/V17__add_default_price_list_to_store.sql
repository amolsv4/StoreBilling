-- Store's default price list, used by PricingResolutionService to resolve a product's effective
-- selling price for that store. Plain nullable FK column - no JPA association on the Store entity
-- side (avoids a new store -> pricing package dependency); validated at the service layer
-- (exists, same organization) before being set, rather than via a NOT NULL/eager relationship.
ALTER TABLE store
    ADD COLUMN default_price_list_id BIGINT NULL AFTER business_type_id,
    ADD CONSTRAINT fk_store_default_price_list FOREIGN KEY (default_price_list_id) REFERENCES price_list (id);

CREATE INDEX idx_store_default_price_list_id ON store (default_price_list_id);
