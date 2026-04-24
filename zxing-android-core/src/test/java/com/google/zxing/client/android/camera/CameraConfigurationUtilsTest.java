package com.google.zxing.client.android.camera;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;

public class CameraConfigurationUtilsTest {

  @Test
  public void fastQrPreviewSizePrefersHighestSizeInsideTargetRange() {
    int[] selected = CameraConfigurationUtils.findFastQrPreviewSizeValue(
        Arrays.asList(
            new int[]{640, 480},
            new int[]{1280, 720},
            new int[]{1920, 1080},
            new int[]{3840, 2160}),
        1080,
        1920,
        null);

    assertPoint(1920, 1080, selected);
  }

  @Test
  public void fastQrPreviewSizeUsesLargestBelowTargetWhenNoTargetRangeExists() {
    int[] selected = CameraConfigurationUtils.findFastQrPreviewSizeValue(
        Arrays.asList(
            new int[]{320, 240},
            new int[]{640, 360},
            new int[]{960, 540}),
        1080,
        1920,
        null);

    assertPoint(960, 540, selected);
  }

  @Test
  public void fastQrPreviewSizeUsesSmallestAboveTargetToAvoidUnboundedMaxResolution() {
    int[] selected = CameraConfigurationUtils.findFastQrPreviewSizeValue(
        Arrays.asList(
            new int[]{3840, 2160},
            new int[]{2560, 1440},
            new int[]{4000, 3000}),
        1080,
        1920,
        null);

    assertPoint(2560, 1440, selected);
  }

  @Test
  public void fastQrPreviewSizeKeepsSixteenByNineOnTallScreens() {
    int[] selected = CameraConfigurationUtils.findFastQrPreviewSizeValue(
        Arrays.asList(
            new int[]{1280, 720},
            new int[]{1920, 1080}),
        1080,
        2400,
        null);

    assertPoint(1920, 1080, selected);
  }

  private static void assertPoint(int expectedX, int expectedY, int[] actual) {
    assertEquals(expectedX, actual[0]);
    assertEquals(expectedY, actual[1]);
  }
}
