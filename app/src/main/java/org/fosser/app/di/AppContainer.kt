package org.fosser.app.di

import org.fosser.app.data.local.AppDatabase
import org.fosser.app.data.repository.DonationRepository
import org.fosser.app.data.repository.FdroidRepository
import org.fosser.app.data.repository.ThemeRepository
import org.fosser.app.ui.details.DetailsViewModel
import org.fosser.app.ui.history.HistoryViewModel
import org.fosser.app.ui.home.HomeViewModel
import org.fosser.app.ui.saved.SavedViewModel
import org.fosser.app.ui.settings.SettingsViewModel

/** Manual DI container (no Koin/Hilt) — simple and maintainable. */
class AppContainer(
    val db: AppDatabase,
    val repository: FdroidRepository,
    val themeRepository: ThemeRepository,
    val donationRepository: DonationRepository,
) {
    fun homeViewModel(): HomeViewModel = HomeViewModel(repository)
    fun detailsViewModel(): DetailsViewModel = DetailsViewModel(repository)
    fun savedViewModel(): SavedViewModel = SavedViewModel(repository)
    fun historyViewModel(): HistoryViewModel = HistoryViewModel(repository)
    fun settingsViewModel(): SettingsViewModel = SettingsViewModel(repository, themeRepository)
}
