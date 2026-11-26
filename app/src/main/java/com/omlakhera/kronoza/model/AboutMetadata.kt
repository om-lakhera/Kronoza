package com.omlakhera.kronoza.model

import androidx.annotation.DrawableRes
import com.omlakhera.kronoza.BuildConfig
import com.omlakhera.kronoza.R

/**
 * Centralized metadata for Kronoza's developer attribution, configurable repository links,
 * verified technology stack, and real third-party open-source dependencies.
 */
data class DeveloperInfo(
    val name: String,
    val initials: String,
    val role: String,
    val githubHandle: String,
    val githubUrl: String,
    val bio: String,
    @DrawableRes val photoResId: Int? = null
)

data class ProjectLinksConfig(
    val developerGithubUrl: String,
    val sourceRepositoryUrl: String? = null,
    val issueTrackerUrl: String? = null,
    val featureRequestsUrl: String? = null,
    val contributingGuideUrl: String? = null
) {
    val hasSourceRepository: Boolean get() = !sourceRepositoryUrl.isNullOrBlank()
    val hasIssueTracker: Boolean get() = !issueTrackerUrl.isNullOrBlank()
    val hasFeatureRequests: Boolean get() = !featureRequestsUrl.isNullOrBlank()
    val hasContributingGuide: Boolean get() = !contributingGuideUrl.isNullOrBlank()
}

data class TechStackItem(
    val id: String,
    val name: String,
    val role: String,
    val version: String,
    val category: String
)

enum class LicenseCategory(val label: String) {
    ALL("All"),
    COMPOSE_UI("Compose & UI"),
    STORAGE_ASYNC("Data & Async"),
    CORE_KOTLIN("Kotlin & Core")
}

data class OpenSourceLicenseItem(
    val id: String,
    val libraryName: String,
    val artifact: String,
    val version: String,
    val licenseName: String,
    val copyrightNotice: String,
    val category: LicenseCategory,
    val licenseText: String
)

object KronozaAboutCatalog {

    val developer = DeveloperInfo(
        name = "Om Lakhera",
        initials = "OL",
        role = "Independent Developer",
        githubHandle = "om-lakhera",
        githubUrl = "https://github.com/om-lakhera",
        bio = "Hi, I'm Om Lakhera, an independent developer interested in building useful software, exploring technology and creating thoughtful digital experiences. Kronoza is one of my projects, built with a focus on simplicity, functionality and privacy.",
        photoResId = R.drawable.img_developer_om
    )

    /**
     * Repository and issue tracker URLs are intentionally null until an actual public URL
     * is configured. UI components check these flags before enabling external links.
     */
    val projectLinks = ProjectLinksConfig(
        developerGithubUrl = developer.githubUrl,
        sourceRepositoryUrl = null,
        issueTrackerUrl = null,
        featureRequestsUrl = null,
        contributingGuideUrl = null
    )

    val appTagline: String = "Make time work for you."

    val appDescription: String =
        "Kronoza is a thoughtfully designed calendar and productivity application built to help people organize their time, manage tasks and stay focused. Designed with privacy and offline-first functionality at its core."

    val versionName: String get() = BuildConfig.VERSION_NAME
    val versionCode: Int get() = BuildConfig.VERSION_CODE
    val applicationId: String get() = BuildConfig.APPLICATION_ID
    val buildType: String get() = BuildConfig.BUILD_TYPE

    val whatsNewItems: List<Pair<String, String>> = listOf(
        "Full Calendar Engine" to "Month, Week, Day, and Agenda views with multi-day spans, recurring occurrence editing, and conflict detection.",
        "Task & Subtask Flow" to "Priority filters, overdue detection, interactive checklists, and live completion metrics.",
        "Reliable Local Reminders" to "AlarmManager exact scheduling paired with WorkManager fallback, reboot recovery, and timezone resilience.",
        "Genuine Local Insights" to "Real-time focus hours, meeting balance, streaks, and 4-week trends derived solely from on-device records.",
        "SHA-256 Verified Backups" to "Portable JSON export and merge-or-replace restore without any cloud accounts."
    )

