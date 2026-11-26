package com.omlakhera.kronoza.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.omlakhera.kronoza.model.DeveloperInfo
import com.omlakhera.kronoza.model.KronozaAboutCatalog
import com.omlakhera.kronoza.model.TechStackItem
import com.omlakhera.kronoza.ui.components.KronozaCard
import com.omlakhera.kronoza.ui.components.KronozaIcon
import com.omlakhera.kronoza.ui.components.KronozaIcons
import com.omlakhera.kronoza.ui.components.KronozaLogoMark
import com.omlakhera.kronoza.ui.components.KronozaStaggeredReveal
import com.omlakhera.kronoza.ui.components.KronozaWordmark
import com.omlakhera.kronoza.ui.components.LogoVariation
import com.omlakhera.kronoza.ui.theme.LocalKronozaColors

private fun openExternalUrl(
    context: Context,
    url: String,
    onError: (String) -> Unit
) {
    val uri = runCatching { Uri.parse(url) }.getOrNull()
    val scheme = uri?.scheme?.lowercase()
    if (uri == null || (scheme != "https" && scheme != "http")) {
        onError("Invalid link URL")
        return
    }
    runCatching {
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }.onFailure {
        onError("No browser available to open link")
    }
}

@Composable
fun DeveloperAvatar(
    developer: DeveloperInfo,
    size: Dp = 64.dp,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val avatarShape = CircleShape
    if (developer.photoResId != null) {
        Image(
            painter = painterResource(id = developer.photoResId),
            contentDescription = "${developer.name} profile photo",
            contentScale = ContentScale.Crop,
            modifier = modifier
                .size(size)
                .clip(avatarShape)
                .border(1.5.dp, ext.cardBorder, avatarShape)
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(avatarShape)
                .background(ext.lavenderContainer)
                .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f), avatarShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = developer.initials,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun AboutCreditsHubScreen(
    onOpenAboutApp: () -> Unit,
    onOpenDeveloperProfile: () -> Unit,
    onOpenTechStack: () -> Unit,
    onOpenLicenses: () -> Unit,
    onOpenSourceContributions: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val context = LocalContext.current
    val dev = KronozaAboutCatalog.developer

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ext.canvas),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Brand & Version Card
            item {
                KronozaStaggeredReveal(index = 0) {
                    KronozaCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onOpenAboutApp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KronozaLogoMark(size = 52.dp, variation = LogoVariation.PRIMARY)
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "KRONOZA",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        letterSpacing = 2.2.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = ext.textPrimary
                                )
                                Text(
                                    text = KronozaAboutCatalog.appTagline,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ext.textSecondary
                                )
                                Spacer(Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = ext.lavenderContainer
                                ) {
                                    Text(
                                        text = "v${KronozaAboutCatalog.versionName} (${KronozaAboutCatalog.versionCode}) • Offline-First",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ext.onLavenderContainer,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            KronozaIcon(
                                imageVector = KronozaIcons.ChevronRight,
                                contentDescription = "Open About Kronoza",
                                tint = ext.textSecondary,
                                size = 18.dp
                            )
                        }
                    }
                }
            }

            // Compact Developer Attribution Banner
            item {
                KronozaStaggeredReveal(index = 1) {
                    KronozaCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onOpenDeveloperProfile
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DeveloperAvatar(developer = dev, size = 48.dp)
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Built by ${dev.name}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ext.textPrimary
                                )
                                Text(
                                    text = "${dev.role} • @${dev.githubHandle}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ext.textSecondary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = ext.mutedSurface,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .clickable {
                                        openExternalUrl(context, dev.githubUrl, onShowMessage)
                                    }
                                    .testTag("about_hub_github_chip")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    KronozaIcon(
                                        imageVector = KronozaIcons.GitHub,
                                        contentDescription = "Open GitHub",
                                        tint = ext.textPrimary,
                                        size = 14.dp
                                    )
                                    Spacer(Modifier.width(5.dp))
                                    Text(
                                        text = "GitHub",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ext.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6 Structured Destinations
            item {
                Text(
                    text = "Explore Credits & Details",
                    style = MaterialTheme.typography.titleMedium,
                    color = ext.textPrimary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            item {
                KronozaStaggeredReveal(index = 2) {
                    KronozaCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            HubDestinationRow(
                                icon = KronozaIcons.Info,
                                iconBg = ext.lavenderContainer,
                                iconTint = ext.lavenderAccent,
                                title = "About Kronoza",
                                subtitle = "Version, build info, overview & what's new",
                                onClick = onOpenAboutApp,
                                testTag = "about_hub_about_app"
                            )
                            HubDestinationRow(
                                icon = KronozaIcons.User,
                                iconBg = ext.skyBlueContainer,
                                iconTint = ext.skyBlueAccent,
                                title = "Developer Profile",
                                subtitle = "${dev.name} • ${dev.role}",
                                onClick = onOpenDeveloperProfile,
                                testTag = "about_hub_developer"
                            )
                            HubDestinationRow(
                                icon = KronozaIcons.Layers,
                                iconBg = ext.mintContainer,
                                iconTint = ext.mintAccent,
                                title = "Technology Stack",
                                subtitle = "${KronozaAboutCatalog.technologyStack.size} core libraries & Android frameworks",
                                onClick = onOpenTechStack,
                                testTag = "about_hub_tech_stack"
                            )
                            HubDestinationRow(
                                icon = KronozaIcons.Scale,
                                iconBg = ext.lavenderContainer,
                                iconTint = ext.lavenderAccent,
                                title = "Open Source Licenses",
                                subtitle = "${KronozaAboutCatalog.openSourceLicenses.size} third-party notices & attributions",
                                onClick = onOpenLicenses,
                                testTag = "about_hub_licenses"
                            )
                            HubDestinationRow(
                                icon = KronozaIcons.Code,
                                iconBg = ext.skyBlueContainer,
                                iconTint = ext.skyBlueAccent,
                                title = "Source Code & Contributions",
                                subtitle = "GitHub profile, repository & issue channels",
                                onClick = onOpenSourceContributions,
                                testTag = "about_hub_source"
                            )
                            HubDestinationRow(
                                icon = KronozaIcons.Shield,
                                iconBg = ext.mintContainer,
                                iconTint = ext.mintAccent,
                                title = "Privacy Information",
                                subtitle = "Local-first architecture & zero tracking",
                                onClick = onOpenPrivacy,
                                testTag = "about_hub_privacy"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AboutKronozaScreen(
    onOpenDeveloperProfile: () -> Unit,
    onOpenLicenses: () -> Unit,
    onOpenPrivacy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val dev = KronozaAboutCatalog.developer

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ext.canvas),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Identity Hero Card
            item {
                KronozaStaggeredReveal(index = 0) {
                    KronozaCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            KronozaWordmark(
                                variation = LogoVariation.PRIMARY,
                                iconSize = 48.dp,
                                showTagline = true
                            )
                            Spacer(Modifier.height(16.dp))
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = ext.lavenderContainer
                            ) {
                                Text(
                                    text = "Version ${KronozaAboutCatalog.versionName} • Build ${KronozaAboutCatalog.versionCode}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ext.onLavenderContainer,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(
                                text = KronozaAboutCatalog.appDescription,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ext.textSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Build & Version Details
            item {
                KronozaStaggeredReveal(index = 1) {
                    KronozaCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Build Information",
                                style = MaterialTheme.typography.titleMedium,
                                color = ext.textPrimary
                            )
                            InfoKeyValueRow("Version", KronozaAboutCatalog.versionName)
                            InfoKeyValueRow("Version Code", KronozaAboutCatalog.versionCode.toString())
                            InfoKeyValueRow("Build Type", KronozaAboutCatalog.buildType.replaceFirstChar { it.uppercase() })
                            InfoKeyValueRow("Application ID", KronozaAboutCatalog.applicationId)
                        }
                    }
                }
            }

            // What's New
            item {
                KronozaStaggeredReveal(index = 2) {
                    KronozaCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(ext.mintContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    KronozaIcon(
                                        imageVector = KronozaIcons.Bolt,
                                        contentDescription = null,
                                        tint = ext.mintAccent,
                                        size = 16.dp
                                    )
                                }
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "What's New in v${KronozaAboutCatalog.versionName}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ext.textPrimary
                                )
                            }
                            KronozaAboutCatalog.whatsNewItems.forEach { (headline, detail) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 6.dp)
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = headline,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = ext.textPrimary
                                        )
                                        Text(
                                            text = detail,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ext.textSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Related Links (Privacy, Licenses, Developer)
            item {
                KronozaStaggeredReveal(index = 3) {
                    KronozaCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            HubDestinationRow(
                                icon = KronozaIcons.User,
                                iconBg = ext.skyBlueContainer,
                                iconTint = ext.skyBlueAccent,
                                title = "Developer Information",
                                subtitle = "${dev.name} • ${dev.role}",
                                onClick = onOpenDeveloperProfile,
                                testTag = "about_app_developer_row"
                            )
                            HubDestinationRow(
                                icon = KronozaIcons.Shield,
                                iconBg = ext.mintContainer,
                                iconTint = ext.mintAccent,
                                title = "Privacy Information",
                                subtitle = "100% on-device persistence & local backups",
                                onClick = onOpenPrivacy,
                                testTag = "about_app_privacy_row"
                            )
                            HubDestinationRow(
                                icon = KronozaIcons.Scale,
                                iconBg = ext.lavenderContainer,
                                iconTint = ext.lavenderAccent,
                                title = "Open Source Licenses",
                                subtitle = "Third-party libraries & license notices",
                                onClick = onOpenLicenses,
                                testTag = "about_app_licenses_row"
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DeveloperProfileScreen(
    onOpenTechStack: () -> Unit,
    onOpenLicenses: () -> Unit,
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val context = LocalContext.current
    val dev = KronozaAboutCatalog.developer
    val links = KronozaAboutCatalog.projectLinks

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ext.canvas),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Developer Identity Card
            item {
                KronozaStaggeredReveal(index = 0) {
                    KronozaCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                DeveloperAvatar(developer = dev, size = 72.dp)
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = dev.name,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = ext.textPrimary
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = dev.role,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = ext.mutedSurface
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            KronozaIcon(
                                                imageVector = KronozaIcons.GitHub,
                                                contentDescription = null,
                                                tint = ext.textSecondary,
                                                size = 13.dp
                                            )
                                            Spacer(Modifier.width(5.dp))
                                            Text(
                                                text = "@${dev.githubHandle}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = ext.textSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = dev.bio,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ext.textSecondary
                            )

                            Button(
                                onClick = { openExternalUrl(context, dev.githubUrl, onShowMessage) },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                contentPadding = PaddingValues(vertical = 13.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("developer_github_button")
                            ) {
                                KronozaIcon(
                                    imageVector = KronozaIcons.GitHub,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    size = 18.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "View GitHub Profile",
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Spacer(Modifier.width(6.dp))
                                KronozaIcon(
                                    imageVector = KronozaIcons.ExternalLink,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                    size = 14.dp
                                )
                            }

                            val repoUrl = links.sourceRepositoryUrl
                            if (!repoUrl.isNullOrBlank()) {
                                OutlinedButton(
                                    onClick = { openExternalUrl(context, repoUrl, onShowMessage) },
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(vertical = 12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("developer_source_repo_button")
                                ) {
                                    KronozaIcon(
                                        imageVector = KronozaIcons.Code,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        size = 18.dp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Kronoza Source Code")
                                }
                            }
                        }
                    }
                }
            }

            // Stack Preview Card
            item {
                KronozaStaggeredReveal(index = 1) {
                    KronozaCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Built With",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = ext.textPrimary
                                )
                                Text(
                                    text = "View all",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable(onClick = onOpenTechStack)
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                KronozaAboutCatalog.technologyStack.forEach { tech ->
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = ext.mutedSurface
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            KronozaIcon(
                                                imageVector = techIconFor(tech.id),
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                size = 13.dp
                                            )
                                            Spacer(Modifier.width(6.dp))
                                            Text(
                                                text = tech.name,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = ext.textPrimary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                KronozaStaggeredReveal(index = 2) {
                    KronozaCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            HubDestinationRow(
                                icon = KronozaIcons.Scale,
                                iconBg = ext.lavenderContainer,
                                iconTint = ext.lavenderAccent,
                                title = "Open Source Credits",
                                subtitle = "View library versions & Apache 2.0 attributions",
                                onClick = onOpenLicenses,
                                testTag = "developer_open_licenses"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TechnologyStackScreen(
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val items = KronozaAboutCatalog.technologyStack

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ext.canvas),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ext.lavenderContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            KronozaIcon(
                                imageVector = KronozaIcons.Layers,
                                contentDescription = null,
                                tint = ext.lavenderAccent,
                                size = 20.dp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Modern Native Android Stack",
                                style = MaterialTheme.typography.titleMedium,
                                color = ext.textPrimary
                            )
                            Text(
                                text = "Technologies actively used to power Kronoza's UI, database, background scheduling, and reactivity.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ext.textSecondary
                            )
                        }
                    }
                }
            }

            itemsIndexed(items, key = { _, item -> item.id }) { index, tech ->
                KronozaStaggeredReveal(index = index.coerceAtMost(6)) {
                    TechStackCard(tech = tech)
                }
            }
        }
    }
}

@Composable
private fun TechStackCard(tech: TechStackItem) {
    val ext = LocalKronozaColors.current
    val (bg, tint) = when (tech.id) {
        "kotlin", "coroutines", "flow" -> ext.lavenderContainer to ext.lavenderAccent
        "compose", "material3", "notification_manager" -> ext.skyBlueContainer to ext.skyBlueAccent
        else -> ext.mintContainer to ext.mintAccent
    }

    KronozaCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                KronozaIcon(
                    imageVector = techIconFor(tech.id),
                    contentDescription = tech.name,
                    tint = tint,
                    size = 20.dp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tech.name,
                    style = MaterialTheme.typography.titleSmall,
                    color = ext.textPrimary
                )
                Text(
                    text = tech.role,
                    style = MaterialTheme.typography.bodySmall,
                    color = ext.textSecondary
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = ext.mutedSurface
                ) {
                    Text(
                        text = tech.version,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = ext.textPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = tech.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = ext.textSecondary
                )
            }
        }
    }
}

@Composable
fun SourceContributionsScreen(
    onShowMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val context = LocalContext.current
    val dev = KronozaAboutCatalog.developer
    val links = KronozaAboutCatalog.projectLinks

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ext.canvas),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Developer GitHub",
                            style = MaterialTheme.typography.titleMedium,
                            color = ext.textPrimary
                        )
                        Text(
                            text = "Follow ${dev.name} (@${dev.githubHandle}) on GitHub for updates, software experiments, and releases.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ext.textSecondary
                        )
                        Button(
                            onClick = { openExternalUrl(context, links.developerGithubUrl, onShowMessage) },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("source_dev_github_button")
                        ) {
                            KronozaIcon(
                                imageVector = KronozaIcons.GitHub,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                size = 18.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("github.com/${dev.githubHandle}")
                            Spacer(Modifier.width(6.dp))
                            KronozaIcon(
                                imageVector = KronozaIcons.ExternalLink,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                size = 14.dp
                            )
                        }
                    }
                }
            }

            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        ConfigurableLinkRow(
                            icon = KronozaIcons.Code,
                            iconBg = ext.lavenderContainer,
                            iconTint = ext.lavenderAccent,
                            title = "Kronoza Source Repository",
                            configuredSubtitle = "Inspect project code and release tags",
                            unconfiguredSubtitle = "Repository URL not configured yet",
                            url = links.sourceRepositoryUrl,
                            onOpenUrl = { url -> openExternalUrl(context, url, onShowMessage) },
                            testTag = "source_repo_row"
                        )
                        ConfigurableLinkRow(
                            icon = KronozaIcons.AlertTriangle,
                            iconBg = ext.coralContainer,
                            iconTint = ext.coralAccent,
                            title = "Report an Issue",
                            configuredSubtitle = "Submit bug reports with device & version info",
                            unconfiguredSubtitle = "Issue tracker link not configured yet",
                            url = links.issueTrackerUrl,
                            onOpenUrl = { url -> openExternalUrl(context, url, onShowMessage) },
                            testTag = "source_issues_row"
                        )
                        ConfigurableLinkRow(
                            icon = KronozaIcons.Bolt,
                            iconBg = ext.skyBlueContainer,
                            iconTint = ext.skyBlueAccent,
                            title = "Feature Requests",
                            configuredSubtitle = "Suggest calendar, task, or workflow improvements",
                            unconfiguredSubtitle = "Feature request board not configured yet",
                            url = links.featureRequestsUrl,
                            onOpenUrl = { url -> openExternalUrl(context, url, onShowMessage) },
                            testTag = "source_features_row"
                        )
                        ConfigurableLinkRow(
                            icon = KronozaIcons.Scale,
                            iconBg = ext.mintContainer,
                            iconTint = ext.mintAccent,
                            title = "Contribution Guidelines",
                            configuredSubtitle = "Coding style, PR checklist & setup guide",
                            unconfiguredSubtitle = "Contribution guide link not configured yet",
                            url = links.contributingGuideUrl,
                            onOpenUrl = { url -> openExternalUrl(context, url, onShowMessage) },
                            testTag = "source_contributing_row"
                        )
                    }
                }
            }

            item {
                KronozaCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "How Contributions Work",
                            style = MaterialTheme.typography.titleSmall,
                            color = ext.textPrimary
                        )
                        Text(
                            text = "Kronoza is crafted with Kotlin, Jetpack Compose, Material 3, and Room. Once a public repository URL is configured in KronozaAboutCatalog.projectLinks, repository, issue tracker, and contribution links become directly clickable here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = ext.textSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyInformationScreen(
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val privacyPillars = listOf(
        Triple(
            KronozaIcons.Lock,
            "100% On-Device Storage",
            "All calendar events, tasks, subtasks, notifications, and reminder schedules are stored locally in an encrypted-partition Room SQLite database and DataStore Preferences on your device."
        ),
        Triple(
            KronozaIcons.Shield,
            "Zero Cloud Accounts or Tracking",
            "Kronoza requires no account sign-in, contains no third-party analytics SDKs, no crash telemetry trackers, and never transmits your schedule to external servers."
        ),
        Triple(
            KronozaIcons.BackupExport,
            "User-Controlled Backups",
            "JSON backups are written directly to a location you pick via Android's Storage Access Framework and protected with a SHA-256 integrity checksum verified before restore."
        ),
        Triple(
            KronozaIcons.Camera,
            "Least-Privilege Permissions",
            "Profile photos use Android's zero-permission Photo Picker. Notifications and exact alarms are strictly optional and used solely to deliver reminders you create."
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ext.canvas),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 720.dp),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                KronozaCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = ext.mintContainer.copy(alpha = 0.45f),
                    borderColor = ext.mintAccent.copy(alpha = 0.25f)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ext.mintAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            KronozaIcon(
                                imageVector = KronozaIcons.Shield,
                                contentDescription = null,
                                tint = Color.White,
                                size = 22.dp
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Private by Architecture",
                                style = MaterialTheme.typography.titleMedium,
                                color = ext.textPrimary
                            )
                            Text(
                                text = "Your time belongs exclusively to you.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ext.textSecondary
                            )
                        }
                    }
                }
            }

            itemsIndexed(privacyPillars) { index, (icon, title, body) ->
                KronozaStaggeredReveal(index = index) {
                    KronozaCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ext.lavenderContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                KronozaIcon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = ext.lavenderAccent,
                                    size = 18.dp
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = ext.textPrimary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = body,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = ext.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HubDestinationRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    val ext = LocalKronozaColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            KronozaIcon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                size = 20.dp
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = ext.textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = ext.textSecondary
            )
        }
        KronozaIcon(
            imageVector = KronozaIcons.ChevronRight,
            contentDescription = null,
            tint = ext.textSecondary,
            size = 16.dp
        )
    }
}

