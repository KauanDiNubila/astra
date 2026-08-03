/**
 * Tracking module — the core of Astra.
 *
 * Owns Session (focused time) and Category. Every derived metric
 * (ranking, heatmap, streak, stats) is an aggregation computed over
 * Session and is never stored as its own table. Neighbouring modules
 * read tracking data through public services only, never by injecting
 * SessionRepository or the entities across the boundary.
 */
package com.astra.tracking;
