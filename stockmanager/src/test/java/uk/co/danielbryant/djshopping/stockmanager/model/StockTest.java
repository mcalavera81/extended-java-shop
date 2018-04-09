package uk.co.danielbryant.djshopping.stockmanager.model;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.function.Function;

import static com.amarinperez.test_utils.Exceptions.expectException;
import static com.amarinperez.utils.Random.randomString;
import static java.lang.String.format;
import static java.util.Arrays.asList;

public class StockTest {
    @Rule
    public ExpectedException onBadInput = ExpectedException.none();

    @Test
    public void cannotHaveNegativeAmountOfStock() {
        onBadInput.expect(IllegalArgumentException.class);
        onBadInput.expectMessage("amount");
        onBadInput.expectMessage("negative");
        new Stock(randomString(), randomString(), -1);
    }

    @Test
    public void availableAmountCanBeZero() {
        new Stock(randomString(), randomString(), 0);
    }

    @Test
    public void availableAmountCanBePositive() {
        new Stock(randomString(), randomString(), 10);
    }

    @Test
    public void idMustHaveValue() {
        Function<String, Stock> constructor = id -> new Stock(id, randomString(), 1);
        final String field = "productId";

        assertCannotCreate(constructor, field);
    }

    @Test
    public void skuMustHaveValue() {
        Function<String, Stock> constructor = sku -> new Stock(randomString(), sku, 1);
        final String field = "sku";

        assertCannotCreate(constructor, field);
    }

    private static void assertCannotCreate(Function<String, Stock> constructor, String field) {
        asList(null, "", "    ").forEach(value ->
                expectException(() -> constructor.apply(value), IllegalArgumentException.class,
                        field, format("Exception expected when testing value '%s' for field '%s;", value, field)));
    }
}