    /**
     * Audited strictly from active dependencies and architecture in Kronoza.
     */
    val technologyStack: List<TechStackItem> = listOf(
        TechStackItem(
            id = "kotlin",
            name = "Kotlin",
            role = "Primary programming language",
            version = "2.4.21",
            category = "Language"
        ),
        TechStackItem(
            id = "compose",
            name = "Jetpack Compose",
            role = "Native declarative Android UI",
            version = "BOM 2024.09.00",
            category = "UI Framework"
        ),
        TechStackItem(
            id = "material3",
            name = "Material 3",
            role = "Adaptive design system & theming",
            version = "Compose M3",
            category = "Design System"
        ),
        TechStackItem(
            id = "room",
            name = "Room",
            role = "Local SQLite database persistence",
            version = "2.7.0",
            category = "Persistence"
        ),
        TechStackItem(
            id = "coroutines",
            name = "Kotlin Coroutines",
            role = "Structured asynchronous operations",
            version = "1.10.2",
            category = "Concurrency"
        ),
        TechStackItem(
            id = "flow",
            name = "Kotlin Flow",
            role = "Reactive state & Room data streams",
            version = "1.10.2",
            category = "Reactive Streams"
        ),
        TechStackItem(
            id = "datastore",
            name = "DataStore",
            role = "Typed local user preferences storage",
            version = "1.1.7",
            category = "Preferences"
        ),
        TechStackItem(
            id = "workmanager",
            name = "WorkManager",
            role = "Deferrable background reminder scheduling",
            version = "2.10.0",
            category = "Background Work"
        ),
        TechStackItem(
            id = "notification_manager",
            name = "Android NotificationManager",
            role = "Channels, alerts & AlarmManager integration",
            version = "Android SDK 36",
            category = "System APIs"
        ),
        TechStackItem(
            id = "gradle",
            name = "Gradle",
            role = "Kotlin DSL build system & version catalog",
            version = "9.3.1 (AGP 9.1.1)",
            category = "Build System"
        )
    )

    private const val APACHE_2_LICENSE_TEXT = """Apache License
Version 2.0, January 2004
http://www.apache.org/licenses/

TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION

1. Definitions.
"License" shall mean the terms and conditions for use, reproduction, and distribution as defined by Sections 1 through 9 of this document.
"Licensor" shall mean the copyright owner or entity authorized by the copyright owner that is granting the License.
"Legal Entity" shall mean the union of the acting entity and all other entities that control, are controlled by, or are under common control with that entity.
"You" (or "Your") shall mean an individual or Legal Entity exercising permissions granted by this License.
"Source" form shall mean the preferred form for making modifications, including but not limited to software source code, documentation source, and configuration files.
"Object" form shall mean any form resulting from mechanical transformation or translation of a Source form, including but not limited to compiled object code, generated documentation, and conversions to other media types.
"Work" shall mean the work of authorship, whether in Source or Object form, made available under the License, as indicated by a copyright notice that is included in or attached to the work.

2. Grant of Copyright License.
Subject to the terms and conditions of this License, each Contributor hereby grants to You a perpetual, worldwide, non-exclusive, no-charge, royalty-free, irrevocable copyright license to reproduce, prepare Derivative Works of, publicly display, publicly perform, sublicense, and distribute the Work and such Derivative Works in Source or Object form.

3. Grant of Patent License.
Subject to the terms and conditions of this License, each Contributor hereby grants to You a perpetual, worldwide, non-exclusive, no-charge, royalty-free, irrevocable (except as stated in this section) patent license to make, have made, use, offer to sell, sell, import, and otherwise transfer the Work.

4. Redistribution.
You may reproduce and distribute copies of the Work or Derivative Works thereof in any medium, with or without modifications, and in Source or Object form, provided that You meet the following conditions:
(a) You must give any other recipients of the Work a copy of this License; and
(b) You must cause any modified files to carry prominent notices stating that You changed the files; and
(c) You must retain, in the Source form of any Derivative Works that You distribute, all copyright, patent, trademark, and attribution notices from the Source form of the Work; and
(d) If the Work includes a "NOTICE" text file as part of its distribution, then any Derivative Works that You distribute must include a readable copy of the attribution notices contained within such NOTICE file.

5. Submission of Contributions.
Unless You explicitly state otherwise, any Contribution intentionally submitted for inclusion in the Work by You to the Licensor shall be under the terms and conditions of this License, without any additional terms or conditions.

6. Trademarks.
This License does not grant permission to use the trade names, trademarks, service marks, or product names of the Licensor, except as required for reasonable and customary use in describing the origin of the Work.

7. Disclaimer of Warranty.
Unless required by applicable law or agreed to in writing, Licensor provides the Work on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.

8. Limitation of Liability.
In no event and under no legal theory, whether in tort (including negligence), contract, or otherwise, unless required by applicable law or agreed to in writing, shall any Contributor be liable to You for damages arising as a result of this License or out of the use or inability to use the Work.

9. Accepting Warranty or Additional Liability.
While redistributing the Work or Derivative Works thereof, You may choose to offer, and charge a fee for, acceptance of support, warranty, indemnity, or other liability obligations consistent with this License."""

