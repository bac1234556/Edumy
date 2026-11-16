/**
 * 02 Catalog module: Course browsing, public listings, search, categories.
 */
@org.springframework.modulith.ApplicationModule(
        displayName = "02 Catalog",
        allowedDependencies = {"shared", "authoring :: api", "learning :: api"})
package com.edumy.catalog;
