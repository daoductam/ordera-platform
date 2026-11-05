-- ==============================================================================
-- V9__seed_demo_data_linn_sushi.sql
-- Nạp dữ liệu mẫu ban đầu khớp 100% với Figma 2.0 (Linn Sushi Sentrum & David Nguyen)
-- ==============================================================================

-- 1. Tenant: Agency Name (Linn Sushi)
INSERT INTO tenants (id, code, name, logo_url, primary_color, secondary_color, status, metadata)
VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'linn_sushi',
    'Agency Name',
    'https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=120&auto=format&fit=crop&q=80',
    '#F5B800',
    '#1E293B',
    'ACTIVE',
    '{"currency": "NOK", "country": "Norway"}'::jsonb
) ON CONFLICT (code) DO NOTHING;

-- 2. Store: Linn Sushi Sentrum
INSERT INTO stores (id, tenant_id, name, address, city, latitude, longitude, phone_number, opening_hours, is_active, accepts_pickup, accepts_dinein)
VALUES (
    'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'Linn Sushi Sentrum',
    'Prinsens gate 10C, 0153 Oslo',
    'Oslo',
    59.9111,
    10.7485,
    '+4722334455',
    '10:00 - 22:00',
    true,
    true,
    true
) ON CONFLICT (id) DO NOTHING;

