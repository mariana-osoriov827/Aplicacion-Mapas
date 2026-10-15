package com.example.taller2.data

import android.location.Location
import android.net.Uri

// Foto tomada durante el recorrido: uri en la galería, nombre (título del marcador) y dónde se tomó
data class RoutePhoto(val uri: Uri, val name: String, val location: Location?)
