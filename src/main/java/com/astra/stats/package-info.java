/**
 * Stats module — read-only aggregations over Session.
 *
 * Ranking, heatmap, streak and dashboard numbers. Has NO entity of its
 * own: it only reads through the public service that tracking exposes.
 * "Today" is computed in the fixed America/Sao_Paulo zone.
 */
package com.astra.stats;
