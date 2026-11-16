/**
 * 06 Payout module: Earning entries, revenue sharing, payout accounts, and instructor withdrawals.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "06 Payout",
        allowedDependencies = {"shared", "identity :: api", "authoring :: api"})
package com.edumy.payout;
