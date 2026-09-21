plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
}

// Fosser — forked from alejandro-piguave/TinderCloneCompose.
// Reused: swipe-card interaction pattern (drag physics, Animatable offset,
// rotation, thresholds), MVVM + StateFlow patterns, Navigation Compose patterns,
// Coil image loading, project/build configuration shape.
// Removed: Firebase/Auth/Chat/Match/Profile-dating backend, dating models.
// Added: F-Droid data layer, Room, RecommendationEngine, 3-way swipe,
// Fosser Material3 UI, Saved/History/Settings, offline cache.
