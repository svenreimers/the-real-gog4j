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
package org.jtaccuino.gog.sampler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.jtaccuino.gog.sampler.meta.DatasetAttribution;
import org.jtaccuino.gog.sampler.meta.Origin;
import org.jtaccuino.gog.sampler.meta.SampleDataset;
import org.junit.jupiter.api.Test;

/**
 * Keeps the dataset provenance in lock-step across its four representations:
 * the {@link SampleDataset} enum (the source of truth), the machine-readable
 * {@code META-INF/gog4j/datasets.properties} manifest shipped with the
 * {@code gog4j-data} jar, the root {@code NOTICE}, the README table, and the
 * bundled CSV/CSV.GZ resources themselves. A new dataset added to one place
 * without the others fails here instead of shipping unattributed.
 */
class DatasetAttributionTest {

    private static final String MANIFEST = "/META-INF/gog4j/datasets.properties";

    @Test
    void everyDatasetHasCompleteAttribution() {
        for (var dataset : SampleDataset.values()) {
            var attribution = dataset.attribution();
            assertFalse(attribution.isEmpty(), dataset + " must carry attribution");
            assertEquals(dataset.feature(), attribution.datasetId(),
                    dataset + " attribution id must match its tag feature");
            assertFalse(attribution.sourceName().isBlank(), dataset + " needs a source name");
            assertFalse(attribution.licenseId().isBlank(), dataset + " needs a licence id");
            assertFalse(attribution.licenseName().isBlank(), dataset + " needs a licence name");
            if (attribution.origin() == Origin.UPSTREAM) {
                assertTrue(attribution.sourceUrl().startsWith("http"),
                        dataset + " upstream data needs a source URL, had '"
                                + attribution.sourceUrl() + "'");
            }
            var publicOrMixed = Set.of("Public-Domain", "Mixed").contains(attribution.licenseId());
            if (!publicOrMixed) {
                assertTrue(attribution.licenseUrl().startsWith("http"),
                        dataset + " needs a licence URL, had '" + attribution.licenseUrl() + "'");
            }
        }
    }

    @Test
    void encodeDecodeRoundTrips() {
        assertEquals(DatasetAttribution.none(), DatasetAttribution.decode(
                DatasetAttribution.none().encode()));
        for (var dataset : SampleDataset.values()) {
            var attribution = dataset.attribution();
            assertEquals(attribution, DatasetAttribution.decode(attribution.encode()),
                    dataset + " attribution must survive the properties wire format");
        }
    }

    @Test
    void manifestMatchesTheRegistry() {
        var props = loadManifest();
        var declaredIds = new TreeSet<String>();
        for (var key : props.stringPropertyNames()) {
            declaredIds.add(key.substring(0, key.indexOf('.')));
        }
        var expectedIds = new TreeSet<String>();
        for (var dataset : SampleDataset.values()) {
            expectedIds.add(dataset.feature());
            var attribution = dataset.attribution();
            var prefix = dataset.feature() + ".";
            assertEquals(attribution.sourceName(), props.getProperty(prefix + "sourceName"));
            assertEquals(attribution.sourceUrl(), props.getProperty(prefix + "sourceUrl"));
            assertEquals(attribution.citation(), props.getProperty(prefix + "citation"));
            assertEquals(attribution.licenseId(), props.getProperty(prefix + "licenseId"));
            assertEquals(attribution.licenseName(), props.getProperty(prefix + "licenseName"));
            assertEquals(attribution.licenseUrl(), props.getProperty(prefix + "licenseUrl"));
            assertEquals(attribution.origin().name(), props.getProperty(prefix + "origin"));
        }
        assertEquals(expectedIds, declaredIds,
                "datasets.properties must describe exactly the registered datasets");
    }

    @Test
    void noticeCoversEveryDataset() {
        var notice = readRepoFile("NOTICE");
        for (var dataset : SampleDataset.values()) {
            var attribution = dataset.attribution();
            assertTrue(notice.contains(attribution.sourceName()),
                    "NOTICE must mention the source of " + dataset);
            assertTrue(notice.contains(attribution.licenseName()),
                    "NOTICE must mention the licence of " + dataset);
            if ("GPL-2.0-only".equals(attribution.licenseId())) {
                assertTrue(notice.contains("mere aggregation"),
                        dataset + " is GPL-2.0-only and must be documented as mere aggregation");
            }
        }
    }

    @Test
    void readmeListsEveryDataset() {
        var readme = readRepoFile("README.md");
        for (var dataset : SampleDataset.values()) {
            assertTrue(readme.contains("`" + dataset.feature() + "`"),
                    "README must list the " + dataset.feature() + " dataset");
        }
    }

    @Test
    void bundledResourcesExist() throws IOException {
        for (var dataset : SampleDataset.values()) {
            for (var resource : dataset.attribution().resources()) {
                try (var in = DatasetAttributionTest.class.getResourceAsStream(resource)) {
                    assertNotNull(in, dataset + " declares missing resource " + resource);
                    assertTrue(in.read() >= 0, dataset + " declares empty resource " + resource);
                }
            }
        }
    }

    @Test
    void everyBundledCsvIsAttributed() throws IOException {
        var examplesDir = repoRoot()
                .resolve("gog4j-data/src/main/resources/examples");
        var declared = java.util.Arrays.stream(SampleDataset.values())
                .flatMap(dataset -> dataset.attribution().resources().stream())
                .map(resource -> resource.substring(resource.indexOf("examples/") + "examples/".length()))
                .collect(Collectors.toSet());

        try (var paths = Files.walk(examplesDir)) {
            var csvResources = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        var name = path.getFileName().toString();
                        return name.endsWith(".csv") || name.endsWith(".csv.gz");
                    })
                    .map(examplesDir::relativize)
                    .map(Path::toString)
                    .map(path -> path.replace('\\', '/'))
                    // The unmodified GWAS sources are git-ignored and may be
                    // present after a local derivation run; they are not bundled.
                    .filter(path -> !path.startsWith("gwas/upstream/"))
                    .collect(Collectors.toCollection(TreeSet::new));
            var unattributed = new ArrayList<String>();
            for (var resource : csvResources) {
                if (!declared.contains(resource)) {
                    unattributed.add(resource);
                }
            }
            assertTrue(unattributed.isEmpty(),
                    "these bundled CSVs have no dataset attribution: " + unattributed);
            assertFalse(csvResources.isEmpty(), "expected to find bundled example CSVs");
        }
    }

    private static Properties loadManifest() {
        var props = new Properties();
        try (var in = DatasetAttributionTest.class.getResourceAsStream(MANIFEST)) {
            assertNotNull(in, "missing " + MANIFEST + " on the classpath");
            props.load(in);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return props;
    }

    private static String readRepoFile(String name) {
        try {
            return Files.readString(repoRoot().resolve(name), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("cannot read " + name, e);
        }
    }

    private static Path repoRoot() {
        var dir = Path.of("").toAbsolutePath();
        while (dir != null && !Files.exists(dir.resolve("settings.gradle"))) {
            dir = dir.getParent();
        }
        assertNotNull(dir, "could not locate the repository root (settings.gradle)");
        return dir;
    }
}
