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
package org.jtaccuino.gog.sampler.meta;

import java.util.List;

/**
 * The {@link TagKind#DATASET} catalogue: the data sources the examples render,
 * keyed to the dflib loader classes in {@code org.jtaccuino.gog.dflib.data}.
 * {@link #SYNTHETIC} covers frames built inline by the example factory itself.
 * <p>
 * Each constant also carries its {@link DatasetAttribution}: the authoritative
 * upstream source, citation and licence. This enum is the single source of
 * truth for dataset provenance — the annotation processor copies it into
 * {@code examples.properties} for the sampler and builder, and
 * {@code NOTICE}, the README table and {@code datasets.properties} are checked
 * against it by the build.
 */
public enum SampleDataset implements SampleTag {

    MPG("mpg", "Fuel Economy (mpg)"),
    MTCARS("mtcars", "Motor Trend Cars"),
    DIAMONDS("diamonds", "Diamonds"),
    FAITHFUL("faithful", "Old Faithful"),
    ANSCOMBE("anscombe", "Anscombe's Quartet"),
    MEAT("meat", "Meat Production"),
    SEATTLE_WEATHER("seattle-weather", "Seattle Hourly Weather"),
    GWAS("gwas", "Height GWAS"),
    POLAR("polar", "Polar Counts"),
    PENGUINS("penguins", "Palmer Penguins"),
    TIPS("tips", "Restaurant Tips"),
    MOUNTAIN("mountain", "Mountain Surface"),
    SPHERE("sphere", "Sphere Points"),
    SYNTHETIC("synthetic", "Synthetic / Function");

    private static final String MIT_URL = "https://opensource.org/license/mit";
    private static final String CC0_URL = "https://creativecommons.org/publicdomain/zero/1.0/";
    private static final String CC_BY_URL = "https://creativecommons.org/licenses/by/4.0/";
    private static final String GPL2_URL = "https://www.gnu.org/licenses/old-licenses/gpl-2.0.html";
    private static final String APACHE_URL = "https://www.apache.org/licenses/LICENSE-2.0";

    /**
     * Shared notice for data copied from R's base {@code datasets} package
     * (GPL-2.0-only): the file is aggregated, not linked, so the project's
     * Apache-2.0 licence is unaffected.
     */
    private static final String R_DATASETS_NOTICE =
            "This file is distributed under the GNU General Public License v2.0 as"
                    + " part of R's datasets package. It is included here as a stand-alone"
                    + " data file (mere aggregation) and does not affect the Apache-2.0"
                    + " licence of the gog4j code.";

    private final String feature;
    private final String label;

    SampleDataset(String feature, String label) {
        this.feature = feature;
        this.label = label;
    }

    @Override
    public TagKind kind() {
        return TagKind.DATASET;
    }

    @Override
    public String feature() {
        return feature;
    }

    @Override
    public String label() {
        return label;
    }

