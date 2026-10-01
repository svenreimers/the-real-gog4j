/*
 * Copyright 2026 JTaccuino project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jtaccuino.gog;

import static org.jtaccuino.gog.test.JavaFxToolkitExtension.onFxThread;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import org.dflib.DataFrame;
import org.dflib.Series;
import org.jtaccuino.gog.dflib.data.SeattleWeatherDatasets;
import org.jtaccuino.gog.examples.dflib.SeattleWeatherPlots3d;
import org.jtaccuino.gog.render.SvgExporter;
import org.jtaccuino.gog.test.JavaFxToolkitExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Renders a 3D figure with a timestamp axis and guards the cube furniture: a
 * {@code DATE}/{@code TIMESTAMP} axis must break and label in calendar terms
 * instead of printing a raw epoch number.
 */
@ExtendWith(JavaFxToolkitExtension.class)
class Timestamp3dRenderTest {

    private static final int WIDTH = 900;
    private static final int HEIGHT = 700;

    @Test
    void aTemporalXAxisIn3dLabelsInCalendarTerms() throws Exception {
        var svg = onFxThread(() -> new SvgExporter().size(WIDTH, HEIGHT)
                .toSvg(SeattleWeatherPlots3d.createTimestampScatter()));
        assertTrue(svg.contains("2010"),
                "the 3D timestamp axis must label with a calendar date");
        // The numeric furniture used to print a truncated integer instead.
        assertFalse(svg.contains("1262304000"),
                "the axis must not print a raw epoch value");
    }

    @Test
    void aTemporalZAxisIn3dLabelsInCalendarTerms() throws Exception {
        var svg = onFxThread(() -> new SvgExporter().size(WIDTH, HEIGHT)
                .toSvg(Ggplot.ggplot3d(SeattleWeatherDatasets.loadFirstDay(),
                        Aes.aes().x("temperature").y("pressure").z("date"))
                        .geoms(Geoms.point3d())));
        assertTrue(svg.contains("2010"), "a temporal z axis must label in calendar terms");
    }

    @Test
    void aDateAxisIn3dLabelsWithYears() throws Exception {
        var when = Series.of(LocalDate.of(2000, 1, 1), LocalDate.of(2005, 1, 1), LocalDate.of(2010, 1, 1));
        var df = DataFrame.byColumn("when", "v", "w")
                .of(when, Series.ofDouble(1, 2, 3), Series.ofDouble(3, 2, 1));
        var svg = onFxThread(() -> new SvgExporter().size(WIDTH, HEIGHT)
                .toSvg(Ggplot.ggplot3d(df, Aes.aes().x("when").y("v").z("w")).geoms(Geoms.point3d())));
        assertTrue(svg.contains("2000"), "a date axis must label with its years");
    }
}
