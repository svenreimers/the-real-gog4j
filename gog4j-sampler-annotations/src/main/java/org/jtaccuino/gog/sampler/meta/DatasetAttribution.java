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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Provenance and licence metadata for one example dataset: where the data came
 * from, under which licence, how to cite it, whether it is a verbatim copy or a
 * derivation, and which bundled resources back it. The sampler and builder show
 * this to the user, {@code NOTICE} and the README table are kept in step with
 * it, and {@code DatasetAttributionTest} fails the build when they drift.
 *
 * <p>The annotation processor serialises one instance per example into
 * {@code examples.properties}; {@link #encode()} / {@link #decode(String)} are
 * the wire format. Every field is Base64-encoded and joined with a unit
 * separator, so arbitrary URLs, citations and notice text survive the
 * {@code java.util.Properties} round trip without escaping ambiguity.
 *
 * @param datasetId   the {@link SampleDataset#feature()} id, e.g. {@code "mpg"}
 * @param sourceName  a short upstream name, e.g. {@code "ggplot2 (R)"}
 * @param sourceUrl   the canonical upstream reference, or an empty string
 * @param citation    the citation/attribution text, or an empty string
 * @param licenseId   the SPDX identifier or short licence id, e.g. {@code "MIT"}
 * @param licenseName the human-readable licence name
 * @param licenseUrl  a link to the licence text, or an empty string
 * @param origin      whether the data is upstream, derived, or synthetic
 * @param modification a note when the bundled form differs from upstream
 * @param notice      required attribution text, or an empty string
 * @param resources   the bundled classpath resources backing this dataset
 */
public record DatasetAttribution(
        String datasetId,
        String sourceName,
        String sourceUrl,
        String citation,
        String licenseId,
        String licenseName,
        String licenseUrl,
        Origin origin,
        String modification,
        String notice,
        List<String> resources) {

    /** Field separator (ASCII unit separator); never appears in the values. */
    private static final String FIELD_SEP = "\u001F";

    /** Separator inside the resource list (ASCII record separator). */
    private static final String RESOURCE_SEP = "\u001E";

    /** Field count of the encoded form. */
    private static final int FIELDS = 11;

    /** Compact constructor rejecting nulls and copying the resource list. */
    public DatasetAttribution {
        Objects.requireNonNull(datasetId, "datasetId");
        sourceName = nullToEmpty(sourceName);
        sourceUrl = nullToEmpty(sourceUrl);
        citation = nullToEmpty(citation);
        licenseId = nullToEmpty(licenseId);
        licenseName = nullToEmpty(licenseName);
        licenseUrl = nullToEmpty(licenseUrl);
        Objects.requireNonNull(origin, "origin");
        modification = nullToEmpty(modification);
        notice = nullToEmpty(notice);
        resources = List.copyOf(resources);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    /** An empty attribution used when a dataset has no registered metadata. */
    public static DatasetAttribution none() {
        return new DatasetAttribution("", "", "", "", "", "", "", Origin.SYNTHETIC,
                "", "", List.of());
    }

    /** Whether this attribution carries no dataset. */
    public boolean isEmpty() {
        return datasetId.isEmpty();
    }

    /**
     * A short, plain-text form for the sampler card and the builder's dataset
     * pane: the upstream name suffixed with the licence (and a derivation
     * marker), then the citation when present.
     *
     * @return the display lines, empty when this attribution is empty
     */
    public List<String> displayLines() {
        if (isEmpty()) {
            return List.of();
        }
        var lines = new ArrayList<String>();
        var headline = new StringBuilder(sourceName);
        if (!licenseName.isBlank()) {
            headline.append(headline.isEmpty() ? "" : " \u00B7 ").append(licenseName);
        }
        // Add the SPDX id when the human name does not already spell it out,
        // so "GPL", "CC0" and "CC BY" are searchable and visible.
        if (!licenseId.isBlank()
                && !licenseName.toLowerCase(Locale.ROOT)
                        .contains(licenseId.toLowerCase(Locale.ROOT))) {
            headline.append(" (").append(licenseId).append(')');
        }
        if (origin == Origin.DERIVED) {
            headline.append(" \u00B7 derived");
        } else if (origin == Origin.SYNTHETIC) {
            headline.append(" \u00B7 generated");
        }
        if (!headline.isEmpty()) {
            lines.add(headline.toString());
        }
        if (!citation.isBlank()) {
            lines.add(citation);
        }
        return lines;
    }

    /**
     * Serialises this attribution to the single-string wire format written into
     * {@code examples.properties}.
     *
     * @return the encoded attribution
     */
    public String encode() {
        var encoder = Base64.getEncoder();
        var fields = new String[] {
                datasetId, sourceName, sourceUrl, citation, licenseId, licenseName,
                licenseUrl, origin.name(), modification, notice,
                String.join(RESOURCE_SEP, resources)
        };
        var out = new StringBuilder();
        for (var i = 0; i < fields.length; i++) {
            if (i > 0) {
                out.append(FIELD_SEP);
            }
            out.append(encoder.encodeToString(fields[i].getBytes(StandardCharsets.UTF_8)));
        }
        return out.toString();
    }

    /**
     * Parses the encoded form produced by {@link #encode()}.
     *
     * @param encoded the encoded attribution
     * @return the parsed attribution
     * @throws IllegalArgumentException if the payload is malformed
     */
    public static DatasetAttribution decode(String encoded) {
        var fields = encoded.split(FIELD_SEP, -1);
        if (fields.length != FIELDS) {
            throw new IllegalArgumentException(
                    "malformed dataset attribution: expected " + FIELDS + " fields, got " + fields.length);
        }
        var raw = new String[FIELDS];
        for (var i = 0; i < FIELDS; i++) {
            raw[i] = new String(Base64.getDecoder().decode(fields[i]), StandardCharsets.UTF_8);
        }
        var resources = raw[10].isEmpty()
                ? List.<String>of()
                : List.of(raw[10].split(RESOURCE_SEP, -1));
        return new DatasetAttribution(raw[0], raw[1], raw[2], raw[3], raw[4], raw[5],
                raw[6], Origin.valueOf(raw[7]), raw[8], raw[9], resources);
    }
}
