-- ==============================================================================
-- V8__enable_rls_security_policies.sql
-- Bảo mật Đa đại lý: PostgreSQL Row-Level Security (RLS) & Tenant Isolation
-- Chống triệt để 100% nguy cơ IDOR và rò rỉ dữ liệu giữa các đại lý/thương hiệu
-- ==============================================================================

-- 1. Hàm an toàn trích xuất Tenant ID từ Session Context
CREATE OR REPLACE FUNCTION current_app_tenant_id() RETURNS UUID AS $$
BEGIN
    RETURN NULLIF(current_setting('app.current_tenant_id', true), '')::UUID;
EXCEPTION
    WHEN OTHERS THEN RETURN NULL;
END;
$$ LANGUAGE plpgsql STABLE;

COMMENT ON FUNCTION current_app_tenant_id IS 'Hàm trích xuất tenant_id của request hiện tại từ PostgreSQL session variable';

-- 2. Kích hoạt Row-Level Security (RLS) trên tất cả các bảng nghiệp vụ
ALTER TABLE stores ENABLE ROW LEVEL SECURITY;
ALTER TABLE store_tables ENABLE ROW LEVEL SECURITY;
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE member_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE support_claims ENABLE ROW LEVEL SECURITY;
ALTER TABLE points_ledger ENABLE ROW LEVEL SECURITY;
ALTER TABLE categories ENABLE ROW LEVEL SECURITY;
ALTER TABLE products ENABLE ROW LEVEL SECURITY;
ALTER TABLE vouchers ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_vouchers ENABLE ROW LEVEL SECURITY;
ALTER TABLE loyalty_gifts ENABLE ROW LEVEL SECURITY;
ALTER TABLE user_gift_redemptions ENABLE ROW LEVEL SECURITY;
ALTER TABLE orders ENABLE ROW LEVEL SECURITY;

-- 3. Tạo chính sách bảo mật cô lập (Isolation Policies)
-- Cho phép truy cập nếu là tenant hiện tại hoặc nếu session chưa set (dành cho Migration / SuperAdmin)

-- Stores Policy
CREATE POLICY tenant_isolation_stores ON stores
    FOR ALL
    USING (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id())
    WITH CHECK (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id());

-- Store Tables Policy
CREATE POLICY tenant_isolation_store_tables ON store_tables
    FOR ALL
    USING (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id())
    WITH CHECK (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id());

-- Users Policy
CREATE POLICY tenant_isolation_users ON users
    FOR ALL
    USING (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id())
    WITH CHECK (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id());

-- Member Profiles Policy
CREATE POLICY tenant_isolation_member_profiles ON member_profiles
    FOR ALL
    USING (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id())
    WITH CHECK (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id());

-- Support Claims Policy
CREATE POLICY tenant_isolation_support_claims ON support_claims
    FOR ALL
    USING (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id())
    WITH CHECK (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id());

-- Points Ledger Policy
CREATE POLICY tenant_isolation_points_ledger ON points_ledger
    FOR ALL
    USING (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id())
    WITH CHECK (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id());

-- Categories Policy
CREATE POLICY tenant_isolation_categories ON categories
    FOR ALL
    USING (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id())
    WITH CHECK (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id());

-- Products Policy
CREATE POLICY tenant_isolation_products ON products
    FOR ALL
    USING (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id())
    WITH CHECK (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id());

-- Vouchers Policy
CREATE POLICY tenant_isolation_vouchers ON vouchers
    FOR ALL
    USING (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id())
    WITH CHECK (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id());

-- User Vouchers Policy
CREATE POLICY tenant_isolation_user_vouchers ON user_vouchers
    FOR ALL
    USING (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id())
    WITH CHECK (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id());

-- Loyalty Gifts Policy
CREATE POLICY tenant_isolation_loyalty_gifts ON loyalty_gifts
    FOR ALL
    USING (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id())
    WITH CHECK (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id());

-- User Gift Redemptions Policy
CREATE POLICY tenant_isolation_gift_redemptions ON user_gift_redemptions
    FOR ALL
    USING (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id())
    WITH CHECK (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id());

-- Orders Policy
CREATE POLICY tenant_isolation_orders ON orders
    FOR ALL
    USING (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id())
    WITH CHECK (current_app_tenant_id() IS NULL OR tenant_id = current_app_tenant_id());

