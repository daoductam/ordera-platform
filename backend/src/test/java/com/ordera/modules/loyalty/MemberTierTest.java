package com.ordera.modules.loyalty;

import com.ordera.modules.loyalty.domain.model.MemberTier;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MemberTier Domain Enum Unit Tests")
class MemberTierTest {

    @Test
    @DisplayName("should_have_exactly_six_tiers_in_proper_order")
    void should_have_exactly_six_tiers_in_proper_order() {
        // Assert
        assertEquals(6, MemberTier.values().length);
        assertEquals(MemberTier.NEW, MemberTier.fromOrder(1));
        assertEquals(MemberTier.SILVER, MemberTier.fromOrder(2));
        assertEquals(MemberTier.GOLD, MemberTier.fromOrder(3));
        assertEquals(MemberTier.PLATINUM, MemberTier.fromOrder(4));
        assertEquals(MemberTier.DIAMOND, MemberTier.fromOrder(5));
        assertEquals(MemberTier.RUBY, MemberTier.fromOrder(6));
    }

    @Test
    @DisplayName("should_return_correct_tier_properties")
    void should_return_correct_tier_properties() {
        // Assert
        assertEquals(0, MemberTier.NEW.getMinPointsRequired());
        assertEquals("#1EB759", MemberTier.NEW.getThemeColorHex());
        assertEquals(400, MemberTier.GOLD.getMinPointsRequired());
        assertEquals("#F5B800", MemberTier.GOLD.getThemeColorHex());
        assertEquals(4000, MemberTier.RUBY.getMinPointsRequired());
        assertEquals("#D9148E", MemberTier.RUBY.getThemeColorHex());
    }
}

