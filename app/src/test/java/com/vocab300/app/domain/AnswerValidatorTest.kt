package com.vocab300.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class AnswerValidatorTest {

    @Test
    fun `chuan hoa hoa thuong va khoang trang`() {
        assertEquals("function", AnswerValidator.normalize("  Function  "))
        assertEquals("pink camera", AnswerValidator.normalize("Pink    Camera"))
    }

    @Test
    fun `chuan hoa bo dau cau khong quan trong`() {
        assertEquals("hello world", AnswerValidator.normalize("Hello, world!"))
    }

    @Test
    fun `ma viet tat phai dung toan bo, khong ap dung gan dung - AUT vs AOT`() {
        val result = AnswerValidator.validate("AOT", "AUT", EntryType.ABBREVIATION)
        assertEquals(AnswerResult.INCORRECT, result)
    }

    @Test
    fun `ma viet tat nhap dung (khong phan biet hoa thuong) la CORRECT`() {
        assertEquals(AnswerResult.CORRECT, AnswerValidator.validate("aut", "AUT", EntryType.ABBREVIATION))
        assertEquals(AnswerResult.CORRECT, AnswerValidator.validate("AUT", "AUT", EntryType.ABBREVIATION))
    }

    @Test
    fun `tu don ngan phai chinh xac sau chuan hoa, sai 1 ky tu la INCORRECT`() {
        assertEquals(AnswerResult.CORRECT, AnswerValidator.validate("function", "Function", EntryType.SHORT_WORD))
        assertEquals(AnswerResult.INCORRECT, AnswerValidator.validate("functoin", "Function", EntryType.SHORT_WORD))
    }

    @Test
    fun `tu don dai sai toi da 1 ky tu la NEARLY_CORRECT`() {
        val result = AnswerValidator.validate("Responsibilty", "Responsibility", EntryType.LONG_WORD) // thiếu 1 chữ 'i'
        assertEquals(AnswerResult.NEARLY_CORRECT, result)
    }

    @Test
    fun `tu don dai sai tu 2 ky tu tro len la INCORRECT`() {
        val result = AnswerValidator.validate("Resposibilty", "Responsibility", EntryType.LONG_WORD) // thiếu 2 chữ
        assertEquals(AnswerResult.INCORRECT, result)
    }

    @Test
    fun `cum tu dai dat nguong ty le tu khoa la NEARLY_CORRECT`() {
        val correct = "does not turn on at all"
        val input = "does not turn on all" // thiếu 1/6 từ khóa ~ 83% khớp
        assertEquals(AnswerResult.NEARLY_CORRECT, AnswerValidator.validate(input, correct, EntryType.LONG_PHRASE))
    }

    @Test
    fun `cum tu dai thieu qua nhieu tu khoa la INCORRECT`() {
        val correct = "does not turn on at all"
        val input = "does"
        assertEquals(AnswerResult.INCORRECT, AnswerValidator.validate(input, correct, EntryType.LONG_PHRASE))
    }

    @Test
    fun `validateChoice chi co dung hoac sai, khong co gan dung`() {
        assertEquals(AnswerResult.CORRECT, AnswerValidator.validateChoice("Chức Năng", "Chức Năng"))
        assertEquals(AnswerResult.INCORRECT, AnswerValidator.validateChoice("Chức Nang", "Chức Năng"))
    }

    @Test
    fun `dap an rong luon la INCORRECT`() {
        assertEquals(AnswerResult.INCORRECT, AnswerValidator.validate("", "Function", EntryType.SHORT_WORD))
        assertEquals(AnswerResult.INCORRECT, AnswerValidator.validate("   ", "AUT", EntryType.ABBREVIATION))
    }
}
