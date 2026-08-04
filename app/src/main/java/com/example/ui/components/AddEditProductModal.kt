package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.CategoryEntity
import com.example.data.local.entity.ProductEntity
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProductModal(
    productToEdit: ProductEntity?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (ProductEntity) -> Unit
) {
    var title by remember { mutableStateOf(productToEdit?.title ?: "") }
    var description by remember { mutableStateOf(productToEdit?.description ?: "") }
    var categoryId by remember { mutableStateOf(productToEdit?.categoryId ?: categories.firstOrNull()?.id ?: "cat_ui") }
    var priceStr by remember { mutableStateOf(productToEdit?.price?.toString() ?: "29.00") }
    var discountPriceStr by remember { mutableStateOf(productToEdit?.discountPrice?.toString() ?: "") }
    var version by remember { mutableStateOf(productToEdit?.version ?: "v1.0") }
    var fileSize by remember { mutableStateOf(productToEdit?.fileSize ?: "45 MB") }
    var fileType by remember { mutableStateOf(productToEdit?.fileType ?: "ZIP") }
    var downloadUrl by remember { mutableStateOf(productToEdit?.fileDownloadUrl ?: "https://assetmarket.com/files/download.zip") }
    var tags by remember { mutableStateOf(productToEdit?.tags ?: "Digital, Asset, Premium") }
    var imageResName by remember { mutableStateOf(productToEdit?.imageResName ?: "img_product_ui_kit") }
    var isEnabled by remember { mutableStateOf(productToEdit?.isEnabled ?: true) }

    var categoryExpanded by remember { mutableStateOf(false) }

    val imageOptions = listOf(
        "img_product_ui_kit" to "UI Kit Template",
        "img_product_3d_pack" to "3D Glossy Icon Pack",
        "img_product_audio_tracks" to "Synthwave Audio Track",
        "img_banner_hero" to "Kotlin Source Code Banner"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("add_edit_product_dialog"),
        confirmButton = {
            Button(
                onClick = {
                    val price = priceStr.toDoubleOrNull() ?: 19.99
                    val discountPrice = discountPriceStr.toDoubleOrNull()
                    val newProduct = ProductEntity(
                        id = productToEdit?.id ?: ("prod_" + UUID.randomUUID().toString().take(8)),
                        title = title.ifBlank { "Untitled Product" },
                        description = description.ifBlank { "No description provided." },
                        categoryId = categoryId,
                        price = price,
                        discountPrice = discountPrice,
                        version = version.ifBlank { "v1.0" },
                        fileSize = fileSize.ifBlank { "10 MB" },
                        fileType = fileType.ifBlank { "ZIP" },
                        fileDownloadUrl = downloadUrl.ifBlank { "https://assetmarket.com/files/download.zip" },
                        imageResName = imageResName,
                        tags = tags,
                        rating = productToEdit?.rating ?: 5.0f,
                        reviewCount = productToEdit?.reviewCount ?: 0,
                        salesCount = productToEdit?.salesCount ?: 0,
                        isEnabled = isEnabled,
                        createdAt = productToEdit?.createdAt ?: System.currentTimeMillis()
                    )
                    onSave(newProduct)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (productToEdit == null) "Create Product" else "Update Product", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) {
                Text("Cancel")
            }
        },
        title = {
            Text(
                text = if (productToEdit == null) "Add Digital Product" else "Edit Digital Product",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Product Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_product_title")
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Selector
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = !categoryExpanded }
                ) {
                    val selectedCategoryName = categories.find { it.id == categoryId }?.name ?: "Select Category"
                    OutlinedTextField(
                        value = selectedCategoryName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    categoryId = cat.id
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("Regular Price ($)") },
                        modifier = Modifier.weight(1f).testTag("input_product_price")
                    )
                    OutlinedTextField(
                        value = discountPriceStr,
                        onValueChange = { discountPriceStr = it },
                        label = { Text("Discount Price ($)") },
                        placeholder = { Text("Optional") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = version,
                        onValueChange = { version = it },
                        label = { Text("Version") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = fileSize,
                        onValueChange = { fileSize = it },
                        label = { Text("File Size") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = fileType,
                    onValueChange = { fileType = it },
                    label = { Text("Format / Type (e.g. Figma / ZIP / PDF)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = downloadUrl,
                    onValueChange = { downloadUrl = it },
                    label = { Text("Digital Download File URL") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma separated)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Cover Preview Asset", style = MaterialTheme.typography.labelLarge)
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    imageOptions.forEach { (resName, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = (imageResName == resName),
                                onClick = { imageResName = resName }
                            )
                            Text(text = label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Product Status (Active/Visible)", fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { isEnabled = it }
                    )
                }
            }
        }
    )
}