    /**
     * Returns the authoritative provenance and licence of this dataset.
     *
     * @return the attribution registered for this constant
     */
    public DatasetAttribution attribution() {
        return switch (this) {
            case MPG -> new DatasetAttribution("mpg",
                    "ggplot2 (R) \u2014 US EPA fuel economy data",
                    "https://ggplot2.tidyverse.org/reference/mpg.html",
                    "H. Wickham, ggplot2: Elegant Graphics for Data Analysis. Fuel economy"
                            + " data originally collected by the US Environmental Protection Agency"
                            + " (1999\u20132008).",
                    "MIT", "MIT License", MIT_URL, Origin.UPSTREAM, "", "",
                    List.of("/examples/mpg/mpg.csv"));

            case MTCARS -> new DatasetAttribution("mtcars",
                    "R datasets \u2014 Motor Trend car road tests",
                    "https://stat.ethz.ch/R-manual/R-devel/library/datasets/html/mtcars.html",
                    "D. A. Henderson & P. F. Velleman (1981). Building multiple regression"
                            + " models interactively. Biometrics 37, 391\u2013411."
                            + " Data extracted from the 1974 Motor Trend US magazine.",
                    "GPL-2.0-only", "GNU General Public License v2.0", GPL2_URL,
                    Origin.UPSTREAM,
                    "Bundled verbatim from R's datasets package.",
                    R_DATASETS_NOTICE,
                    List.of("/examples/mtcars/mtcars.csv"));

            case DIAMONDS -> new DatasetAttribution("diamonds",
                    "ggplot2 (R)",
                    "https://ggplot2.tidyverse.org/reference/diamonds.html",
                    "H. Wickham, ggplot2: Elegant Graphics for Data Analysis."
                            + " Prices and attributes of 53,940 round-cut diamonds.",
                    "MIT", "MIT License", MIT_URL, Origin.UPSTREAM, "", "",
                    List.of("/examples/diamonds/diamonds.csv"));

            case FAITHFUL -> new DatasetAttribution("faithful",
                    "R datasets \u2014 Old Faithful geyser",
                    "https://stat.ethz.ch/R-manual/R-devel/library/datasets/html/faithful.html",
                    "W. H\u00E4rdle (1991). Smoothing Techniques with Implementation in S."
                            + " Springer. Eruption and waiting times of the Old Faithful geyser,"
                            + " Yellowstone National Park.",
                    "GPL-2.0-only", "GNU General Public License v2.0", GPL2_URL,
                    Origin.UPSTREAM,
                    "faithful.csv is bundled verbatim from R's datasets package."
                            + " faithfuld.csv is a 2-D kernel density estimate computed from it"
                            + " and committed for the density examples.",
                    R_DATASETS_NOTICE,
                    List.of("/examples/faithful/faithful.csv", "/examples/faithfuld/faithfuld.csv"));

            case ANSCOMBE -> new DatasetAttribution("anscombe",
                    "Anscombe's quartet (1973)",
                    "https://doi.org/10.1080/00031305.1973.10478966",
                    "F. J. Anscombe (1973). Graphs in Statistical Analysis."
                            + " The American Statistician 27(1), 17\u201321.",
                    "Public-Domain", "Public domain (factual data)", "",
                    Origin.UPSTREAM,
                    "The four quartets are encoded inline in AnscombeDatasets.java.",
                    "",
                    List.of());

            case MEAT -> new DatasetAttribution("meat",
                    "USDA Economic Research Service \u2014 Livestock & Meat Domestic Data",
                    "https://www.ers.usda.gov/data-products/livestock-and-meat-domestic-data",
                    "U.S. Department of Agriculture, Economic Research Service."
                            + " Monthly US red-meat and poultry production.",
                    "Public-Domain", "Public domain (US Government work)", "",
                    Origin.UPSTREAM, "", "",
                    List.of("/examples/meat/meat.csv"));

            case SEATTLE_WEATHER -> new DatasetAttribution("seattle-weather",
                    "NOAA/NWS Seattle climate normals (via vega-datasets)",
                    "https://github.com/vega/vega-datasets",
                    "National Oceanic and Atmospheric Administration / National Weather Service"
                            + " hourly Seattle weather normals.",
                    "Public-Domain", "Public domain (US Government work)", "",
                    Origin.UPSTREAM, "", "",
                    List.of("/examples/seattle-weather/seattle-weather-hourly.csv"));

            case GWAS -> new DatasetAttribution("gwas",
                    "GIANT consortium human height GWAS",
                    "https://doi.org/10.1038/s41586-022-05275-y",
                    "L. Yengo, S. Vedantam, E. Marouli, J. Sidorenko, E. Bartell, S. Sakaue,"
                            + " et al. (2022). A saturated map of common genetic variants"
                            + " associated with human height. Nature 610, 704\u2013712.",
                    "CC-BY-4.0", "Creative Commons Attribution 4.0", CC_BY_URL,
                    Origin.DERIVED,
                    "Uniform 1-in-3 thinning of the summary statistics plus the published"
                            + " lead SNPs; p-values are unchanged. See examples/gwas/README.md.",
                    "",
                    List.of("/examples/gwas/height_lead_snps.csv",
                            "/examples/gwas/height_gwas_thinned.csv.gz"));

            case POLAR -> new DatasetAttribution("polar",
                    "Aggregated from mtcars (GPL-2.0-only) and diamonds (MIT)",
                    "",
                    "",
                    "Mixed", "Mixed: GPL-2.0-only (mtcars) and MIT (diamonds)", "",
                    Origin.DERIVED,
                    "Cylinder counts are aggregated from mtcars; clarity/cut counts are"
                            + " aggregated from diamonds at load time.",
                    "Derived counts inherit the licences of their sources: mtcars is"
                            + " GPL-2.0-only and diamonds is MIT.",
                    List.of());

            case PENGUINS -> new DatasetAttribution("penguins",
                    "palmerpenguins (R)",
                    "https://allisonhorst.github.io/palmerpenguins/",
                    "A. M. Horst, A. P. Hill & K. B. Gorman (2020). palmerpenguins:"
                            + " Palmer Archipelago (Antarctica) penguin data. Data collected by"
                            + " K. B. Gorman and the Palmer Station LTER.",
                    "CC0-1.0", "Creative Commons Zero v1.0 Universal", CC0_URL,
                    Origin.UPSTREAM, "", "",
                    List.of("/examples/penguins/penguins.csv"));

            case TIPS -> new DatasetAttribution("tips",
                    "reshape2 (R) \u2014 restaurant tips",
                    "https://github.com/hadley/reshape",
                    "P. G. Bryant & M. Smith (1995). Practical Data Analysis: Case Studies"
                            + " in Business Statistics. Richard D. Irwin.",
                    "MIT", "MIT License", MIT_URL, Origin.UPSTREAM, "", "",
                    List.of("/examples/tips/tips.csv"));

            case MOUNTAIN -> new DatasetAttribution("mountain",
                    "Generated mountain surface (gog4j)",
                    "",
                    "",
                    "Apache-2.0", "Apache License 2.0", APACHE_URL, Origin.SYNTHETIC,
                    "Sampled from a two-peak height function over [-3, 3]\u00B2 in"
                            + " MountainDatasets.java.",
                    "",
                    List.of());

            case SPHERE -> new DatasetAttribution("sphere",
                    "Generated sphere point cloud (gog4j)",
                    "",
                    "",
                    "Apache-2.0", "Apache License 2.0", APACHE_URL, Origin.SYNTHETIC,
                    "Sampled on a latitude/longitude grid in SpherePointsDatasets.java.",
                    "",
                    List.of());

            case SYNTHETIC -> new DatasetAttribution("synthetic",
                    "Synthetic frame generated in-code (gog4j)",
                    "",
                    "",
                    "Apache-2.0", "Apache License 2.0", APACHE_URL, Origin.SYNTHETIC,
                    "",
                    "",
                    List.of());
        };
    }
}
