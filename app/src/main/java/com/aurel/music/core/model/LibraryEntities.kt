package com.aurel.music.core.model

import androidx.compose.runtime.Immutable

@Immutable
data class Album(
    val id: Long,
    val title: String,
    val artistId: Long,
    val artistName: String,
    val trackCount: Int,
    val year: Int = 0,
    val artworkUri: String? = null
)

@Immutable
data class Artist(
    val id: Long,
    val name: String,
    val trackCount: Int,
    val albumCount: Int
)

@Immutable
data class Playlist(
    val id: Long,
    val name: String,
    val trackCount: Int,
    val createdAt: Long,
    val updatedAt: Long
)
