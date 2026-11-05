package uz.ozbekkeyboard

import org.junit.Assert.*
import org.junit.Test
import uz.ozbekkeyboard.data.EmojiData

class EmojiDataTest {

    @Test
    fun testEmojiCategoriesCount() {
        val categories = EmojiData.categories
        assertTrue("Should have at least 6 emoji categories", categories.size >= 6)
    }

    @Test
    fun testAllCategoriesHaveEmojis() {
        for (cat in EmojiData.categories) {
            assertTrue("Category ${cat.name} should have emojis", cat.emojis.isNotEmpty())
            assertTrue("Category icon should not be empty", cat.icon.isNotEmpty())
        }
    }

    @Test
    fun testFlagsCategoryHasUzbekFlag() {
        val flags = EmojiData.categories.firstOrNull { it.id == "flags" }
        assertNotNull(flags)
        assertTrue("Flags must contain Uzbekistan flag 🇺🇿", flags!!.emojis.contains("🇺🇿"))
    }
}
