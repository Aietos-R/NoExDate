package org.umn.noexdate.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(navController: NavHostController) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("Rafael Lesmana") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var profileUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            try {
                // Pengecekan Max 5MB
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                val sizeIndex = cursor?.getColumnIndex(android.provider.OpenableColumns.SIZE)
                cursor?.moveToFirst()
                val sizeInBytes = sizeIndex?.let { cursor.getLong(it) } ?: 0L
                cursor?.close()

                val maxBytes = 5 * 1024 * 1024 // 5 MB
                if (sizeInBytes > maxBytes) {
                    Toast.makeText(context, "Ukuran foto terlalu besar (Maks 5MB)", Toast.LENGTH_SHORT).show()
                } else {
                    profileUri = uri
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Gagal memuat foto", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(20.dp)) {
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface, CircleShape).clickable { navController.popBackStack() }, contentAlignment = Alignment.Center) { Icon(
                Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali", tint = MaterialTheme.colorScheme.onSurface) }
            Text("Edit Profil", color = MaterialTheme.colorScheme.onSurface, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Box(modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary, CircleShape).clickable {
                Toast.makeText(context, "Profil Diperbarui", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }, contentAlignment = Alignment.Center) { Icon(Icons.Default.Check, contentDescription = "Simpan", tint = MaterialTheme.colorScheme.background) }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
            if (profileUri != null) {
                AsyncImage(model = profileUri, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(120.dp).clip(CircleShape))
            } else {
                Box(modifier = Modifier.size(120.dp).background(MaterialTheme.colorScheme.surface, CircleShape), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
                }
            }
            Box(
                modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-4).dp, y = (-4).dp).size(36.dp).background(MaterialTheme.colorScheme.primary, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape).clickable { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.CameraAlt, contentDescription = "Ubah Foto", tint = MaterialTheme.colorScheme.background, modifier = Modifier.size(18.dp)) }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text("NAMA LENGKAP", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = name, onValueChange = { name = it }, modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedTextColor = MaterialTheme.colorScheme.onSurface), shape = RoundedCornerShape(16.dp))

        Text("NOMOR TELEPON", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = phone, onValueChange = { phone = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 16.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedTextColor = MaterialTheme.colorScheme.onSurface), shape = RoundedCornerShape(16.dp))

        Text("EMAIL", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        OutlinedTextField(value = email, onValueChange = { email = it }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email), modifier = Modifier.fillMaxWidth().padding(top = 8.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary, unfocusedBorderColor = MaterialTheme.colorScheme.outline, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface, focusedTextColor = MaterialTheme.colorScheme.onSurface), shape = RoundedCornerShape(16.dp))
    }
    Spacer(modifier = Modifier.height(170.dp))
}