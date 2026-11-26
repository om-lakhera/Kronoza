package com.omlakhera.kronoza.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.omlakhera.kronoza.model.KronozaAboutCatalog
import com.omlakhera.kronoza.model.LicenseCategory
import com.omlakhera.kronoza.model.OpenSourceLicenseItem
import com.omlakhera.kronoza.ui.components.KronozaCard
import com.omlakhera.kronoza.ui.components.KronozaEmptyStateCard
import com.omlakhera.kronoza.ui.components.KronozaIcon
import com.omlakhera.kronoza.ui.components.KronozaIcons
import com.omlakhera.kronoza.ui.components.KronozaStaggeredReveal
import com.omlakhera.kronoza.ui.theme.LocalKronozaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceLicensesScreen(
    modifier: Modifier = Modifier
) {
    val ext = LocalKronozaColors.current
    val allLicenses = remember { KronozaAboutCatalog.openSourceLicenses }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(LicenseCategory.ALL) }
    var activeLicense by remember { mutableStateOf<OpenSourceLicenseItem?>(null) }

    val filteredLicenses = remember(allLicenses, searchQuery, selectedCategory) {
        val q = searchQuery.trim().lowercase()
        allLicenses.filter { item ->
            val matchesCategory = selectedCategory == LicenseCategory.ALL || item.category == selectedCategory
            val matchesQuery = q.isEmpty() ||
                item.libraryName.lowercase().contains(q) ||
                item.artifact.lowercase().contains(q) ||
                item.licenseName.lowercase().contains(q) ||
                item.copyrightNotice.lowercase().contains(q)
            matchesCategory && matchesQuery
        }
    }

    if (activeLicense != null) {
        val item = activeLicense!!
        ModalBottomSheet(
            onDismissRequest = { activeLicense = null },
            containerColor = ext.card,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.libraryName,
                        style = MaterialTheme.typography.titleLarge,
                        color = ext.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = ext.lavenderContainer
                    ) {
                        Text(
                            text = "v${item.version}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = ext.onLavenderContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
                Text(
                    text = item.artifact,
                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = FontFamily.Monospace),
                    color = ext.textSecondary
                )
                Text(
                    text = "${item.licenseName} • ${item.copyrightNotice}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ext.mutedSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(14.dp)
                    ) {
                        Text(
                            text = item.licenseText,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                            color = ext.textPrimary
                        )
                    }
                }
            }
        }
    }

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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KronozaIcon(
                            imageVector = KronozaIcons.Search,
                            contentDescription = "Search licenses",
                            tint = ext.textSecondary,
                            size = 18.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search libraries, artifacts, or licenses...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ext.textSecondary
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = ext.textPrimary),
                                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("licenses_search_input")
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(32.dp)
                            ) {
                                KronozaIcon(
                                    imageVector = KronozaIcons.Close,
                                    contentDescription = "Clear search",
                                    tint = ext.textSecondary,
                                    size = 16.dp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LicenseCategory.entries.forEach { category ->
                        val selected = category == selectedCategory
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (selected) MaterialTheme.colorScheme.primary else ext.card,
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable { selectedCategory = category }
                                .testTag("licenses_filter_${category.name.lowercase()}")
                        ) {
                            Text(
                                text = category.label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (selected) MaterialTheme.colorScheme.onPrimary else ext.textSecondary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "${filteredLicenses.size} dependencies • Tap a library to read full license text",
                    style = MaterialTheme.typography.labelMedium,
                    color = ext.textSecondary
                )
            }

            if (filteredLicenses.isEmpty()) {
                item {
                    KronozaEmptyStateCard(
                        icon = KronozaIcons.Scale,
                        title = "No matching dependencies",
                        subtitle = "Try another library name or switch the category filter.",
                        actionLabel = "Reset Filter",
                        onAction = {
                            searchQuery = ""
                            selectedCategory = LicenseCategory.ALL
                        }
                    )
                }
            } else {
                itemsIndexed(filteredLicenses, key = { _, item -> item.id }) { index, licenseItem ->
                    KronozaStaggeredReveal(index = index.coerceAtMost(6)) {
                        KronozaCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("license_card_${licenseItem.id}"),
                            onClick = { activeLicense = licenseItem }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = licenseItem.libraryName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = ext.textPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = ext.lavenderContainer
                                    ) {
                                        Text(
                                            text = licenseItem.version,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = ext.onLavenderContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = licenseItem.artifact,
                                    style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Monospace),
                                    color = ext.textSecondary
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = licenseItem.copyrightNotice,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = ext.textSecondary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = ext.mintContainer
                                    ) {
                                        Text(
                                            text = licenseItem.licenseName,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium,
                                            color = ext.onMintContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
