package com.vocab300.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class EntryClassifierTest {

    @Test
    fun `AUT duoc nhan dien la ma viet tat`() {
        assertEquals(EntryType.ABBREVIATION, EntryClassifier.classify("AUT"))
        assertEquals(EntryType.ABBREVIATION, EntryClassifier.classify("OQC"))
        assertEquals(EntryType.ABBREVIATION, EntryClassifier.classify("RF"))
    }

    @Test
    fun `chu thuong hoac co khoang trang thi khong tinh la ma viet tat`() {
        assertEquals(EntryType.SHORT_WORD, EntryClassifier.classify("aut"))
        assertEquals(EntryType.MEDIUM_PHRASE, EntryClassifier.classify("A B"))
    }

    @Test
    fun `tu don ngan toi da 12 ky tu`() {
        assertEquals(EntryType.SHORT_WORD, EntryClassifier.classify("Function"))
        assertEquals(EntryType.SHORT_WORD, EntryClassifier.classify("Reliability")) // 11 ký tự
    }

    @Test
    fun `tu don dai tren 12 ky tu`() {
        assertEquals(EntryType.LONG_WORD, EntryClassifier.classify("Responsibility")) // 14 ký tự
    }

    @Test
    fun `cum tu 2-4 tu la medium phrase`() {
        assertEquals(EntryType.MEDIUM_PHRASE, EntryClassifier.classify("Pink Camera"))
        assertEquals(EntryType.MEDIUM_PHRASE, EntryClassifier.classify("No Power On"))
    }

    @Test
    fun `cum tu tu 5 tu tro len la long phrase`() {
        assertEquals(EntryType.LONG_PHRASE, EntryClassifier.classify("Does Not Turn On At All"))
    }

    @Test
    fun `cum tu duoi 5 tu nhung dai hon 30 ky tu van la long phrase`() {
        val fourWordsOver30Chars = "Extremely long descriptive phrasing" // 4 từ, 36 ký tự
        assertEquals(EntryType.LONG_PHRASE, EntryClassifier.classify(fourWordsOver30Chars))
    }

    @Test
    fun `hasLongMeaning nhan dien nghia dai`() {
        assertEquals(true, EntryClassifier.hasLongMeaning("Kiểm tra gia tốc điều kiện người dùng khi khởi động"))
        assertEquals(false, EntryClassifier.hasLongMeaning("Chức Năng"))
    }
}
