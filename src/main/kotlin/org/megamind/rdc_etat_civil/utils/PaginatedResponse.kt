package org.megamind.rdc_etat_civil.utils

import org.springframework.data.domain.Page

data class PaginatedResponse<T>(
    val content: List<T>,
    val totalElements: Long,
    val totalPages: Int,
    val pageNumber: Int,
    val pageSize: Int,
    val hasNext: Boolean,
    val hasPrevious: Boolean,
    val isFirst: Boolean,
    val isLast: Boolean
) {
    companion object {
        fun <T> fromPage(page: Page<T>): PaginatedResponse<T> {
            return PaginatedResponse(
                content = page.content,
                totalElements = page.totalElements,
                totalPages = page.totalPages,
                pageNumber = page.number,
                pageSize = page.size,
                hasNext = page.hasNext(),
                hasPrevious = page.hasPrevious(),
                isFirst = page.isFirst,
                isLast = page.isLast
            )
        }
    }
}