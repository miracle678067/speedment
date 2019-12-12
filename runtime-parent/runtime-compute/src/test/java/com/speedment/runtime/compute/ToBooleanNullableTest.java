/*
 *
 * Copyright (c) 2006-2019, Speedment, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); You may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.speedment.runtime.compute;

import static com.speedment.runtime.compute.expression.ExpressionType.BOOLEAN_NULLABLE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.speedment.runtime.compute.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.function.Function;

final class ToBooleanNullableTest {

    private static final ToBooleanNullable<String> DEFAULT_NULLABLE = string -> string.length() > 2;

    @ParameterizedTest
    @ValueSource(strings = "test")
    void of(String input) {
        Function<String, Boolean> function = string -> string.length() > 2;
        ToBooleanNullable<String> fromFunction = ToBooleanNullable.of(function);

        assertNotNull(fromFunction);
        assertEquals(function.apply(input), fromFunction.apply(input));

        ToBooleanNullable<String> raw = DEFAULT_NULLABLE;
        ToBooleanNullable<String> fromRaw = ToBooleanNullable.of(raw);

        assertNotNull(fromFunction);
        assertEquals(raw.apply(input), fromRaw.apply(input));
    }

    @Test
    void expressionType() {
        ToBooleanNullable<String> toBooleanNullable = string -> null;

        Assertions.assertEquals(BOOLEAN_NULLABLE, toBooleanNullable.expressionType());
    }

    @Test
    void orThrow() {
        ToBooleanNullable<String> nullValue = string -> null;
        assertDoesNotThrow(nullValue::orThrow);

        ToBoolean<String> toBoolean = nullValue.orThrow();
        assertThrows(NullPointerException.class, () -> toBoolean.applyAsBoolean(""));

        assertDoesNotThrow(DEFAULT_NULLABLE::orThrow);
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "foo", "test"})
    void orElseGet(String input) {
        ToBooleanNullable<String> nullValue = string -> null;
        ToBoolean<String> toBoolean = nullValue.orElseGet(string -> string.length() > 2);

        assertEquals(input.length() > 2, toBoolean.applyAsBoolean(input));

        toBoolean = DEFAULT_NULLABLE.orElseGet(string -> true);

        assertEquals(input.length() > 2, toBoolean.applyAsBoolean(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test", "foo", ""})
    void orElse(String input) {
        ToBooleanNullable<String> nullValue = string -> null;
        ToBoolean<String> toBoolean = nullValue.orElse(true);

        assertTrue(toBoolean.applyAsBoolean(input));

        toBoolean = DEFAULT_NULLABLE.orElse(true);

        assertEquals(input.length() > 2, toBoolean.applyAsBoolean(input));
    }

    @Test
    void mapToDoubleIfPresent() {
        ToDoubleNullable<String> toDoubleNullable = DEFAULT_NULLABLE
                .mapToDoubleIfPresent(bool -> bool ? 1 : 0);

        assertNotNull(toDoubleNullable);
        assertEquals(1, toDoubleNullable.applyAsDouble("three"));
        assertEquals(0, toDoubleNullable.applyAsDouble("1"));
    }

    @Test
    void mapIfPresent() {
        ToBooleanNullable<String> toBooleanNullable = DEFAULT_NULLABLE.mapIfPresent(bool -> !bool);

        assertNotNull(toBooleanNullable);
        assertTrue(toBooleanNullable.applyAsBoolean("1"));
        assertFalse(toBooleanNullable.applyAsBoolean("three"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"test", "foo"})
    void hash(String input) {
        ToBooleanNullable<String> nullValue = string -> null;
        assertEquals(0, nullValue.hash(input));

        assertNotEquals(0, DEFAULT_NULLABLE.hash(input));
    }

    @Test
    void compare() {
        ToBooleanNullable<String> raw = string -> string.length() > 4 ? true : null;

        Pair<String, String> nullNull = new Pair<>("foo", "bar");
        Pair<String, String> nullHas = new Pair<>("foo", "longer");
        Pair<String, String> hasNull = new Pair<>("longer", "foo");
        Pair<String, String> hasHas = new Pair<>("longer", "longer");

        assertEquals(0, raw.compare(nullNull.getFirst(), nullNull.getSecond()));
        assertEquals(1, raw.compare(nullHas.getFirst(), nullHas.getSecond()));
        assertEquals(-1, raw.compare(hasNull.getFirst(), hasNull.getSecond()));
        assertEquals(0, raw.compare(hasHas.getFirst(), hasHas.getSecond()));
    }

    @Test
    void compose() {
        assertThrows(NullPointerException.class, () -> DEFAULT_NULLABLE.compose(null));

        ToBooleanNullable<Boolean> composed = DEFAULT_NULLABLE.compose(Object::toString);

        assertNotNull(composed);
    }
}