    /**
     * Real open-source dependencies declared in app/build.gradle.kts and gradle/libs.versions.toml.
     */
    val openSourceLicenses: List<OpenSourceLicenseItem> = listOf(
        OpenSourceLicenseItem(
            id = "compose-ui",
            libraryName = "Jetpack Compose UI & Graphics",
            artifact = "androidx.compose.ui:ui / ui-graphics (compose-bom)",
            version = "2024.09.00",
            licenseName = "Apache License 2.0",
            copyrightNotice = "Copyright (C) The Android Open Source Project",
            category = LicenseCategory.COMPOSE_UI,
            licenseText = APACHE_2_LICENSE_TEXT
        ),
        OpenSourceLicenseItem(
            id = "compose-material3",
            libraryName = "Jetpack Compose Material 3",
            artifact = "androidx.compose.material3:material3",
            version = "2024.09.00 (BOM)",
            licenseName = "Apache License 2.0",
            copyrightNotice = "Copyright (C) The Android Open Source Project",
            category = LicenseCategory.COMPOSE_UI,
            licenseText = APACHE_2_LICENSE_TEXT
        ),
        OpenSourceLicenseItem(
            id = "activity-compose",
            libraryName = "AndroidX Activity Compose",
            artifact = "androidx.activity:activity-compose",
            version = "1.10.1",
            licenseName = "Apache License 2.0",
            copyrightNotice = "Copyright (C) The Android Open Source Project",
            category = LicenseCategory.COMPOSE_UI,
            licenseText = APACHE_2_LICENSE_TEXT
        ),
        OpenSourceLicenseItem(
            id = "navigation-compose",
            libraryName = "AndroidX Navigation Compose",
            artifact = "androidx.navigation:navigation-compose",
            version = "2.8.9",
            licenseName = "Apache License 2.0",
            copyrightNotice = "Copyright (C) The Android Open Source Project",
            category = LicenseCategory.COMPOSE_UI,
            licenseText = APACHE_2_LICENSE_TEXT
        ),
        OpenSourceLicenseItem(
            id = "lifecycle-compose",
            libraryName = "AndroidX Lifecycle (Runtime & ViewModel Compose)",
            artifact = "androidx.lifecycle:lifecycle-runtime-ktx / lifecycle-runtime-compose / lifecycle-viewmodel-compose",
            version = "2.8.7",
            licenseName = "Apache License 2.0",
            copyrightNotice = "Copyright (C) The Android Open Source Project",
            category = LicenseCategory.COMPOSE_UI,
            licenseText = APACHE_2_LICENSE_TEXT
        ),
        OpenSourceLicenseItem(
            id = "room",
            libraryName = "AndroidX Room (Runtime, KTX & KSP Compiler)",
            artifact = "androidx.room:room-runtime / room-ktx / room-compiler",
            version = "2.7.0",
            licenseName = "Apache License 2.0",
            copyrightNotice = "Copyright (C) The Android Open Source Project",
            category = LicenseCategory.STORAGE_ASYNC,
            licenseText = APACHE_2_LICENSE_TEXT
        ),
        OpenSourceLicenseItem(
            id = "datastore",
            libraryName = "AndroidX DataStore Preferences",
            artifact = "androidx.datastore:datastore-preferences",
            version = "1.1.7",
            licenseName = "Apache License 2.0",
            copyrightNotice = "Copyright (C) The Android Open Source Project",
            category = LicenseCategory.STORAGE_ASYNC,
            licenseText = APACHE_2_LICENSE_TEXT
        ),
        OpenSourceLicenseItem(
            id = "workmanager",
            libraryName = "AndroidX WorkManager KTX",
            artifact = "androidx.work:work-runtime-ktx",
            version = "2.10.0",
            licenseName = "Apache License 2.0",
            copyrightNotice = "Copyright (C) The Android Open Source Project",
            category = LicenseCategory.STORAGE_ASYNC,
            licenseText = APACHE_2_LICENSE_TEXT
        ),
        OpenSourceLicenseItem(
            id = "kotlinx-coroutines",
            libraryName = "KotlinX Coroutines (Core & Android)",
            artifact = "org.jetbrains.kotlinx:kotlinx-coroutines-core / kotlinx-coroutines-android",
            version = "1.10.2",
            licenseName = "Apache License 2.0",
            copyrightNotice = "Copyright (C) JetBrains s.r.o. and Kotlin Contributors",
            category = LicenseCategory.STORAGE_ASYNC,
            licenseText = APACHE_2_LICENSE_TEXT
        ),
        OpenSourceLicenseItem(
            id = "core-ktx",
            libraryName = "AndroidX Core KTX",
            artifact = "androidx.core:core-ktx",
            version = "1.18.0",
            licenseName = "Apache License 2.0",
            copyrightNotice = "Copyright (C) The Android Open Source Project",
            category = LicenseCategory.CORE_KOTLIN,
            licenseText = APACHE_2_LICENSE_TEXT
        ),
        OpenSourceLicenseItem(
            id = "kotlin-stdlib",
            libraryName = "Kotlin & Compose Compiler Plugin",
            artifact = "org.jetbrains.kotlin.plugin.compose",
            version = "2.4.21",
            licenseName = "Apache License 2.0",
            copyrightNotice = "Copyright (C) JetBrains s.r.o. and Kotlin Contributors",
            category = LicenseCategory.CORE_KOTLIN,
            licenseText = APACHE_2_LICENSE_TEXT
        )
    )
}
