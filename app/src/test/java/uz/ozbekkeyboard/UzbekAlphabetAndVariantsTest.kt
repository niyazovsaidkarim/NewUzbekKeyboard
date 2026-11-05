package uz.ozbekkeyboard

import org.junit.Assert.*
import org.junit.Test
import uz.ozbekkeyboard.data.UzbekLetters

class UzbekAlphabetAndVariantsTest {

    @Test
    fun testUzbekLatinVariantsLowercase() {
        val oVariants = UzbekLetters.getVariants("o", isUppercase = false)
        assertTrue("o variants must contain ö", oVariants.contains("ö"))

        val gVariants = UzbekLetters.getVariants("g", isUppercase = false)
        assertTrue("g variants must contain ğ", gVariants.contains("ğ"))

        val sVariants = UzbekLetters.getVariants("s", isUppercase = false)
        assertTrue("s variants must contain ş", sVariants.contains("ş"))

        val cVariants = UzbekLetters.getVariants("c", isUppercase = false)
        assertTrue("c variants must contain ç", cVariants.contains("ç"))
    }

    @Test
    fun testUzbekLatinVariantsUppercase() {
        val oUpperVariants = UzbekLetters.getVariants("O", isUppercase = true)
        assertTrue("O variants must contain Ö", oUpperVariants.contains("Ö"))

        val gUpperVariants = UzbekLetters.getVariants("G", isUppercase = true)
        assertTrue("G variants must contain Ğ", gUpperVariants.contains("Ğ"))

        val sUpperVariants = UzbekLetters.getVariants("S", isUppercase = true)
        assertTrue("S variants must contain Ş", sUpperVariants.contains("Ş"))

        val cUpperVariants = UzbekLetters.getVariants("C", isUppercase = true)
        assertTrue("C variants must contain Ç", cUpperVariants.contains("Ç"))
    }

    @Test
    fun testTutuqSignMapping() {
        assertEquals("ʼ", UzbekLetters.TUTUQ_STANDARD)
        assertEquals("'", UzbekLetters.TUTUQ_ASCII)

        val apostropheVariants = UzbekLetters.getVariants("'", isUppercase = false)
        assertTrue("Apostrophe variants must contain Uzbek tutuq sign ʼ (U+02BC)", apostropheVariants.contains(UzbekLetters.TUTUQ_STANDARD))
    }

    @Test
    fun testPrimaryUzbekVariants() {
        assertEquals('ö', UzbekLetters.getPrimaryUzbekVariant('o'))
        assertEquals('Ö', UzbekLetters.getPrimaryUzbekVariant('O'))
        assertEquals('ğ', UzbekLetters.getPrimaryUzbekVariant('g'))
        assertEquals('Ğ', UzbekLetters.getPrimaryUzbekVariant('G'))
        assertEquals('ş', UzbekLetters.getPrimaryUzbekVariant('s'))
        assertEquals('Ş', UzbekLetters.getPrimaryUzbekVariant('S'))
        assertEquals('ç', UzbekLetters.getPrimaryUzbekVariant('c'))
        assertEquals('Ç', UzbekLetters.getPrimaryUzbekVariant('C'))
        assertNull(UzbekLetters.getPrimaryUzbekVariant('x'))
    }

    @Test
    fun testHasVariants() {
        assertTrue(UzbekLetters.hasVariants("o"))
        assertTrue(UzbekLetters.hasVariants("g"))
        assertTrue(UzbekLetters.hasVariants("s"))
        assertTrue(UzbekLetters.hasVariants("c"))
        assertTrue(UzbekLetters.hasVariants("'"))
        assertFalse(UzbekLetters.hasVariants("x"))
    }
}
