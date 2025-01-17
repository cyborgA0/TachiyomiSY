package eu.kanade.domain.category.interactor

import eu.kanade.domain.category.model.Category
import eu.kanade.domain.category.model.CategoryUpdate
import eu.kanade.domain.category.repository.CategoryRepository
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.ui.library.LibraryGroup
import eu.kanade.tachiyomi.ui.library.setting.SortDirectionSetting
import eu.kanade.tachiyomi.ui.library.setting.SortModeSetting

class SetSortModeForCategory(
    private val preferences: PreferencesHelper,
    private val categoryRepository: CategoryRepository,
) {

    suspend fun await(category: Category, sortDirectionSetting: SortDirectionSetting) {
        val sort = if (preferences.categorizedDisplaySettings().get() /* SY --> */ && preferences.groupLibraryBy().get() == LibraryGroup.BY_DEFAULT/* SY <-- */) {
            SortModeSetting.fromFlag(category.flags)
        } else {
            preferences.librarySortingMode().get()
        }
        await(category, sort, sortDirectionSetting)
    }

    suspend fun await(category: Category, sortModeSetting: SortModeSetting) {
        val direction = if (preferences.categorizedDisplaySettings().get() /* SY --> */ && preferences.groupLibraryBy().get() == LibraryGroup.BY_DEFAULT/* SY <-- */) {
            SortDirectionSetting.fromFlag(category.flags)
        } else {
            preferences.librarySortingAscending().get()
        }
        await(category, sortModeSetting, direction)
    }

    suspend fun await(category: Category, sortModeSetting: SortModeSetting, sortDirectionSetting: SortDirectionSetting) {
        var flags = category.flags and SortModeSetting.MASK.inv() or (sortModeSetting.flag and SortModeSetting.MASK)
        flags = flags and SortDirectionSetting.MASK.inv() or (sortDirectionSetting.flag and SortDirectionSetting.MASK)
        // SY -->
        val isDefaultGroup = preferences.groupLibraryBy().get() == LibraryGroup.BY_DEFAULT
        // SY <--
        if (preferences.categorizedDisplaySettings().get() /* SY --> */ && isDefaultGroup/* SY <-- */) {
            categoryRepository.updatePartial(
                CategoryUpdate(
                    id = category.id,
                    flags = flags,
                ),
            )
        } else {
            preferences.librarySortingMode().set(sortModeSetting)
            preferences.librarySortingAscending().set(sortDirectionSetting)
            // SY -->
            if (isDefaultGroup) {
                // SY <--
                categoryRepository.updateAllFlags(flags)
                // SY -->
            }
            // SY <--
        }
    }
}
