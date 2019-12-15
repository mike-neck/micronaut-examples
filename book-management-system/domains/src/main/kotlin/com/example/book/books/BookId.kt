package com.example.book.books

import com.example.ids.Id
import com.example.ids.IdGen
import com.example.ids.StrictId
import com.example.ids.toLongOrNull

data class BookId(override val value: Long): Id<Long> {
  companion object : StrictId<BookId, Long> {
    override fun from(value: Long): BookId = BookId(value)
    override fun stringToValue(value: String?): Long? = value.toLongOrNull()
    override val nameOfId: String = "bookId"
    fun IdGen.newBookId(): BookId = BookId(this.newLongId())
  }
}
