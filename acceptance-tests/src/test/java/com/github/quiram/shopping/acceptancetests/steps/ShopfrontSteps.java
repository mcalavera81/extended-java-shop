package com.github.quiram.shopping.acceptancetests.steps;

import com.github.quiram.shopping.acceptancetests.entities.Product;
import com.github.quiram.utils.Collections;
import net.serenitybdd.core.Serenity;
import net.thucydides.core.annotations.Step;
import org.hamcrest.Matchers;

import java.math.BigDecimal;
import java.util.*;

import static com.github.quiram.utils.Collections.mergeMaps;
import static com.github.quiram.utils.Collections.toMap;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static net.serenitybdd.core.Serenity.sessionVariableCalled;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class ShopfrontSteps extends StepsBase {
    private static final String PRICES_COLLECTION_KEY = "prices_collection";
    private static final String SHOPFRONT_IP = System.getenv("shopfront_ip");
    private static final String SHOPFRONT_URL = format("http://%s:8010/", SHOPFRONT_IP);
    private List<Product> productList;

    @Step
    public void shopfront_service_is_ready() {
        waitForService(SHOPFRONT_URL);
    }

    @Step
    public void user_obtains_the_list_of_products() {
        productList = getAllProducts();
    }

    @Step("There are {0} products in the list")
    public void product_list_has_size(int size) {
        assertThat(productList, Matchers.hasSize(size));
    }

    @Step
    public void check_all_prices() {
        checkPrices();
    }

    @Step
    public void check_all_prices_again() {
        checkPrices();
    }

    @Step
    public void prices_have_not_changed() {
        Map<Integer, Set<BigDecimal>> allPrices = aggregatePrices();
        allPrices.forEach((i, s) -> assertThat(allPrices.get(i), hasSize(1)));
    }

    @Step
    public void all_prices_have_changed() {
        Map<Integer, Set<BigDecimal>> allPrices = aggregatePrices();
        allPrices.forEach((i, s) -> assertThat(allPrices.get(i), hasSize(2)));
    }

    private Map<Integer, Set<BigDecimal>> aggregatePrices() {
        Optional<Map<Integer, Set<BigDecimal>>> aggregatedPrices = getPricesCollection().stream().reduce((m1, m2) -> mergeMaps(Collections::mergeSets, m1, m2));
        assertTrue(aggregatedPrices.isPresent());
        return aggregatedPrices.get();
    }

    @SuppressWarnings("unchecked")
    private Collection<Map<Integer, Set<BigDecimal>>> getPricesCollection() {
        Collection<Map<Integer, Set<BigDecimal>>> pricesCollection = (Collection<Map<Integer, Set<BigDecimal>>>) sessionVariableCalled(PRICES_COLLECTION_KEY);
        return pricesCollection == null ? new LinkedList<>() : pricesCollection;
    }

    private void checkPrices() {
        final Map<Integer, Set<BigDecimal>> pricesCurrentlyOnDisplay = toMap(getAllProducts(), Product::getId, p -> singleton(p.getPrice()));
        addToPrices(pricesCurrentlyOnDisplay);
    }

    private void addToPrices(Map<Integer, Set<BigDecimal>> prices) {
        final Collection<Map<Integer, Set<BigDecimal>>> pricesCollection = getPricesCollection();
        pricesCollection.add(prices);
        Serenity.setSessionVariable(PRICES_COLLECTION_KEY).to(pricesCollection);
    }

    private List<Product> getAllProducts() {
        return asList(
                given().contentType(JSON)
                        .when().get(SHOPFRONT_URL + "products")
                        .andReturn().body().as(Product[].class)
        );
    }
}