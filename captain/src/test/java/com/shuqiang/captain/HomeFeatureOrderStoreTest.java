package com.shuqiang.captain;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HomeFeatureOrderStoreTest {
    @Test
    public void resolveOrderShouldAppendNewIdsAndIgnoreUnknownIds() {
        assertEquals(
                Arrays.asList("c", "a", "b", "d"),
                HomeFeatureOrderStore.resolveOrder(
                        Arrays.asList("a", "b", "c", "d"),
                        Arrays.asList("c", "ghost", "a"))
        );
    }

    @Test
    public void mergeVisibleOrderShouldKeepHiddenSlotsStable() {
        assertEquals(
                Arrays.asList("c", "hidden", "a", "b"),
                HomeFeatureOrderStore.mergeVisibleOrder(
                        Arrays.asList("a", "hidden", "b", "c"),
                        Arrays.asList("c", "a", "b"))
        );
    }

    @Test
    public void mergeVisibleOrderShouldHandleEmptyVisibleList() {
        assertEquals(
                Collections.singletonList("only"),
                HomeFeatureOrderStore.mergeVisibleOrder(
                        Collections.singletonList("only"),
                        Collections.emptyList())
        );
    }

    @Test
    public void moveItemShouldShiftIntermediateItemsWhenMovingForward() {
        List<String> ids = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));
        HomeFeatureOrderStore.moveItem(ids, 0, 2);
        assertEquals(Arrays.asList("b", "c", "a", "d"), ids);
    }

    @Test
    public void moveItemShouldShiftIntermediateItemsWhenMovingBackward() {
        List<String> ids = new ArrayList<>(Arrays.asList("a", "b", "c", "d"));
        HomeFeatureOrderStore.moveItem(ids, 3, 1);
        assertEquals(Arrays.asList("a", "d", "b", "c"), ids);
    }
}
