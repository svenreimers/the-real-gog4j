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
package org.jtaccuino.gog.examples.dflib;

import static org.jtaccuino.gog.Aes.aes;
import static org.jtaccuino.gog.Geoms.point3d;
import static org.jtaccuino.gog.Ggplot.ggplot3d;
import static org.jtaccuino.gog.labs.Labs.labs;

import org.dflib.DataFrame;
import org.jtaccuino.gog.Plot;
import org.jtaccuino.gog.dflib.data.SeattleWeatherDatasets;
import org.jtaccuino.gog.sampler.meta.SampleCoord;
import org.jtaccuino.gog.sampler.meta.SampleDataset;
import org.jtaccuino.gog.sampler.meta.SampleFeature;
import org.jtaccuino.gog.sampler.meta.SampleGeom;
import org.jtaccuino.gog.sampler.meta.SamplePlot;

/**
 * The Seattle hourly weather dataset on a 3D cube, exercising a timestamp axis
 * under {@code coord3d()}: the date column breaks and labels in calendar terms
 * exactly as it does on a Cartesian axis.
 */
public class SeattleWeatherPlots3d {

    /** Utility class; not meant to be instantiated. */
    private SeattleWeatherPlots3d() {
    }

    /**
     * A day of hourly readings plotted in three dimensions with the date on the
     * x axis, temperature on y, and pressure on z.
     *
     * @return the configured plot
     */
    @SamplePlot(description = "Seattle hourly weather on a 3D cube: the timestamp x axis breaks and labels in calendar terms.",
            title = "Timestamp Axis in 3D",
            dataset = SampleDataset.SEATTLE_WEATHER,
            geoms = {SampleGeom.POINT},
            coords = {SampleCoord.COORD3D},
            features = {SampleFeature.THREE_D})
    public static Plot<DataFrame> createTimestampScatter() {
        return ggplot3d(SeattleWeatherDatasets.loadFirstDay(),
                aes().x("date").y("temperature").z("pressure"))
                .geoms(point3d().size(6.0))
                .labs(labs("Timestamp axis in 3D", "Date", "Temperature (°C)"));
    }
}
