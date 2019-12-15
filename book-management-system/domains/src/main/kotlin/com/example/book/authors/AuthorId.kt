package com.example.book.authors

import com.example.ids.Id
import com.example.ids.IdGen
import com.example.ids.StrictId
import com.example.ids.toLongOrNull

data class AuthorId(override val value: Long): Id<Long> {
  companion object: StrictId<AuthorId, Long> {
    override fun from(value: Long): AuthorId = AuthorId(value)
    override fun stringToValue(value: String?): Long? = value.toLongOrNull()
    override val nameOfId: String = "authorId"
    fun IdGen.newAuthorId(): AuthorId = AuthorId(this.newLongId())
  }
}