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
package org.jtaccuino.gog.builder;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Supplier;
import org.dflib.DataFrame;
import org.jtaccuino.gog.controls.PlotDataset;
import org.jtaccuino.gog.dflib.data.DiamondsDatasets;
import org.jtaccuino.gog.dflib.data.FaithfulDatasets;
import org.jtaccuino.gog.dflib.data.MeatDatasets;
import org.jtaccuino.gog.dflib.data.MountainDatasets;
import org.jtaccuino.gog.dflib.data.MpgDatasets;
import org.jtaccuino.gog.dflib.data.MtcarsDatasets;
import org.jtaccuino.gog.dflib.data.PenguinsDatasets;
import org.jtaccuino.gog.dflib.data.SpherePointsDatasets;
import org.jtaccuino.gog.dflib.data.TipsDatasets;
import org.jtaccuino.gog.sampler.meta.SampleDataset;

/**
 * The built-in dflib datasets offered by the {@code PlotBuilder}, each adapted
 * to the data-agnostic {@link PlotDataset} contract so the builder's controls
 * package stays free of a DataFrame dependency.
 */
public final class DflibDatasets {

    /** Utility class; not meant to be instantiated. */
    private DflibDatasets() {
    }

    /** The datasets the builder offers, in display order. */
    public static List<PlotDataset> all() {
        return List.of(
                dflib("MPG", SampleDataset.MPG, "MpgDatasets.loadMpg()",
                        "org.jtaccuino.gog.dflib.data.MpgDatasets", MpgDatasets::loadMpg),
                dflib("Diamonds", SampleDataset.DIAMONDS, "DiamondsDatasets.loadDiamonds()",
                        "org.jtaccuino.gog.dflib.data.DiamondsDatasets", DiamondsDatasets::loadDiamonds),
                dflib("Penguins", SampleDataset.PENGUINS, "PenguinsDatasets.loadPenguins()",
                        "org.jtaccuino.gog.dflib.data.PenguinsDatasets", PenguinsDatasets::loadPenguins),
                dflib("Tips", SampleDataset.TIPS, "TipsDatasets.loadTips()",
                        "org.jtaccuino.gog.dflib.data.TipsDatasets", TipsDatasets::loadTips),
                dflib("Faithful", SampleDataset.FAITHFUL, "FaithfulDatasets.loadFaithful()",
                        "org.jtaccuino.gog.dflib.data.FaithfulDatasets", FaithfulDatasets::loadFaithful),
                dflib("Mtcars", SampleDataset.MTCARS, "MtcarsDatasets.loadNumericMtcars()",
                        "org.jtaccuino.gog.dflib.data.MtcarsDatasets", MtcarsDatasets::loadNumericMtcars),
                dflib("Meat", SampleDataset.MEAT, "MeatDatasets.getProductionData()",
                        "org.jtaccuino.gog.dflib.data.MeatDatasets", MeatDatasets::getProductionData),
                dflib("Mountain Surface", SampleDataset.MOUNTAIN, "MountainDatasets.loadMountain()",
                        "org.jtaccuino.gog.dflib.data.MountainDatasets", MountainDatasets::loadMountain),
                dflib("Sphere Points", SampleDataset.SPHERE, "SpherePointsDatasets.loadSpherePoints()",
                        "org.jtaccuino.gog.dflib.data.SpherePointsDatasets", SpherePointsDatasets::loadSpherePoints));
    }

    private static PlotDataset dflib(String displayName, SampleDataset dataset, String loaderExpr,
            String loaderImport, Supplier<DataFrame> loader) {
        return new PlotDataset() {
            @Override
            public String displayName() {
                return displayName;
            }

            @Override
            public List<String> attributionLines() {
                return dataset.attribution().displayLines();
            }

            @Override
            public List<String> columns() {
                var index = loader.get().getColumnsIndex();
                var columns = new ArrayList<String>(index.size());
                for (String column : index) {
                    columns.add(column);
                }
                return columns;
            }

            @Override
            public List<String> distinctValues(String column) {
                var seen = new LinkedHashSet<String>();
                var frame = loader.get();
                var series = frame.getColumn(column);
                for (var i = 0; i < frame.height(); i++) {
                    seen.add(String.valueOf(series.get(i)));
                }
                return List.copyOf(seen);
            }

            @Override
            public Object data() {
                return loader.get();
            }

            @Override
            public String loaderExpr() {
                return loaderExpr;
            }

            @Override
            public String loaderImport() {
                return loaderImport;
            }

            @Override
            public String toString() {
                return displayName;
            }
        };
    }
}
