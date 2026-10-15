# Taller2 💜

Taller: tomar fotos a lo largo de un recorrido mostrado en un mapa (Jetpack Compose + Kotlin).
Pensada para un **foldable abierto**.

## Antes de ejecutar
1. Abre `app/src/main/res/values/mapbox_access_token.xml` y pega tu token público de Mapbox (`pk...`).
2. Sincroniza Gradle y ejecuta en el emulador **Pixel Fold / foldable abierto** (o en un dispositivo).

## Librerías
| Librería | Versión |
|---|---|
| CameraX | 1.7.0-alpha03 |
| Mapbox Maps Compose | 11.31.1 |
| Accompanist Permissions | 0.37.3 |
| Play Services Location | 21.4.0 |
| Coil (miniaturas) | 3.6.3 |

## Estructura
```
data/RoutePhoto.kt                  Foto del recorrido (uri, nombre, ubicación)
utils/ImageFileUtils.kt             Opciones para guardar la foto en la galería (MediaStore)
viewmodels/CameraPreviewViewModel   CameraX: vista previa, cambiar lente, tomar foto
viewmodels/RouteViewModel           Ubicación: iniciar/detener recorrido, fotos, borrar recorrido
ui/components/CameraPreviewContent  Vista previa + botón de foto + botón cambiar cámara
ui/components/PhotoListComponent    Lista horizontal de fotos (LazyRow)
ui/components/MapComponent          Mapa: ubicación actual, ruta, marcadores y botones
ui/components/PhotoMarker           Marcador con miniatura de la foto (bono)
ui/components/NoPermissionCard      Mensaje cuando no se otorga un permiso
ui/screens/HomeScreen               Permisos + las 2 regiones (cámara / mapa)
```

## Requisitos cubiertos
1. Dos regiones: cámara y mapa (en vertical o lado a lado según la forma de la pantalla).
2. Al iniciar se piden cámara y ubicación; si se niega alguno, la región muestra el mensaje.
3. El mapa muestra la ubicación actual + botones **iniciar/detener** y **borrar** recorrido.
4. Vista previa de cámara, botón de foto, botón frontal/trasera y lista horizontal de fotos debajo.
5. La foto se guarda en la galería (`Pictures/Taller2`) y aparece como marcador donde se tomó, con su nombre como título.
6. Borrar recorrido elimina la ruta, los marcadores y la lista de fotos (los archivos siguen en la galería).
- **Bono:** el marcador es la miniatura de la foto (`ViewAnnotation`).
