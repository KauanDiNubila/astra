/**
 * User module — identity and profile.
 *
 * Owns the User entity, authentication (JWT, added in slice 2) and the
 * public profile. Other modules reference a user only by id; they never
 * reach into this module's repository or entity directly.
 */
package com.astra.user;