-- 3. Store Tables (Bàn ăn)
INSERT INTO store_tables (id, tenant_id, store_id, table_number, seating_capacity, qr_code_token, is_occupied)
VALUES
    ('b1111111-1111-1111-1111-111111111101', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Table 01', 4, 'qr_linn_tbl_01', false),
    ('b1111111-1111-1111-1111-111111111102', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Table 02', 2, 'qr_linn_tbl_02', false),
    ('b1111111-1111-1111-1111-111111111105', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Table 05', 6, 'qr_linn_tbl_05', false)
ON CONFLICT (id) DO NOTHING;

-- 4. 6 Tier Definitions (New, Silver, Gold, Platinum, Diamond, Ruby)
INSERT INTO tier_definitions (id, tenant_id, tier_name, tier_order, min_points_required, theme_color_hex, earn_points_rate, benefits_summary)
VALUES
    ('c1111111-1111-1111-1111-111111111111', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'New', 1, 0, '#1EB759', 1.0000, '["Tích 1 điểm mỗi 10 NOK chi tiêu"]'::jsonb),
    ('c2222222-2222-2222-2222-222222222222', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Silver', 2, 200, '#00A3FF', 1.0000, '["Tích 1 điểm mỗi 10 NOK", "Ưu đãi sinh nhật"]'::jsonb),
    ('c3333333-3333-3333-3333-333333333333', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Gold', 3, 400, '#F5B800', 1.2000, '["Tích x1.2 điểm", "Miễn phí topping", "Quà sinh nhật"]'::jsonb),
    ('c4444444-4444-4444-4444-444444444444', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Platinum', 4, 1000, '#FF5B4D', 1.5000, '["Tích x1.5 điểm", "Voucher độc quyền", "Hỗ trợ ưu tiên"]'::jsonb),
    ('c5555555-5555-5555-5555-555555555555', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Diamond', 5, 2000, '#7C3AED', 1.8000, '["Tích x1.8 điểm", "Miễn phí đồ uống hàng tháng", "Quà tặng VIP"]'::jsonb),
    ('c6666666-6666-6666-6666-666666666666', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Ruby', 6, 4000, '#D9148E', 2.0000, '["Tích x2.0 điểm", "Bàn VIP ưu tiên", "Lời mời sự kiện Tasting riêng"]'::jsonb)
ON CONFLICT (tenant_id, tier_name) DO NOTHING;

-- 5. Users: David Nguyen & ABY TRAN
INSERT INTO users (id, tenant_id, phone_number, email, full_name, password_hash, is_phone_verified, is_email_verified, role, status)
VALUES
    ('d0000000-0000-0000-0000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '+4797362748', 'info@ordera.no', 'David Nguyen', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.8H2lY47JpB6kP3Vz7wV3K.fKk6Wl0i', true, true, 'CUSTOMER', 'ACTIVE'),
    ('d0000000-0000-0000-0000-000000000003', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', '+4797362749', 'abytran@ordera.no', 'ABY TRAN', '$2a$10$7EqJtq98hPqEX7fNZaFWoO.8H2lY47JpB6kP3Vz7wV3K.fKk6Wl0i', true, true, 'CUSTOMER', 'ACTIVE')
ON CONFLICT (tenant_id, phone_number) DO NOTHING;

-- 6. Member Profiles
INSERT INTO member_profiles (id, tenant_id, user_id, current_tier_id, current_points, lifetime_points, date_of_birth, gender, avatar_url)
VALUES
    ('d0000000-0000-0000-0000-000000000002', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0000000-0000-0000-0000-000000000001', 'c2222222-2222-2222-2222-222222222222', 120, 300, '2000-08-23', 'MALE', 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=80&auto=format&fit=crop&q=80'),
    ('d0000000-0000-0000-0000-000000000004', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0000000-0000-0000-0000-000000000003', 'c1111111-1111-1111-1111-111111111111', 0, 0, '2000-08-23', 'FEMALE', NULL)
ON CONFLICT (user_id) DO NOTHING;

-- 7. Points Ledger: Lịch sử điểm của David Nguyen (300 - 180 = 120 điểm khả dụng)
INSERT INTO points_ledger (id, tenant_id, member_id, transaction_type, points_delta, balance_after, reference_type, reference_id, description, created_at)
VALUES
    ('e0000000-0000-0000-0000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0000000-0000-0000-0000-000000000002', 'ORDER_EARN', 300, 300, 'ORDERS', 'ORD-20261015-INIT', 'Tích điểm đơn hàng khai trương', '2026-10-15 12:00:00+00'),
    ('e0000000-0000-0000-0000-000000000002', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0000000-0000-0000-0000-000000000002', 'VOUCHER_REDEEM', -180, 120, 'USER_VOUCHERS', 'VOUCH-PREV-REDEEM', 'Đổi mã giảm giá ưu đãi', '2026-10-28 15:30:00+00')
ON CONFLICT DO NOTHING;

-- 8. Categories: Boba Tea & Sushi Rolls
INSERT INTO categories (id, tenant_id, name, display_order, is_active)
VALUES
    ('c0000000-0000-0000-0000-000000000001', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Boba Tea & Drinks', 1, true),
    ('c0000000-0000-0000-0000-000000000002', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'Sushi Rolls', 2, true)
ON CONFLICT (id) DO NOTHING;

-- 9. Products: Aurora & Tuna Maki
INSERT INTO products (id, tenant_id, category_id, name, description, base_price, image_url, is_available, vat_rate)
VALUES
    ('f1111111-1111-1111-1111-111111111111', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'c0000000-0000-0000-0000-000000000001', 'Aurora', 'Trà sữa hoa đậu biếc thơm ngậy kết hợp trân châu xoài dâu tây', 89.00, 'https://images.unsplash.com/photo-1558857563-b37cf5a932d4?w=200&auto=format&fit=crop&q=80', true, 15.00),
    ('f2222222-2222-2222-2222-222222222222', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'c0000000-0000-0000-0000-000000000002', 'Tuna Maki', 'Cuộn sushi cá ngừ đại dương tươi ngon bọc rong biển giòn', 89.00, 'https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=200&auto=format&fit=crop&q=80', true, 15.00)
ON CONFLICT (id) DO NOTHING;

-- 10. Product Modifiers cho món Aurora
INSERT INTO product_modifier_groups (id, product_id, name, min_selection, max_selection, display_order)
VALUES
    ('g1111111-1111-1111-1111-111111111101', 'f1111111-1111-1111-1111-111111111111', 'Size', 1, 1, 1),
    ('g1111111-1111-1111-1111-111111111102', 'f1111111-1111-1111-1111-111111111111', 'Topping', 0, 3, 2),
    ('g1111111-1111-1111-1111-111111111103', 'f1111111-1111-1111-1111-111111111111', 'Sweetness Level', 1, 1, 3),
    ('g1111111-1111-1111-1111-111111111104', 'f1111111-1111-1111-1111-111111111111', 'Ice Level', 1, 1, 4)
ON CONFLICT (id) DO NOTHING;

INSERT INTO product_modifiers (id, group_id, name, extra_price, is_default)
VALUES
    ('m1111111-1111-1111-1111-111111111101', 'g1111111-1111-1111-1111-111111111101', 'Size M', 0.00, true),
    ('m1111111-1111-1111-1111-111111111102', 'g1111111-1111-1111-1111-111111111101', 'Size L', 15.00, false),
    ('m1111111-1111-1111-1111-111111111103', 'g1111111-1111-1111-1111-111111111102', 'Mango Popping', 10.00, false),
    ('m1111111-1111-1111-1111-111111111104', 'g1111111-1111-1111-1111-111111111102', 'Strawberry Popping', 10.00, false),
    ('m1111111-1111-1111-1111-111111111105', 'g1111111-1111-1111-1111-111111111102', 'Tapioca', 10.00, false),
    ('m1111111-1111-1111-1111-111111111106', 'g1111111-1111-1111-1111-111111111103', 'Standard', 0.00, true),
    ('m1111111-1111-1111-1111-111111111107', 'g1111111-1111-1111-1111-111111111104', 'Less ice', 0.00, true)
ON CONFLICT (id) DO NOTHING;

-- 11. Vouchers: 100 NOK & -30%
INSERT INTO vouchers (id, tenant_id, code, title, description, discount_type, discount_value, min_order_value, points_cost, valid_from, valid_until, is_active)
VALUES
    ('v1111111-1111-1111-1111-111111111111', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'SAVE100', 'Voucher Name', 'Giảm 100 NOK giá trị cho Order của bạn', 'FIXED_AMOUNT', 100.00, 250.00, 100, now(), '2025-12-03 23:59:59+00', true),
    ('v2222222-2222-2222-2222-222222222222', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'OFF30', 'Voucher Name', 'Giảm 30% giá trị cho đơn hàng của bạn', 'PERCENTAGE', 30.00, 0.00, 1000, now(), '2025-12-03 23:59:59+00', true)
ON CONFLICT (tenant_id, code) DO NOTHING;

-- 12. Loyalty Gifts: Cốc giữ nhiệt Tumbler Hồng
INSERT INTO loyalty_gifts (id, tenant_id, title, description, image_url, points_cost, stock_quantity, is_active)
VALUES (
    'g0000000-0000-0000-0000-000000000001',
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11',
    'Gift Name',
    'Phần này mô tả món quà tặng và lưu ý mà Admin thêm vào',
    'https://images.unsplash.com/photo-1577937927133-66ef06acdf18?w=160&auto=format&fit=crop&q=80',
    100,
    10,
    true
) ON CONFLICT (id) DO NOTHING;

-- 13. User Vouchers: David Nguyen sở hữu 2 voucher trong My Rewards
INSERT INTO user_vouchers (id, tenant_id, member_id, voucher_id, status, redeemed_at)
VALUES
    ('uv111111-1111-1111-1111-111111111111', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0000000-0000-0000-0000-000000000002', 'v1111111-1111-1111-1111-111111111111', 'AVAILABLE', now()),
    ('uv222222-2222-2222-2222-222222222222', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'd0000000-0000-0000-0000-000000000002', 'v2222222-2222-2222-2222-222222222222', 'AVAILABLE', now())
ON CONFLICT (id) DO NOTHING;