@Composable
private fun ConfigurableLinkRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    configuredSubtitle: String,
    unconfiguredSubtitle: String,
    url: String?,
    onOpenUrl: (String) -> Unit,
    testTag: String
) {
    val ext = LocalKronozaColors.current
    val enabled = !url.isNullOrBlank()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (enabled) Modifier.clickable { onOpenUrl(url!!) }
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (enabled) iconBg else ext.mutedSurface),
            contentAlignment = Alignment.Center
        ) {
            KronozaIcon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) iconTint else ext.textSecondary,
                size = 20.dp
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = if (enabled) ext.textPrimary else ext.textSecondary
            )
            Text(
                text = if (enabled) configuredSubtitle else unconfiguredSubtitle,
                style = MaterialTheme.typography.bodySmall,
                color = ext.textSecondary
            )
        }
        if (enabled) {
            KronozaIcon(
                imageVector = KronozaIcons.ExternalLink,
                contentDescription = "Open link",
                tint = MaterialTheme.colorScheme.primary,
                size = 16.dp
            )
        } else {
            Surface(
                shape = RoundedCornerShape(999.dp),
                color = ext.mutedSurface
            ) {
                Text(
                    text = "Unconfigured",
                    style = MaterialTheme.typography.labelSmall,
                    color = ext.textSecondary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun InfoKeyValueRow(label: String, value: String) {
    val ext = LocalKronozaColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = ext.textSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = ext.textPrimary
        )
    }
}

private fun techIconFor(id: String): ImageVector = when (id) {
    "kotlin" -> KronozaIcons.Code
    "compose" -> KronozaIcons.Layers
    "material3" -> KronozaIcons.Palette
    "room" -> KronozaIcons.Database
    "coroutines" -> KronozaIcons.Bolt
    "flow" -> KronozaIcons.Repeat
    "datastore" -> KronozaIcons.Sliders
    "workmanager" -> KronozaIcons.Clock
    "notification_manager" -> KronozaIcons.Bell
    "gradle" -> KronozaIcons.Cpu
    else -> KronozaIcons.Code
}
