package com.vocab300.app.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ImportDecisionTest {

    @Test
    fun `lan dau mo app, chua co co import, DB rong thi phai import`() {
        val shouldImport = ImportDecision.shouldImport(
            importFlagAlreadySet = false, currentWordCountInDb = 0, expectedWordCount = 300
        )
        assertTrue(shouldImport)
    }

    @Test
    fun `da import roi (co flag) thi KHONG duoc import lai du mo app nhieu lan`() {
        val shouldImport = ImportDecision.shouldImport(
            importFlagAlreadySet = true, currentWordCountInDb = 300, expectedWordCount = 300
        )
        assertFalse(shouldImport)
    }

    @Test
    fun `co flag nhung DB bi thieu du lieu van KHONG import lai (uu tien khong trung du lieu)`() {
        // Quyết định có chủ đích: một khi đã đánh dấu import xong thì không tự động ghi đè lại,
        // tránh việc import chồng dữ liệu nếu người dùng đã xoá bớt/sửa dữ liệu thủ công.
        val shouldImport = ImportDecision.shouldImport(
            importFlagAlreadySet = true, currentWordCountInDb = 100, expectedWordCount = 300
        )
        assertFalse(shouldImport)
    }

    @Test
    fun `chua co flag nhung DB da du 300 tu thi khong can import lai`() {
        val shouldImport = ImportDecision.shouldImport(
            importFlagAlreadySet = false, currentWordCountInDb = 300, expectedWordCount = 300
        )
        assertFalse(shouldImport)
    }

    @Test
    fun `chua co flag va DB thieu du lieu (vi du 299) thi van import`() {
        val shouldImport = ImportDecision.shouldImport(
            importFlagAlreadySet = false, currentWordCountInDb = 299, expectedWordCount = 300
        )
        assertTrue(shouldImport)
    }
}
