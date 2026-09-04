package com.skillnet.serializedcoupon.service;

import com.skillnet.serializedcoupon.domain.CouponCodes;
import com.skillnet.serializedcoupon.exception.BusinessValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponCodeGeneratorTest {

    private static final Pattern REQUIRED_PATTERN = Pattern.compile("^FF[0-9]{4}[A-Z0-9]{8}$");
    private CouponCodeGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new CouponCodeGenerator();
    }

    @Test
    void generatedCodeHasLength14AndMatchesRequiredPattern() {
        String code = generator.generate("1234");
        assertThat(code).hasSize(CouponCodes.TOTAL_LENGTH);
        assertThat(code).startsWith("FF");
        assertThat(code.substring(2, 6)).isEqualTo("1234");
        assertThat(REQUIRED_PATTERN.matcher(code).matches()).isTrue();
    }

    @Test
    void generatedSuffixDoesNotContainAmbiguousCharacters() {
        Set<Character> forbidden = Set.of('I', 'O', '0', '1');
        for (int i = 0; i < 200; i++) {
            String code = generator.generate("0000");
            String suffix = code.substring(6);
            assertThat(suffix).hasSize(8);
            for (char ch : suffix.toCharArray()) {
                assertThat(forbidden).doesNotContain(ch);
                assertThat(CouponCodes.SUFFIX_ALPHABET.indexOf(ch)).isGreaterThanOrEqualTo(0);
            }
        }
    }

    @Test
    void programCode0000And9999AreAccepted() {
        assertThat(generator.generate("0000")).startsWith("FF0000");
        assertThat(generator.generate("9999")).startsWith("FF9999");
    }

    @Test
    void generatedCodesAreMostlyUnique() {
        Set<String> codes = new HashSet<>();
        for (int i = 0; i < 500; i++) {
            codes.add(generator.generate("4321"));
        }
        assertThat(codes.size()).isGreaterThan(490);
    }

    @Test
    void invalidProgramCodesAreRejected() {
        assertThatThrownBy(() -> generator.generate("123")).isInstanceOf(BusinessValidationException.class);
        assertThatThrownBy(() -> generator.generate("12345")).isInstanceOf(BusinessValidationException.class);
        assertThatThrownBy(() -> generator.generate("12ab")).isInstanceOf(BusinessValidationException.class);
        assertThatThrownBy(() -> generator.generate(null)).isInstanceOf(BusinessValidationException.class);
        assertThatThrownBy(() -> generator.generate("abcd")).isInstanceOf(BusinessValidationException.class);
    }
}
